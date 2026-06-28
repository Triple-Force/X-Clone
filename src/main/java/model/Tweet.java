package model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data //Getter, Setter, toString, Equals
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tweet
{
    // --- Identity ---
    private Long id;

    // --- Author ---
    private Long authorId;

    // --- Content ---
    private String content;

    // --- Metadata ---
    private LocalDateTime createdAt;

    // --- Relationships ---
    private Long quotedTweetId; // null for normal tweets, non-null for quote tweets
}
