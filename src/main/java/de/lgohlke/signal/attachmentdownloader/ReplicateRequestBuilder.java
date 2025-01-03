package de.lgohlke.signal.attachmentdownloader;

import de.lgohlke.signal.attachmentdownloader.mapping.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
class ReplicateRequestBuilder {
    private final Path sourceFolder;
    private final Path targetFolder;
    private final Boolean flatGroupDir;

    public List<ReplicateRequest> build(Message message) {
        var envelope = message.getEnvelope();
        var dataMessage = envelope.getDataMessage();

        var attachments = dataMessage.getAttachments();
        if (attachments.isEmpty()) {
            log.debug("no attachments: {}", message);
            return List.of();
        }

        var timestamp = dataMessage.getTimestamp();
        if (timestamp == null) {
            throw new IllegalStateException("timestamp is null in dataMessage: " + message);
        }
        TargetSubfolderComputer subfolderComputer = new TargetSubfolderComputer();
        Path computedPath = subfolderComputer.computePath(envelope);
        Path targetPath = targetFolder.resolve(computedPath);

        List<ReplicateRequest> moveRequests = new ArrayList<>();
        for (var attachment : attachments) {
            val id = attachment.getId();

            var sourceFile = sourceFolder.resolve(id);

            String filename = new TargetAttachmentFilename(timestamp, id).createFilename();
            Path targetFile;
            if (dataMessage.getGroupInfo() == null) {
                targetFile = targetPath.resolve(filename);
            } else {
                if (flatGroupDir) {
                    targetFile = targetPath.resolve(filename);
                } else {
                    String sourceUUid = envelope.getSourceUuid().toString();
                    targetFile = targetPath.resolve(sourceUUid)
                                           .resolve(filename);
                }
            }
            moveRequests.add(new ReplicateRequest(sourceFile, targetFile));
        }
        return moveRequests;
    }

    static record ReplicateRequest(Path source, Path target) {
    }
}
