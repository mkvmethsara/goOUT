package com.squadx.goout.Controller;

import com.squadx.goout.Dto.PostResponseDto;
import com.squadx.goout.Entity.Post;
import com.squadx.goout.Entity.User;
import com.squadx.goout.Repository.PostRepository;
import com.squadx.goout.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post newPost, Authentication authentication) {

        // 1. Identify who is posting via their JWT token
        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Attach the author ID and current timestamp
        newPost.setAuthorId(currentUser.getId());
        newPost.setCreatedAt(LocalDateTime.now());

        // 3. Save to MongoDB
        Post savedPost = postRepository.save(newPost);
        return ResponseEntity.ok(savedPost);
    }

    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getFeed() {

        // 1. Fetch all posts from DB, sorted newest first!
        List<Post> allPosts = postRepository.findAllByOrderByCreatedAtDesc();
        List<PostResponseDto> feedResponse = new ArrayList<>();

        // 2. Loop through every post and attach the Author's details
        for (Post post : allPosts) {
            Optional<User> authorOpt = userRepository.findById(post.getAuthorId());

            String authorName = "Unknown Traveler";
            String avatarUrl = null;

            if (authorOpt.isPresent()) {
                User u = authorOpt.get();

                String fName = u.getFirstName() != null ? u.getFirstName() : "";
                String lName = u.getLastName() != null ? u.getLastName() : "";
                String fullName = (fName + " " + lName).trim();

                if (!fullName.isEmpty()) {
                    authorName = fullName;
                }
                avatarUrl = u.getAvatarUrl();
            }

            PostResponseDto.AuthorDto authorDto = new PostResponseDto.AuthorDto(authorName, avatarUrl);

            PostResponseDto dto = new PostResponseDto(
                    post.getId(),
                    post.getContent(),
                    post.getLocation(),
                    post.getImageUrl(),
                    post.getCreatedAt(),
                    authorDto
            );

            feedResponse.add(dto);
        }

        return ResponseEntity.ok(feedResponse);
    }
}