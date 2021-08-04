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
        var input = "test";
        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

        var delegator = new StdinDelegator(inputStream);

        delegator.handle();
    }
}
