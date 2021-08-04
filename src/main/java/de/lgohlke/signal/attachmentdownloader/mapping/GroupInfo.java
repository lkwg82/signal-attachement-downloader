package de.lgohlke.signal.attachmentdownloader.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
class GroupInfo {
    private String groupId;
    private String type;
}
