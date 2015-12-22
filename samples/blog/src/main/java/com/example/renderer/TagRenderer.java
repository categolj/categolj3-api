package com.example.renderer;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TagRenderer {

    public String render(List<String> tags) {
        UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
        return tags.stream()
                .map(tag -> "<a href=\"" + builder.replacePath("/tags/{tag}/entries").buildAndExpand(tag) + "\">" + tag + "</a>")
                .collect(Collectors.joining(", "));
    }
}
