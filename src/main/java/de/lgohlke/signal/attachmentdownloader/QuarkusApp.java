package de.lgohlke.signal.attachmentdownloader;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import picocli.CommandLine;

public class QuarkusApp implements QuarkusApplication {

    @Override
    public int run(String... args) {
        MainCommand command = new MainCommand();
        return new CommandLine(command).execute(args);
    }

    public static void main(String... args) {
        String[] _args = new String[]{
                "--messages-log=messages.log",
                "--messages-log=messages.log.1",
                "--messages-log=messages.log.2",
        };
        Quarkus.run(QuarkusApp.class, _args);
    }
}
