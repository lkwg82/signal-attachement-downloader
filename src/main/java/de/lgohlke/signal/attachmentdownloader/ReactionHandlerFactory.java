package de.lgohlke.signal.attachmentdownloader;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class ReactionHandlerFactory {
    private final Map<String, String> emojiMap;
    private final Path movedAttachmentPath;

    public ReactionHandlerFactory(Map<String, String> emojiMap, Path movedAttachmentPath) {
        this.emojiMap = new HashMap<>(emojiMap);
        ;
        this.movedAttachmentPath = movedAttachmentPath;
    }

    private void addEmojiMapFromEnvironment() {
        Map<String, String> mergedEnvAndProperties = new HashMap<>();
        System.getProperties()
              .forEach((k, v) -> mergedEnvAndProperties.put(String.valueOf(k), String.valueOf(v)));
        mergedEnvAndProperties.putAll(System.getenv());

        mergedEnvAndProperties.entrySet()
                              .stream()
                              .filter(e -> e.getKey().startsWith("EMOJI_MAP_"))
                              .forEach(e -> {
                                  log.info("picks {}={}", e.getKey(), e.getValue());
                                  String emoji = e.getKey().replaceFirst("EMOJI_MAP_", "");
                                  String folder = e.getValue();
                                  emojiMap.put(emoji, folder);
                              });
    }

    Map<String, ReactionHandler> createReactionHandlerMap() {
        addEmojiMapFromEnvironment();

        failOnDuplicatePaths();
        failOnInvalidReactions();

        var reactionHandlerMap = new HashMap<String, ReactionHandler>();
        emojiMap.forEach((key, value) -> {
            Path baseReactionsPath = Path.of("reactions");
            Path reactionPath = baseReactionsPath.resolve(value).normalize();
            if (!reactionPath.startsWith(baseReactionsPath)) { // detect path traversal issue
                throw new IllegalStateException("Sth fishy with the path: " + reactionPath);
            }

            ReactionHandler reactionHandler = new ReactionHandler(movedAttachmentPath, reactionPath);
            reactionHandlerMap.put(key, reactionHandler);
        });
        log.info("reaction handler map: {}", reactionHandlerMap);
        return reactionHandlerMap;
    }

    private void failOnDuplicatePaths() {
        Set<String> values = new HashSet<>(emojiMap.values());
        if (values.size() != emojiMap.size()) {
            throw new IllegalArgumentException("duplicate paths in " + emojiMap);
        }
    }

    private void failOnInvalidReactions() {
        emojiMap.keySet().forEach(k -> {
            if (k.length() != 2) {
                throw new IllegalArgumentException("invalid reaction " + k);
            }
        });
    }
}
