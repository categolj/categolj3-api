# categolj3-api

CategoLJ3 API Server is REST API Server for Blog Application

[![Build Status](https://travis-ci.org/categolj/categolj3-api.svg?branch=master)](https://travis-ci.org/categolj/categolj3-api)

``` xml
<dependency>
    <groupId>am.ik.categolj3</groupId>
    <artifactId>categolj3-api</artifactId>
    <version>1.0.0.M6</version>
</dependency>
```

Add `@EnableCategoLJ3ApiServer` in Spring Boot app:

``` java
import am.ik.categolj3.api.EnableCategoLJ3ApiServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableCategoLJ3ApiServer
public class BlogApiServer {

    public static void main(String[] args) {
        SpringApplication.run(BlogApiServer.class, args);
    }
}
```

Check API [http://localhost:8080/docs/api-guide.html](http://localhost:8080/docs/api-guide.html)

See [samples](samples)
