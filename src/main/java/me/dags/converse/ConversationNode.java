package me.dags.converse;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.*;
import org.spongepowered.api.command.args.parsing.InputTokenizer;
import org.spongepowered.api.command.args.parsing.SingleArg;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents a single node in a conversation.
 * The node prompts the CommandSource and waits for input.
 * It then parses the input and routes the Conversation onto the next node.
 * The node is also used to determine tab-completions for active Conversations.
 */
public final class ConversationNode {

    private final ConversationRouter router;
    private final ConversationPrompt prompt;
    private final List<CommandElement> parameters;
    private final CommandElement sequence;
    private final InputTokenizer tokenizer;
    private final ConversationRoute route;

    private ConversationNode(Builder builder) {
        this.router = builder.router;
        this.prompt = builder.prompt;
        this.tokenizer = builder.tokenizer;
        this.parameters = ImmutableList.copyOf(builder.parameters);
        this.sequence = GenericArguments.seq(builder.parameters.toArray(new CommandElement[builder.parameters.size()]));
        this.route = builder.route;
    }

    public List<String> complete(CommandSource source, String input) throws ArgumentParseException {
        List<SingleArg> args = tokenizer.tokenize(input, false);
        CommandArgs commandArgs = new CommandArgs(input, args);
        CommandContext commandContext = new CommandContext();
        return sequence.complete(source, commandArgs, commandContext);
    }

    public void process(Conversation conversation) throws ConversationException {
        Optional<CommandSource> source = conversation.getSource();
        if (source.isPresent()) {
            ConversationRoute route = router.process(source.get(), conversation.getContext());
            conversation.nextRoute(route);
        }
    }

    public void parse(CommandSource source, String input, ContextCollection context) throws ArgumentParseException {
        List<SingleArg> args = tokenizer.tokenize(input, false);
        CommandArgs commandArgs = new CommandArgs(input, args);
        CommandContext commandContext = new CommandContext();
        ConversationContext currentContext = new ConversationContext();

        context.putContext(getRoute(), currentContext);
        context.setCurrent(currentContext);

        for (CommandElement element : parameters) {
            element.parse(source, commandArgs, commandContext);
            currentContext.putAll(element.getKey(), commandContext.getAll(element.getKey()));
        }
    }

    public ConversationRoute getRoute() {
        return route;
    }

    public ConversationPrompt getPrompt() {
        return prompt;
    }

    public static Builder route(String route) {
        return route(ConversationRoute.goTo(route));
    }

    public static Builder route(ConversationRoute route) {
        return new Builder(route);
    }

    public static final class Builder {

        private final ConversationRoute route;

        private InputTokenizer tokenizer = InputTokenizer.quotedStrings(false);
        private ConversationPrompt prompt = ConversationPrompt.EMPTY;
        private List<CommandElement> parameters = new ArrayList<>();
        private ConversationRouter router = null;

        Builder(ConversationRoute route) {
            this.route = route;
        }

        /**
         * Route the conversation to the provided ConversationRoute without any additional processing
         * @param route The next ConversationRoute the Conversation should move on to
         * @return The current Builder
         */
        public Builder router(ConversationRoute route) {
            this.router = (src, context) -> route;
            return this;
        }

        /**
         * Route the conversation via a ConversationRouter
         * @param router The Router that will evaluate the current ConversationContext and decide which route to move
         *               on to next
         * @return The current Builder
         */
        public Builder router(ConversationRouter router) {
            this.router = router;
            return this;
        }

        /**
         * Assign a simple Text prompt for the ConversationNode
         * @param prompt The Text used to prompt the CommandSource for input
         * @return The current Builder
         */
        public Builder prompt(Text prompt) {
            this.prompt = (src, context) -> prompt;
            return this;
        }

        /**
         * Assign a ConversationPrompt for the ConversationNode
         * @param prompt The ConversationPrompt used to prompt the CommandSource for input
         * @return The current Builder
         */
        public Builder prompt(ConversationPrompt prompt) {
            this.prompt = prompt;
            return this;
        }

        /**
         * The parameter(s) that the CommandSource is expected to input
         * @param elements The parameters
         * @return The current Builder
         */
        public Builder parameters(CommandElement... elements) {
            Collections.addAll(parameters, elements);
            return this;
        }

        /**
         * Provide an alternate InputTokenizer used to parse input strings
         * @param tokenizer The InputTokenizer
         * @return The current Builder
         */
        public Builder inputTokenizer(InputTokenizer tokenizer) {
            this.tokenizer = tokenizer;
            return this;
        }

        /**
         * Build a new ConversationNode from the current Builder
         * @return The current Builder
         */
        public ConversationNode build() {
            Preconditions.checkNotNull(router);
            Preconditions.checkNotNull(prompt);
            Preconditions.checkNotNull(parameters);
            return new ConversationNode(this);
        }
    }
}
