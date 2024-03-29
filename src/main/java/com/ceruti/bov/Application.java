package com.ceruti.bov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) throws Exception {
        SpringApplication app = new SpringApplication(Application.class);

//         DO NOT COMMIT THIS:
//        app.setDefaultProperties(Collections
//                .singletonMap("server.port", "8081"));
//        System.setProperty("server.port", "8081");

        SpringApplication.run(Application.class, args);
    }
}
