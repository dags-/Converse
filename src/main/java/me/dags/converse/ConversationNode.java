package me.dags.converse;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.parsing.InputTokenizer;
import org.spongepowered.api.command.args.parsing.SingleArg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public final class ConversationNode {

    private final ConversationRouter router;
    private final ConversationPrompt prompt;
    private final List<CommandElement> parameters;
    private final InputTokenizer tokenizer;
    private final ConversationRoute route;

    private ConversationNode(Builder builder) {
        this.router = builder.executor;
        this.prompt = builder.prompt;
        this.tokenizer = builder.tokenizer;
        this.parameters = ImmutableList.copyOf(builder.parameters);
        this.route = builder.route;
    }

    public void process(Conversation conversation) throws ConversationException {
        Optional<CommandSource> source = conversation.getSource();
        if (source.isPresent()) {
            ConversationRoute route = router.process(source.get(), conversation.getContext());
            conversation.nextRoute(route);
        }
    }

    public void parse(CommandSource source, String input, ConversationContext context) throws ArgumentParseException {
        List<SingleArg> args = tokenizer.tokenize(input, false);
        CommandArgs commandArgs = new CommandArgs(input, args);
        CommandContext commandContext = new CommandContext();
        for (CommandElement element : parameters) {
            element.parse(source, commandArgs, commandContext);
            context.putAll(element.getKey(), commandContext.getAll(element.getKey()));
        }
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
        private List<CommandElement> parameters = new ArrayList<>();
        private ConversationRouter executor = null;

        Builder(String route) {
            this.route = ConversationRoute.goTo(route);
        }

        public Builder router(ConversationRouter executor) {
            this.executor = executor;
            return this;
        }

        public Builder prompt(ConversationPrompt prompt) {
            this.prompt = prompt;
            return this;
        }

        public Builder parameter(CommandElement... elements) {
            Collections.addAll(parameters, elements);
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
