package ru.ilya.filedrive.service;

import jakarta.transaction.Transactional;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.ilya.filedrive.dao.UserRepository;
import ru.ilya.filedrive.entity.User;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;


@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Autowired
    private UserService userService;

    private User createRandomUser() {
        return User.builder()
                .username(UUID.randomUUID().toString())
                .password(RandomStringUtils.random(30))
                .email(Math.random() < 0.3 ? null : RandomStringUtils.random(20) + "@gmail.com")
                .build();
    }

    @Test
    public void sanityTest() {
        User user = createRandomUser();
        User created = assertDoesNotThrow(() -> userService.createUser(user, true));

        assertThat(created.getId()).isGreaterThan(0);
        assertThat(created.getUsername()).isEqualTo(user.getUsername());
        assertThat(created.getEmail()).isEqualTo(user.getEmail());
        assertThat(created.getAuthorities()).isEqualTo(Set.of());
    }

    @Transactional
    @RepeatedTest(1)
    public void unhashedPasswordTests() {

        String password = RandomStringUtils.random(30);

        User u = User.builder()
                .username(UUID.randomUUID().toString())
                .password(password)
                .build();

        User created = assertDoesNotThrow(() -> userService.createUser(u, false));

        assertNotNull(created);

        assertThat(created.getUsername()).isEqualTo(u.getUsername());
        assertThat(created.getEmail()).isEqualTo(u.getEmail());
        assertThat(created.getPassword()).isNotEqualTo(u.getPassword());
        assertThat(created.getAuthorities()).isEqualTo(Set.of());

        User loaded = assertDoesNotThrow(() -> userService.loadUser(u.getUsername(), password));

        assertThat(created).isEqualTo(loaded);

    }
}

