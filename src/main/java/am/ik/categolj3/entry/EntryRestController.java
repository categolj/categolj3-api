package am.ik.categolj3.entry;

import com.google.common.base.Splitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "api")
public class EntryRestController {
    @Autowired
    EntryService entryService;
    @Autowired
    EntryProperties entryProperties;

    @RequestMapping(path = "entries", method = RequestMethod.GET)
    Page<Entry> getEntries(@PageableDefault Pageable pageable) {
        return entryService.findAll(pageable);
    }

    @RequestMapping(path = "entries", method = RequestMethod.GET, params = "q")
    Page<Entry> searchEntries(@PageableDefault Pageable pageable, @RequestParam String q) {
        return entryService.findByQuery(q, pageable);
    }

    @RequestMapping(path = "users/{createdBy}/entries", method = RequestMethod.GET)
    Page<Entry> getEntriesByCreatedBy(@PageableDefault Pageable pageable, @PathVariable String createdBy) {
        return entryService.findByCreatedBy(createdBy, pageable);
    }

    @RequestMapping(path = "tags/{tag}/entries", method = RequestMethod.GET)
    Page<Entry> getEntriesByTag(@PageableDefault Pageable pageable, @PathVariable String tag) {
        return entryService.findByTag(tag, pageable);
    }

    @RequestMapping(path = "categories/{categories}/entries", method = RequestMethod.GET)
    Page<Entry> getEntriesByCategories(@PageableDefault Pageable pageable, @PathVariable String categories) {
        List<String> c = Splitter.on(entryProperties.getCategoriesSeparator())
                .trimResults()
                .omitEmptyStrings()
                .splitToList(categories)
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        return entryService.findByCategories(c, pageable);
    }

    @RequestMapping(path = "entries/{entryId}", method = RequestMethod.GET)
    Entry getEntry(@PathVariable Long entryId) {
        return entryService.findOne(entryId);
    }
}
