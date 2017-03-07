package me.dags.converse;

import com.google.common.base.Preconditions;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.*;
import org.spongepowered.api.command.args.parsing.InputTokenizer;
import org.spongepowered.api.command.args.parsing.SingleArg;

import java.util.List;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public final class ConversationNode {

    private final ConversationRouter router;
    private final ConversationPrompt prompt;
    private final CommandElement parameters;
    private final InputTokenizer tokenizer;
    private final ConversationRoute route;

    private ConversationNode(Builder builder) {
        this.router = builder.executor;
        this.prompt = builder.prompt;
        this.tokenizer = builder.tokenizer;
        this.parameters = builder.parameters;
        this.route = builder.route;
    }

    public void process(Conversation conversation) throws ConversationException {
        Optional<CommandSource> source = conversation.getSource();
        if (source.isPresent()) {
            ConversationRoute route = router.process(source.get(), conversation.getContext());
            conversation.nextRoute(route);
        }
    }

    public void parse(CommandSource source, String input, CommandContext context) throws ArgumentParseException {
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

    public static Builder builder(String route) {
        return new Builder(route);
    }

    public static final class Builder {

        private final ConversationRoute route;

        private InputTokenizer tokenizer = InputTokenizer.quotedStrings(false);
        private ConversationPrompt prompt = ConversationPrompt.EMPTY;
        private CommandElement parameters = GenericArguments.seq();
        private ConversationRouter executor = null;

        Builder(String route) {
            this.route = ConversationRoute.goTo(route);
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
            return new ConversationNode(this);
        }
    }
}
