package com.example;

import am.ik.categolj3.api.EnableCategoLJ3ApiServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableCategoLJ3ApiServer
public class HelloCategolj3Application {

    public static void main(String[] args) {
        SpringApplication.run(HelloCategolj3Application.class, args);
    }
}
