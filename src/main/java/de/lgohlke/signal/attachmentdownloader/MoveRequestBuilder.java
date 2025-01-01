package de.lgohlke.signal.attachmentdownloader;

import de.lgohlke.signal.attachmentdownloader.mapping.DataMessage;
import de.lgohlke.signal.attachmentdownloader.mapping.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
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
            log.info("no attachments");
            return List.of();
        }

        var source = envelope.getSourceUuid();
        var timestamp = dataMessage.getTimestamp();
        if (timestamp == null) {
            throw new IllegalStateException("timestamp is null in dataMessage: " + message);
        }
        SimpleDateFormat dformat = new SimpleDateFormat("yyyy-MM-dd");
        var formattedDate = dformat.format(new Date(timestamp.getTime()));

        Path attachmentsMovedPath = buildAttachmentsMovedPath(dataMessage);

        List<MoveRequest> moveRequests = new ArrayList<>();
        for (var attachment : attachments) {
            val id = attachment.getId();

            var sourceFile = attachmentsOfSignal.resolve(id);

            String filename = formattedDate + "_" + id;
            Path targetFile;
            if (flatGroupDir && dataMessage.getGroupInfo() != null) {
                targetFile = attachmentsMovedPath.resolve(filename);
            } else {
                targetFile = attachmentsMovedPath.resolve(source.toString())
                                                 .resolve(filename);
            }
            moveRequests.add(new MoveRequest(sourceFile, targetFile));
        }
        return moveRequests;
    }

    private Path buildAttachmentsMovedPath(DataMessage dataMessage) {
        var groupInfo = dataMessage.getGroupInfo();
        if (groupInfo == null) {
            return attachmentsMoved.resolve("direct");
        }
        var base64GroupId = Base64.getEncoder()
                                  .encodeToString(groupInfo.getGroupId()
                                                           .getBytes(StandardCharsets.UTF_8));
        return attachmentsMoved.resolve("groups")
                               .resolve(base64GroupId);
    }
}
