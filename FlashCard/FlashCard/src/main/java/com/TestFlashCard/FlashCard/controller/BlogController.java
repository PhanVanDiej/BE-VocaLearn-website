package com.TestFlashCard.FlashCard.controller;

import java.io.IOException;
import java.util.List;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.TestFlashCard.FlashCard.entity.BlogCategory;
import com.TestFlashCard.FlashCard.request.BlogCategoryCreateRequest;
import com.TestFlashCard.FlashCard.request.BlogCreateRequest;
import com.TestFlashCard.FlashCard.response.BlogResponse;
import com.TestFlashCard.FlashCard.service.BlogService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/blog")
@RequiredArgsConstructor
public class BlogController {
    @Autowired
    private final BlogService blogService;
    @Autowired
    private final ObjectMapper objectMapper;

    @GetMapping("/category/getAll")
    public ResponseEntity<?> getAllBlogCategory() throws IOException {
        List<BlogCategory> categories = blogService.getAllCategory();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @PostMapping("/category/create")
    public ResponseEntity<?> createBlogCategory(@RequestBody BlogCategoryCreateRequest request) throws IOException {
        blogService.createCategory(request);
        return ResponseEntity.ok("Create new Blog Category successfully!");
    }

    @DeleteMapping("/category/delete/{id}")
    public ResponseEntity<?> deleteBlogCategory(@PathVariable Integer id) throws IOException {
        blogService.deleteCategory(id);
        return ResponseEntity.ok("Delete Blog Category successfully!");
    }

    @PutMapping("/category/update/{id}")
    public ResponseEntity<?> updateBlogCategory(@PathVariable Integer id,
            @RequestBody BlogCategoryCreateRequest request) {
        blogService.updateCategory(request, id);
        return ResponseEntity.ok("Update Blog Category with id: " + id + " successfully!");
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllBlog() throws IOException {
        List<BlogResponse> responses = blogService.getAllBlog();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getBlogById(@PathVariable Integer id) throws IOException {
        BlogResponse response = blogService.getBlogById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createBlog(@RequestPart String dataJson,
            @RequestPart(required = false) MultipartFile image) throws IOException {

        BlogCreateRequest request = objectMapper.readValue(dataJson, BlogCreateRequest.class);
        blogService.createBlog(request, image);
        return ResponseEntity.ok("Create new Blog successfully!");
    }

    @PutMapping(value = "update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateBlog(@RequestPart String dataJson,
            @RequestPart(required = false) MultipartFile image, @PathVariable Integer id) throws IOException {

        BlogCreateRequest request = objectMapper.readValue(dataJson, BlogCreateRequest.class);
        if (request.getCategory() == null || request.getDetail() == null || request.getShortDetail() == null
                || request.getTitle() == null)
            throw new BadRequestException("Cannot update the Blog with id: " + id + ". Fields must not be null!");
        blogService.updateBlog(request, image, id);
        return ResponseEntity.ok("Update Blog with id: " + id + " successfully!");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteBlog(@PathVariable Integer id) throws IOException {
        blogService.deleteBlog(id);
        return ResponseEntity.ok("Delete Blog with id: " + id + " successfully!");
    }

    @GetMapping("/filter")
    public ResponseEntity<?> getByCategory(@RequestParam String category) throws IOException {
        return new ResponseEntity<>(blogService.getByCategory(category), HttpStatus.OK);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Test OK");
    }

}
