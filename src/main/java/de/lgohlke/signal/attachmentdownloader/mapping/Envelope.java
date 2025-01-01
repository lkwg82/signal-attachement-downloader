package de.lgohlke.signal.attachmentdownloader.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Envelope {
    private DataMessage dataMessage = new DataMessage();
    private UUID sourceUuid; // would be the successor of <source>
    private Timestamp timestamp;
}
