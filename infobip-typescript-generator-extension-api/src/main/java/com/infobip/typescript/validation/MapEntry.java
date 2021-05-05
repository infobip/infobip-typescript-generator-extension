package com.infobip.typescript.validation;

import java.util.Map;
import java.util.Objects;

class MapEntry<K, V> implements Map.Entry<K, V> {

    private final K key;
    private final V value;

    MapEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MapEntry<?, ?> mapEntry = (MapEntry<?, ?>) o;
        return Objects.equals(key, mapEntry.key) && Objects.equals(value, mapEntry.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}
