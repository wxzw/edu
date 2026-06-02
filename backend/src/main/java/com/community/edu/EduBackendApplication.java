package com.community.edu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.community.edu.mapper")
@SpringBootApplication
public class EduBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduBackendApplication.class, args);
    }
}
