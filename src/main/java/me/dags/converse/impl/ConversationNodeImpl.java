package me.dags.converse.impl;

import com.google.common.base.Preconditions;
import me.dags.converse.api.*;
import me.dags.converse.dummy.Spunge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.args.parsing.InputTokenizer;
import org.spongepowered.api.command.args.parsing.SingleArg;

import java.util.List;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
final class ConversationNodeImpl implements ConversationNode {

    private final ConversationRouter router;
    private final ConversationPrompt prompt;
    private final CommandElement parameters;
    private final InputTokenizer tokenizer;
    private final ConversationRoute route;

    private ConversationNodeImpl(Builder builder) {
        this.router = builder.executor;
        this.prompt = builder.prompt;
        this.tokenizer = builder.tokenizer;
        this.parameters = builder.parameters;
        this.route = builder.route;
    }

    @Override
    public void process(Conversation conversation) throws CommandException {
        Optional<CommandSource> source = conversation.getSource();
        if (source.isPresent()) {
            ConversationRoute route = router.process(source.get(), conversation.getContext());
            conversation.nextRoute(route);
        } else {
            Spunge.getConversationManager().exitConversation(conversation.getIdentifier());
        }
    }

    public void parse(CommandSource source, String input, CommandContext context) throws CommandException {
        List<SingleArg> args = tokenizer.tokenize(input, false);
        CommandArgs commandArgs = new CommandArgs(input, args);
        parameters.parse(source, commandArgs, context);
    }

    public ConversationRoute getRoute() {
        return route;
    }

    public ConversationPrompt getPrompt() {
        return prompt;
    }

    static final class Builder implements ConversationNode.Builder {

        private final ConversationRoute route;

        private InputTokenizer tokenizer = InputTokenizer.quotedStrings(false);
        private ConversationPrompt prompt = ConversationPrompt.EMPTY;
        private CommandElement parameters = GenericArguments.seq();
        private ConversationRouter executor = null;

        Builder(String route) {
            this.route = ConversationRoute.next(route);
        }

        public Builder executor(ConversationRouter executor) {
            this.executor = executor;
            return this;
        }

        public Builder prompt(ConversationPrompt prompt) {
            this.prompt = prompt;
            return this;
        }

        public Builder parameter(CommandElement... elements) {
            this.parameters = GenericArguments.seq(elements);
            return this;
        }

        public Builder inputTokenizer(InputTokenizer tokenizer) {
            this.tokenizer = tokenizer;
            return this;
        }

        public ConversationNode build() {
            Preconditions.checkNotNull(executor);
            Preconditions.checkNotNull(prompt);
            Preconditions.checkNotNull(parameters);
            return new ConversationNodeImpl(this);
        }
    }
}
