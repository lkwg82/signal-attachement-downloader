package de.lgohlke.signal.attachmentdownloader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lgohlke.signal.attachmentdownloader.mapping.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MappingFilter implements Filter<String>, Debuggable {
    private final Filter<Message> messageFilter;
    private boolean isDebug;
    // object mapper is not thread safe so wrap it safely
    private final ThreadLocal<ObjectMapper> mapperThreadLocal = new ThreadLocal<>();

    @Override
    public void handle(String input) {
        ObjectMapper mapper = retrieveCurrentMapper();
        try {
            var message = mapper.readValue(input, Message.class);
            messageFilter.handle(message);
        } catch (JsonProcessingException e) {
            log.info("ignored message (not parsable)");
            if (isDebug) {
                log.info(e.getMessage());
            }
        }
    }

    private ObjectMapper retrieveCurrentMapper() {
        ObjectMapper objectMapper = mapperThreadLocal.get();
        if (objectMapper == null) {
            ObjectMapper ob = new ObjectMapper();
            mapperThreadLocal.set(ob);
            return ob;
        }
        return objectMapper;
    }

    @Override
    public void setDebug(boolean flag) {
        this.isDebug = flag;
    }
}
