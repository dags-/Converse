package me.dags.converse;

import com.google.common.base.Stopwatch;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Contains all information relevant to a single instance of a Conversation.
 * The CommandSource involved is weakly referenced.
 * The time between user inputs is monitored for the purposes of expiring stale Conversations.
 *
 * The Conversation will end without further processing if:
 *  - the CommandSource has been garbage collected
 *  - the Conversation has been assigned the 'exit' or 'end' ConversationRoute
 *  - the Conversation has expired
 *
 *  If Conversation is assigned a ConversationRoute that does not exist, a ConversationException will be thrown.
 *  This will typically result in the Conversation being exited & disposed-of unless a third party is handling
 *  the exception.
 */
public final class Conversation {

    private final String identifier;
    private final ConversationSpec spec;
    private final WeakReference<CommandSource> reference;
    private final ContextCollection context = new ContextCollection();
    private final Stopwatch stopwatch = Stopwatch.createStarted();

    private ConversationNode node = null;

    Conversation(CommandSource source, ConversationSpec spec) {
        this.identifier = source.getIdentifier();
        this.reference = new WeakReference<>(source);
        this.spec = spec;
    }

    public List<String> complete(String input) throws ArgumentParseException {
        Optional<CommandSource> source = getSource();
        if (source.isPresent()) {
            punchIn();
            if (node != null) {
                return node.complete(source.get(), input);
            }
        } else {
            spec.onExit(this);
            Converse.getConversationManager().removeConversation(this);
        }
        return Collections.emptyList();
    }

    public void process(String input) throws ConversationException, ArgumentParseException {
        if (getSpec().isExitKeyword(input)) {
            spec.onExit(this);
            Converse.getConversationManager().removeConversation(this);
            return;
        }

        Optional<CommandSource> source = getSource();
        if (source.isPresent()) {
            punchIn();
            if (node != null) {
                // parse the input string
                node.parse(source.get(), input, context);

                // feed back the input to the CommandSource
                Optional<TextTemplate> template = node.getInputTemplate();
                if (template.isPresent()) {
                    Map<String, Object> args = getContext().getCurrent().toMap();
                    args.put("raw_input", input);
                    Text message = template.get().apply(args).build();
                    source.get().sendMessage(message);
                }

                // process the input data
                node.process(this);
            }
        } else {
            spec.onExit(this);
            Converse.getConversationManager().removeConversation(this);
        }
    }

    public List<String> completeSafely(String input) {
        try {
            return complete(input);
        } catch (ArgumentParseException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
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
            Converse.getConversationManager().removeConversation(this);
        }
    }

    void nextRoute(ConversationRoute next) throws ConversationException {
        Optional<CommandSource> commandSource = getSource();
        if (!commandSource.isPresent()) {
            spec.onExit(this);
            Converse.getConversationManager().removeConversation(this);
            return;
        }

        if (next.isExit()) {
            spec.onExit(this);
            Converse.getConversationManager().removeConversation(this);
            return;
        }

        if (next.isTerminal()) {
            spec.onComplete(this);
            Converse.getConversationManager().removeConversation(this);
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

    public ContextCollection getContext() {
        return context;
    }

    ConversationSpec getSpec() {
        return spec;
    }

    boolean hasExpired() {
        return spec.getExpireTime() > 0 && stopwatch.elapsed(spec.getTimeUnit()) >= spec.getExpireTime();
    }

    boolean suppressMessages() {
        return spec.suppressMessages();
    }

    private void punchIn() {
        stopwatch.reset().start();
    }
}
