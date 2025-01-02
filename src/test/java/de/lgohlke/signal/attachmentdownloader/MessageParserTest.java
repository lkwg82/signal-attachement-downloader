package de.lgohlke.signal.attachmentdownloader;

import de.lgohlke.signal.attachmentdownloader.mapping.Message;
import lombok.val;
import org.intellij.lang.annotations.Language;
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

    @Test
    void should_parse_sync_message() {
        @Language("JSON")
        val json = """
                {
                  "envelope": {
                    "source": "+491867",
                    "sourceNumber": "+491867",
                    "sourceUuid": "34856e8c-a700-4d08-97d7-7457b8946d96",
                    "sourceName": "source",
                    "sourceDevice": 1,
                    "timestamp": 1735822640580,
                    "serverReceivedTimestamp": 1735822639691,
                    "serverDeliveredTimestamp": 1735822647879,
                    "syncMessage": {
                      "sentMessage": {
                        "destination": null,
                        "destinationNumber": null,
                        "destinationUuid": null,
                        "timestamp": 1735822640580,
                        "message": null,
                        "expiresInSeconds": 0,
                        "viewOnce": false,
                        "reaction": {
                          "emoji": "👍",
                          "targetAuthor": "+491235",
                          "targetAuthorNumber": "+491235",
                          "targetAuthorUuid": "2e694b04-122e-4bb5-9f8f-84ebed3968f4",
                          "targetSentTimestamp": 1735822516100,
                          "isRemove": false
                        },
                        "groupInfo": {
                          "groupId": "MHKEjxDmxBA/mJ710o=",
                          "groupName": "test",
                          "revision": 2,
                          "type": "DELIVER"
                        }
                      }
                    }
                  },
                  "account": "+491867"
                }
                
                """;
        Optional<Message> messageOptional = messageParser.parse(json);
        assertThat(messageOptional).isNotEmpty();
        assertThat(messageOptional.get().getEnvelope().getSyncMessage().getSentMessage().getReaction()
                                  .getEmoji()).isEqualTo("👍");
    }
}
