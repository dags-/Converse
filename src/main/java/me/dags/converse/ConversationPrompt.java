package me.dags.converse;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;

/**
 * @author dags <dags@dags.me>
 */
public interface ConversationPrompt {

    ConversationPrompt EMPTY = (source, context) -> Text.EMPTY;

    Text getPrompt(CommandSource source, CommandContext context);
}
