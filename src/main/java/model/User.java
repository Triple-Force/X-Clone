package model;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data //Getter, Setter, toString, Equals
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User
{
    // --- Identity ---
    private Long id;
    private String username;
    private String email;

    // --- Security ---
    private String passwordHash;

    // --- Profile ---
    private String displayName;
    private String bio;
    private String profilePictureUrl;

    // --- Metadata ---
    private LocalDateTime createdAt;

}
