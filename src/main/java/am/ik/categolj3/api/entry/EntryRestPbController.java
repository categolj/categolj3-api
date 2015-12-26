package am.ik.categolj3.api.entry;

import am.ik.categolj3.protos.Entry;
import am.ik.categolj3.protos.EntryPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api", produces = "application/x-protobuf")
public class EntryRestPbController {
    @Autowired
    EntryRestController delegate;

    @RequestMapping(path = "entries", method = RequestMethod.GET)
    EntryPage getEntries(@PageableDefault Pageable pageable, @RequestParam(defaultValue = "false") boolean excludeContent) {
        Page<am.ik.categolj3.api.entry.Entry> page = this.delegate.getEntries(pageable, excludeContent);
        return toEntryPageBuilder(page).build();
    }

    @RequestMapping(path = "entries", method = RequestMethod.GET, params = "q")
    EntryPage searchEntries(@PageableDefault Pageable pageable, @RequestParam String q, @RequestParam(defaultValue = "false") boolean excludeContent) {
        Page<am.ik.categolj3.api.entry.Entry> page = this.delegate.searchEntries(pageable, q, excludeContent);
        return toEntryPageBuilder(page).build();
    }

    @RequestMapping(path = "users/{createdBy}/entries", method = RequestMethod.GET)
    EntryPage getEntriesByCreatedBy(@PageableDefault Pageable pageable, @PathVariable String createdBy, @RequestParam(defaultValue = "false") boolean excludeContent) {
        Page<am.ik.categolj3.api.entry.Entry> page = this.delegate.getEntriesByCreatedBy(pageable, createdBy, excludeContent);
        return toEntryPageBuilder(page).build();
    }

    @RequestMapping(path = "users/{updatedBy}/entries", method = RequestMethod.GET, params = "updated")
    EntryPage getEntriesByUpdatedBy(@PageableDefault Pageable pageable, @PathVariable String updatedBy, @RequestParam(defaultValue = "false") boolean excludeContent) {
        Page<am.ik.categolj3.api.entry.Entry> page = this.delegate.getEntriesByUpdatedBy(pageable, updatedBy, excludeContent);
        return toEntryPageBuilder(page).build();
    }

    @RequestMapping(path = "tags/{tag}/entries", method = RequestMethod.GET)
    EntryPage getEntriesByTag(@PageableDefault Pageable pageable, @PathVariable String tag, @RequestParam(defaultValue = "false") boolean excludeContent) {
        Page<am.ik.categolj3.api.entry.Entry> page = this.delegate.getEntriesByTag(pageable, tag, excludeContent);
        return toEntryPageBuilder(page).build();
    }

    @RequestMapping(path = "categories/{categories}/entries", method = RequestMethod.GET)
    EntryPage getEntriesByCategories(@PageableDefault Pageable pageable, @PathVariable String categories, @RequestParam(defaultValue = "false") boolean excludeContent) {
        Page<am.ik.categolj3.api.entry.Entry> page = this.delegate.getEntriesByCategories(pageable, categories, excludeContent);
        return toEntryPageBuilder(page).build();
    }

    @RequestMapping(path = "entries/{entryId}", method = RequestMethod.GET)
    Entry getEntry(@PathVariable Long entryId) {
        am.ik.categolj3.api.entry.Entry entry = this.delegate.getEntry(entryId);
        return toEntryBuilder(entry).build();
    }

    Entry.Builder toEntryBuilder(am.ik.categolj3.api.entry.Entry entry) {
        Entry.FrontMatter.Builder frontMatterBuilder = Entry.FrontMatter.newBuilder()
                .setTitle(entry.getFrontMatter().getTitle());
        entry.getFrontMatter().getTags().forEach(frontMatterBuilder::addTags);
        entry.getFrontMatter().getCategories().forEach(frontMatterBuilder::addCategories);
        return Entry.newBuilder()
                .setEntryId(entry.getEntryId())
                .setContent(entry.getContent())
                .setCreated(Entry.Author.newBuilder()
                        .setName(entry.getCreated().getName())
                        .setDate(entry.getCreated().getDate().toEpochSecond()))
                .setUpdated(Entry.Author.newBuilder()
                        .setName(entry.getUpdated().getName())
                        .setDate(entry.getUpdated().getDate().toEpochSecond()))
                .setFrontMatter(frontMatterBuilder);
    }

    EntryPage.Builder toEntryPageBuilder(Page<am.ik.categolj3.api.entry.Entry> page) {
        EntryPage.Builder entryPageBuilder = EntryPage.newBuilder()
                .setTotalPages(page.getTotalPages())
                .setTotalElements(page.getTotalElements())
                .setFirst(page.isFirst())
                .setLast(page.isLast())
                .setNumberOfElements(page.getNumberOfElements())
                .setSize(page.getSize())
                .setNumber(page.getNumber());
        page.getContent().forEach(e -> entryPageBuilder.addContent(toEntryBuilder(e)));
        return entryPageBuilder;
    }
}
