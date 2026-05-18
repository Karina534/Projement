package org.project.projemento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjementoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjementoApplication.class, args);
    }

}

// login - "/api/users/login/token"
// refresh - "/auth/refresh/token"
// logout - "/auth/logout"