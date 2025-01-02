package de.lgohlke.signal.attachmentdownloader;

import de.lgohlke.signal.attachmentdownloader.mapping.DataMessage;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;

@RequiredArgsConstructor
class TargetSubfolderComputer {
    Path computePath(DataMessage dataMessage) {
        var groupInfo = dataMessage.getGroupInfo();
        if (groupInfo == null) {
            return Path.of("direct");
        }
        byte[] groupId = groupInfo.getGroupId()
                                  .getBytes(StandardCharsets.UTF_8);
        var base64GroupId = Base64.getEncoder()
                                  .encodeToString(groupId);
        return Path.of("groups", base64GroupId);
    }
}
