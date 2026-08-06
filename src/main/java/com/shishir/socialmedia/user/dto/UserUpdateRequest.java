package com.shishir.socialmedia.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {

    @NotBlank(message = "Username cannot be blank")
    private String username;

    private String firstName;
    private String lastName;

    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;

    private String profileImage;
}
