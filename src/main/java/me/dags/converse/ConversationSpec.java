package me.dags.converse;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.parsing.InputTokenizer;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author dags <dags@dags.me>
 */
public class ConversationSpec implements CommandExecutor {

    private final Map<Text, ConversationNode> nodes;
    private final MessageListener listener;
    private final Text first;

    final Map<String, Conversation> conversations = new ConcurrentHashMap<>();
    final ConversationPrompt timeoutMessage;
    final ConversationPrompt exitMessage;
    final InputTokenizer tokenizer;
    final Text exitKeyword;
    final TimeUnit timeUnit;
    final Object plugin;
    final long timeOut;

    private ConversationSpec(Object plugin, Builder builder) {
        this.nodes = ImmutableMap.copyOf(builder.nodes);
        this.timeoutMessage = builder.timeoutMessage;
        this.listener = new MessageListener(this);
        this.exitMessage = builder.exitMessage;
        this.exitKeyword = builder.exitKeyword;
        this.tokenizer = builder.tokenizer;
        this.timeUnit = builder.timeUnit;
        this.timeOut = builder.timeout;
        this.first = builder.first;
        this.plugin = plugin;

        Sponge.getEventManager().registerListeners(plugin, listener);

        Task.builder()
                .execute(() -> conversations.values().forEach(Conversation::checkExpiration))
                .interval(15, TimeUnit.SECONDS)
                .delay(10, TimeUnit.SECONDS)
                .async()
                .submit(plugin);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        startConversation(src, first);
        return CommandResult.success();
    }

    public void startConversation(CommandSource source, Text first) throws CommandException {
        Conversation conversation = new Conversation(this, source);
        conversations.put(source.getIdentifier(), conversation);
        conversation.next(ConversationResult.of(first));
    }

    void endConversation(Conversation conversation, ConversationPrompt prompt) {
        conversations.remove(conversation.getIdentifier());

        if (prompt == ConversationPrompt.EMPTY) {
            return;
        }

        Task.builder().execute(() -> {
            CommandSource source = conversation.getSource();
            if (source != null) {
                Text message = prompt.getPrompt(source, conversation.getContext());
                source.sendMessage(message);
            }
        }).submit(plugin);
    }

    Optional<ConversationNode> getNode(Text key) {
        return Optional.ofNullable(nodes.get(key));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Map<Text, Text> registeredArgs = new HashMap<>();
        private final Map<Text, ConversationNode> nodes = new HashMap<>();
        private ConversationPrompt timeoutMessage = (src, args) -> Text.of("The converse timed out");
        private ConversationPrompt exitMessage = (src, args) -> Text.of("The converse has ended");
        private InputTokenizer tokenizer = InputTokenizer.quotedStrings(false);
        private Text exitKeyword = Text.of("exit");
        private Text first = Text.EMPTY;
        private long timeout = 5L;
        private TimeUnit timeUnit = TimeUnit.MINUTES;

        public Builder first(Text first) {
            this.first = first;
            return this;
        }

        public Builder first(ConversationNode first) {
            return child(first).first(first.getKey());
        }

        public Builder exitMessage(ConversationPrompt exitMessage) {
            this.exitMessage = exitMessage;
            return this;
        }

        public Builder timeoutMessage(ConversationPrompt timeoutMessage) {
            this.timeoutMessage = timeoutMessage;
            return this;
        }

        public Builder timeout(long time, TimeUnit unit) {
            this.timeout = time;
            this.timeUnit = unit;
            return this;
        }

        public Builder child(ConversationNode... nodes) {
            for (ConversationNode node : nodes) {
                verify(node);
                this.nodes.put(node.getKey(), node);
            }
            return this;
        }

        public ConversationSpec build(Object plugin) {
            Preconditions.checkNotNull(plugin);

            if (!nodes.containsKey(first)) {
                String err = String.format("Missing ConversationNode for Key: %s", first.toPlain());
                throw new UnsupportedOperationException(err);
            }

            return new ConversationSpec(plugin, this);
        }

        private void verify(ConversationNode node) {
            for (CommandElement element : node.getParameters()) {
                Text name = element.getKey();
                Text owner = registeredArgs.get(name);
                if (owner == null) {
                    registeredArgs.put(name, node.getKey());
                } else if(!owner.equals(node.getKey())) {
                    String current = owner.toPlain();
                    String other = node.getKey().toPlain();
                    String err = String.format("Duplicate parameter names found in ConversationNode %s & %s", current, other);
                    throw new UnsupportedOperationException(err);
                }
            }
        }
    }
}
