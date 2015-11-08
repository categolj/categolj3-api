package am.ik.categolj3.entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
    public Entry findOne(Long entryId) {
        return this.simpleEntryOperations.findOne(entryId);
    }
}
