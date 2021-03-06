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
package am.ik.categolj3.api.git;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.PullResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("api/git")
@Slf4j
public class GitRestController {
    @Autowired
    GitStore gitStore;

    @RequestMapping(path = "pull")
    CompletableFuture<String> pull() {
        return gitStore.pull()
                .thenApply(PullResult::toString)
                .exceptionally(e -> {
                    log.error("Failed to git pull", e);
                    return e.toString();
                });
    }
}
