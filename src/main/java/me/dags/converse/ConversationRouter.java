package me.dags.converse;

import org.spongepowered.api.command.CommandSource;

/**
 * Handles the routing of the conversation from one ConversationNode to another.
 *
 * Similar to a CommandExecutor, the ConversationRouter is called after the input from a CommandSource has been
 * parsed. The most recently parsed information can be retrieved from the provided ContextCollection via
 * ContextCollection.getCurrent().
 * ConversationContexts from previous nodes can also be queried.
 */
@FunctionalInterface
public interface ConversationRouter {

    /**
     * @param src The CommandSource involved in this conversation.
     * @param contexts The contexts of the conversation.
     * @return The ConversationRoute of the next ConversationNode to move onto.
     */
    ConversationRoute process(CommandSource src, ContextCollection contexts);
}
