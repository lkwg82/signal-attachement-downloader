package de.lgohlke.signal.attachmentdownloader.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.ToString;

import java.sql.Timestamp;

@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
class Envelope {
    private DataMessage dataMessage;
    private String source;
    private Timestamp timestamp;
}
