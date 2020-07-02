package com.cube.examples.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;

import com.cube.examples.users.User;
import com.cube.examples.users.UserRepository;

@Configuration
@Slf4j
public class LoadDatabase {

    @Value("${data.users:admin,userman,user1,user2,user3,user4,user5}")
    private String[] users;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("clientSecret:secret")
    private String clientSecret;

    int rnd(int size) {
        return (int) (Math.random() * size);
    }

    @Bean
    CommandLineRunner initUsers(UserRepository repo) {

        return args -> {
            for (int i = 0; i < users.length; i++) {
                String email = users[i] + "@" + users[i] + ".com";
                User.Role role = i > 1 ? User.Role.USER : i == 0 ? User.Role.ADMIN : User.Role.USER_MANAGER;
                String pwd = passwordEncoder.encode("pwd");
                log.info("save {}", repo.save(new User(null, email, pwd, role)));
            }
        };
    }
}
