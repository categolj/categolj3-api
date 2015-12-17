package am.ik.categolj3.api;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CategoLJ3ApiConfig.class)
public @interface EnableCategoLJ3ApiServer {
}
