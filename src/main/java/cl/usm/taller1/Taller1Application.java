package cl.usm.taller1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class Taller1Application {

    public static void main(String[] args) {
        SpringApplication.run(Taller1Application.class, args);
    }

}
