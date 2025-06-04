package com.TestFlashCard.FlashCard.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.TestFlashCard.FlashCard.entity.Blog;
import com.TestFlashCard.FlashCard.entity.BlogCategory;
import com.TestFlashCard.FlashCard.exception.ResourceExistedException;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.IBlogCategory_Repository;
import com.TestFlashCard.FlashCard.repository.IBlog_Repository;
import com.TestFlashCard.FlashCard.request.BlogCategoryCreateRequest;
import com.TestFlashCard.FlashCard.request.BlogCreateRequest;
import com.TestFlashCard.FlashCard.response.BlogResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlogService {

    @Autowired
    private final IBlogCategory_Repository blogCategory_Repository;
    @Autowired
    private final IBlog_Repository blog_Repository;
    @Autowired
    private final MediaService mediaService;
    @Autowired
    private final DigitalOceanStorageService storageService;

    @Transactional
    public void createCategory(BlogCategoryCreateRequest request) throws IOException {
        if (blogCategory_Repository.findByTitle(request.getTitle()) != null)
            throw new ResourceExistedException("Cannot create new Blog Category. This category has been existed!");
        BlogCategory category = new BlogCategory();
        category.setTitle(request.getTitle());
        category.setDeleted(false);
        blogCategory_Repository.save(category);
    }

    @Transactional
    public List<BlogCategory> getAllCategory() throws IOException {
        return blogCategory_Repository.findByIsDeletedFalse();
    }

    @Transactional
    public void updateCategory(BlogCategoryCreateRequest request, int id) {
        if (blogCategory_Repository.findByTitle(request.getTitle()) != null)
            throw new ResourceExistedException(
                    "Cannot update new Blog Category. The category's title has been existed!");
        BlogCategory blogCategory = blogCategory_Repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the Blog Category with id: " + id));
        blogCategory.setTitle(request.getTitle());
        blogCategory_Repository.save(blogCategory);
    }

    @Transactional
    public void deleteCategory(int id) {
        BlogCategory blogCategory = blogCategory_Repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the Blog Category with id: " + id));
        blogCategory.setDeleted(true);
        blogCategory_Repository.save(blogCategory);
    }

    @Transactional
    public List<BlogResponse> getAllBlog() {
        List<Blog> blogs = blog_Repository.findAll();
        return blogs.stream().map(this::convertToBlogResponse).toList();
    }

    @Transactional
    public BlogResponse getBlogById(int id) {
        Blog blog = blog_Repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find Blog with id: " + id));
        return convertToBlogResponse(blog);
    }

    @Transactional
    public void createBlog(BlogCreateRequest request, MultipartFile image) throws IOException {
        BlogCategory category = blogCategory_Repository.findByTitle(request.getCategory());
        if (category == null)
            throw new ResourceNotFoundException("Cannot find the Blog Category with name : " + request.getCategory());
        Blog blog = new Blog();
        blog.setTitle(request.getTitle());
        blog.setCategory(category);
        blog.setDetail(request.getDetail());
        blog.setShortDetail(request.getShortDetail());

        if (image != null) {
            blog.setImage(mediaService.getImageUrl(image));
        }

        blog_Repository.save(blog);
    }

    @Transactional
    public void updateBlog(BlogCreateRequest request, MultipartFile image, int id) throws IOException {
        BlogCategory category = blogCategory_Repository.findByTitle(request.getCategory());
        if (category == null)
            throw new ResourceNotFoundException("Cannot find the Blog Category with name : " + request.getCategory());
        Blog blog = blog_Repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the Blog with id: " + id));

        if (blog.getImage() != null) {
            storageService.deleteImage(blog.getImage());
            blog.setImage(mediaService.getImageUrl(image));
        }
        blog.setTitle(request.getTitle());
        blog.setCategory(category);
        blog.setDetail(request.getDetail());
        blog.setShortDetail(request.getShortDetail());

        blog_Repository.save(blog);
    }

    @Transactional
    public void deleteBlog(int id) {
        Blog blog = blog_Repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find the Blog with id: " + id));
        if (blog.getImage() != null)
            storageService.deleteImage(blog.getImage());
        blog_Repository.delete(blog);
    }

    public BlogResponse convertToBlogResponse(Blog blog) {
        return new BlogResponse(
                blog.getId(),
                blog.getTitle(),
                blog.getCategory().getTitle(),
                blog.getShortDetail(),
                blog.getImage(),
                blog.getDetail());
    }
}
