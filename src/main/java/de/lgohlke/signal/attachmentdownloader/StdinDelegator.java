package de.lgohlke.signal.attachmentdownloader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lgohlke.signal.attachmentdownloader.mapping.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Slf4j
@RequiredArgsConstructor
class StdinDelegator implements Debuggable {
    private final InputStream stdin;
    private final AttachmentMover attachmentMover;
    private boolean isDebug;

    public void handle() throws IOException {
        var mapper = new ObjectMapper();
        BufferedReader stdin = new BufferedReader(new InputStreamReader(this.stdin));
        var line = "";
        while ((line = stdin.readLine()) != null) {
            log.info("read line: |" + line + "|");
            try {
                var message = mapper.readValue(line, Message.class);
                System.out.println(message);
                attachmentMover.handle(message);
            } catch (JsonProcessingException e) {
                log.info("ignored message (not parsable)");
                if (isDebug) {
                    log.info(e.getMessage());
                }
            }
        }
    }

    @Override
    public void setDebug(boolean flag) {
        this.isDebug = flag;
    }
}
