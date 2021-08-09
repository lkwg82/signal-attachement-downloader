package de.lgohlke.signal.attachmentdownloader.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class MappingTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @SneakyThrows
    public void direct_message_without_attachments() {
        var data = """
                {
                  "envelope": {
                    "source": "+491734982893",
                    "sourceDevice": 2,
                    "timestamp": 1628069823084,
                    "dataMessage": {
                      "timestamp": 1628069823084,
                      "message": "test",
                      "expiresInSeconds": 2419200,
                      "viewOnce": false,
                      "mentions": [],
                      "attachments": [],
                      "contacts": []
                    }
                  }
                }
                """;

        var message = mapper.readValue(data, Message.class);

        var envelope = message.getEnvelope();
        assertThat(envelope.getSource()).isEqualTo("+491734982893");
        assertThat(envelope.getTimestamp()).isEqualTo(Timestamp.from(Instant.ofEpochMilli(1628069823084L)));

        var dataMessage = envelope.getDataMessage();
        assertThat(dataMessage.getTimestamp()).isEqualTo(Timestamp.from(Instant.ofEpochMilli(1628069823084L)));
        assertThat(dataMessage.getMessage()).isEqualTo("test");
        assertThat(dataMessage.getAttachments()).isEmpty();
    }

    @Test
    @SneakyThrows
    public void group_message_without_attachments() {
        var data = """
                {
                   "envelope": {
                     "source": "+491734982893",
                     "sourceDevice": 2,
                     "timestamp": 1628071747721,
                     "dataMessage": {
                       "timestamp": 1628071747721,
                       "message": "test2",
                       "expiresInSeconds": 86400,
                       "viewOnce": false,
                       "mentions": [],
                       "attachments": [],
                       "contacts": [],
                       "groupInfo": {
                         "groupId": "TITxYXAWoi2NHsDAFiKvpaFVGosLwo/Fal5StbhEAD8=",
                         "type": "DELIVER"
                       }
                     }
                   }
                 }
                """;

        var message = mapper.readValue(data, Message.class);

        var envelope = message.getEnvelope();
        var dataMessage = envelope.getDataMessage();
        assertThat(dataMessage.getAttachments()).isEmpty();
        assertThat(dataMessage.getMessage()).isEqualTo("test2");
        assertThat(dataMessage.getGroupInfo()
                              .getGroupId()).isEqualTo("TITxYXAWoi2NHsDAFiKvpaFVGosLwo/Fal5StbhEAD8=");
    }

    @Test
    @SneakyThrows
    public void group_message_with_attachments() {
        var data = """
                                
                  {
                    "envelope": {
                      "source": "+491734982893",
                      "sourceDevice": 2,
                      "timestamp": 1628072284662,
                      "dataMessage": {
                        "timestamp": 1628072284662,
                        "message": "test3",
                        "expiresInSeconds": 86400,
                        "viewOnce": false,
                        "mentions": [],
                        "attachments": [
                          {
                            "contentType": "image/jpeg",
                            "filename": "Björn.jpeg",
                            "id": "4520004565653960349",
                            "size": 18386
                          },
                          {
                            "contentType": "image/jpeg",
                            "filename": "IMG_6498.jpeg",
                            "id": "3708258610491210395",
                            "size": 413771
                          },
                          {
                            "contentType": "image/jpeg",
                            "filename": "IMG_6624.jpeg",
                            "id": "2765837196910908984",
                            "size": 439030
                          },
                          {
                            "contentType": "image/jpeg",
                            "filename": "IMG_20170512_204401.jpeg",
                            "id": "7541237625886758790",
                            "size": 308729
                          }
                        ],
                        "contacts": [],
                        "groupInfo": {
                          "groupId": "TITxYXAWoi2NHsDAFiKvpaFVGosLwo/Fal5StbhEAD8=",
                          "type": "DELIVER"
                        }
                      }
                    }
                  }
                """;

        var message = mapper.readValue(data, Message.class);

        var envelope = message.getEnvelope();
        var dataMessage = envelope.getDataMessage();
        assertThat(dataMessage.getMessage()).isEqualTo("test3");
        assertThat(dataMessage.getGroupInfo()
                              .getGroupId()).isEqualTo("TITxYXAWoi2NHsDAFiKvpaFVGosLwo/Fal5StbhEAD8=");

        var attachments = dataMessage.getAttachments();
        assertThat(attachments).hasSize(4);
        var firstAttachment = attachments.get(0);
        assertThat(firstAttachment.getSize()).isEqualTo(18386);
        assertThat(firstAttachment.getId()).isEqualTo("4520004565653960349");
    }


    @Test
    @SneakyThrows
    void should_handle_syncMessage() {
        var data = """
                {"envelope":{"source":"+493334299307","sourceDevice":3,"timestamp":1628071748623,"syncMessage":{"sentMessage":{"timestamp":1628071748623,"message":"⏳ [6410c2]","expiresInSeconds":86400,"viewOnce":false,"mentions":[],"attachments":[],"contacts":[],"groupInfo":{"groupId":"TITxYXAWoi2NHsDAFiKvpaFVGosLwo/Fal5StbhEAD8=","type":"DELIVER"},"destination":null}}}}
                """;
        mapper.readValue(data, Message.class);
    }

    @Test
    @SneakyThrows
    void should_handle_receiptMessage() {
        var data = """
                {"envelope":{"source":"+491734982893","sourceDevice":2,"timestamp":1628071749765,"receiptMessage":{"when":1628071749765,"isDelivery":true,"isRead":false,"timestamps":[1628071748623]}}}
                """;
        mapper.readValue(data, Message.class);
    }

}