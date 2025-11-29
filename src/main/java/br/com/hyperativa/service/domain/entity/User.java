package br.com.hyperativa.service.domain.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Objects;

@Entity
@Table(name = "user")
public final class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    public User username(final String username) {
        this.username = username;
        return this;
    }

    public User password(final String password) {
        this.password = password;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user)) return false;
        return Objects.equals(this.username, user.username) &&
                (this.getId() != null && Objects.equals(this.getId(), user.getId()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId(), username);
    }

    public boolean validatePassword(final String password, final BCryptPasswordEncoder encoder) {
        return encoder.matches(password, this.password);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + this.getId() + '\'' +
                "username='" + username + '\'' +
                '}';
    }
}
