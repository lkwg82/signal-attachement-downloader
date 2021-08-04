package de.lgohlke.signal.attachmentdownloader;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lgohlke.signal.attachmentdownloader.mapping.Message;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Slf4j
class StdinDelegator {
    private final InputStream stdin;

    public StdinDelegator(InputStream inputStream) {
        stdin = inputStream;
    }

    public void handle() throws IOException {
        var mapper = new ObjectMapper();
        BufferedReader stdin = new BufferedReader(new InputStreamReader(this.stdin));
        var line = "";
        while ((line = stdin.readLine()) != null) {
            log.info("read line:" + line);
            try {
                var message = mapper.readValue(line, Message.class);
                System.out.println(message);
            } catch (JsonParseException e) {
                log.info("ignored message (not parsable)");
            }
        }
    }
}
