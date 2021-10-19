package de.lgohlke.signal.attachmentdownloader;

import de.lgohlke.signal.attachmentdownloader.mapping.Message;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentLinkedQueue;

import static org.assertj.core.api.Assertions.assertThat;

public class MappingFilterTest {
    @Test
    void should_handle_valid_json() {
        val messages = new ConcurrentLinkedQueue<Message>();

        new MappingFilter(messages::add).handle("{\"envelope\":{}}}");

        assertThat(messages).hasSize(1);
    }
}
