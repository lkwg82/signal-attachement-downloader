package de.lgohlke.signal.attachmentdownloader.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NonNull;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Envelope {
    @NonNull
    private DataMessage dataMessage = new DataMessage();
    @NonNull
    private SyncMessage syncMessage = new SyncMessage();
    private UUID sourceUuid; // would be the successor of <source>
    private Timestamp timestamp;
}
