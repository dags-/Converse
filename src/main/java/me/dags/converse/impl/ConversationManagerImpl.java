package me.dags.converse.impl;

import com.google.common.collect.Maps;
import me.dags.converse.api.Conversation;
import me.dags.converse.api.ConversationManager;
import me.dags.converse.api.ConversationNode;
import me.dags.converse.api.ConversationSpec;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.text.channel.MutableMessageChannel;

import java.util.Map;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public final class ConversationManagerImpl implements ConversationManager {

    private final Map<String, Conversation> conversations = Maps.newConcurrentMap();
    private final SpongeExecutorService syncExecutor;

    public ConversationManagerImpl(SpongeExecutorService syncExecutor) {
        this.syncExecutor = syncExecutor;
    }

    @Override
    public Optional<Conversation> getConversation(String identifier) {
        return Optional.ofNullable(conversations.get(identifier));
    }

    @Override
    public ConversationSpec.Builder getSpecBuilder() {
        return new ConversationSpecImpl.Builder();
    }

    @Override
    public ConversationNode.Builder getNodeBuilder(String route) {
        return new ConversationNodeImpl.Builder(route);
    }

    @Override
    public void addConversation(Conversation conversation) {
        conversations.put(conversation.getIdentifier(), conversation);
    }

    @Override
    public Conversation removeConversation(String identifier) {
        return conversations.remove(identifier);
    }

    @Override
    public void completeConversation(String identifier) {
        Conversation conversation = removeConversation(identifier);
        if (conversation != null) {
            ConversationSpec spec = conversation.getSpec();
            syncExecutor.submit(() -> spec.onComplete(conversation));
        }
    }

    @Override
    public void exitConversation(String identifier) {
        Conversation conversation = removeConversation(identifier);
        if (conversation != null) {
            ConversationSpec spec = conversation.getSpec();
            syncExecutor.submit(() -> spec.onExit(conversation));
        }
    }

    @Override
    public void expireConversation(String identifier) {
        Conversation conversation = removeConversation(identifier);
        if (conversation != null) {
            ConversationSpec spec = conversation.getSpec();
            syncExecutor.submit(() -> spec.onExpire(conversation));
        }
    }

    @Override
    public void removeFromChannel(MutableMessageChannel channel) {
        conversations.values().stream()
                .filter(Conversation::hidesMessages)
                .map(Conversation::getSource)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(channel::removeMember);
    }

    @Override
    public void post(Conversation conversation, String input) {
        if (conversation != null) {
            syncExecutor.submit(() -> conversation.processSafely(input));
        }
    }

    public void tickConversations() {
        conversations.values().stream()
                .filter(Conversation::hasExpired)
                .map(Conversation::getIdentifier)
                .forEach(this::expireConversation);
    }
}
