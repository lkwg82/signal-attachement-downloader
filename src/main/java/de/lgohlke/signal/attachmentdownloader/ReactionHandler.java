package de.lgohlke.signal.attachmentdownloader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;

@Slf4j
@RequiredArgsConstructor
class ReactionHandler {
    private final Path reactionFolder;

    void handle(Path attachment) {
        reactionFolder.toFile().mkdirs();

        log.info("{}", attachment);
        log.info("{}", attachment.getFileName());
        log.info("{}", reactionFolder);
        Path target = reactionFolder.resolve(attachment.getFileName());
        if (target.toFile().exists()) {
            log.info("already handled");
        } else {
            try {
                log.info("trying hardlinking");
                java.nio.file.Files.createLink(target, attachment);
            } catch (IOException e) {
                log.error(e.getMessage());
                try {
                    log.info("trying copying");
                    java.nio.file.Files.copy(attachment, target);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "ReactionHandler{" +
                "reactionFolder=" + reactionFolder +
                '}';
    }
}
