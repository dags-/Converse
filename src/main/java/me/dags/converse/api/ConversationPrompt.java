package me.dags.converse.api;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;

/**
 * @author dags <dags@dags.me>
 */
public interface ConversationPrompt {

    ConversationPrompt EMPTY = (source, context) -> Text.EMPTY;

    Text apply(CommandSource source, CommandContext context);
}
