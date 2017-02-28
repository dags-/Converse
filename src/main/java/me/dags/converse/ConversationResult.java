package me.dags.converse;

import org.spongepowered.api.text.Text;

/**
 * @author dags <dags@dags.me>
 */
public class ConversationResult {

    private static final ConversationResult error = new ConversationResult(Text.EMPTY);
    private static final ConversationResult empty = new ConversationResult(Text.EMPTY);
    private static final ConversationResult finish = new ConversationResult(Text.EMPTY);

    private final Text key;

    private ConversationResult(Text key) {
        this.key = key;
    }

    boolean hasKey() {
        return key != Text.EMPTY;
    }

    Text getKey() {
        return key;
    }

    public static ConversationResult of(String key) {
        return of(Text.of(key));
    }

    public static ConversationResult of(Text key) {
        return new ConversationResult(key);
    }

    public static ConversationResult error() {
        return error;
    }

    public static ConversationResult empty() {
        return error;
    }


    public static ConversationResult finish() {
        return finish;
    }
}
