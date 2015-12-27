/*
 * Copyright (C) 2015 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.ik.categolj3.api.entry;

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

    final SearchEntryOperations.SearchOptions excludeContentOption = SearchEntryOperations.SearchOptions.builder()
            .excludeContent(true)
            .build();

    @RequestMapping(path = "entries", method = RequestMethod.GET)
    Page<Entry> getEntries(@PageableDefault Pageable pageable, @RequestParam(defaultValue = "false") boolean excludeContent) {
        return excludeContent ? entryService.findAll(pageable, excludeContentOption) : entryService.findAll(pageable);
    }

    @RequestMapping(path = "entries", method = RequestMethod.GET, params = "q")
    Page<Entry> searchEntries(@PageableDefault Pageable pageable, @RequestParam String q, @RequestParam(defaultValue = "false") boolean excludeContent) {
        return excludeContent ? entryService.findByQuery(q, pageable, excludeContentOption) : entryService.findByQuery(q, pageable);
    }

    @RequestMapping(path = "users/{createdBy}/entries", method = RequestMethod.GET)
    Page<Entry> getEntriesByCreatedBy(@PageableDefault Pageable pageable, @PathVariable String createdBy, @RequestParam(defaultValue = "false") boolean excludeContent) {
        return excludeContent ? entryService.findByCreatedBy(createdBy, pageable, excludeContentOption) : entryService.findByCreatedBy(createdBy, pageable);
    }

    @RequestMapping(path = "users/{updatedBy}/entries", method = RequestMethod.GET, params = "updated")
    Page<Entry> getEntriesByUpdatedBy(@PageableDefault Pageable pageable, @PathVariable String updatedBy, @RequestParam(defaultValue = "false") boolean excludeContent) {
        return excludeContent ? entryService.findByUpdatedBy(updatedBy, pageable, excludeContentOption) : entryService.findByUpdatedBy(updatedBy, pageable);
    }

    @RequestMapping(path = "tags/{tag}/entries", method = RequestMethod.GET)
    Page<Entry> getEntriesByTag(@PageableDefault Pageable pageable, @PathVariable String tag, @RequestParam(defaultValue = "false") boolean excludeContent) {
        return excludeContent ? entryService.findByTag(tag, pageable, excludeContentOption) : entryService.findByTag(tag, pageable);
    }

    @RequestMapping(path = "categories/{categories}/entries", method = RequestMethod.GET)
    Page<Entry> getEntriesByCategories(@PageableDefault Pageable pageable, @PathVariable String categories, @RequestParam(defaultValue = "false") boolean excludeContent) {
        List<String> c = Splitter.on(entryProperties.getCategoriesSeparator())
                .trimResults()
                .omitEmptyStrings()
                .splitToList(categories)
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        return excludeContent ? entryService.findByCategories(c, pageable, excludeContentOption) : entryService.findByCategories(c, pageable);
    }

    @RequestMapping(path = "entries/{entryId}", method = RequestMethod.GET)
    Entry getEntry(@PathVariable Long entryId) {
        return entryService.findOne(entryId);
    }
}
