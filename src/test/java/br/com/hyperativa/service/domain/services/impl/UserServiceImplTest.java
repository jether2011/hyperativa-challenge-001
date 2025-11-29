package br.com.hyperativa.service.domain.services.impl;

import br.com.hyperativa.service.domain.entity.User;
import br.com.hyperativa.service.domain.entity.dto.UserCreateDTO;
import br.com.hyperativa.service.domain.entity.dto.UserGetDTO;
import br.com.hyperativa.service.domain.exceptions.NotFoundException;
import br.com.hyperativa.service.domain.exceptions.UserCreateException;
import br.com.hyperativa.service.resources.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserCreateDTO testUserCreateDTO;

    @BeforeEach
    void setUp() {
        testUser = new User()
                .username("testuser")
                .password("encodedPassword123");
        testUser.setId(1L);
        testUserCreateDTO = new UserCreateDTO("testuser", "password123");
    }

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        // Given
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserGetDTO result = userService.createUser(testUserCreateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("testuser");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw UserCreateException when save fails")
    void shouldThrowUserCreateExceptionWhenSaveFails() {
        // Given
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> userService.createUser(testUserCreateDTO))
                .isInstanceOf(UserCreateException.class)
                .hasMessageContaining("User create error");
    }

    @Test
    @DisplayName("Should get user successfully")
    void shouldGetUserSuccessfully() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserGetDTO result = userService.getUser("testuser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("testuser");
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should throw NotFoundException when user not found")
    void shouldThrowNotFoundExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUser("nonexistent"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should get user entity successfully")
    void shouldGetUserEntitySuccessfully() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserEntity("testuser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should validate if user exists")
    void shouldValidateIfUserExists() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When
        boolean exists = userService.validateIfUserExists("testuser");
        boolean notExists = userService.validateIfUserExists("nonexistent");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should get all users with pagination")
    void shouldGetAllUsersWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        User user2 = new User().username("user2").password("pass2");
        List<User> users = List.of(testUser, user2);
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // When
        Page<UserGetDTO> result = userService.getAllUsers(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).username()).isEqualTo("testuser");
        verify(userRepository, times(1)).findAll(pageable);
    }
}
