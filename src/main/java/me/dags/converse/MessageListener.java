package me.dags.converse;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.channel.MutableMessageChannel;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class MessageListener {

    private final ConversationManager manager;

    MessageListener(ConversationManager manager) {
        this.manager = manager;
    }

    @Listener (order = Order.PRE)
    public void preMessage(MessageChannelEvent event) {
        manager.tickConversations();
    }

    @Listener (order = Order.LAST)
    public void lastMessage(MessageChannelEvent event) {
        if (event.isMessageCancelled()) {
            return;
        }

        MutableMessageChannel channel = event.getChannel().orElse(event.getOriginalChannel()).asMutable();
        manager.removeFromChannel(channel);
    }

    @Listener (order = Order.PRE)
    public void preChat(MessageChannelEvent.Chat event, @Root CommandSource source) {
        if (event.isMessageCancelled()) {
            return;
        }

        Optional<Conversation> conversation = manager.getConversation(source.getIdentifier());
        if (conversation.isPresent()) {
            event.setCancelled(true);
            event.setMessageCancelled(true);
            String raw = event.getRawMessage().toPlain();
            manager.process(conversation.get(), raw);
        }
    }
}
