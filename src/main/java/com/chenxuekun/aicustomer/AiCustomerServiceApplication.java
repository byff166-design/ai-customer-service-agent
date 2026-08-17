package com.chenxuekun.aicustomer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.chenxuekun.aicustomer.mapper")
@SpringBootApplication
public class AiCustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCustomerServiceApplication.class, args);
    }
}
