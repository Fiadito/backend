package pe.edu.upc.fiadito.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Fiadito Platform: API REST de Fiadito (Spring Boot 3, Java 21, MySQL 8).
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableJpaAuditing
public class FiaditoPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(FiaditoPlatformApplication.class, args);
    }
}
