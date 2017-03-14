package me.dags.converse;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

/**
 * Provides a Text object that is used to prompt a CommandSource for input.
 */
@FunctionalInterface
public interface ConversationPrompt {

    ConversationPrompt EMPTY = (src, context) -> Text.EMPTY;

    /**
     * @param src The CommandSource involved in the conversation.
     * @param contexts The contexts of the conversation.
     * @return The Text that will be sent to the CommandSource to prompt them for input.
     */
    Text apply(CommandSource src, ContextCollection contexts);
}
