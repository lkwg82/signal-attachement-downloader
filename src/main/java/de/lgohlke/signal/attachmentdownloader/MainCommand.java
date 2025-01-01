package de.lgohlke.signal.attachmentdownloader;

import de.lgohlke.signal.attachmentdownloader.mapping.Reaction;
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
public class MainCommand implements Runnable {
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

        System.out.println(emojiMap);
        System.out.println(reactionHandlerMap);


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
                                     System.out.println("line: " + line);
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
        System.out.println("read " + count + " lines");
    }

    record SourceUuid_Timestamp(UUID source, Timestamp timestamp) {
    }

    public static void main(String[] args) {
        MainCommand command = new MainCommand();
        String[] _args = new String[]{
                "--map-reaction-to-subfolder=👍=keep_it",
                "--map-reaction-to-subfolder=🎁=calendar",
                "--messages-log=messages.log",
                "--messages-log=messages.log.1",
                "--messages-log=messages.log.2",
        };
        int exitCode = new CommandLine(command).execute(_args);
        System.exit(exitCode);
    }
}
