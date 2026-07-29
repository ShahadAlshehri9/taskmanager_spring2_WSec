package com.example.taskmanager.Security;

import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class CustomUserDetailsService implements UserDetailsService {
private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /* UserDetailsService: It is an interface in Spring Security used to load user-specific
    data during authentication. fetching user details from a data source such as a database.
     UserDetails: It is an interface that represents the authenticated user in Spring Security. It acts as a container.
    - Username
    - Password
    - Roles / Authorities
    - Account status (enabled, locked, expired, etc.)
    */
//table ↔ Spring Security
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //the user object will hold the user returned from the repo for authentication the method findByUsername will return optional which not require the presentment of User entity so it may or may not hold  a value.
        // the .orElseThrow() create a exception and throw it in case of null value and prevent ugly NullPointerException errors it will stop and return an error with no user named ----.
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("No user named " + username));
        // () ->... constructs and throws the Spring Security UsernameNotFoundException if the user is missing from the database.


        //Return the account (username, stored hash, roles), and Spring compares the
        //typed password to the hash using the encoder in the SecurityConfig bean
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),                                   // the stored hash
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))//this to match the add ROLE prefix in .hasRole("")
        );
    }
}
