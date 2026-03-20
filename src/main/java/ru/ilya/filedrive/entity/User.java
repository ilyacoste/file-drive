package ru.ilya.filedrive.entity;

import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Table(name = "users")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    public enum Authority {
        ROLE_USER,
        ROLE_ADMIN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "varchar(100)", unique = true, nullable = false)
    private String username;

    @Column(columnDefinition = "varchar(100)", unique = true)
    private String email;

    @Column(columnDefinition = "varchar(100)", nullable = false)
    private String password;

    @ElementCollection
    @CollectionTable(
            name="user_authorities",
            joinColumns = @JoinColumn(name = "user_id")
    )
    private Set<String> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities.stream().map(SimpleGrantedAuthority::new).toList();
    }

    public boolean hasAuthority(Authority authority) {
        return authorities.contains(authority.name());
    }
}
