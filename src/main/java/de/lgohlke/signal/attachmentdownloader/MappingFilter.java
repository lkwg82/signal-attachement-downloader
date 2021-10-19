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

    @Override
    public void handle(String input) {
        var mapper = new ObjectMapper();
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

    @Override
    public void setDebug(boolean flag) {
        this.isDebug = flag;
    }
}
