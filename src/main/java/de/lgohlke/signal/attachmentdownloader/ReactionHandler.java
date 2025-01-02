package de.lgohlke.signal.attachmentdownloader;

import de.lgohlke.signal.attachmentdownloader.mapping.Envelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;

@Slf4j
@RequiredArgsConstructor
class ReactionHandler {
    private final TargetSubfolderComputer subfolderComputer = new TargetSubfolderComputer();
    private final Path basePath;
    private final Path reactionFolder;

    Path handle(Path attachment, Envelope envelope) {
        Path subFolderPath = subfolderComputer.computePath(envelope);
        Path fullPath = basePath.resolve(subFolderPath).resolve(reactionFolder);
        fullPath.toFile().mkdirs();

        log.info("source {}", attachment);
        log.info("source filename {}", attachment.getFileName());
        log.info("target path {}", fullPath);
        Path target = fullPath.resolve(attachment.getFileName());
        if (target.toFile().exists()) {
            log.info("already handled");
        } else {
            try {
                log.info("hardlinking ... {}", attachment.getFileName());
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
        return target;
    }

    @Override
    public String toString() {
        return "ReactionHandler{" +
                "basePath=" + basePath + "," +
                "reactionFolder=" + reactionFolder +
                '}';
    }
}
