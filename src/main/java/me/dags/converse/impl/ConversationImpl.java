package me.dags.converse.impl;

import com.google.common.base.Stopwatch;
import me.dags.converse.api.Conversation;
import me.dags.converse.api.ConversationNode;
import me.dags.converse.api.ConversationRoute;
import me.dags.converse.api.ConversationSpec;
import me.dags.converse.dummy.Spunge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author dags <dags@dags.me>
 */
final class ConversationImpl implements Conversation {

    private final String identifier;
    private final ConversationSpecImpl spec;
    private final WeakReference<CommandSource> reference;
    private final CommandContext context = new CommandContext();
    private final Stopwatch stopwatch = Stopwatch.createStarted();

    private ConversationNode node = null;

    ConversationImpl(CommandSource source, ConversationSpecImpl spec) {
        this.identifier = source.getIdentifier();
        this.reference = new WeakReference<>(source);
        this.spec = spec;
    }

    @Override
    public void process(String input) throws CommandException {
        if (getSpec().isExitKeyword(input)) {
            spec.onExit(this);
            Spunge.getConversationManager().removeConversation(getIdentifier());
            return;
        }

        Optional<CommandSource> source = getSource();
        if (source.isPresent()) {
            punchIn();
            if (node != null) {
                node.parse(source.get(), input, context);
                node.process(this);
            }
        } else {
            Spunge.getConversationManager().exitConversation(getIdentifier());
        }
    }

    @Override
    public void nextRoute(ConversationRoute next) throws CommandException {
        Optional<CommandSource> commandSource = getSource();
        if (!commandSource.isPresent()) {
            Spunge.getConversationManager().exitConversation(getIdentifier());
            return;
        }

        if (next.isTerminal()) {
            spec.onComplete(this);
            Spunge.getConversationManager().removeConversation(getIdentifier());
            return;
        }

        CommandSource source = commandSource.get();

        if (next.isPresent()) {
            Optional<ConversationNode> nextNode = spec.getNode(next);

            if (nextNode.isPresent()) {
                node = nextNode.get();
                Text prompt = node.getPrompt().apply(source, context);
                source.sendMessage(prompt);
            } else {
                throw new CommandException(Text.of("Reached an unknown node in the conversation: ", next));
            }
        }
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public Optional<CommandSource> getSource() {
        return Optional.ofNullable(reference.get());
    }

    @Override
    public CommandContext getContext() {
        return context;
    }

    @Override
    public ConversationSpec getSpec() {
        return spec;
    }

    @Override
    public long getAge(TimeUnit timeUnit) {
        return stopwatch.elapsed(timeUnit);
    }

    private void punchIn() {
        stopwatch.reset().start();
    }
}
