package de.lgohlke.signal.attachmentdownloader;

import java.nio.file.Path;

record MoveRequest(Path source, Path target) {
}
