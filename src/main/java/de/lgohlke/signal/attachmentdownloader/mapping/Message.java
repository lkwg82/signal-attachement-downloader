package de.lgohlke.signal.attachmentdownloader.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {
    private Envelope envelope = new Envelope();
}
