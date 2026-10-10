package com.crmapp.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the CRM backend (see the cahier des charges, section 12 —
 * detailed technical architecture).
 */
@SpringBootApplication
public class CrmBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrmBackendApplication.class, args);
    }

}
