package de.lgohlke.signal.attachmentdownloader;

import de.lgohlke.signal.attachmentdownloader.mapping.Message;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command
@Slf4j
public class MainCommand implements Runnable {
    @CommandLine.Option(names = {"-d", "--signal-attachment-dir"}, description = "location of attachments of signal-cli", defaultValue = "$HOME/.local/share/signal-cli/attachments")
    private String signalAttachmentDirectory;

    @CommandLine.Option(names = {"-t", "--moved-attachment-dir"}, description = "location of attachments to be replicated to", defaultValue = "moved_attachments", required = true)
    private String movedAttachmentDir;

    @CommandLine.Option(names = {"-m", "--messages-log"}, description = "location of messages log", defaultValue = "messages.log", required = true)
    private List<File> messagesLogs;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display a help message")
    private boolean helpRequested;

    @CommandLine.Option(names = {"--flat-group-dir"}, description = "when set false, in a group directory each senders gets a separate subfolder", defaultValue = "true")
    private boolean group_dir_is_flat;

    @CommandLine.Option(names = {"--debug"}, description = "show additional debug messages")
    private boolean showDebugInfos;


    @CommandLine.Option(names = {"--map-reaction-to-subfolder"}, description = "copy attachments based on emojis to subfolders, " +
            "can also be added via ENVIRONMENT 'EMOJI_MAP_👍=keep_it', ...")
    private Map<String, String> emojiMap = new HashMap<>();

    @SneakyThrows
    @Override
    public void run() {
        signalAttachmentDirectory = signalAttachmentDirectory.replace("$HOME", System.getenv("HOME"));

        log.info("reading from {} ...", messagesLogs.stream().map(File::toString).collect(Collectors.joining(",")));
        log.info("search in {} for attachments", signalAttachmentDirectory);

        var attachmentMover = new AttachmentReplicator(Path.of(signalAttachmentDirectory),
                                                       Path.of(movedAttachmentDir),
                                                       group_dir_is_flat);

        // have problems to configure via cli
        val preconfiguredEmojiMap = new HashMap<>(emojiMap);
        preconfiguredEmojiMap.put("🎁", "calendar");

        var reactionHandlerMap = new ReactionHandlerFactory(preconfiguredEmojiMap,
                                                            Path.of(movedAttachmentDir)).createReactionHandlerMap();

        Map<SourceUuid_Timestamp, List<Path>> targetPathsMap = new HashMap<>();

        MessageParser parser = new MessageParser();
        long count = streamMessagesFromExistingFiles().peek(line -> {
            if (line.contains("dataMessage") && (line.contains("reaction") || line.contains("filename"))) {
                log.info("line: {}", line);
            }
            parser.parse(line).ifPresent(message -> {
                List<Path> target_paths = attachmentMover.handle(message);
                if (!target_paths.isEmpty()) {
                    UUID sourceUuid = message.getEnvelope().getSourceUuid();
                    Timestamp timestamp = message.getEnvelope().getTimestamp();
                    SourceUuid_Timestamp key = new SourceUuid_Timestamp(sourceUuid, timestamp);
                    targetPathsMap.put(key, target_paths);
                }

                checkWithReactionHandler(message, reactionHandlerMap, targetPathsMap);
            });
        }).count();
        log.info("read {} lines", count);
    }

    private static void checkWithReactionHandler(Message message, Map<String, ReactionHandler> reactionHandlerMap, Map<SourceUuid_Timestamp, List<Path>> targetPathsMap) {
        val envelope = message.getEnvelope();
        val reactionFromDataMessage = envelope.getDataMessage().getReaction();
        val reactionFromSyncMessage = envelope.getSyncMessage().getSentMessage().getReaction();
        val reaction = reactionFromDataMessage == null ? reactionFromSyncMessage : reactionFromDataMessage;

        if (reaction == null) {
            return;
        }

        val emoji = reaction.getEmoji();
        val reactionHandler = reactionHandlerMap.get(emoji);

        if (reactionHandler == null) {
            return;
        }

        val lookupKey = new SourceUuid_Timestamp(reaction.getTargetAuthorUuid(), reaction.getTargetSentTimestamp());

        List<Path> paths = targetPathsMap.get(lookupKey);
        if (paths == null) {
            log.warn("no original entry found");
            return;
        }
        paths.forEach(path -> reactionHandler.handle(path, envelope));
    }

    private Stream<String> streamMessagesFromExistingFiles() {
        return messagesLogs.stream()
                           .filter(File::exists)
                           .map(File::toPath)
                           .flatMap(path -> {
                               try {
                                   return Files.lines(path);
                               } catch (IOException e) {
                                   log.error(e.getMessage(), e);
                                   return Stream.empty();
                               }
                           });
    }

    record SourceUuid_Timestamp(UUID source, Timestamp timestamp) {
    }
}
