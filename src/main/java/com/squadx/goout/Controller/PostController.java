package com.squadx.goout.Controller;

import com.squadx.goout.Dto.PostResponseDto;
import com.squadx.goout.Entity.Post;
import com.squadx.goout.Service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post newPost, Authentication authentication) {
        String userEmail = authentication.getName();
        Post savedPost = postService.createPost(newPost, userEmail);
        return ResponseEntity.ok(savedPost);
    }

    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getFeed() {
        List<PostResponseDto> feedResponse = postService.getFeed();
        return ResponseEntity.ok(feedResponse);
    }
}