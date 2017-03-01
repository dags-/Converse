package me.dags.converse.api;

import org.spongepowered.api.text.channel.MutableMessageChannel;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public interface ConversationManager {

    ConversationSpec.Builder getSpecBuilder();

    ConversationNode.Builder getNodeBuilder(String route);

    Optional<Conversation> getConversation(String identifier);

    void addConversation(Conversation conversation);

    Conversation removeConversation(String identifier);

    void completeConversation(String identifier);

    void exitConversation(String identifier);

    void expireConversation(String identifier);

    void removeFromChannel(MutableMessageChannel channel);

    void post(Conversation conversation, String input);
}
