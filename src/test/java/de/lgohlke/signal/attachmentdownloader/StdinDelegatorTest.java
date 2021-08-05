package de.lgohlke.signal.attachmentdownloader;

import de.lgohlke.signal.attachmentdownloader.mapping.Message;
import io.quarkus.test.junit.QuarkusTest;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@QuarkusTest
public class StdinDelegatorTest {
    @Test
    void should_ignore_not_valid_json() throws IOException {
        getDelegator("test").handle();
    }

    @Test
    void should_handle_valid_json() throws IOException {
        getDelegator("{\"envelope\":{}}}").handle();
    }

    private StdinDelegator getDelegator(String input) {
        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        val attachmentMover = new AttachmentMover(Path.of("/tmp"), Path.of("dummy")) {
            @Override
            public void handle(Message message) throws IOException {
                // ok
            }
        };
        return new StdinDelegator(inputStream, attachmentMover);
    }
}
