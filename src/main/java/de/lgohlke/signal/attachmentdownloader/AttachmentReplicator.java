package de.lgohlke.signal.attachmentdownloader;

import de.lgohlke.signal.attachmentdownloader.mapping.GroupInfo;
import de.lgohlke.signal.attachmentdownloader.mapping.Message;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class AttachmentReplicator {
    private final ReplicateRequestBuilder replicateRequestBuilder;

    public AttachmentReplicator(Path sourceFolder, Path targetFolder) {
        this(sourceFolder, targetFolder, false);
    }

    public AttachmentReplicator(Path sourceFolder, Path targetFolder, boolean flatGroupDir) {
        if (targetFolder.equals(sourceFolder)) {
            throw new IllegalArgumentException("paths should be different: " + sourceFolder + " <> " + targetFolder);
        }

        var attachmentsOfSignalF = sourceFolder.toFile();
        if (!attachmentsOfSignalF.exists()) {
            throw new IllegalArgumentException("could not find signal attachment path:" + sourceFolder);
        }

        if (attachmentsOfSignalF.exists() && attachmentsOfSignalF.isFile()) {
            throw new IllegalArgumentException("signal attachment path should be a directory:" + sourceFolder);
        }

        var attachmentsMovedF = targetFolder.toFile();
        if (attachmentsMovedF.exists() && attachmentsMovedF.isFile()) {
            throw new IllegalArgumentException("moved attachment path should be a directory:" + targetFolder);
        }

        replicateRequestBuilder = new ReplicateRequestBuilder(sourceFolder, targetFolder, flatGroupDir);
    }

    /**
     * @return List of paths of replicated files
     */
    public List<Path> handle(Message message) {
        var moveRequests = replicateRequestBuilder.build(message);

        if (moveRequests.isEmpty()) {
            return List.of();
        }

        moveRequests.forEach(request -> {
            Path target = request.target();
            createTargetDirectory(target);
            GroupInfo groupInfo = message.getEnvelope().getDataMessage().getGroupInfo();
            if (groupInfo != null) {
                updateOrCreateGroupnameFile(target.getParent(), groupInfo.getGroupName());
            }
            moveAttachment(request.source(), target);
        });

        return moveRequests.stream().map(ReplicateRequestBuilder.ReplicateRequest::target).toList();
    }

    @SneakyThrows
    private void updateOrCreateGroupnameFile(Path targetDirectory, @NonNull String groupName) {
        Path path = targetDirectory.resolve(".groupname");
        if (path.toFile().exists()) {
            String content = Files.readString(path);
            if (groupName.equals(content)) {
                return;
            }
            log.info("update groupname file in {}, from {} to  {}", targetDirectory, content, groupName);
        } else {
            log.info("create groupname file in {}: {}", targetDirectory, groupName);
        }
        Files.writeString(path, groupName);
    }

    private void createTargetDirectory(Path targetFile) {
        var parent = targetFile.getParent();
        if (!parent.toFile()
                   .exists() && !parent.toFile()
                                       .mkdirs()) {
            throw new IllegalStateException("failed to create target directory:" + parent);
        }
    }

    private static void moveAttachment(Path sourceFile, Path targetFile) {
        if (targetFile.toFile()
                      .exists()) {
            log.info("already moved {} to {}", sourceFile, targetFile);
            return;
        }

        try {
            java.nio.file.Files.createLink(targetFile, sourceFile);
        } catch (IOException e) {
            log.error("linking failed: {} ", e.getMessage());
            try {
                log.info("fallback to copying {}", targetFile);
                java.nio.file.Files.copy(sourceFile, targetFile);
            } catch (IOException ex) {
                log.error("could not move {} to {}", sourceFile, targetFile);
                log.error(e.getMessage(), e);
            }
        }
        log.info("replicated {} to {}", sourceFile, targetFile);
    }
}
