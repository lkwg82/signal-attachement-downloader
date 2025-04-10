package de.lgohlke.signal.attachmentdownloader;

import de.lgohlke.signal.attachmentdownloader.mapping.Attachment;
import de.lgohlke.signal.attachmentdownloader.mapping.DataMessage;
import de.lgohlke.signal.attachmentdownloader.mapping.Envelope;
import de.lgohlke.signal.attachmentdownloader.mapping.GroupInfo;
import de.lgohlke.signal.attachmentdownloader.mapping.Message;
import lombok.SneakyThrows;
import lombok.val;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AttachmentReplicatorTest {

    private final File tempFolder = Files.newTemporaryFolder();
    private final Message message = createTestMessage();

    private final Path sourceFolder = tempFolder.toPath()
                                                .resolve("attachments");
    private final Path targetFolder = tempFolder.toPath()
                                                .resolve("new_attachments");
    private AttachmentReplicator replicator;

    @BeforeEach
    void setUp() {
        //noinspection ResultOfMethodCallIgnored
        tempFolder.mkdirs();
        //noinspection ResultOfMethodCallIgnored
        sourceFolder.toFile().mkdirs();
        replicator = new AttachmentReplicator(sourceFolder, targetFolder);
    }

    @Test
    void should_hardlink_attachment_instead_of_move() throws IOException {
        createTestAttachment(sourceFolder, message);

        List<Path> paths = replicator.handle(message);

        Object attribute = java.nio.file.Files.getAttribute(paths.getFirst(), "unix:nlink");
        int hardlinks = Integer.parseInt("" + attribute);
        assertThat(hardlinks).isEqualTo(2);
    }

    @Test
    void should_move_attachment_from_direct_message() throws IOException {
        createTestAttachment(sourceFolder, message);

        var paths = replicator.handle(message);

        assertThat(paths.getFirst()).exists();
        assertThat(paths.getFirst().toFile()).isFile();
    }

    @Test
    void should_accept_already_moved_attachment() throws IOException {
        var attachment = createTestAttachment(sourceFolder, message);

        String filename = buildReplicatedFilename(message, attachment);
        var targetFile = targetFolder.resolve("direct")
                                     .resolve(message.getEnvelope()
                                                     .getSourceUuid().toString())
                                     .resolve(filename)
                                     .toFile();

        moveAttachmentToMovedFolder(sourceFolder, attachment, targetFile);

        // action
        List<Path> paths = replicator.handle(message);

        assertThat(paths.getFirst()).exists();
        assertThat(paths.getFirst().toFile()).isFile();
    }

    @Test
    void should_move_attachment_from_group_message() throws IOException {
        var groupInfo = new GroupInfo();
        groupInfo.setGroupId("asdas/dasd");
        groupInfo.setGroupName("egal");
        groupInfo.setType("DELIVER");
        message.getEnvelope()
               .getDataMessage()
               .setGroupInfo(groupInfo);
        Attachment testAttachment = createTestAttachment(sourceFolder, message);
        message.getEnvelope().getDataMessage().setAttachments(List.of(testAttachment));

        List<Path> paths = replicator.handle(message);

        assertThat(paths.getFirst()).exists();
        assertThat(paths.getFirst().toFile()).isFile();
    }

    @Test
    void should_create_group_name_marker() throws IOException {
        var groupInfo = new GroupInfo();
        groupInfo.setGroupId(UUID.randomUUID().toString());
        groupInfo.setType("DELIVER");
        message.getEnvelope()
               .getDataMessage()
               .setGroupInfo(groupInfo);
        Attachment testAttachment = createTestAttachment(sourceFolder, message);
        message.getEnvelope().getDataMessage().setAttachments(List.of(testAttachment));

        groupInfo.setGroupName("test");
        List<Path> paths = replicator.handle(message);

        Path groupNameFile = paths.getFirst().getParent().resolve(".groupname");
        assertThat(groupNameFile).exists();
        assertThat(groupNameFile.toFile()).isFile();
        assertThat(groupNameFile.toFile()).hasContent("test\n");
    }

    @Test
    void should_update_group_name_marker() throws IOException {
        var groupInfo = new GroupInfo();
        groupInfo.setGroupId(UUID.randomUUID().toString());
        groupInfo.setType("DELIVER");
        message.getEnvelope()
               .getDataMessage()
               .setGroupInfo(groupInfo);
        Attachment testAttachment = createTestAttachment(sourceFolder, message);
        message.getEnvelope().getDataMessage().setAttachments(List.of(testAttachment));

        groupInfo.setGroupName("test");
        List<Path> paths = replicator.handle(message);
        Path groupNameFile = paths.getFirst().getParent().resolve(".groupname");
        assertThat(groupNameFile.toFile()).hasContent("test\n");

        // update
        groupInfo.setGroupName("test2");
        replicator.handle(message);
        assertThat(groupNameFile.toFile()).hasContent("test2\n");
    }

    @Test
    void should_fail_on_wrong_signal_attachment_dir() {
        Path source = Path.of("a");
        Path target = Path.of("b");

        assertThatThrownBy(() -> new AttachmentReplicator(source, target))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("could not find signal attachment path:");
    }

    @SneakyThrows
    @Test
    void should_fail_on_when_signal_attachment_dir_is_no_dir() {
        var file = tempFolder.toPath()
                             .resolve("a")
                             .toFile();
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();

        val sourceFolder2 = file.toPath();
        val targetFolder2 = Path.of("b");

        assertThatThrownBy(() -> new AttachmentReplicator(sourceFolder2, targetFolder2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("signal attachment path should be a directory");
    }

    @SneakyThrows
    @Test
    void should_fail_on_when_moved_attachment_dir_is_no_dir() {
        var target = tempFolder.toPath()
                               .resolve("b")
                               .toFile();
        //noinspection ResultOfMethodCallIgnored
        target.createNewFile();
        Path localTargetFolder = target.toPath();

        assertThatThrownBy(() -> new AttachmentReplicator(sourceFolder, localTargetFolder))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("moved attachment path should be a directory");
    }

    @Test
    void should_detect_same_paths() {
        assertThatThrownBy(() -> new AttachmentReplicator(sourceFolder, sourceFolder))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("paths should be different");
    }

    private void moveAttachmentToMovedFolder(Path sourceFolder, Attachment attachment, File targetFile) throws IOException {
        var sourcePath = sourceFolder.resolve(attachment.getId());
        //noinspection ResultOfMethodCallIgnored
        targetFile.getParentFile()
                  .mkdirs();
        java.nio.file.Files.move(sourcePath, targetFile.toPath());
        assertThat(sourcePath).doesNotExist();
    }

    private String buildReplicatedFilename(Message message, Attachment attachment) {
        var timestamp = message.getEnvelope()
                               .getDataMessage()
                               .getTimestamp();

        TargetAttachmentFilename targetAttachmentFilename = new TargetAttachmentFilename(timestamp, attachment.getId());
        return targetAttachmentFilename.createFilename();
    }

    private Attachment createTestAttachment(Path attachmentsOfSignal, Message message) throws IOException {
        var attachment = new Attachment();
        attachment.setFilename("IMG_01.jpg");
        attachment.setId("3149734190872347104.jpg");
        var attachmentFile = attachmentsOfSignal.resolve(attachment.getId())
                                                .toFile();
        //noinspection ResultOfMethodCallIgnored
        attachmentFile.getParentFile().mkdirs();
        //noinspection ResultOfMethodCallIgnored
        attachmentFile.createNewFile();

        var dataMessage = message.getEnvelope()
                                 .getDataMessage();
        dataMessage.setAttachments(List.of(attachment));
        dataMessage.setTimestamp(new Timestamp(1628069823084L));

        return attachment;
    }

    private Message createTestMessage() {
        var envelope = new Envelope();
        envelope.setSourceUuid(UUID.fromString("f0856790-6342-4610-a018-1588e741155e"));

        envelope.setDataMessage(new DataMessage());
        var localMessage = new Message();
        localMessage.setEnvelope(envelope);

        assertThat(envelope.getDataMessage()
                           .getGroupInfo()).isNull();

        return localMessage;
    }
}
