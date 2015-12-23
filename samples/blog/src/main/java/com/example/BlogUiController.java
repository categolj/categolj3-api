package com.example;

import am.ik.categolj3.api.entry.Entry;
import am.ik.marked4j.Marked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Controller
public class BlogUiController {

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    Marked marked;

    final ParameterizedTypeReference<Page<Entry>> typeReference = new ParameterizedTypeReference<Page<Entry>>() {
    };

    @RequestMapping(value = {"/", "/entries"})
    String home(Model model, UriComponentsBuilder builder, @PageableDefault(size = 3) Pageable pageable) {
        UriComponents uri = builder
                .replacePath("/api/entries")
                .queryParam("page", pageable.getPageNumber())
                .queryParam("size", pageable.getPageSize())
                .queryParam("excludeContent", true)
                .build();
        Page<Entry> entries = restTemplate.exchange(uri.toUri(), HttpMethod.GET, HttpEntity.EMPTY, typeReference).getBody();
        model.addAttribute("page", entries);
        return "index";
    }

    @RequestMapping(value = {"/", "/entries"}, params = "q")
    String search(Model model, UriComponentsBuilder builder, @RequestParam("q") String query, @PageableDefault(size = 3) Pageable pageable) {
        UriComponents uri = builder
                .replacePath("/api/entries")
                .queryParam("q", query)
                .queryParam("page", pageable.getPageNumber())
                .queryParam("size", pageable.getPageSize())
                .queryParam("excludeContent", true)
                .build();
        Page<Entry> entries = restTemplate.exchange(uri.toUri(), HttpMethod.GET, HttpEntity.EMPTY, typeReference).getBody();
        model.addAttribute("page", entries);
        return "index";
    }

    @RequestMapping(path = "/entries/{entryId}")
    String byId(Model model, UriComponentsBuilder builder, @PathVariable("entryId") Long entryId) {
        UriComponents uri = builder
                .replacePath("/api/entries/{entryId}")
                .buildAndExpand(entryId);
        Entry entry = restTemplate.exchange(uri.toUri(), HttpMethod.GET, HttpEntity.EMPTY, Entry.class).getBody();
        model.addAttribute("entry", entry);
        return "entry";
    }

    @RequestMapping(path = "/entries/{entryId}", params = "partial")
    @ResponseBody
    String partialById(UriComponentsBuilder builder, @PathVariable("entryId") Long entryId) {
        UriComponents uri = builder
                .replacePath("/api/entries/{entryId}")
                .buildAndExpand(entryId);
        Entry entry = restTemplate.exchange(uri.toUri(), HttpMethod.GET, HttpEntity.EMPTY, Entry.class).getBody();
        return marked.marked(entry.getContent());
    }

    @RequestMapping(path = "/tags/{tag}/entries")
    String byTag(Model model, UriComponentsBuilder builder, @PathVariable("tag") String tag, @PageableDefault(size = 3) Pageable pageable) {
        UriComponents uri = builder
                .replacePath("/api/tags/{tag}/entries")
                .queryParam("page", pageable.getPageNumber())
                .queryParam("size", pageable.getPageSize())
                .queryParam("excludeContent", true)
                .buildAndExpand(tag);
        Page<Entry> entries = restTemplate.exchange(uri.toUri(), HttpMethod.GET, HttpEntity.EMPTY, typeReference).getBody();
        model.addAttribute("page", entries);
        return "index";
    }

    @RequestMapping(path = "/categories/{categories}/entries")
    String byCategories(Model model, UriComponentsBuilder builder, @PathVariable("categories") String categories, @PageableDefault(size = 3) Pageable pageable) {
        UriComponents uri = builder
                .replacePath("/api/categories/{categories}/entries")
                .queryParam("page", pageable.getPageNumber())
                .queryParam("size", pageable.getPageSize())
                .queryParam("excludeContent", true)
                .buildAndExpand(categories);
        Page<Entry> entries = restTemplate.exchange(uri.toUri(), HttpMethod.GET, HttpEntity.EMPTY, typeReference).getBody();
        model.addAttribute("page", entries);
        return "index";
    }

    @RequestMapping(path = "/users/{name}/entries")
    String byCreatedBy(Model model, UriComponentsBuilder builder, @PathVariable("name") String name, @PageableDefault(size = 3) Pageable pageable) {
        UriComponents uri = builder
                .replacePath("/api/users/{name}/entries")
                .queryParam("page", pageable.getPageNumber())
                .queryParam("size", pageable.getPageSize())
                .queryParam("excludeContent", true)
                .buildAndExpand(name);
        Page<Entry> entries = restTemplate.exchange(uri.toUri(), HttpMethod.GET, HttpEntity.EMPTY, typeReference).getBody();
        model.addAttribute("page", entries);
        return "index";
    }

    @RequestMapping(path = "/users/{name}/entries", params = "updated")
    String byUpdatedBy(Model model, UriComponentsBuilder builder, @PathVariable("name") String name, @PageableDefault(size = 3) Pageable pageable) {
        UriComponents uri = builder
                .replacePath("/api/users/{name}/entries")
                .queryParam("updated")
                .queryParam("page", pageable.getPageNumber())
                .queryParam("size", pageable.getPageSize())
                .queryParam("excludeContent", true)
                .buildAndExpand(name);
        Page<Entry> entries = restTemplate.exchange(uri.toUri(), HttpMethod.GET, HttpEntity.EMPTY, typeReference).getBody();
        model.addAttribute("page", entries);
        return "index";
    }

    @RequestMapping(path = "/tags")
    String tags(Model model, UriComponentsBuilder builder) {
        UriComponents uri = builder
                .replacePath("/api/tags")
                .build();
        List<String> tags = restTemplate.exchange(uri.toUri(), HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<List<String>>() {
        }).getBody();
        model.addAttribute("tags", tags);
        return "tags";
    }

    @RequestMapping(path = "/categories")
    String categories(Model model, UriComponentsBuilder builder) {
        UriComponents uri = builder
                .replacePath("/api/categories")
                .build();
        List<List<String>> categories = restTemplate.exchange(uri.toUri(), HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<List<List<String>>>() {
        }).getBody();
        model.addAttribute("categories", categories);
        return "categories";
    }

    @lombok.Data
    public static class Page<T> {
        private List<T> content;
        private String sort;
        private long totalPages;
        private long totalElements;
        private boolean first;
        private boolean last;
        private long numberOfElements;
        private int size;
        private int number;
    }
}
