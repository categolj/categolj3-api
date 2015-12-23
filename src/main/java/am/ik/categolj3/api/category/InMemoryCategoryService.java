package am.ik.categolj3.api.category;

import am.ik.categolj3.api.entry.Entry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class InMemoryCategoryService implements CategoryService {
    final Cache entryCache;

    public InMemoryCategoryService(CacheManager cacheManager) {
        this.entryCache = cacheManager.getCache("entry");
        if (this.entryCache == null) {
            throw new IllegalArgumentException("cache named 'entry' is not found!");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<List<String>> findAllOrderByNameAsc() {
        Object nativeCache = entryCache.getNativeCache();
        Map<Object, Entry> cache;
        if (nativeCache instanceof com.google.common.cache.Cache) {
            cache = ((com.google.common.cache.Cache) nativeCache).asMap();
        } else if (nativeCache instanceof Map) {
            cache = (Map<Object, Entry>) entryCache.getNativeCache();
        } else {
            log.warn("native cache is not map -> {}", nativeCache);
            return Collections.emptyList();
        }
        return cache.values().stream()
                .map(e -> e.getFrontMatter().getCategories())
                .distinct()
                .sorted(Comparator.comparing(categories -> String.join(",", categories)))
                .collect(Collectors.toList());
    }
}
