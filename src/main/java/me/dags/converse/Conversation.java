package me.dags.converse;

import com.google.common.base.Stopwatch;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.parsing.SingleArg;
import org.spongepowered.api.text.Text;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public final class Conversation {

    private final String identifier;
    private final ConversationSpec spec;
    private final WeakReference<CommandSource> source;
    private final CommandContext context = new CommandContext();
    private final Stopwatch stopwatch = Stopwatch.createStarted();

    private Text next = Text.EMPTY;

    Conversation(ConversationSpec spec, CommandSource source) {
        this.identifier = source.getIdentifier();
        this.source = new WeakReference<>(source);
        this.spec = spec;
    }

    public void process(Text input) {
        CommandSource source = getSource();
        stopwatch.reset().start();

        if (source == null) {
            spec.endConversation(this, ConversationPrompt.EMPTY);
            return;
        }

        if (input.equals(spec.exitKeyword)) {
            spec.endConversation(this, spec.exitMessage);
            return;
        }

        Optional<ConversationNode> node = spec.getNode(next);

        try {
            if (node.isPresent()) {
                String raw = input.toPlain();
                List<SingleArg> args = spec.tokenizer.tokenize(raw, false);
                CommandArgs commandArgs = new CommandArgs(raw, args);

                ConversationResult result = node.get().process(source, commandArgs, context);
                if (result.hasKey()) {
                    next(result);
                }
            }
        } catch (CommandException e) {
            Text message = e.getText();
            if (message != null) {
                source.sendMessage(message);
            }
        }
    }

    String getIdentifier() {
        return identifier;
    }

    CommandSource getSource() {
        return source.get();
    }

    CommandContext getContext() {
        return context;
    }

    void checkExpiration() {
        CommandSource source = getSource();
        if (source == null) {
            spec.endConversation(this, ConversationPrompt.EMPTY);
            return;
        }

        stopwatch.elapsed(spec.timeUnit);

        if (stopwatch.elapsed(spec.timeUnit) >= spec.timeOut) {
            spec.endConversation(this, spec.timeoutMessage);
        }
    }

    void next(ConversationResult result) throws CommandException {
        CommandSource source = getSource();
        if (source == null) {
            return;
        }

        next = result.getKey();

        Optional<ConversationNode> node = spec.getNode(next);
        if (node.isPresent()) {
            ConversationResult pre = node.get().preprocess(source, context);

            if (pre == ConversationResult.finish()) {
                spec.endConversation(this, spec.exitMessage);
                return;
            }

            if (pre.hasKey() && !pre.getKey().equals(next)) {
                next(pre);
                return;
            }

            Text prompt = node.get().getPrompt(source, context);
            source.sendMessage(prompt);

        } else {
            throw new CommandException(Text.of("Missing ConversationNode ", next));
        }
    }
}
