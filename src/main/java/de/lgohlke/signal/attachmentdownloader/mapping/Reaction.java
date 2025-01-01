package de.lgohlke.signal.attachmentdownloader.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Reaction {
    private String emoji;
    private UUID targetAuthorUuid;
    private Timestamp targetSentTimestamp;
    private Boolean isRemove;
}