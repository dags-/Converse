package me.dags.converse.dummy;

import me.dags.converse.api.Conversation;
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

    @Listener (order = Order.LAST)
    public void onMessage(MessageChannelEvent event) {
        if (event.isMessageCancelled()) {
            return;
        }

        MutableMessageChannel channel = event.getChannel().orElse(event.getOriginalChannel()).asMutable();
        Spunge.getConversationManager().removeFromChannel(channel);
    }

    @Listener (order = Order.PRE)
    public void onChat(MessageChannelEvent.Chat event, @Root CommandSource source) {
        if (event.isMessageCancelled()) {
            return;
        }

        Optional<Conversation> conversation = Spunge.getConversationManager().getConversation(source.getIdentifier());
        if (conversation.isPresent()) {
            event.setCancelled(true);
            event.setMessageCancelled(true);
            String raw = event.getRawMessage().toPlain();
            Spunge.getConversationManager().post(conversation.get(), raw);
        }
    }
}
