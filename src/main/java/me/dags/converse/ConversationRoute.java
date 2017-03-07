package me.dags.converse;

/**
 * @author dags <dags@dags.me>
 */
public final class ConversationRoute {

    private static final ConversationRoute END = new ConversationRoute("");

    private final String key;

    private ConversationRoute(String key) {
        this.key = key;
    }

    public boolean isTerminal() {
        return this == END;
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

    public static ConversationRoute goTo(String key) {
        return new ConversationRoute(key);
    }

    public static ConversationRoute end() {
        return END;
    }
}
