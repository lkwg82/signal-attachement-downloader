package de.lgohlke.signal.attachmentdownloader;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
        return new StdinDelegator(inputStream);
    }
}
