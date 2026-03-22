package ru.ilya.filedrive.service;

import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.ilya.filedrive.dao.UserRepository;
import ru.ilya.filedrive.entity.User;
import ru.ilya.filedrive.exception.InvalidCredentialsException;
import ru.ilya.filedrive.exception.UserAlreadyExistsException;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@Log4j2
public class UserService {
    private static final String UNHASHED_PASSWORD_PREFIX = "u:";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private String makeUserPassword(String rawPassword, boolean hash) {
        return hash? passwordEncoder.encode(rawPassword) : UNHASHED_PASSWORD_PREFIX + rawPassword;
    }

    private boolean passwordMatch(String rawPassword, String target) {
        Objects.requireNonNull(rawPassword);
        Objects.requireNonNull(target);

        if (target.startsWith(UNHASHED_PASSWORD_PREFIX)) {
            return rawPassword.equals(target.substring(UNHASHED_PASSWORD_PREFIX.length()));
        } else {
            return passwordEncoder.matches(rawPassword, target);
        }
    }

    public User createUser(User user, boolean hashPassword) throws UserAlreadyExistsException {
        Objects.requireNonNull(user);

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException("User with username=" + user.getUsername() + " already exists");
        } else if(user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("User with email=" + user.getEmail() + " already exists");
        }

        User toSave = user.withPassword(makeUserPassword(user.getPassword(), hashPassword));

        return userRepository.save(toSave);
    }

    public User loadUser(String username, String password) throws InvalidCredentialsException {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);

        return userRepository.findByUsername(username)
                .filter(found -> passwordMatch(password, found.getPassword()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
    }

    @PostConstruct
    private void addMaster() {
        if (!userRepository.existsByUsername("master")) {
            User master = User.builder()
                    .username("master")
                    .password("master")
                    .authorities(Set.of(User.Authority.ROLE_ADMIN.name(), User.Authority.ROLE_USER.name()))
                    .email(null)
                    .build();

            userRepository.save(master);
        }
    }

}
