package com.ticket_booking.service.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IRedisService<K, F, V> {
    
    void set(K key, V value);
    
    void setTimeToLive(K key, Long timeoutInDays);
    
    void hashSet(K key, F field, V value);

    boolean hashExists(K key, F field);

    V get(K key);

    public Map<F, V> getField(K key);

    V hashGet(K key,F field);

    List<V> hashGetByFieldPrefix(K key, String fieldPrefix);

    Set<F> getFieldPrefixes(K key);
    
    void delete(K key);

    void delete(K key, F fields);

    void delete(K key, List<F> fields);

    
}
