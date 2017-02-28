package me.dags.converse;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;

/**
 * @author dags <dags@dags.me>
 */
public interface ConversationExecutor {

    ConversationExecutor EMPTY = (source, args) -> ConversationResult.empty();

    ConversationResult execute(CommandSource source, CommandContext args);
}
