package de.lgohlke.signal.attachmentdownloader;

import lombok.RequiredArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@RequiredArgsConstructor
class TargetAttachmentFilename {
    public static final String FORMAT = "yyyy-MM-dd";

    private final Timestamp timestamp;
    private final String attachmentId;

    public String createFilename() {
        SimpleDateFormat dformat = new SimpleDateFormat(FORMAT);
        var formattedDate = dformat.format(new Date(timestamp.getTime()));
        return formattedDate + "_" + attachmentId;
    }
}
