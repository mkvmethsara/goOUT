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

    // UPDATED: Now requires Authentication so we know who is asking for the feed!
    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getFeed(Authentication authentication) {
        String userEmail = authentication.getName();
        List<PostResponseDto> feedResponse = postService.getFeed(userEmail);
        return ResponseEntity.ok(feedResponse);
    }

    // ADDED: The Toggle Like Endpoint
    @PostMapping("/{postId}/like")
    public ResponseEntity<String> toggleLike(@PathVariable String postId, Authentication authentication) {
        String userEmail = authentication.getName();
        postService.toggleLike(postId, userEmail);
        return ResponseEntity.ok("Post like status toggled");
    }
}