package me.dags.converse;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;

/**
 * @author dags <dags@dags.me>
 */
@FunctionalInterface
public interface ConversationRouter {

    ConversationRoute process(CommandSource source, CommandContext context);
}
