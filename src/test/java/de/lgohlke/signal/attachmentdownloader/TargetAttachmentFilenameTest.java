package de.lgohlke.signal.attachmentdownloader;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class TargetAttachmentFilenameTest {

    @Test
    void should_work() {
        Timestamp timestamp = Timestamp.from(Instant.ofEpochMilli(1735766805705L));
        TargetAttachmentFilename targetAttachmentFilename = new TargetAttachmentFilename(timestamp, "abc.jpg");
        assertThat(targetAttachmentFilename.createFilename()).isEqualTo("2025-01-01_abc.jpg");
    }

}
