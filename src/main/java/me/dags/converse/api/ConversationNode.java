package me.dags.converse.api;

import me.dags.converse.dummy.Spunge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;

/**
 * @author dags <dags@dags.me>
 */
public interface ConversationNode {

    static Builder builder(String route) {
        return Spunge.getConversationManager().getNodeBuilder(route);
    }

    void process(Conversation conversation) throws CommandException;

    void parse(CommandSource source, String input, CommandContext context) throws CommandException;

    ConversationRoute getRoute();

    ConversationPrompt getPrompt();

    interface Builder {

        Builder executor(ConversationRouter executor);

        Builder prompt(ConversationPrompt prompt);

        Builder parameter(CommandElement... parameters);

        ConversationNode build();
    }
}
