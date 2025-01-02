package de.lgohlke.signal.attachmentdownloader;

import de.lgohlke.signal.attachmentdownloader.mapping.DataMessage;
import de.lgohlke.signal.attachmentdownloader.mapping.GroupInfo;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class TargetSubfolderTest {
    private final TargetSubfolderComputer subFolderComputer = new TargetSubfolderComputer();

    @Test
    void should_return_direct() {
        DataMessage dataMessage = new DataMessage();
        Path path = subFolderComputer.computePath(dataMessage);

        assertThat(path.toString()).isEqualTo("direct");
    }

    @Test
    void should_return_group() {
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId("abc");
        DataMessage dataMessage = new DataMessage();
        dataMessage.setGroupInfo(groupInfo);
        Path path = subFolderComputer.computePath(dataMessage);

        assertThat(path.toString()).isEqualTo("groups/YWJj");
    }
}
