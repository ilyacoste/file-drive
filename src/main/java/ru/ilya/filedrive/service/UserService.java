package ru.ilya.filedrive.service;

import jakarta.annotation.PostConstruct;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.ilya.filedrive.dao.UserRepository;
import ru.ilya.filedrive.entity.User;
import ru.ilya.filedrive.exception.InvalidCredentialsException;

import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    private static final String UNHASHED_PASSWORD_PREFIX = "u:";

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    private boolean passwordMatch(String guess, String target) {
        if (target.startsWith(UNHASHED_PASSWORD_PREFIX)) {
            return guess.equals(target.substring(UNHASHED_PASSWORD_PREFIX.length()));
        } else {
            throw new IllegalStateException("Hashed password are forbidden");
        }
    }

    private UserDetails loadUserDetails(String username, String password) throws InvalidCredentialsException {
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty() || !passwordMatch(password, user.get().getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        return user.get();
    }

}
