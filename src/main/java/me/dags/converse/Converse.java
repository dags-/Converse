package me.dags.converse;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.TabCompleteEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.channel.MutableMessageChannel;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author dags <dags@dags.me>
 */
@Plugin(id = Converse.ID, name = "Converse", version = "0.1")
public final class Converse {

    private static final ConversationManager conversationManager = new ConversationManager();
    static final String ID = "converse";

    public static ConversationManager getConversationManager() {
        return conversationManager;
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        Task.builder()
                .async()
                .interval(10, TimeUnit.SECONDS)
                .execute(() -> Converse.getConversationManager().tickConversations(this))
                .submit(this);
    }

    @Listener(order = Order.LAST)
    public void onTabComplete(TabCompleteEvent.Chat event, @Root CommandSource source) {
        Optional<Conversation> conversation = Converse.getConversationManager().getConversation(source);
        if (conversation.isPresent()) {
            List<String> completions = conversation.get().completeSafely(event.getRawMessage());
            event.getTabCompletions().addAll(completions);
        }
    }

    @Listener (order = Order.LAST)
    public void onMessage(MessageChannelEvent event) {
        if (event.isMessageCancelled()) {
            return;
        }

        MutableMessageChannel channel = event.getChannel().orElse(event.getOriginalChannel()).asMutable();
        Converse.getConversationManager().removeFromChannel(channel);
    }

    @Listener (order = Order.PRE)
    public void onChat(MessageChannelEvent.Chat event, @Root CommandSource source) {
        if (event.isMessageCancelled()) {
            return;
        }

        Optional<Conversation> conversation = Converse.getConversationManager().getConversation(source.getIdentifier());
        if (conversation.isPresent()) {
            event.setCancelled(true);
            event.setMessageCancelled(true);
            String raw = event.getRawMessage().toPlain();
            Converse.getConversationManager().process(this, conversation.get(), raw);
        }
    }
}
