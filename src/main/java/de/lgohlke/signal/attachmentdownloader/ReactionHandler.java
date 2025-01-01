package de.lgohlke.signal.attachmentdownloader;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Path;

@RequiredArgsConstructor
class ReactionHandler {
    private final Path reactionFolder;

    void handle(Path attachment) {
        reactionFolder.toFile().mkdirs();

        System.out.println(attachment);
        System.out.println(attachment.getFileName());
        System.out.println(reactionFolder);
        Path target = reactionFolder.resolve(attachment.getFileName());
        if (target.toFile().exists()) {
            System.out.println("already handled");
        } else {
            try {
                System.out.println("trying hardlinking");
                java.nio.file.Files.createLink(target, attachment);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                try {
                    System.out.println("trying copying");
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
