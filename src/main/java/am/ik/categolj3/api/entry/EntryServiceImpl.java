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
    public Page<Entry> findAll(Pageable pageable, SearchOptions options) {
        return this.searchEntryOperations.findAll(pageable, options);
    }

    @Override
    public Page<Entry> findByTag(String tag, Pageable pageable, SearchOptions options) {
        return this.searchEntryOperations.findByTag(tag, pageable, options);
    }

    @Override
    public Page<Entry> findByCategories(List<String> categories, Pageable pageable, SearchOptions options) {
        return this.searchEntryOperations.findByCategories(categories, pageable, options);
    }

    @Override
    public Page<Entry> findByCreatedBy(String user, Pageable pageable, SearchOptions options) {
        return this.searchEntryOperations.findByCreatedBy(user, pageable, options);
    }

    @Override
    public Page<Entry> findByUpdatedBy(String user, Pageable pageable, SearchOptions options) {
        return this.searchEntryOperations.findByUpdatedBy(user, pageable, options);
    }

    @Override
    public Page<Entry> findByQuery(String keyword, Pageable pageable, SearchOptions options) {
        return this.searchEntryOperations.findByQuery(keyword, pageable, options);
    }

    @Override
    public Entry findOne(Long entryId) {
        return this.simpleEntryOperations.findOne(entryId);
    }
}
