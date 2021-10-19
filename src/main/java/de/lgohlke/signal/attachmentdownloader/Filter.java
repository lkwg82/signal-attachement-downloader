package de.lgohlke.signal.attachmentdownloader;

public interface Filter<T> {
    void handle(T input);
}
