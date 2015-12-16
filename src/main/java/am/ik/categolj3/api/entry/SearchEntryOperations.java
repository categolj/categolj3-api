package am.ik.categolj3.api.entry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SearchEntryOperations {
    Page<Entry> findAll(Pageable pageable);

    Page<Entry> findByTag(String tag, Pageable pageable);

    Page<Entry> findByCategories(List<String> categories, Pageable pageable);

    Page<Entry> findByCreatedBy(String user, Pageable pageable);

    Page<Entry> findByQuery(String q, Pageable pageable);
}
