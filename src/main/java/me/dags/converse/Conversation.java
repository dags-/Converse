package me.dags.converse;

import com.google.common.base.Stopwatch;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.text.Text;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author dags <dags@dags.me>
 */
public final class Conversation {

    private final String identifier;
    private final ConversationSpec spec;
    private final WeakReference<CommandSource> reference;
    private final ConversationContext context = new ConversationContext();
    private final Stopwatch stopwatch = Stopwatch.createStarted();

    private ConversationNode node = null;

    Conversation(CommandSource source, ConversationSpec spec) {
        this.identifier = source.getIdentifier();
        this.reference = new WeakReference<>(source);
        this.spec = spec;
    }

    public void processSafely(String input) {
        try {
            process(input);
        } catch (ArgumentParseException e) {
            if (e.getText() != null) {
                getSource().ifPresent(source -> source.sendMessage(e.getText()));
            }
        } catch (ConversationException e) {
            e.printStackTrace();
            spec.onExit(this);
            spec.getManager().removeConversation(this);
        }
    }

    public void process(String input) throws ConversationException, ArgumentParseException {
        if (getSpec().isExitKeyword(input)) {
            spec.onExit(this);
            spec.getManager().removeConversation(this);
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
            spec.onExit(this);
            spec.getManager().removeConversation(this);
        }
    }

    void nextRoute(ConversationRoute next) throws ConversationException {
        Optional<CommandSource> commandSource = getSource();
        if (!commandSource.isPresent()) {
            spec.onExit(this);
            spec.getManager().removeConversation(this);
            return;
        }

        if (next.isExit()) {
            spec.onExit(this);
            spec.getManager().removeConversation(this);
            return;
        }

        if (next.isTerminal()) {
            spec.onComplete(this);
            spec.getManager().removeConversation(this);
            return;
        }

        CommandSource source = commandSource.get();
        Optional<ConversationNode> nextNode = spec.getNode(next);

        if (nextNode.isPresent()) {
            node = nextNode.get();
            Text prompt = node.getPrompt().apply(source, context);
            source.sendMessage(prompt);
        } else {
            throw new ConversationException(Text.of("Reached an unknown node in the conversation: ", next));
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    public Optional<CommandSource> getSource() {
        return Optional.ofNullable(reference.get());
    }

    public ConversationContext getContext() {
        return context;
    }

    ConversationSpec getSpec() {
        return spec;
    }

    long getAge(TimeUnit timeUnit) {
        return stopwatch.elapsed(timeUnit);
    }

    boolean hasExpired() {
        return spec.hasExpired(this);
    }

    boolean suppressMessages() {
        return spec.suppressMessages();
    }

    private void punchIn() {
        stopwatch.reset().start();
    }
}
