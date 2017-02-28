package me.dags.converse;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MutableMessageChannel;

import java.util.Objects;

/**
 * @author dags <dags@dags.me>
 */
public final class MessageListener {

    private final ConversationSpec spec;

    MessageListener(ConversationSpec spec) {
        this.spec = spec;
    }

    @Listener(order = Order.LAST)
    public void messageEvent(MessageChannelEvent event) {
        if (event.isMessageCancelled()) {
            return;
        }

        MutableMessageChannel channel = event.getChannel().orElse(event.getOriginalChannel()).asMutable();

        spec.conversations.values().stream()
                .map(Conversation::getSource)
                .filter(Objects::nonNull)
                .forEach(channel::removeMember);

        event.setChannel(channel);
    }

    @Listener(order = Order.PRE)
    public void chatEvent(MessageChannelEvent.Chat event, @Root CommandSource source) {
        if (event.isMessageCancelled()) {
            return;
        }

        Conversation conversation = spec.conversations.get(source.getIdentifier());
        if (conversation != null) {
            event.setCancelled(true);
            event.setMessageCancelled(true);
            Text input = event.getRawMessage();
            Task.builder().execute(() -> conversation.process(input)).submit(spec.plugin);
        }
    }
}
