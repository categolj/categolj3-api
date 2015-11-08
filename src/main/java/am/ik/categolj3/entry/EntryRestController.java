package am.ik.categolj3.entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/entries")
public class EntryRestController {
    @Autowired
    EntryService entryService;

    @RequestMapping(method = RequestMethod.GET)
    Page<Entry> getEntries(@PageableDefault Pageable pageable) {
        return entryService.findAll(pageable);
    }

    @RequestMapping(path = "{entryId}", method = RequestMethod.GET)
    Entry getEntry(@PathVariable Long entryId) {
        return entryService.findOne(entryId);
    }
}
