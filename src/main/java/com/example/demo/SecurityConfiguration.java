package com.example.demo;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Created by ren_xt
 */
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .anyRequest().authenticated()
                .and()
            .formLogin()//默认 username 为 "user", password 从启动日志中查找
                .permitAll();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
//                spring security 会自动为角色添加 "ROLE_" 前缀
                .withUser("tom").password("tom").roles("ADMIN", "USER")
                .and()
                .withUser("jack").password("jack").roles("USER")
                .and()
                .withUser("locked").password("locked").roles("USER").accountLocked(true)
                .and()
                .withUser("disabled").password("disabled").roles("USER").disabled(true);
    }
}
