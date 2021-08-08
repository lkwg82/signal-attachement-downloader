package de.lgohlke.signal.attachmentdownloader;

import de.lgohlke.signal.attachmentdownloader.mapping.DataMessage;
import de.lgohlke.signal.attachmentdownloader.mapping.Message;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Base64;

@Slf4j
public class AttachmentMover implements Debuggable {
    private final Path attachmentsOfSignal;
    private final Path attachmentsMoved;
    private boolean isDebug;

    public AttachmentMover(Path attachmentsOfSignal, Path attachmentsMoved) {
        if (attachmentsMoved.equals(attachmentsOfSignal)) {
            throw new IllegalArgumentException("paths should be different: " + attachmentsOfSignal + " <> " + attachmentsMoved);
        }

        var attachmentsOfSignalF = attachmentsOfSignal.toFile();
        if (!attachmentsOfSignalF
                .exists()) {
            throw new IllegalArgumentException("could not find signal attachment path:" + attachmentsOfSignal);
        }

        if (attachmentsOfSignalF.exists() && attachmentsOfSignalF.isFile()) {
            throw new IllegalArgumentException("signal attachment path should be a directory:" + attachmentsOfSignal);
        }

        var attachmentsMovedF = attachmentsMoved.toFile();
        if (attachmentsMovedF.exists() && attachmentsMovedF.isFile()) {
            throw new IllegalArgumentException("moved attachment path should be a directory:" + attachmentsMoved);
        }

        this.attachmentsOfSignal = attachmentsOfSignal;
        this.attachmentsMoved = attachmentsMoved;
    }

    public void handle(Message message) throws IOException {
        var target = attachmentsMoved.toFile();
        if (!target.exists()) {
            if (target.mkdirs()) {
                log.info("created moved attachment directory:" + attachmentsMoved);
            } else {
                throw new IllegalStateException("could not create directory:" + attachmentsMoved);
            }
        }

        var envelope = message.getEnvelope();
        var dataMessage = envelope.getDataMessage();

        var attachments = dataMessage.getAttachments();
        if (attachments.isEmpty()) {
            log.info("no attachments");
            return;
        }

        var source = envelope.getSource();
        var timestamp = dataMessage.getTimestamp();
        if (timestamp == null) {
            throw new IllegalStateException("timestamp is null in dataMessage: " + message);
        }
        SimpleDateFormat dformat = new SimpleDateFormat("yyyy-MM-dd");
        var formattedDate = dformat.format(new Date(timestamp.getTime()));

        Path attachmentsMovedPath = buildAttachmentsMovedPath(dataMessage);

        for (var attachment : attachments) {
            var cleanedFilename = attachment.getFilename()
                                            .replaceAll("\\\\", "_")
                                            .replaceAll("/", "_");
            var id = attachment.getId();

            var sourceFile = attachmentsOfSignal.resolve(id + "");
            var targetFile = attachmentsMovedPath.resolve(source)
                                                 .resolve(formattedDate + "_" + id + "_" + cleanedFilename);

            var parent = targetFile.getParent();
            if (!parent.toFile()
                       .exists() && !parent.toFile()
                                           .mkdirs()) {
                throw new IllegalStateException("failed to create sender source directory:" + parent);
            }

            try {
                if (targetFile.toFile()
                              .exists()) {
                    log.info("already moved " + sourceFile + " to " + targetFile);
                } else {
                    java.nio.file.Files.move(sourceFile, targetFile);
                    log.info("moved " + sourceFile + " to " + targetFile);
                }
            } catch (IOException e) {
                log.error("could not move " + sourceFile + " to " + targetFile);
                throw e;
            }
        }
    }

    private Path buildAttachmentsMovedPath(DataMessage dataMessage) {
        var groupInfo = dataMessage.getGroupInfo();
        if (groupInfo == null) {
            return attachmentsMoved;
        }
        var base64GroupId = Base64.getEncoder()
                                  .encodeToString(groupInfo.getGroupId()
                                                           .getBytes(StandardCharsets.UTF_8));
        return attachmentsMoved.resolve("groups")
                               .resolve(base64GroupId);
    }

    @Override
    public void setDebug(boolean flag) {
        this.isDebug = flag;
    }
}
