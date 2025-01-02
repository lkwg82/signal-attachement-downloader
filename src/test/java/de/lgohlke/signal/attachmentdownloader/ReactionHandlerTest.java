package de.lgohlke.signal.attachmentdownloader;

import de.lgohlke.signal.attachmentdownloader.mapping.DataMessage;
import de.lgohlke.signal.attachmentdownloader.mapping.Envelope;
import de.lgohlke.signal.attachmentdownloader.mapping.GroupInfo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Files;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ReactionHandlerTest {
    private final File tempFolder = Files.newTemporaryFolder();

    private final Path basePath = Path.of("movedAttachments");
    private final Path reactionFolder = Path.of("keep_it");

    private Path tempPath;
    private ReactionHandler handler;

    @BeforeEach
    void setUp() {
        tempFolder.mkdirs();
        tempPath = tempFolder.toPath();
        handler = new ReactionHandler(tempPath.resolve(basePath), reactionFolder);
        log.info("tempPath: {}", tempPath);
    }

    @Test
    void should_work_for_group_reaction() {
        var attachmentPath = createDummyAttachment(tempPath, "test.jpg");

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId("abcdf");
        DataMessage dataMessage = new DataMessage();
        dataMessage.setGroupInfo(groupInfo);

        Envelope envelope = createRandomEnvelope();
        envelope.setDataMessage(dataMessage);

        Path actualPath = handler.handle(attachmentPath, envelope);

        Path expectedPath = tempPath.resolve(basePath)
                                    .resolve("groups/YWJjZGY=")
                                    .resolve(reactionFolder)
                                    .resolve(attachmentPath.getFileName());
        assertThat(actualPath).isEqualTo(expectedPath);
        assertThat(expectedPath).exists();
    }

    @Test
    void should_work_for_direct_reaction() {
        var attachmentPath = createDummyAttachment(tempPath, "test.jpg");

        Envelope envelope = createRandomEnvelope();

        Path actualPath = handler.handle(attachmentPath, envelope);

        Path expectedPath = tempPath.resolve(basePath)
                                    .resolve("direct/" + envelope.getSourceUuid())
                                    .resolve(reactionFolder)
                                    .resolve(attachmentPath.getFileName());
        assertThat(actualPath).isEqualTo(expectedPath);
        assertThat(expectedPath).exists();
    }

    @SneakyThrows
    private static @NotNull Path createDummyAttachment(Path tempPath, String filename) {
        var file = tempPath.resolve(filename)
                           .toFile();
        if (!file.createNewFile()) {
            throw new IllegalStateException();
        }
        return file.toPath();
    }

    private static @NotNull Envelope createRandomEnvelope() {
        UUID sourceUuid = UUID.randomUUID();

        Envelope envelope = new Envelope();
        DataMessage dataMessage = new DataMessage();
        envelope.setDataMessage(dataMessage);
        envelope.setSourceUuid(sourceUuid);
        return envelope;
    }
}
