package de.lgohlke.signal.attachmentdownloader;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

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
            required = true
    )
    private String movedAttachmentDir;

    @CommandLine.Option(
            names = {"-m", "--messages-log"},
            description = "location of messages log",
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
            names = {"--debug"},
            description = "show additional debug messages"
    )
    private boolean showDebugInfos;

    @SneakyThrows
    @Override
    public void run() {
        signalAttachmentDirectory = signalAttachmentDirectory.replace("$HOME", System.getenv("HOME"));

        log.info("reading from {} ...", messagesLog.toString());
        log.info("search in {} for attachments", signalAttachmentDirectory);
        var attachmentMover = new AttachmentMover(Path.of(signalAttachmentDirectory), Path.of(movedAttachmentDir));
        var mappingFilter = new MappingFilter(attachmentMover);

        mappingFilter.setDebug(showDebugInfos);

        int count = 0;
        for (String line : Files.readAllLines(messagesLog.toPath())) {
            mappingFilter.handle(line);
            count++;
        }
        log.info("read {} lines", count);
    }
}
