package am.ik.categolj3.api.git;

import am.ik.categolj3.api.entry.Entry;
import am.ik.categolj3.api.entry.SimpleEntryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GitEntryOperations implements SimpleEntryOperations {
    @Autowired
    GitStore gitStore;

    @Override
    public Entry findOne(Long entryId) {
        return gitStore.get(entryId);
    }
}
