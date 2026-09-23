package com.anpr.accesscontrol.config;

import com.anpr.accesscontrol.model.Admin;
import com.anpr.accesscontrol.repository.AdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Pri svakom pokretanju aplikacije proverava da li postoji ijedan admin -
 * ako ne postoji, pravi jednog sa kredencijalima iz application.properties
 * (podrazumevano username=admin, lozinka konfigurabilna). Ovo se izvrsava
 * samo jednom - cim postoji bar jedan admin u bazi, seeder nista ne radi.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final String defaultUsername;
    private final String defaultPassword;

    public AdminSeeder(AdminRepository adminRepository,
                        PasswordEncoder passwordEncoder,
                        @Value("${admin.default.username:admin}") String defaultUsername,
                        @Value("${admin.default.password:admin123}") String defaultPassword) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.defaultUsername = defaultUsername;
        this.defaultPassword = defaultPassword;
    }

    @Override
    public void run(String... args) {
        if (adminRepository.count() == 0) {
            Admin admin = new Admin();
            admin.setUsername(defaultUsername);
            admin.setPasswordHash(passwordEncoder.encode(defaultPassword));
            admin.setRole("ADMIN");
            adminRepository.save(admin);

            log.info("Kreiran podrazumevani admin nalog - username: '{}'. " +
                    "Promeni lozinku ili ovo podesavanje pre nego sto ovo ode u produkciju.",
                    defaultUsername);
        }
    }
}
