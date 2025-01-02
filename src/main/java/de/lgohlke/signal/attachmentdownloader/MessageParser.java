package de.lgohlke.signal.attachmentdownloader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lgohlke.signal.attachmentdownloader.mapping.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class MessageParser {
    // object mapper is not thread safe so wrap it safely
    private final ThreadLocal<ObjectMapper> mapperThreadLocal = new ThreadLocal<>();

    private ObjectMapper retrieveCurrentMapper() {
        ObjectMapper objectMapper = mapperThreadLocal.get();
        if (objectMapper == null) {
            ObjectMapper ob = new ObjectMapper();
            mapperThreadLocal.set(ob);
            return ob;
        }
        return objectMapper;
    }

    Optional<Message> parse(String line) {
        ObjectMapper objectMapper = retrieveCurrentMapper();
        try {
            Message message = objectMapper.readValue(line, Message.class);
            return Optional.of(message);
        } catch (JsonProcessingException e) {
            log.warn("ignored message (not parsable)");
            log.warn(e.getMessage());
        }
        return Optional.empty();
    }
}
