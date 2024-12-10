package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableTransactionManagement
@EnableFeignClients
public class SsServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsServerApplication.class, args);
    }

}
