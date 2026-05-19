package org.project.projemento.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.project.projemento.config.OpenApiConfig;
import org.project.projemento.dto.UserRegistrationRequest;
import org.project.projemento.dto.UserResponse;
import org.project.projemento.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Registration and current user endpoints")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user", security = {})
    public UserResponse register(@Valid @RequestBody UserRegistrationRequest request) {
        return userService.register(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public UserResponse currentUser(Authentication authentication) {
        return userService.getByEmail(authentication.getName());
    }
}
