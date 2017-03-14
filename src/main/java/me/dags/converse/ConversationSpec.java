package me.dags.converse;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * The ConversationSpec is used to generate new Conversation instances.
 * ConversationSpec implements CommandExecutor so that it can easily be integrated with Sponge's command API.
 */
public final class ConversationSpec implements CommandExecutor {

    private final Map<ConversationRoute, ConversationNode> nodes;
    private final Set<String> exitKeywords;
    private final ConversationRoute first;
    private final Consumer<Conversation> onExit;
    private final Consumer<Conversation> onExpire;
    private final Consumer<Conversation> onComplete;
    private final TimeUnit timeUnit;
    private final long expireTime;
    private final boolean suppressMessages;

    private ConversationSpec(Builder builder) {
        nodes = ImmutableMap.copyOf(builder.children);
        exitKeywords = ImmutableSet.copyOf(builder.exitKeywords);
        first = builder.first;
        onExit = builder.onExit;
        onExpire = builder.onExpire;
        onComplete = builder.onComplete;
        expireTime = builder.expireTime;
        timeUnit = builder.timeUnit;
        suppressMessages = builder.suppressMessages;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        return startConversation(src);
    }

    public CommandResult startConversation(CommandSource source) throws CommandException {
        try {
            Conversation conversation = new Conversation(source, this);
            Converse.getConversationManager().addConversation(conversation);
            conversation.nextRoute(first);
            return CommandResult.success();
        } catch (ConversationException e) {
            throw new CommandException(e.getText(), false);
        }
    }

    public CommandSpec.Builder toCommand() {
        return CommandSpec.builder().executor(this);
    }

    Optional<ConversationNode> getNode(ConversationRoute key) {
        return Optional.ofNullable(nodes.get(key));
    }

    boolean isExitKeyword(String input) {
        return exitKeywords.contains(input);
    }

    boolean suppressMessages() {
        return suppressMessages;
    }

    long getExpireTime() {
        return expireTime;
    }

    TimeUnit getTimeUnit() {
        return timeUnit;
    }

    void onExit(Conversation conversation) {
        onExit.accept(conversation);
    }

    void onExpire(Conversation conversation) {
        onExpire.accept(conversation);
    }

    void onComplete(Conversation conversation) {
        onComplete.accept(conversation);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Map<ConversationRoute, ConversationNode> children = new HashMap<>();
        private Set<String> exitKeywords = Sets.newHashSet("exit");
        private ConversationRoute first = null;
        private Consumer<Conversation> onExit = event(Text.of("The conversation has ended"));
        private Consumer<Conversation> onExpire = event(Text.of("The conversation has expired"));
        private Consumer<Conversation> onComplete = conversation -> {};
        private TimeUnit timeUnit = TimeUnit.MINUTES;
        private long expireTime = 1L;
        private boolean suppressMessages = true;

        private Builder() {}

        /**
         * Register the given ConversationNodes with the spec
         * @param children The ConversationNodes to be registered
         * @return The current Builder
         */
        public ConversationSpec.Builder nodes(ConversationNode... children) {
            for (ConversationNode node : children) {
                Preconditions.checkNotNull(node);
                this.children.put(node.getRoute(), node);
            }
            return this;
        }

        /**
         * Define the first ConversationNode that new Conversations should be routed to on creation
         * @param first The first ConversationNode in new conversations
         * @return The current Builder
         */
        public ConversationSpec.Builder first(ConversationNode first) {
            Preconditions.checkNotNull(first);
            this.first = first.getRoute();
            this.children.put(first.getRoute(), first);
            return this;
        }

        /**
         * Specify whether Chat messages sent to the CommandSource should be suppressed/muted during the conversation.
         * By default, this value is set true.
         * @param suppressMessage
         * @return The current Builder
         */
        public ConversationSpec.Builder suppressMessages(boolean suppressMessage) {
            this.suppressMessages = suppressMessage;
            return this;
        }

        /**
         * Specify keywords that can be used to exit the conversation at any time.
         * By default, 'exit' is used
         * @param aliases The aliases used to exit the conversation
         * @return The current builder
         */
        public ConversationSpec.Builder exitAliases(String... aliases) {
            exitKeywords.clear();
            Collections.addAll(exitKeywords, aliases);
            return this;
        }

        /**
         * Specify a consumer for the Conversation that is called when the conversation is completed
         * @param consumer The consumer of the Conversation
         * @return The current builder
         */
        public ConversationSpec.Builder onComplete(Consumer<Conversation> consumer) {
            Preconditions.checkNotNull(consumer);
            this.onComplete = consumer;
            return this;
        }

        /**
         * Specify a consumer for the Conversation that is called when the conversation is exited early
         * @param consumer The consumer for the Conversation
         * @return The current builder
         */
        public ConversationSpec.Builder onExit(Consumer<Conversation> consumer) {
            Preconditions.checkNotNull(consumer);
            this.onExit = consumer;
            return this;
        }

        /**
         * Specify a consumer for the Conversation that is called when the conversation has expired and is disposed
         * @param consumer The consumer for the Conversation
         * @return The current builder
         */
        public ConversationSpec.Builder onExpire(Consumer<Conversation> consumer) {
            Preconditions.checkNotNull(consumer);
            this.onExpire = consumer;
            return this;
        }

        /**
         * Specify how long the Conversation wait for input before it expires and is disposed.
         * The default is 1 minute
         * @param period The time time period to wait
         * @param unit The unit of time to use for the given period
         * @return The current Builder
         */
        public Builder timeOut(long period, TimeUnit unit) {
            Preconditions.checkNotNull(unit);
            this.expireTime = period;
            this.timeUnit = unit;
            return this;
        }

        /**
         * Build the new ConversationSpec. The 'first' node must be specified
         * @return The newly create ConversationSpec
         */
        public ConversationSpec build() {
            Preconditions.checkNotNull(first, "The root ConversationNode cannot be null!");
            return new ConversationSpec(this);
        }

        private static Consumer<Conversation> event(Text message) {
            return conversation -> conversation.getSource().ifPresent(source -> source.sendMessage(message));
        }
    }
}
