package me.dags.converse.dummy;

import me.dags.converse.api.ConversationManager;
import me.dags.converse.impl.ConversationManagerImpl;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.util.concurrent.TimeUnit;

/**
 * @author dags <dags@dags.me>
 */
public final class Spunge {

    private static final Spunge instance = new Spunge();

    private ConversationManagerImpl conversationManager;

    private Spunge() {}

    public static ConversationManager getConversationManager() {
        return instance.conversationManager;
    }

    public static void init(Object plugin) {
        instance.onInit(plugin);
    }

    public void onInit(Object plugin) {
        if (conversationManager == null) {
            conversationManager = new ConversationManagerImpl(Sponge.getScheduler().createSyncExecutor(plugin));

            Sponge.getEventManager().registerListeners(plugin, new MessageListener());

            Task.builder().async()
                    .interval(10, TimeUnit.SECONDS)
                    .execute(() -> {
                        conversationManager.tickConversations();
                        System.out.println("..");
                    })
                    .submit(plugin);
        }
    }
}
