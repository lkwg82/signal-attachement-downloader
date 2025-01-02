package de.lgohlke.signal.attachmentdownloader;

import de.lgohlke.signal.attachmentdownloader.mapping.Envelope;
import de.lgohlke.signal.attachmentdownloader.mapping.GroupInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;

@RequiredArgsConstructor
class TargetSubfolderComputer {
    Path computePath(Envelope envelope) {
        var groupInfoFromDataMessage = envelope.getDataMessage().getGroupInfo();
        if (groupInfoFromDataMessage != null) {
            return computeGroupPath(groupInfoFromDataMessage);
        }

        GroupInfo groupInfo = envelope.getSyncMessage().getSentMessage().getGroupInfo();
        if (groupInfo == null) {
            return Path.of("direct", envelope.getSourceUuid().toString());
        }
        return computeGroupPath(groupInfo);

    }

    private static Path computeGroupPath(@NonNull GroupInfo groupInfo) {
        byte[] groupId = groupInfo.getGroupId()
                                  .getBytes(StandardCharsets.UTF_8);
        var base64GroupId = Base64.getEncoder()
                                  .encodeToString(groupId);
        return Path.of("groups", base64GroupId);
    }
}
