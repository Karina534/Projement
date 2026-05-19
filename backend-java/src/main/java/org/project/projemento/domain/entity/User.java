package org.project.projemento.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.project.projemento.domain.enums.Role;

@Getter
@ToString(exclude = {"password"})
@Entity
@Table(name = "users")
public class User extends BaseEntity{
    @Setter
    @Column(name = "username", nullable = false)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between {min} and {max} characters")
    private String username;

    @Setter
    @Column(name = "email", nullable = false, unique = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Setter
    @JsonIgnore
    @Column(name = "password", nullable = false)
    @Size(min = 6, max = 100, message = "Password must be between {min} and {max} characters")
    private String password;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.USER;


    public User() {
    }

    public User(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}