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
    private final MoveRequestBuilder builder = new MoveRequestBuilder(attachmentsOfSignal,
                                                                      attachmentsMoved,
                                                                      false);
    private final MoveRequestBuilder builderFlatGroup = new MoveRequestBuilder(attachmentsOfSignal,
                                                                               attachmentsMoved,
                                                                               true);

    @Test
    void should_handle_missing_filename() {
        @Language("JSON") val rawMessage = """
                {
                  "envelope": {
                    "source": "+49123456",
                     "sourceUuid": "f0856790-6342-4610-a018-1588e741155e",
                    "timestamp": 1634635559631,
                    "dataMessage": {
                      "timestamp": 1634635559631,
                      "message": null,
                      "attachments": [
                        {
                          "contentType": "image/jpeg",
                          "filename": null,
                          "id": "xxx-file-id2.jpg",
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
        assertThat(firstRequest.source()).isEqualTo(Path.of("source", "xxx-file-id2.jpg"));
        assertThat(firstRequest.target()).isEqualTo(Path.of("target",
                                                            "groups",
                                                            "eHh4LWdyb3VwLWlkMg==",
                                                            "f0856790-6342-4610-a018-1588e741155e",
                                                            "2021-10-19_xxx-file-id2.jpg"));
    }

    @Test
    void should_handle_with_filename() {
        @Language("JSON")
        val rawMessage = """
                {
                   "envelope": {
                     "source": "+49145678",
                     "sourceNumber": "+49145678",
                     "sourceUuid": "f0856790-6342-4610-a018-1588e741155e",
                     "sourceName": "source",
                     "sourceDevice": 1,
                     "timestamp": 1735763814857,
                     "serverReceivedTimestamp": 1735763816559,
                     "serverDeliveredTimestamp": 1735763825761,
                     "dataMessage": {
                       "timestamp": 1735763814857,
                       "message": null,
                       "expiresInSeconds": 2419200,
                       "viewOnce": false,
                       "attachments": [
                         {
                           "contentType": "image/jpeg",
                           "filename": "signal-2025-01-01-213654.jpeg",
                           "id": "tzZEgpooGjRPevvfzQts.jpeg",
                           "size": 445583,
                           "width": 2048,
                           "height": 1536,
                           "caption": null,
                           "uploadTimestamp": null
                         }
                       ]
                     }
                   },
                   "account": "+4915454"
                 }
                """;
        val message = buildMessage(rawMessage);

        val requests = builder.build(message);
        assertThat(requests).hasSize(1);

        var firstRequest = requests.get(0);
        assertThat(firstRequest.source()).isEqualTo(Path.of("source", "tzZEgpooGjRPevvfzQts.jpeg"));
        assertThat(firstRequest.target()).isEqualTo(Path.of("target",
                                                            "direct",
                                                            "f0856790-6342-4610-a018-1588e741155e",
                                                            "2025-01-01_tzZEgpooGjRPevvfzQts.jpeg"));
    }

    @Test
    void should_handle_with_group_and_filename() {
        @Language("JSON") val rawMessage = """
                {
                   "envelope": {
                     "source": "+49123456",
                     "sourceUuid": "f0856790-6342-4610-a018-1588e741155e",
                     "timestamp": 1634587644427,
                     "dataMessage": {
                       "timestamp": 1634587644427,
                       "attachments": [
                         {
                           "contentType": "image/jpeg",
                           "filename": "signal-2021-10-18-220724.jpeg",
                           "id": "xxx-file-id.jpg",
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
        assertThat(firstRequest.source()).isEqualTo(Path.of("source", "xxx-file-id.jpg"));
        assertThat(firstRequest.target()).isEqualTo(Path.of("target",
                                                            "groups",
                                                            "eHh4LWdyb3VwLWlkMg==",
                                                            "f0856790-6342-4610-a018-1588e741155e",
                                                            "2021-10-18_xxx-file-id.jpg"));
    }

    @Test
    void should_handle_with_group_and_filename_flat_structure() {
        @Language("JSON") val rawMessage = """
                {
                   "envelope": {
                     "sourceUuid": "f0856790-6342-4610-a018-1588e741155e",
                     "timestamp": 1634587644427,
                     "dataMessage": {
                       "timestamp": 1634587644427,
                       "attachments": [
                         {
                           "contentType": "image/jpeg",
                           "filename": "signal-2021-10-18-220724.jpeg",
                           "id": "xxx-file-id.jpg",
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

        val requests = builderFlatGroup.build(message);
        assertThat(requests).hasSize(1);

        var firstRequest = requests.get(0);
        assertThat(firstRequest.source()).isEqualTo(Path.of("source", "xxx-file-id.jpg"));
        assertThat(firstRequest.target()).isEqualTo(Path.of("target",
                                                            "groups",
                                                            "eHh4LWdyb3VwLWlkMg==",
                                                            "2021-10-18_xxx-file-id.jpg"));
    }

    @Test
    void should_have_no_attachments() {
        @Language("JSON") val rawMessage = """
                {
                   "envelope": {
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
