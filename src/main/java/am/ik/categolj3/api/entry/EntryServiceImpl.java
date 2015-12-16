package am.ik.categolj3.api.entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EntryServiceImpl implements EntryService {
    @Autowired
    SimpleEntryOperations simpleEntryOperations;

    @Autowired
    SearchEntryOperations searchEntryOperations;

    @Override
    public Page<Entry> findAll(Pageable pageable) {
        return this.searchEntryOperations.findAll(pageable);
    }

    @Override
    public Page<Entry> findByTag(String tag, Pageable pageable) {
        return this.searchEntryOperations.findByTag(tag, pageable);
    }

    @Override
    public Page<Entry> findByCategories(List<String> categories, Pageable pageable) {
        return this.searchEntryOperations.findByCategories(categories, pageable);
    }

    @Override
    public Page<Entry> findByCreatedBy(String user, Pageable pageable) {
        return this.searchEntryOperations.findByCreatedBy(user, pageable);
    }

    @Override
    public Page<Entry> findByQuery(String keyword, Pageable pageable) {
        return this.searchEntryOperations.findByQuery(keyword, pageable);
    }

    @Override
    public Entry findOne(Long entryId) {
        return this.simpleEntryOperations.findOne(entryId);
    }
}
