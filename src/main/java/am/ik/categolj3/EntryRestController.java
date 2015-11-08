package am.ik.categolj3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/entries")
public class EntryRestController {
    @Autowired
    GitEntryOperations gitEntryOperations;

    @RequestMapping(path = "{entryId}", method = RequestMethod.GET)
    Entry getEntry(@PathVariable Long entryId) {
        return gitEntryOperations.findOne(entryId);
    }
}
