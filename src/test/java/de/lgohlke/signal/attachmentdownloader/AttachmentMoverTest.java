package de.lgohlke.signal.attachmentdownloader;

import de.lgohlke.signal.attachmentdownloader.mapping.Attachment;
import de.lgohlke.signal.attachmentdownloader.mapping.DataMessage;
import de.lgohlke.signal.attachmentdownloader.mapping.Envelope;
import de.lgohlke.signal.attachmentdownloader.mapping.GroupInfo;
import de.lgohlke.signal.attachmentdownloader.mapping.Message;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class AttachmentMoverTest {

    private final File tempFolder = Files.newTemporaryFolder();

    @BeforeEach
    void setUp() {
        tempFolder.mkdirs();
    }

    @Test
    void should_move_attachment_from_direct_message() throws IOException {
        Message message = createTestMessage();
        assertThat(message.getEnvelope()
                          .getDataMessage()
                          .getGroupInfo()).isNull();

        var attachmentsOfSignal = prepareSignalAttachmentPath();
        var attachmentsMoved = prepareMovedAttachmentPath();
        var attachment = createTestAttachment(attachmentsOfSignal, message);


        SimpleDateFormat dformat = new SimpleDateFormat("yyyy-MM-dd");
        var timestamp = message.getEnvelope()
                               .getDataMessage()
                               .getTimestamp();

        var filename = dformat.format(new Date(timestamp.getTime())) + "_" + attachment.getId() + "_" + attachment.getFilename();
        var movedAttachment = attachmentsMoved.resolve("sourceA")
                                              .resolve(filename)
                                              .toFile();

        // action
        new AttachmentMover(attachmentsOfSignal, attachmentsMoved).handle(message);

        assertThat(movedAttachment).exists();
        assertThat(movedAttachment).isFile();
    }

    @Test
    void should_move_attachment_from_group_message() throws IOException {
        Message message = createTestMessage();
        var groupInfo = new GroupInfo();
        groupInfo.setGroupId("asdas/dasd");
        groupInfo.setType("DELIVER");
        message.getEnvelope()
               .getDataMessage()
               .setGroupInfo(groupInfo);

        var attachmentsOfSignal = prepareSignalAttachmentPath();
        var attachmentsMoved = prepareMovedAttachmentPath();
        var attachment = createTestAttachment(attachmentsOfSignal, message);


        SimpleDateFormat dformat = new SimpleDateFormat("yyyy-MM-dd");
        var timestamp = message.getEnvelope()
                               .getDataMessage()
                               .getTimestamp();

        var filename = dformat.format(new Date(timestamp.getTime())) + "_" + attachment.getId() + "_" + attachment.getFilename();
        var base64GroupId = Base64.getEncoder()
                                  .encodeToString(groupInfo.getGroupId()
                                                           .getBytes(StandardCharsets.UTF_8));
        var movedAttachment = attachmentsMoved.resolve("groups")
                                              .resolve(base64GroupId)
                                              .resolve("sourceA")
                                              .resolve(filename)
                                              .toFile();

        // action
        new AttachmentMover(attachmentsOfSignal, attachmentsMoved).handle(message);

        assertThat(movedAttachment).exists();
        assertThat(movedAttachment).isFile();
    }

    private Path prepareMovedAttachmentPath() {
        return tempFolder.toPath()
                         .resolve("new_attachments");
    }

    private Path prepareSignalAttachmentPath() {
        var attachmentsOfSignal = tempFolder.toPath()
                                            .resolve("attachments");
        attachmentsOfSignal.toFile()
                           .mkdirs();
        return attachmentsOfSignal;
    }

    private Attachment createTestAttachment(Path attachmentsOfSignal, Message message) throws IOException {
        var attachment = new Attachment();
        attachment.setFilename("IMG_01.jpg");
        attachment.setId(3149734190872347104L);
        var attachmentFile = attachmentsOfSignal.resolve(attachment.getId() + "")
                                                .toFile();
        attachmentFile.createNewFile();

        var dataMessage = message.getEnvelope()
                                 .getDataMessage();
        dataMessage.setAttachments(List.of(attachment));
        dataMessage.setTimestamp(new Timestamp(1628069823084L));

        return attachment;
    }

    private Message createTestMessage() throws IOException {
        var message = new Message();
        var envelope = new Envelope();
        envelope.setSource("sourceA");
        var dataMessage = new DataMessage();

        envelope.setDataMessage(dataMessage);
        message.setEnvelope(envelope);
        return message;
    }

    @Test
    void should_fail_on_wrong_signal_attachment_dir() {
        try {
            new AttachmentMover(Path.of("a"), Path.of("b"));
            fail("should fail");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).startsWith("could not find signal attachment path:");
        }
    }

    @Test
    void should_fail_on_when_signal_attachment_dir_is_no_dir() {
        try {
            var file = tempFolder.toPath()
                                 .resolve("a")
                                 .toFile();
            file.createNewFile();

            new AttachmentMover(file.toPath(), Path.of("b"));
            fail("should fail");
        } catch (IllegalArgumentException | IOException e) {
            assertThat(e.getMessage()).startsWith("signal attachment path should be a directory");
        }
    }

    @Test
    void should_fail_on_when_moved_attachment_dir_is_no_dir() {
        try {
            var attachmentsOfSignal = tempFolder.toPath()
                                                .resolve("a")
                                                .toFile();

            attachmentsOfSignal.mkdir();

            var file = tempFolder.toPath()
                                 .resolve("b")
                                 .toFile();
            file.createNewFile();

            new AttachmentMover(attachmentsOfSignal.toPath(), file.toPath());
            fail("should fail");
        } catch (IllegalArgumentException | IOException e) {
            assertThat(e.getMessage()).startsWith("moved attachment path should be a directory");
        }
    }

    @Test
    void should_detect_same_paths() {
        try {
            new AttachmentMover(Path.of("a"), Path.of("a"));
            fail("should fail");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).startsWith("paths should be different");
        }
    }

}
