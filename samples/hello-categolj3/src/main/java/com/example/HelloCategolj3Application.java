package com.example;

import am.ik.categolj3.api.EnableCategoLJ3Api;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableCategoLJ3Api
public class HelloCategolj3Application {

    public static void main(String[] args) {
        SpringApplication.run(HelloCategolj3Application.class, args);
    }
}
