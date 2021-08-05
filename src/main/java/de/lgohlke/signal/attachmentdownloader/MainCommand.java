package de.lgohlke.signal.attachmentdownloader;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@CommandLine.Command
@Slf4j
public class MainCommand implements Runnable {
    @CommandLine.Option(names = {"-d", "--attachment-dir"}, description = "location of attachments of signal-cli", defaultValue = "$HOME/.local/share/signal-cli/attachments")
    private String attachmentDir;
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display a help message")
    private boolean helpRequested;

    @SneakyThrows
    @Override
    public void run() {
        attachmentDir = attachmentDir.replace("$HOME", System.getenv("HOME"));

        log.info("starting reading from stdin ...");
        log.info("search in " + attachmentDir + " for attachments");
        new StdinDelegator(System.in, null).handle();
    }
}
