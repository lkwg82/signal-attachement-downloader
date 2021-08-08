package de.lgohlke.signal.attachmentdownloader;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.nio.file.Path;

@CommandLine.Command
@Slf4j
public class MainCommand implements Runnable {
    @CommandLine.Option(names = {"-d", "--signal-attachment-dir"}, description = "location of attachments of signal-cli", defaultValue = "$HOME/.local/share/signal-cli/attachments")
    private String signalAttachmentDirectory;
    @CommandLine.Option(names = {"-t", "--moved-attachment-dir"}, description = "location of attachments to be moved to", required = true)
    private String movedAttachmentDir;
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display a help message")
    private boolean helpRequested;

    @CommandLine.Option(names = {"--debug"}, description = "show additional debug messages")
    private boolean showDebugInfos;

    @SneakyThrows
    @Override
    public void run() {
        signalAttachmentDirectory = signalAttachmentDirectory.replace("$HOME", System.getenv("HOME"));

        log.info("starting reading from stdin ...");
        log.info("search in " + signalAttachmentDirectory + " for attachments");
        var attachmentMover = new AttachmentMover(Path.of(signalAttachmentDirectory), Path.of(movedAttachmentDir));
        var stdinDelegator = new StdinDelegator(System.in, attachmentMover);
        stdinDelegator.setDebug(showDebugInfos);
        stdinDelegator.handle();
    }
}
