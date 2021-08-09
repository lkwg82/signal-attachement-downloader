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
    private final Message message = createTestMessage();

    @BeforeEach
    void setUp() {
        tempFolder.mkdirs();
    }

    @Test
    void should_move_attachment_from_direct_message() throws IOException {
        var attachmentsOfSignal = prepareSignalAttachmentPath();
        var attachmentsMoved = prepareMovedAttachmentPath();

        var movedAttachment = buildMovedAttachmentFile(message, attachmentsOfSignal, attachmentsMoved);

        // action
        new AttachmentMover(attachmentsOfSignal, attachmentsMoved).handle(message);

        assertThat(movedAttachment).exists();
        assertThat(movedAttachment).isFile();
    }

    @Test
    void should_accept_already_moved_attachment() throws IOException {
        var attachmentsOfSignal = prepareSignalAttachmentPath();
        var attachmentsMoved = prepareMovedAttachmentPath();
        var attachment = createTestAttachment(attachmentsOfSignal, message);

        String filename = buildMovedFilename(message, attachment);
        var movedAttachment = attachmentsMoved.resolve(message.getEnvelope()
                                                              .getSource())
                                              .resolve(filename)
                                              .toFile();

        moveAttachmentToMovedFolder(attachmentsOfSignal, attachment, movedAttachment);

        // action
        new AttachmentMover(attachmentsOfSignal, attachmentsMoved).handle(message);

        assertThat(movedAttachment).exists();
        assertThat(movedAttachment).isFile();
    }

    @Test
    void should_move_attachment_from_group_message() throws IOException {
        var groupInfo = new GroupInfo();
        groupInfo.setGroupId("asdas/dasd");
        groupInfo.setType("DELIVER");
        message.getEnvelope()
               .getDataMessage()
               .setGroupInfo(groupInfo);

        var attachmentsOfSignal = prepareSignalAttachmentPath();
        var attachmentsMoved = prepareMovedAttachmentPath();
        var attachment = createTestAttachment(attachmentsOfSignal, message);

        var movedAttachment = buildMovedAttachmentFileForGroup(groupInfo, attachmentsMoved, attachment);

        // action
        new AttachmentMover(attachmentsOfSignal, attachmentsMoved).handle(message);

        assertThat(movedAttachment).exists();
        assertThat(movedAttachment).isFile();
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

    private File buildMovedAttachmentFile(Message message, Path attachmentsOfSignal, Path attachmentsMoved) throws IOException {
        var attachment = createTestAttachment(attachmentsOfSignal, message);
        String filename = buildMovedFilename(message, attachment);
        var movedAttachment = attachmentsMoved.resolve(message.getEnvelope()
                                                              .getSource())
                                              .resolve(filename)
                                              .toFile();
        return movedAttachment;
    }

    private void moveAttachmentToMovedFolder(Path attachmentsOfSignal, Attachment attachment, File movedAttachment) throws IOException {
        var sourcePath = attachmentsOfSignal.resolve(attachment.getId() + "");
        movedAttachment.getParentFile()
                       .mkdirs();
        java.nio.file.Files.move(sourcePath, movedAttachment.toPath());
        assertThat(sourcePath).doesNotExist();
    }

    private String buildMovedFilename(Message message, Attachment attachment) {
        SimpleDateFormat dformat = new SimpleDateFormat("yyyy-MM-dd");
        var timestamp = message.getEnvelope()
                               .getDataMessage()
                               .getTimestamp();

        return dformat.format(new Date(timestamp.getTime())) + "_" + attachment.getId() + "_" + attachment.getFilename();
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
        attachment.setId("3149734190872347104L");
        var attachmentFile = attachmentsOfSignal.resolve(attachment.getId() + "")
                                                .toFile();
        attachmentFile.createNewFile();

        var dataMessage = message.getEnvelope()
                                 .getDataMessage();
        dataMessage.setAttachments(List.of(attachment));
        dataMessage.setTimestamp(new Timestamp(1628069823084L));

        return attachment;
    }

    private Message createTestMessage() {
        var message = new Message();
        var envelope = new Envelope();
        envelope.setSource("sourceA");
        var dataMessage = new DataMessage();

        envelope.setDataMessage(dataMessage);
        message.setEnvelope(envelope);

        assertThat(message.getEnvelope()
                          .getDataMessage()
                          .getGroupInfo()).isNull();

        return message;
    }

    private File buildMovedAttachmentFileForGroup(GroupInfo groupInfo, Path attachmentsMoved, Attachment attachment) {
        String filename = buildMovedFilename(message, attachment);
        var base64GroupId = Base64.getEncoder()
                                  .encodeToString(groupInfo.getGroupId()
                                                           .getBytes(StandardCharsets.UTF_8));
        var movedAttachment = attachmentsMoved.resolve("groups")
                                              .resolve(base64GroupId)
                                              .resolve("sourceA")
                                              .resolve(filename)
                                              .toFile();
        return movedAttachment;
    }
}
