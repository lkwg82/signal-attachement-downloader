package de.lgohlke.signal.attachmentdownloader;

import de.lgohlke.signal.attachmentdownloader.mapping.Message;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageParserTest {
    @Test
    void should_handle_valid_json() {

        MessageParser messageParser = new MessageParser();
        Optional<Message> messageOptional = messageParser.parse("{\"envelope\":{}}}");

        assertThat(messageOptional).isNotEmpty();
    }
}
