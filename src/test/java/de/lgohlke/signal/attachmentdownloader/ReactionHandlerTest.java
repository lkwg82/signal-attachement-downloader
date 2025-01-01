package de.lgohlke.signal.attachmentdownloader;

import lombok.SneakyThrows;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class ReactionHandlerTest {
    private final File tempFolder = Files.newTemporaryFolder();

    @BeforeEach
    void setUp() {
        tempFolder.mkdirs();
    }

    @Test
    @SneakyThrows
    void should_work() {
        var file = tempFolder.toPath()
                             .resolve("test.jpg")
                             .toFile();
        file.createNewFile();


        Path reactionFolder = tempFolder.toPath().resolve("keep_it");
        ReactionHandler handler = new ReactionHandler(reactionFolder);

        handler.handle(file.toPath());

        assertThat(reactionFolder.resolve(file.toPath().getFileName())).exists();
    }
}
