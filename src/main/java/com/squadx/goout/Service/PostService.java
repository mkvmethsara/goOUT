package com.squadx.goout.Service;

import com.squadx.goout.Dto.PostResponseDto;
import com.squadx.goout.Entity.Post;
import com.squadx.goout.Entity.User;
import com.squadx.goout.Repository.PostRepository;
import com.squadx.goout.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public Post createPost(Post newPost, String userEmail) {
        // 1. Identify who is posting
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Attach the author ID and current timestamp
        newPost.setAuthorId(currentUser.getId());
        newPost.setCreatedAt(LocalDateTime.now());

        // 3. Save to MongoDB
        return postRepository.save(newPost);
    }
    // 🌟 NEW: A dedicated, lightweight public feed for the Landing Page
    public List<PostResponseDto> getPublicTrendingFeed() {
        // Fetch all posts, sorted newest first
        List<Post> allPosts = postRepository.findAllByOrderByCreatedAtDesc();
        List<PostResponseDto> feedResponse = new ArrayList<>();

        // Loop through and map them, but skip the "isLikedByCurrentUser" logic!
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

            int likeCount = (post.getLikedBy() != null) ? post.getLikedBy().size() : 0;

            PostResponseDto.AuthorDto authorDto = new PostResponseDto.AuthorDto(authorName, avatarUrl);

            // We hardcode isLiked to false since no one is logged in!
            PostResponseDto dto = new PostResponseDto(
                    post.getId(),
                    post.getContent(),
                    post.getLocation(),
                    post.getImageUrl(),
                    post.getCreatedAt(),
                    likeCount,
                    false,
                    authorDto
            );
            feedResponse.add(dto);
        }
        return feedResponse;
    }

    // UPDATED: We now accept the current user's email so we can calculate 'isLikedByCurrentUser'
    public List<PostResponseDto> getFeed(String currentUserEmail) {

        // Find out who is scrolling the feed
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String currentUserId = currentUser.getId();

        // 1. Fetch all posts from DB, sorted newest first!
        List<Post> allPosts = postRepository.findAllByOrderByCreatedAtDesc();
        List<PostResponseDto> feedResponse = new ArrayList<>();

        // 2. Loop through every post and attach the Author's details and Like details
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

            // ADDED: Calculate the Like metrics!
            int likeCount = (post.getLikedBy() != null) ? post.getLikedBy().size() : 0;
            boolean isLiked = (post.getLikedBy() != null) && post.getLikedBy().contains(currentUserId);

            PostResponseDto.AuthorDto authorDto = new PostResponseDto.AuthorDto(authorName, avatarUrl);

            PostResponseDto dto = new PostResponseDto(
                    post.getId(),
                    post.getContent(),
                    post.getLocation(),
                    post.getImageUrl(),
                    post.getCreatedAt(),
                    likeCount, // Included here!
                    isLiked,   // Included here!
                    authorDto
            );

            feedResponse.add(dto);
        }

        return feedResponse;
    }

    // ADDED: The logic to Like or Unlike a post
    public com.squadx.goout.Dto.LikeResponseDto toggleLike(String postId, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Failsafe: Ensure the list exists
        if (post.getLikedBy() == null) {
            post.setLikedBy(new ArrayList<>());
        }

        boolean isNowLiked;
        // The Toggle Logic: If they already liked it, remove them. Otherwise, add them.
        if (post.getLikedBy().contains(currentUser.getId())) {
            post.getLikedBy().remove(currentUser.getId());
            isNowLiked = false;
        } else {
            post.getLikedBy().add(currentUser.getId());
            isNowLiked = true;
        }

        postRepository.save(post);

        // Return the exact numbers to the frontend!
        return new com.squadx.goout.Dto.LikeResponseDto(true, isNowLiked, post.getLikedBy().size());
    }
}