package br.com.hyperativa.service.domain.services;

import br.com.hyperativa.service.domain.entity.User;
import br.com.hyperativa.service.domain.entity.dto.UserCreateDTO;
import br.com.hyperativa.service.domain.entity.dto.UserGetDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserGetDTO createUser(final UserCreateDTO userCreate);

    UserGetDTO getUser(final String username);

    User getUserEntity(final String username);

    boolean validateIfUserExists(final String username);

    Page<UserGetDTO> getAllUsers(final Pageable pageable);
}
