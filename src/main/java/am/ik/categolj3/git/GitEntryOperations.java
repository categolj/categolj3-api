package am.ik.categolj3.git;

import am.ik.categolj3.entry.Entry;
import am.ik.categolj3.entry.SimpleEntryOperations;
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
