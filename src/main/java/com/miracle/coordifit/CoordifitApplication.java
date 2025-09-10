package com.miracle.coordifit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.miracle.coordifit.image.repository")
public class CoordifitApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoordifitApplication.class, args);
    }
}