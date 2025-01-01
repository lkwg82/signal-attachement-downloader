package de.lgohlke.signal.attachmentdownloader;

import de.lgohlke.signal.attachmentdownloader.mapping.Message;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageParserTest {
    private final MessageParser messageParser = new MessageParser();

    @Test
    void should_handle_valid_json() {
        assertThat(messageParser.parse("{\"envelope\":{}}}")).isNotEmpty();
    }

    @Test
    void should_parse_with_reaction() {
        // language=json
        var json = """
                {
                  "envelope": {
                    "source": "+49145678",
                    "sourceNumber": "+49145678",
                    "sourceUuid": "f0856790-6342-4610-a018-1588e741155e",
                    "sourceName": "me",
                    "sourceDevice": 3,
                    "timestamp": 1735753198168,
                    "serverReceivedTimestamp": 1735753198293,
                    "serverDeliveredTimestamp": 1735753236195,
                    "dataMessage": {
                      "timestamp": 1735753198168,
                      "message": null,
                      "expiresInSeconds": 2419200,
                      "viewOnce": false,
                      "reaction": {
                        "emoji": "🎁",
                        "targetAuthor": "+49145678",
                        "targetAuthorNumber": "+49145678",
                        "targetAuthorUuid": "f0856790-6342-4610-a018-1588e741155e",
                        "targetSentTimestamp": 1735753086087,
                        "isRemove": false
                      }
                    }
                  },
                  "account": "+49123456"
                }
                """;

        Optional<Message> messageOptional = messageParser.parse(json);
        assertThat(messageOptional).isNotEmpty();
        assertThat(messageOptional.get().getEnvelope().getDataMessage().getReaction().getEmoji()).isEqualTo("🎁");

    }

}
