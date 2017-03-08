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
import org.spongepowered.api.text.Text;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public final class ConversationSpec implements CommandExecutor {

    private final Map<ConversationRoute, ConversationNode> nodes;
    private final Set<String> exitKeywords;
    private final ConversationRoute root;
    private final ConversationManager manager;
    private final Consumer<Conversation> onExit;
    private final Consumer<Conversation> onExpire;
    private final Consumer<Conversation> onComplete;
    private final TimeUnit timeUnit;
    private final long expireTime;
    private final boolean suppressMessages;

    private ConversationSpec(Builder builder) {
        nodes = ImmutableMap.copyOf(builder.children);
        exitKeywords = ImmutableSet.copyOf(builder.exitKeywords);
        root = builder.root;
        manager = builder.manager;
        onExit = builder.onExit;
        onExpire = builder.onExpire;
        onComplete = builder.onComplete;
        expireTime = builder.expireTime;
        timeUnit = builder.timeUnit;
        suppressMessages = builder.suppressMessages;
    }

    Optional<ConversationNode> getNode(ConversationRoute key) {
        return Optional.ofNullable(nodes.get(key));
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        return startConversation(src);
    }

    public CommandResult startConversation(CommandSource source) throws CommandException {
        try {
            Conversation conversation = new Conversation(source, this);
            manager.addConversation(conversation);
            conversation.nextRoute(root);
            return CommandResult.success();
        } catch (ConversationException e) {
            throw new CommandException(e.getText(), false);
        }
    }

    ConversationManager getManager() {
        return manager;
    }

    boolean isExitKeyword(String input) {
        return exitKeywords.contains(input);
    }

    boolean suppressMessages() {
        return suppressMessages;
    }

    boolean hasExpired(Conversation conversation) {
        return conversation.getAge(timeUnit) >= expireTime;
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

    public static class Builder {

        private final ConversationManager manager;

        private Map<ConversationRoute, ConversationNode> children = new HashMap<>();
        private Set<String> exitKeywords = Sets.newHashSet("exit");
        private ConversationRoute root = null;
        private Consumer<Conversation> onExit = event(Text.of("The conversation has ended"));
        private Consumer<Conversation> onExpire = event(Text.of("The conversation has expired"));
        private Consumer<Conversation> onComplete = conversation -> {};
        private TimeUnit timeUnit = TimeUnit.MINUTES;
        private long expireTime = 1L;
        private boolean suppressMessages = true;

        Builder(ConversationManager manager) {
            this.manager = manager;
        }

        public ConversationSpec.Builder nodes(ConversationNode... children) {
            for (ConversationNode node : children) {
                Preconditions.checkNotNull(node);
                this.children.put(node.getRoute(), node);
            }
            return this;
        }

        public ConversationSpec.Builder root(ConversationNode root) {
            Preconditions.checkNotNull(root);
            this.root = root.getRoute();
            this.children.put(root.getRoute(), root);
            return this;
        }

        public ConversationSpec.Builder suppressMessages(boolean hide) {
            this.suppressMessages = hide;
            return this;
        }

        public ConversationSpec.Builder exitAlias(String... aliases) {
            exitKeywords.clear();
            Collections.addAll(exitKeywords, aliases);
            return this;
        }

        public ConversationSpec.Builder onComplete(Consumer<Conversation> consumer) {
            Preconditions.checkNotNull(consumer);
            this.onComplete = consumer;
            return this;
        }

        public ConversationSpec.Builder onExit(Consumer<Conversation> consumer) {
            Preconditions.checkNotNull(consumer);
            this.onExit = consumer;
            return this;
        }

        public ConversationSpec.Builder onExpire(Consumer<Conversation> consumer) {
            Preconditions.checkNotNull(consumer);
            this.onExpire = consumer;
            return this;
        }

        public Builder timeOut(long period, TimeUnit unit) {
            Preconditions.checkNotNull(unit);
            this.expireTime = period;
            this.timeUnit = unit;
            return this;
        }

        public ConversationSpec build() {
            Preconditions.checkNotNull(root, "The root ConversationNode cannot be null!");
            return new ConversationSpec(this);
        }

        private static Consumer<Conversation> event(Text message) {
            return conversation -> conversation.getSource().ifPresent(source -> source.sendMessage(message));
        }
    }
}
