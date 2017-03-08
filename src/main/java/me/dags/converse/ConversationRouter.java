package me.dags.converse;

import org.spongepowered.api.command.CommandSource;

/**
 * @author dags <dags@dags.me>
 */
@FunctionalInterface
public interface ConversationRouter {

    ConversationRoute process(CommandSource src, ConversationContext context);
}
