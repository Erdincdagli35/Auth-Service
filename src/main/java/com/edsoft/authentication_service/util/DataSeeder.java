package com.edsoft.authentication_service.util;

import com.edsoft.authentication_service.model.AppUser;
import com.edsoft.authentication_service.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Set;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(AppUserRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.findByUsername("admin").isEmpty()) {
                repo.save(new AppUser(null, "admin", encoder.encode("123"), Set.of("ROLE_ADMIN")));
            }
            if (repo.findByUsername("customer").isEmpty()) {
                repo.save(new AppUser(null, "customer", encoder.encode("123"), Set.of("ROLE_CUSTOMER")));
            }
        };
    }
}