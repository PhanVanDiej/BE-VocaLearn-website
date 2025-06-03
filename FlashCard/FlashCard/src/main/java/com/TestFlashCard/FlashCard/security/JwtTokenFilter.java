package com.TestFlashCard.FlashCard.security;

import java.io.IOException;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.TestFlashCard.FlashCard.Enum.TokenError;
import com.TestFlashCard.FlashCard.service.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private JwtTokenProvider tokenProvider;

    private CustomUserDetailsService userDetailsService;

    public JwtTokenFilter(JwtTokenProvider tokenProvider, CustomUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/user/login",
            "/api/user/create",
            "/api/user/register",
            "/api/user/forgot-password",
            "/api/user/verify-reset-code");

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println(">> [FILTER] Path: " + request.getServletPath());

        String path = request.getServletPath();

        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || isPublicEndpoint(path))  {
            filterChain.doFilter(request, response);
            System.out.println(">> [FILTER] Path Pass Auth: " + request.getServletPath());
            return;
        }

        try {
            String token = getTokenFromRequest(request);
            if (token == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write(String.format(
                        "{\"error\": \"%s\", \"message\": \"%s\"}",
                        TokenError.NULL.getCode(),
                        TokenError.NULL.getMessage()));
                return;
            }

            TokenValidationResult tokenValidationResult = tokenProvider.validateToken(token);
            if (!tokenValidationResult.isValid()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write(String.format(
                        "{\"error\": \"%s\", \"message\": \"%s\"}",
                        tokenValidationResult.getErrorCode(),
                        tokenValidationResult.getMessage()));
                return;
            }

            int userId = tokenProvider.getUserIdFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserById(userId);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // ✅ Sau khi xử lý thành công → tiếp tục filter
            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            // ✅ Tránh tiếp tục xử lý sau khi đã set lỗi
            ex.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"UNKNOWN\", \"message\": \"Unknown error occurred.\"}");
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}