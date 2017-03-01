package me.dags.converse.api;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author dags <dags@dags.me>
 */
public interface Conversation {

    void process(String input) throws CommandException;

    default void processSafely(String input) {
        try {
            process(input);
        } catch (CommandException e) {
            if (e.getText() != null) {
                getSource().ifPresent(source -> source.sendMessage(e.getText()));
            }
        }
    }

    void nextRoute(ConversationRoute route) throws CommandException;

    String getIdentifier();

    Optional<CommandSource> getSource();

    CommandContext getContext();

    ConversationSpec getSpec();

    long getAge(TimeUnit timeUnit);

    default boolean hasExpired() {
        return getSpec().hasExpired(this);
    }

    default boolean hidesMessages() {
        return getSpec().hidesMessages();
    }
}
