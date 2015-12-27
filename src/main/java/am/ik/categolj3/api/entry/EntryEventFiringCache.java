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

import am.ik.categolj3.api.event.EntryEvictEvent;
import am.ik.categolj3.api.event.EntryPutEvent;
import am.ik.categolj3.api.event.EventManager;
import org.springframework.cache.Cache;

public class EntryEventFiringCache implements Cache {
    private final Cache delegate;
    private final EventManager eventManager;

    public EntryEventFiringCache(Cache delegate, EventManager eventManager) {
        this.delegate = delegate;
        this.eventManager = eventManager;
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    @Override
    public ValueWrapper get(Object key) {
        return delegate.get(key);
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        return delegate.get(key, type);
    }

    @Override
    public void put(Object key, Object value) {
        delegate.put(key, value);
        eventManager.registerEntryPutEvent(new EntryPutEvent((Entry) value));
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        ValueWrapper wrapper = delegate.putIfAbsent(key, value);
        if (wrapper == null) {
            eventManager.registerEntryPutEvent(new EntryPutEvent((Entry) value));
        }
        return wrapper;
    }

    @Override
    public void evict(Object key) {
        delegate.evict(key);
        eventManager.registerEntryEvictEvent(new EntryEvictEvent((Long) key));
    }

}
