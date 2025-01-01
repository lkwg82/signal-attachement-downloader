package de.lgohlke.signal.attachmentdownloader;

import de.lgohlke.signal.attachmentdownloader.mapping.Message;
import lombok.val;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class MoveRequestTest {
    private final Path attachmentsOfSignal = Path.of("source");
    private final Path attachmentsMoved = Path.of("target");
    private final MoveRequestBuilder builder = new MoveRequestBuilder(attachmentsOfSignal, attachmentsMoved);

    @Test
    void should_handle_missing_filename() {
        @Language("JSON") val rawMessage = """
                {
                  "envelope": {
                    "source": "+49123456",
                    "timestamp": 1634635559631,
                    "dataMessage": {
                      "timestamp": 1634635559631,
                      "message": null,
                      "attachments": [
                        {
                          "contentType": "image/jpeg",
                          "filename": null,
                          "id": "xxx-file-id2",
                          "size": 278001
                        }
                      ],
                      "groupInfo": {
                        "groupId": "xxx-group-id2",
                        "type": "DELIVER"
                      }
                    }
                  }
                }""";
        val message = buildMessage(rawMessage);

        val requests = builder.build(message);
        assertThat(requests).hasSize(1);

        var firstRequest = requests.get(0);
        assertThat(firstRequest.source()).isEqualTo(Path.of("source", "xxx-file-id2"));
        assertThat(firstRequest.target()).isEqualTo(Path.of("target",
                                                            "groups",
                                                            "eHh4LWdyb3VwLWlkMg==",
                                                            "+49123456",
                                                            "2021-10-19_xxx-file-id2_empty.jpeg"));
    }

    @Test
    void should_handle_with_filename() {
        @Language("JSON") val rawMessage = """
                {
                   "envelope": {
                     "source": "+49123456",
                     "timestamp": 1634587644427,
                     "dataMessage": {
                       "timestamp": 1634587644427,
                       "attachments": [
                         {
                           "contentType": "image/jpeg",
                           "filename": "signal-2021-10-18-220724.jpeg",
                           "id": "xxx-file-id",
                           "size": 1192256
                         }
                       ],
                       "groupInfo": null
                     }
                   }
                 }""";
        val message = buildMessage(rawMessage);

        val requests = builder.build(message);
        assertThat(requests).hasSize(1);

        var firstRequest = requests.get(0);
        assertThat(firstRequest.source()).isEqualTo(Path.of("source", "xxx-file-id"));
        assertThat(firstRequest.target()).isEqualTo(Path.of("target",
                                                            "+49123456",
                                                            "2021-10-18_xxx-file-id_signal-2021-10-18-220724.jpeg"));
    }

    @Test
    void should_handle_with_group_and_filename() {
        @Language("JSON") val rawMessage = """
                {
                   "envelope": {
                     "source": "+49123456",
                     "timestamp": 1634587644427,
                     "dataMessage": {
                       "timestamp": 1634587644427,
                       "attachments": [
                         {
                           "contentType": "image/jpeg",
                           "filename": "signal-2021-10-18-220724.jpeg",
                           "id": "xxx-file-id",
                           "size": 1192256
                         }
                       ],
                       "groupInfo": {
                         "groupId": "xxx-group-id2",
                         "type": "DELIVER"
                       }
                     }
                   }
                 }""";
        val message = buildMessage(rawMessage);

        val requests = builder.build(message);
        assertThat(requests).hasSize(1);

        var firstRequest = requests.get(0);
        assertThat(firstRequest.source()).isEqualTo(Path.of("source", "xxx-file-id"));
        assertThat(firstRequest.target()).isEqualTo(Path.of("target",
                                                            "groups",
                                                            "eHh4LWdyb3VwLWlkMg==",
                                                            "+49123456",
                                                            "2021-10-18_xxx-file-id_signal-2021-10-18-220724.jpeg"));
    }

    @Test
    void should_have_no_attachments() {
        @Language("JSON") val rawMessage = """
                {
                   "envelope": {
                     "source": "+49123456",
                     "timestamp": 1634587644427,
                     "dataMessage": {
                       "timestamp": 1634587644427,
                       "attachments": [],
                       "groupInfo": {
                         "groupId": "xxx-group-id2",
                         "type": "DELIVER"
                       }
                     }
                   }
                 }""";
        val message = buildMessage(rawMessage);

        val requests = builder.build(message);
        assertThat(requests).isEmpty();
    }

    private Message buildMessage(String rawMessage) {
        Optional<Message> optionalMessage = new MessageParser().parse(rawMessage);
        if (optionalMessage.isPresent()) {
            return optionalMessage.get();
        }
        throw new IllegalStateException();
    }
}
