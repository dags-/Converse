package me.dags.converse;

import com.google.common.collect.Maps;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.channel.MutableMessageChannel;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ConversationManager {

    private final Map<String, Conversation> conversations = Maps.newConcurrentMap();

    ConversationManager(){}

    public Optional<Conversation> getConversation(CommandSource source) {
        return Optional.ofNullable(conversations.get(source.getIdentifier()));
    }

    public Optional<Conversation> getConversation(String identifier) {
        return Optional.ofNullable(conversations.get(identifier));
    }

    public Conversation removeConversation(Conversation conversation) {
        return removeConversation(conversation.getIdentifier());
    }

    public Conversation removeConversation(String identifier) {
        return conversations.remove(identifier);
    }

    public void addConversation(Conversation conversation) {
        conversations.put(conversation.getIdentifier(), conversation);
    }

    void removeFromChannel(MutableMessageChannel channel) {
        conversations.values().stream()
                .filter(Conversation::suppressMessages)
                .map(Conversation::getSource)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(channel::removeMember);
    }

    void tickConversations(Converse plugin) {
        List<Conversation> expired = conversations.values().stream()
                .filter(Conversation::hasExpired)
                .collect(Collectors.toList());

        Task.builder().execute(() -> expired.stream()
                .map(this::removeConversation)
                .forEach(conversation -> conversation.getSpec().onExpire(conversation)))
                .submit(plugin);
    }

    void process(Converse plugin, Conversation conversation, String input) {
        Task.builder().execute(() -> conversation.processSafely(input)).submit(plugin);
    }
}
