package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by ren_xt
 */
@Component
public class SpringDataUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public SpringDataUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(s);
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(),
                Stream.of(user.getRoles()).map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList())
        );
    }
}
