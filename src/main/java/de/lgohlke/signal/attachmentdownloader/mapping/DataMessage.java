package de.lgohlke.signal.attachmentdownloader.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.List;

@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
class DataMessage {
    private List<Attachment> attachments;
    private GroupInfo groupInfo;
    private String message;
    private Timestamp timestamp;
}
