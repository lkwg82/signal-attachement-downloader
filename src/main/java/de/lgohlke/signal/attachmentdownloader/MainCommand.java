package de.lgohlke.signal.attachmentdownloader;

import de.lgohlke.signal.attachmentdownloader.mapping.Reaction;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
public class MainCommand implements Runnable, QuarkusApplication {
    @CommandLine.Option(names = {"-d", "--signal-attachment-dir"}, description = "location of attachments of signal-cli", defaultValue = "$HOME/.local/share/signal-cli/attachments")
    private String signalAttachmentDirectory;

    @CommandLine.Option(names = {"-t", "--moved-attachment-dir"}, description = "location of attachments to be moved to", defaultValue = "moved_attachments", required = true)
    private String movedAttachmentDir;

    @CommandLine.Option(names = {"-m", "--messages-log"}, description = "location of messages log", defaultValue = "messages.log", required = true)
    private List<File> messagesLogs;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display a help message")
    private boolean helpRequested;

    @CommandLine.Option(names = {"--flat-group-dir"}, description = "when set false, in a group directory each senders gets a separate subfolder")
    private boolean group_dir_is_flat = true;

    @CommandLine.Option(names = {"--debug"}, description = "show additional debug messages")
    private boolean showDebugInfos;


    @CommandLine.Option(names = {"--map-reaction-to-subfolder"}, description = "copy attachments based on emojis to subfolders")
    private Map<String, String> emojiMap = new HashMap<>();

    @SneakyThrows
    @Override
    public void run() {
        signalAttachmentDirectory = signalAttachmentDirectory.replace("$HOME", System.getenv("HOME"));

        log.info("reading from {} ...", messagesLogs.stream().map(File::toString).collect(Collectors.joining(",")));
        log.info("search in {} for attachments", signalAttachmentDirectory);

        var attachmentMover = new AttachmentMover(Path.of(signalAttachmentDirectory),
                                                  Path.of(movedAttachmentDir),
                                                  group_dir_is_flat);
        MessageParser parser = new MessageParser();
        var reactionHandlerMap = createReactionHandlerMap(emojiMap);

        Map<SourceUuid_Timestamp, List<Path>> targetPathsMap = new HashMap<>();

        long count = messagesLogs.stream()
                                 .filter(File::exists)
                                 .map(File::toPath)
                                 .flatMap(path -> {
                                     try {
                                         return Files.lines(path);
                                     } catch (IOException e) {
                                         e.printStackTrace();
                                         return Stream.empty();
                                     }
                                 })
                                 .peek(line -> {
                                     log.info("line: " + line);
                                     parser.parse(line)
                                           .ifPresent(message -> {
                                               List<Path> target_paths = attachmentMover.handle(message);
                                               if (!target_paths.isEmpty()) {
                                                   UUID sourceUuid = message.getEnvelope().getSourceUuid();
                                                   Timestamp timestamp = message.getEnvelope().getTimestamp();
                                                   targetPathsMap.put(new SourceUuid_Timestamp(sourceUuid, timestamp),
                                                                      target_paths);
                                               }

                                               Reaction reaction = message.getEnvelope().getDataMessage().getReaction();
                                               if (reaction != null) {
                                                   String emoji = reaction.getEmoji();
                                                   if (reactionHandlerMap.containsKey(emoji)) {
                                                       SourceUuid_Timestamp lookupKey = new SourceUuid_Timestamp(
                                                               reaction.getTargetAuthorUuid(),
                                                               reaction.getTargetSentTimestamp());
                                                       ReactionHandler reactionHandler = reactionHandlerMap.get(emoji);
                                                       targetPathsMap.get(lookupKey).forEach(reactionHandler::handle);
                                                   }
                                               }


                                           });
                                 })
                                 .count();
        log.info("read {} lines", count);
    }

    private HashMap<String, ReactionHandler> createReactionHandlerMap(Map<String, String> emojiMap) {
        var reactionHandlerMap = new HashMap<String, ReactionHandler>();
        emojiMap.forEach((key, value) -> {
                             Path baseReactionsPath = Path.of(movedAttachmentDir).resolve("reactions");
                             Path reactionPath = baseReactionsPath.resolve(value)
                                                                  .normalize();
                             if (!reactionPath.startsWith(baseReactionsPath)) { // detect path traversal issue
                                 throw new IllegalStateException("Sth fishy with the path: " + reactionPath);
                             } else {
                                 reactionHandlerMap.put(key, new ReactionHandler(reactionPath));
                             }
                         }
        );
        log.info("reaction handler map: {}", reactionHandlerMap);
        return reactionHandlerMap;
    }

    record SourceUuid_Timestamp(UUID source, Timestamp timestamp) {
    }

    @Override
    public int run(String... args) throws Exception {
        MainCommand command = new MainCommand();
        return new CommandLine(command).execute(args);
    }

    public static void main(String... args) {
        String[] _args = new String[]{
                "--map-reaction-to-subfolder=👍=keep_it",
                "--map-reaction-to-subfolder=🎁=calendar",
                "--messages-log=messages.log",
                "--messages-log=messages.log.1",
                "--messages-log=messages.log.2",
        };
        Quarkus.run(MainCommand.class, _args);
    }
}
