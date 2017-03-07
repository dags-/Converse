package me.dags.converse;

import com.google.common.collect.Maps;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.text.channel.MutableMessageChannel;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */
public final class ConversationManager {

    private final Map<String, Conversation> conversations = Maps.newConcurrentMap();
    private final SpongeExecutorService syncExecutor;

    private ConversationManager(SpongeExecutorService syncExecutor){
        this.syncExecutor = syncExecutor;
    }

    public Optional<Conversation> getConversation(String identifier) {
        return Optional.ofNullable(conversations.get(identifier));
    }

    public ConversationSpec.Builder specBuilder() {
        return new ConversationSpec.Builder(this);
    }

    public ConversationNode.Builder nodeBuilder(String route) {
        return new ConversationNode.Builder(route);
    }

    public void addConversation(Conversation conversation) {
        conversations.put(conversation.getIdentifier(), conversation);
    }

    public Conversation removeConversation(Conversation conversation) {
        return removeConversation(conversation.getIdentifier());
    }

    public Conversation removeConversation(String identifier) {
        return conversations.remove(identifier);
    }

    public void removeFromChannel(MutableMessageChannel channel) {
        conversations.values().stream()
                .filter(Conversation::suppressMessages)
                .map(Conversation::getSource)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(channel::removeMember);
    }

    void tickConversations() {
        List<Conversation> expired = conversations.values().stream()
                .filter(Conversation::hasExpired)
                .collect(Collectors.toList());

        syncExecutor.submit(() -> expired.stream()
                .map(this::removeConversation)
                .forEach(conversation -> conversation.getSpec().onExpire(conversation)));
    }

    void process(Conversation conversation, String input) {
        syncExecutor.submit(() -> conversation.processSafely(input));
    }

    public static ConversationManager create(Object plugin) {
        SpongeExecutorService executor = Sponge.getScheduler().createSyncExecutor(plugin);
        ConversationManager manager = new ConversationManager(executor);
        MessageListener listener = new MessageListener(manager);
        Sponge.getEventManager().registerListeners(plugin, listener);
        return manager;
    }
}
