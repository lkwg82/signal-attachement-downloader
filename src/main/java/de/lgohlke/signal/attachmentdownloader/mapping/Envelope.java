package de.lgohlke.signal.attachmentdownloader.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.sql.Timestamp;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Envelope {
    private DataMessage dataMessage = new DataMessage();
    private String source = "unknown";
    private Timestamp timestamp;
}
