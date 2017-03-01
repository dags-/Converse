package me.dags.converse.api;

import me.dags.converse.dummy.Spunge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public interface ConversationSpec extends CommandExecutor {

    static Builder builder() {
        return Spunge.getConversationManager().getSpecBuilder();
    }

    @Override
    default CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        return startConversation(source);
    }

    CommandResult startConversation(CommandSource source) throws CommandException;

    boolean isExitKeyword(String input);

    boolean hidesMessages();

    void onComplete(Conversation conversation);

    void onExit(Conversation conversation);

    void onExpire(Conversation conversation);

    boolean hasExpired(Conversation conversation);

    interface Builder {

        Builder nodes(ConversationNode... children);

        Builder root(ConversationNode root);

        Builder hideMessages(boolean hide);

        Builder exitAlias(String... aliases);

        Builder onComplete(Consumer<Conversation> consumer);

        Builder onExit(Consumer<Conversation> consumer);

        Builder onExpire(Consumer<Conversation> consumer);

        Builder timeOut(long period, TimeUnit unit);

        ConversationSpec build();
    }
}
