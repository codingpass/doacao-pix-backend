package br.com.pixdonation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PixDonationApplication {

    public static void main(String[] args) {
        SpringApplication.run(PixDonationApplication.class, args);
    }
}