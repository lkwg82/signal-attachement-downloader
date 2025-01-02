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
class MoveRequestBuilder {
    private final Path attachmentsOfSignal;
    private final Path attachmentsMoved;
    private final Boolean flatGroupDir;

    public List<MoveRequest> build(Message message) {
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
        Path attachmentsMovedPath = attachmentsMoved.resolve(computedPath);

        List<MoveRequest> moveRequests = new ArrayList<>();
        for (var attachment : attachments) {
            val id = attachment.getId();

            var sourceFile = attachmentsOfSignal.resolve(id);

            String filename = new TargetAttachmentFilename(timestamp, id).createFilename();
            Path targetFile;
            if (dataMessage.getGroupInfo() == null) {
                targetFile = attachmentsMovedPath.resolve(filename);
            } else {
                if (flatGroupDir) {
                    targetFile = attachmentsMovedPath.resolve(filename);
                } else {
                    String sourceUUid = envelope.getSourceUuid().toString();
                    targetFile = attachmentsMovedPath.resolve(sourceUUid)
                                                     .resolve(filename);
                }
            }
            moveRequests.add(new MoveRequest(sourceFile, targetFile));
        }
        return moveRequests;
    }
}
