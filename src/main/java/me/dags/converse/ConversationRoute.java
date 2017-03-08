package me.dags.converse;

/**
 * @author dags <dags@dags.me>
 */
public final class ConversationRoute {

    private static final ConversationRoute END = new ConversationRoute("");
    private static final ConversationRoute EXIT = new ConversationRoute("");

    private final String key;
    private final int hash;

    private ConversationRoute(String key) {
        this.key = key;
        this.hash = key.hashCode();
    }

    boolean isExit() {
        return this == EXIT;
    }

    boolean isTerminal() {
        return this == END;
    }

    @Override
    public boolean equals(Object other) {
        return other != null && other.getClass() == this.getClass() && other.toString().equals(this.toString());
    }

    @Override
    public int hashCode() {
        return hash;
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

    public static ConversationRoute exit() {
        return EXIT;
    }
}
