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
