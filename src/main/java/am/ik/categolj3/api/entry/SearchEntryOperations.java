package am.ik.categolj3.api.entry;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SearchEntryOperations {
    Page<Entry> findAll(Pageable pageable, SearchOptions options);

    default Page<Entry> findAll(Pageable pageable) {
        return findAll(pageable, SearchOptions.DEFAULT);
    }

    Page<Entry> findByTag(String tag, Pageable pageable, SearchOptions options);

    default Page<Entry> findByTag(String tag, Pageable pageable) {
        return findByTag(tag, pageable, SearchOptions.DEFAULT);
    }

    Page<Entry> findByCategories(List<String> categories, Pageable pageable, SearchOptions options);

    default Page<Entry> findByCategories(List<String> categories, Pageable pageable) {
        return findByCategories(categories, pageable, SearchOptions.DEFAULT);
    }

    Page<Entry> findByCreatedBy(String user, Pageable pageable, SearchOptions options);

    default Page<Entry> findByCreatedBy(String user, Pageable pageable) {
        return findByCreatedBy(user, pageable, SearchOptions.DEFAULT);
    }

    Page<Entry> findByUpdatedBy(String user, Pageable pageable, SearchOptions options);

    default Page<Entry> findByUpdatedBy(String user, Pageable pageable) {
        return findByUpdatedBy(user, pageable, SearchOptions.DEFAULT);
    }

    Page<Entry> findByQuery(String q, Pageable pageable, SearchOptions options);

    default Page<Entry> findByQuery(String q, Pageable pageable) {
        return findByQuery(q, pageable, SearchOptions.DEFAULT);
    }

    @Data
    @Builder
    class SearchOptions {
        private boolean excludeContent;

        public static final SearchOptions DEFAULT = SearchOptions.builder()
                .excludeContent(false)
                .build();
    }
}
