package me.dags.converse;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class ConversationNode {

    private final Text key;
    private final ConversationPrompt prompt;
    private final ConversationExecutor executor;
    private final ConversationExecutor preprocess;
    private final List<CommandElement> parameters;

    private ConversationNode(Text key, Builder builder) {
        this.key = key;
        this.prompt = builder.prompt;
        this.parameters = builder.parameters;
        this.executor = builder.executor;
        this.preprocess = builder.preprocessor;
    }

    Text getKey() {
        return key;
    }

    List<CommandElement> getParameters() {
        return parameters;
    }

    Text getPrompt(CommandSource source, CommandContext context) {
        return prompt.getPrompt(source, context);
    }

    ConversationResult preprocess(CommandSource source, CommandContext context) {
        return preprocess.execute(source, context);
    }

    ConversationResult process(CommandSource source, CommandArgs commandArgs, CommandContext context) throws ArgumentParseException {
        for (CommandElement element : parameters) {
            element.parse(source, commandArgs, context);
        }
        return executor.execute(source, context);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private ConversationPrompt prompt = ConversationPrompt.EMPTY;
        private List<CommandElement> parameters = new ArrayList<>();
        private ConversationExecutor executor = ConversationExecutor.EMPTY;
        private ConversationExecutor preprocessor = ConversationExecutor.EMPTY;

        /**
         * Generates a prompt message that is sent to the CommandSource instructing them on what input is required from
         * them
         *
         * @param prompt
         * @return
         */
        public Builder prompt(ConversationPrompt prompt) {
            this.prompt = prompt;
            return this;
        }

        // Called before prompting the user for input
        // If return

        /**
         * The preproecessor executor is called immediately before prompting the user for input
         *
         * If the returned ConversationResult is 'finish', then the converse terminates without any further action
         *
         * If the result contains a key for a different ConversationNode, then the current node will not
         * be processed any further and the converse will move onto the new node
         *
         * With any other result, the converse will continue with the current ConversationNode and prompt the user
         * for their input
         *
         * @param preproecessor
         * @return The current builder
         */
        public Builder preprocessor(ConversationExecutor preproecessor) {
            this.preprocessor = preproecessor;
            return this;
        }

        /**
         * The executor that handles input arguments for the current ConversationNode and directs the converse
         * to the next
         *
         * @param executor TODO
         * @return The current builder
         */
        public Builder executor(ConversationExecutor executor) {
            this.executor = executor;
            return this;
        }

        /**
         * The parameters accepted/required by the ConversationNode
         *
         * @param elements
         * @return The current builder
         */
        public Builder parameter(CommandElement... elements) {
            for (CommandElement element : elements) {
                this.parameters.add(element);
            }
            return this;
        }

        public ConversationNode build(String key) {
            return build(Text.of(key));
        }

        public ConversationNode build(Text key) {
            return new ConversationNode(key, this);
        }
    }
}
