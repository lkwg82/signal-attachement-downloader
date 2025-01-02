package de.lgohlke.signal.attachmentdownloader;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReactionHandlerFactoryTest {
    private final Path movedAttachments = Path.of("moved_attachments");

    @Test
    void should_create_reaction_handlers() {
        Map<String, String> mapping = Map.of("🎁", "calendar");
        val reactionHandlerMap = new ReactionHandlerFactory(mapping,
                                                            movedAttachments).createReactionHandlerMap();

        ReactionHandler actual = reactionHandlerMap.get("🎁");
        assertThat(actual).isInstanceOf(ReactionHandler.class);
    }

    @Test
    void should_fail_on_invalid_reaction() {
        Map<String, String> mapping = Map.of("🎁🎁", "calendar");

        val factory = new ReactionHandlerFactory(mapping, movedAttachments);

        assertThatThrownBy(factory::createReactionHandlerMap)
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_fail_on_duplicate_paths() {
        Map<String, String> mapping = Map.of("🎁", "calendar", "😀", "calendar");

        val factory = new ReactionHandlerFactory(mapping, movedAttachments);

        assertThatThrownBy(factory::createReactionHandlerMap)
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test()
    void should_use_config_from_environment() {
        val key = "EMOJI_MAP_😀";
        try {
            System.setProperty(key, "calendar2");
            Map<String, String> mapping = Map.of("🎁", "calendar");

            val factory = new ReactionHandlerFactory(mapping, movedAttachments);

            assertThat(factory.createReactionHandlerMap()).hasSize(2);
        } finally {
            System.clearProperty(key);
        }
    }
}