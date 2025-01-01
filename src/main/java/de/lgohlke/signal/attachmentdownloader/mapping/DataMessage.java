package de.lgohlke.signal.attachmentdownloader.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataMessage {
    private List<Attachment> attachments = List.of();
    private GroupInfo groupInfo;
    private String message;
    private Reaction reaction;
    private Timestamp timestamp;
}
