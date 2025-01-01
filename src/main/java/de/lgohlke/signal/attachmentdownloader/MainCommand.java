package de.lgohlke.signal.attachmentdownloader;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@CommandLine.Command
@Slf4j
public class MainCommand implements Runnable {
    @CommandLine.Option(
            names = {"-d", "--signal-attachment-dir"},
            description = "location of attachments of signal-cli",
            defaultValue = "$HOME/.local/share/signal-cli/attachments"
    )
    private String signalAttachmentDirectory;

    @CommandLine.Option(
            names = {"-t", "--moved-attachment-dir"},
            description = "location of attachments to be moved to",
            defaultValue = "moved_attachments",
            required = true
    )
    private String movedAttachmentDir;

    @CommandLine.Option(
            names = {"-m", "--messages-log"},
            description = "location of messages log",
            defaultValue = "messages.log",
            required = true
    )
    private File messagesLog;

    @CommandLine.Option(
            names = {"-h", "--help"},
            usageHelp = true,
            description = "display a help message"
    )
    private boolean helpRequested;

    @CommandLine.Option(
            names = {"--flat-group-dir"},
            description = "when set false, in a group directory each senders gets a separate subfolder"
    )
    private boolean group_dir_is_flat = true;

    @CommandLine.Option(
            names = {"--debug"},
            description = "show additional debug messages"
    )
    private boolean showDebugInfos;


    @CommandLine.Option(
            names = {"--map-emoji-to-subfolder"},
            description = "copy attachments based on emojis to subfolders"
    )
    private Map<String, String> emojiMap;

    @SneakyThrows
    @Override
    public void run() {
        signalAttachmentDirectory = signalAttachmentDirectory.replace("$HOME", System.getenv("HOME"));

        log.info("reading from {} ...", messagesLog.toString());
        log.info("search in {} for attachments", signalAttachmentDirectory);

        var attachmentMover = new AttachmentMover(Path.of(signalAttachmentDirectory),
                                                  Path.of(movedAttachmentDir),
                                                  group_dir_is_flat);
        MessageParser parser = new MessageParser();

        int count = 0;
        for (String line : Files.readAllLines(messagesLog.toPath())) {
            System.out.println("line: " + line);
            parser.parse(line)
                  .ifPresent(attachmentMover::handle);
            count++;
        }
        log.info("read {} lines", count);
    }

    public static void main(String[] args) {
        MainCommand command = new MainCommand();
        int exitCode = new CommandLine(command).execute(args);
        System.exit(exitCode);
    }
}
