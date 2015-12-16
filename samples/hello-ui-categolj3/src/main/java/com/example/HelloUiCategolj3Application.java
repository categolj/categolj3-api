package com.example;

import am.ik.categolj3.api.EnableCategoLJ3Api;
import am.ik.categolj3.api.entry.Entry;
import am.ik.marked4j.Marked;
import am.ik.marked4j.MarkedBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableCategoLJ3Api
@Controller
public class HelloUiCategolj3Application {

    public static void main(String[] args) {
        SpringApplication.run(HelloUiCategolj3Application.class, args);
    }


    @Autowired
    RestTemplate restTemplate;

    @RequestMapping("/")
    String home(Model model, @Value("#{request.requestURL}") StringBuffer requestUrl) {
        Page<Entry> entries = restTemplate.exchange(requestUrl.append("/api/entries").toString()
                , HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<Page<Entry>>() {
                }).getBody();
        model.addAttribute("page", entries);
        return "index";
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    Marked marked() {
        return new MarkedBuilder()
                .breaks(true)
                .sanitize(true)
                .build();
    }
}
