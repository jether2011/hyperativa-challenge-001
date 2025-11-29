package br.com.hyperativa.service.domain.services.impl;

import br.com.hyperativa.service.domain.entity.User;
import br.com.hyperativa.service.domain.entity.dto.UserCreateDTO;
import br.com.hyperativa.service.domain.entity.dto.UserGetDTO;
import br.com.hyperativa.service.domain.exceptions.NotFoundException;
import br.com.hyperativa.service.domain.exceptions.UserCreateException;
import br.com.hyperativa.service.domain.services.UserService;
import br.com.hyperativa.service.resources.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserGetDTO createUser(final UserCreateDTO userCreate) {
        try {
            final User user = userRepository.save(
                    new User()
                            .username(userCreate.username())
                            .password(userCreate.password())
            );

            return new UserGetDTO(user.getUsername());
        } catch (Exception e) {
            throw new UserCreateException("User create error", e);
        }
    }

    @Override
    public UserGetDTO getUser(final String username) {
        return userRepository.findByUsername(username)
                .map(user -> new UserGetDTO(user.getUsername()))
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public User getUserEntity(final String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public boolean validateIfUserExists(final String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Override
    public Page<UserGetDTO> getAllUsers(final Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(user -> new UserGetDTO(user.getUsername()));
    }
}
