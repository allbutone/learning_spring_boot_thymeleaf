package com.example.demo;

import org.springframework.data.repository.CrudRepository;

/**
 * Created by ren_xt
 */
public interface UserRepository extends CrudRepository<User, Long> {
    User findByUsername(String username);
}
