package de.lgohlke.signal.attachmentdownloader;

import de.lgohlke.signal.attachmentdownloader.mapping.Message;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Slf4j
public class AttachmentMover {
    private final Path attachmentsMoved;
    private final MoveRequestBuilder moveRequestBuilder;

    public AttachmentMover(Path attachmentsOfSignal, Path attachmentsMoved) {
        this(attachmentsOfSignal, attachmentsMoved, false);
    }

    public AttachmentMover(Path attachmentsOfSignal, Path attachmentsMoved, boolean flatGroupDir) {
        if (attachmentsMoved.equals(attachmentsOfSignal)) {
            throw new IllegalArgumentException("paths should be different: " + attachmentsOfSignal + " <> " + attachmentsMoved);
        }

        var attachmentsOfSignalF = attachmentsOfSignal.toFile();
        if (!attachmentsOfSignalF.exists()) {
            throw new IllegalArgumentException("could not find signal attachment path:" + attachmentsOfSignal);
        }

        if (attachmentsOfSignalF.exists() && attachmentsOfSignalF.isFile()) {
            throw new IllegalArgumentException("signal attachment path should be a directory:" + attachmentsOfSignal);
        }

        var attachmentsMovedF = attachmentsMoved.toFile();
        if (attachmentsMovedF.exists() && attachmentsMovedF.isFile()) {
            throw new IllegalArgumentException("moved attachment path should be a directory:" + attachmentsMoved);
        }

        this.attachmentsMoved = attachmentsMoved;
        moveRequestBuilder = new MoveRequestBuilder(attachmentsOfSignal, attachmentsMoved, flatGroupDir);
    }

    public void handle(Message message) {
        var moveRequests = moveRequestBuilder.build(message);

        if (moveRequests.isEmpty())
            return;

        var target = attachmentsMoved.toFile();
        createTargetDirectory(target);

        moveRequests.forEach(request -> {
            createSenderSourceDirectory(request.target());
            try {
                moveAttachment(request.source(), request.target());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void createSenderSourceDirectory(Path targetFile) {
        var parent = targetFile.getParent();
        if (!parent.toFile()
                   .exists() && !parent.toFile()
                                       .mkdirs()) {
            throw new IllegalStateException("failed to create sender source directory:" + parent);
        }
    }

    private static void moveAttachment(Path sourceFile, Path targetFile) throws IOException {
        try {
            if (targetFile.toFile()
                          .exists()) {
                log.info("already moved {} to {}", sourceFile, targetFile);
            } else {
                java.nio.file.Files.move(sourceFile, targetFile);
                log.info("moved {} to {}", sourceFile, targetFile);
            }
        } catch (IOException e) {
            log.error("could not move {} to {}", sourceFile, targetFile);
            throw e;
        }
    }

    private void createTargetDirectory(File target) {
        if (!target.exists()) {
            if (target.mkdirs()) {
                log.info("created moved attachment directory:{}", attachmentsMoved);
            } else {
                throw new IllegalStateException("could not create directory:" + attachmentsMoved);
            }
        }
    }
}
