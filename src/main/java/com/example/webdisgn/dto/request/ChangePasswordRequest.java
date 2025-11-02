package com.example.webdisgn.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "La password attuale è obbligatoria")
    private String oldPassword;

    @NotBlank(message = "La nuova password è obbligatoria")
    private String newPassword;
}
