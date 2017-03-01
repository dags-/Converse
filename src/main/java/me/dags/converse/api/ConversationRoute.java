package me.dags.converse.api;

/**
 * @author dags <dags@dags.me>
 */
public final class ConversationRoute {

    public static final ConversationRoute EMPTY = new ConversationRoute("");
    private static final ConversationRoute COMPLETE = new ConversationRoute("");

    private final String key;

    private ConversationRoute(String key) {
        this.key = key;
    }

    public boolean isPresent() {
        return this != EMPTY && this != COMPLETE;
    }

    public boolean isTerminal() {
        return this == COMPLETE;
    }

    @Override
    public boolean equals(Object other) {
        return other != null && other.getClass() == this.getClass() && other.hashCode() == this.hashCode();
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return key;
    }

    public static ConversationRoute next(String key) {
        return new ConversationRoute(key);
    }

    public static ConversationRoute complete() {
        return COMPLETE;
    }
}
