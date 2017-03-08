package me.dags.converse;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

/**
 * @author dags <dags@dags.me>
 */
public interface ConversationPrompt {

    ConversationPrompt EMPTY = (src, context) -> Text.EMPTY;

    Text apply(CommandSource src, ConversationContext context);
}
