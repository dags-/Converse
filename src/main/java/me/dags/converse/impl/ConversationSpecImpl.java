package me.dags.converse.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import me.dags.converse.api.Conversation;
import me.dags.converse.api.ConversationNode;
import me.dags.converse.api.ConversationRoute;
import me.dags.converse.api.ConversationSpec;
import me.dags.converse.dummy.Spunge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
final class ConversationSpecImpl implements ConversationSpec {

    private final Map<ConversationRoute, ConversationNode> nodes;
    private final Set<String> exitKeywords;
    private final ConversationRoute root;
    private final Consumer<Conversation> onExit;
    private final Consumer<Conversation> onExpire;
    private final Consumer<Conversation> onComplete;
    private final TimeUnit timeUnit;
    private final long expireTime;
    private final boolean hideMessages;

    private ConversationSpecImpl(Builder builder) {
        nodes = ImmutableMap.copyOf(builder.children);
        exitKeywords = ImmutableSet.copyOf(builder.exitKeywords);
        root = builder.root;
        onExit = builder.onExit;
        onExpire = builder.onExpire;
        onComplete = builder.onComplete;
        expireTime = builder.expireTime;
        timeUnit = builder.timeUnit;
        hideMessages = builder.hideMessages;
    }

    Optional<ConversationNode> getNode(ConversationRoute key) {
        return Optional.ofNullable(nodes.get(key));
    }

    @Override
    public CommandResult startConversation(CommandSource source) throws CommandException {
        ConversationImpl conversation = new ConversationImpl(source, this);
        Spunge.getConversationManager().addConversation(conversation);
        conversation.nextRoute(root);
        return CommandResult.success();
    }

    @Override
    public boolean isExitKeyword(String input) {
        return exitKeywords.contains(input);
    }

    @Override
    public boolean hidesMessages() {
        return hideMessages;
    }

    @Override
    public void onExit(Conversation conversation) {
        onExit.accept(conversation);
    }

    @Override
    public void onExpire(Conversation conversation) {
        onExpire.accept(conversation);
    }

    @Override
    public void onComplete(Conversation conversation) {
        onComplete.accept(conversation);
    }

    @Override
    public boolean hasExpired(Conversation conversation) {
        return conversation.getAge(timeUnit) >= expireTime;
    }

    static class Builder implements ConversationSpec.Builder {

        private Map<ConversationRoute, ConversationNode> children = new HashMap<>();
        private Set<String> exitKeywords = Sets.newHashSet("exit");
        private ConversationRoute root = ConversationRoute.EMPTY;
        private Consumer<Conversation> onExit = event(Text.of("The conversation has ended"));
        private Consumer<Conversation> onExpire = event(Text.of("The conversation has expired"));
        private Consumer<Conversation> onComplete = conversation -> {};
        private TimeUnit timeUnit = TimeUnit.MINUTES;
        private long expireTime = 1L;
        private boolean hideMessages = true;

        @Override
        public ConversationSpec.Builder nodes(ConversationNode... children) {
            for (ConversationNode node : children) {
                Preconditions.checkNotNull(node);
                this.children.put(node.getRoute(), node);
            }
            return this;
        }

        @Override
        public ConversationSpec.Builder root(ConversationNode root) {
            Preconditions.checkNotNull(root);
            this.root = root.getRoute();
            this.children.put(root.getRoute(), root);
            return this;
        }

        @Override
        public ConversationSpec.Builder hideMessages(boolean hide) {
            this.hideMessages = hide;
            return this;
        }

        @Override
        public ConversationSpec.Builder exitAlias(String... aliases) {
            exitKeywords.clear();
            Collections.addAll(exitKeywords, aliases);
            return this;
        }

        @Override
        public ConversationSpec.Builder onComplete(Consumer<Conversation> consumer) {
            Preconditions.checkNotNull(consumer);
            this.onComplete = consumer;
            return this;
        }

        @Override
        public ConversationSpec.Builder onExit(Consumer<Conversation> consumer) {
            Preconditions.checkNotNull(consumer);
            this.onExit = consumer;
            return this;
        }

        @Override
        public ConversationSpec.Builder onExpire(Consumer<Conversation> consumer) {
            Preconditions.checkNotNull(consumer);
            this.onExpire = consumer;
            return this;
        }

        @Override
        public Builder timeOut(long period, TimeUnit unit) {
            Preconditions.checkNotNull(unit);
            this.expireTime = period;
            this.timeUnit = unit;
            return this;
        }

        @Override
        public ConversationSpecImpl build() {
            return new ConversationSpecImpl(this);
        }

        private static Consumer<Conversation> event(Text message) {
            return conversation -> conversation.getSource().ifPresent(source -> source.sendMessage(message));
        }
    }
}
