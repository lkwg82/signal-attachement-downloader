package de.lgohlke.signal.attachmentdownloader.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
class Attachment {
    private String filename;
    private long id;
    private int size;
}
