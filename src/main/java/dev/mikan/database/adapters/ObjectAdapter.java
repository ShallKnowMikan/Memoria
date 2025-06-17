package dev.mikan.database.adapters;

public interface ObjectAdapter<T> {

    String serialize(T obj);

    T deserialize(String data);
}
