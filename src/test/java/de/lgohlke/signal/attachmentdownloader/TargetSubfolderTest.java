package de.lgohlke.signal.attachmentdownloader;

import de.lgohlke.signal.attachmentdownloader.mapping.DataMessage;
import de.lgohlke.signal.attachmentdownloader.mapping.Envelope;
import de.lgohlke.signal.attachmentdownloader.mapping.GroupInfo;
import de.lgohlke.signal.attachmentdownloader.mapping.SentMessage;
import de.lgohlke.signal.attachmentdownloader.mapping.SyncMessage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TargetSubfolderTest {
    private final TargetSubfolderComputer subFolderComputer = new TargetSubfolderComputer();

    @Test
    void should_return_direct() {
        Envelope envelope = createRandomEnvelope();
        UUID sourceUuid = envelope.getSourceUuid();
        Path path = subFolderComputer.computePath(envelope);

        assertThat(path).hasToString("direct/" + sourceUuid);
    }

    @Test
    void should_return_group_with_datamessage() {
        Envelope envelope = createRandomEnvelope();
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId("abc");

        DataMessage dataMessage = new DataMessage();
        dataMessage.setGroupInfo(groupInfo);
        envelope.setDataMessage(dataMessage);

        Path path = subFolderComputer.computePath(envelope);

        assertThat(path).hasToString("groups/YWJj");
    }

    @Test
    void should_return_group_with_syncmessage() {
        Envelope envelope = createRandomEnvelope();

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId("abcd");

        SentMessage sentMessage = new SentMessage();
        SyncMessage syncMessage = new SyncMessage();
        sentMessage.setGroupInfo(groupInfo);
        syncMessage.setSentMessage(sentMessage);
        envelope.setSyncMessage(syncMessage);

        Path path = subFolderComputer.computePath(envelope);

        assertThat(path).hasToString("groups/YWJjZA==");
    }

    private static @NotNull Envelope createRandomEnvelope() {
        Envelope envelope = new Envelope();
        envelope.setSourceUuid(UUID.randomUUID());
        return envelope;
    }
    
}
