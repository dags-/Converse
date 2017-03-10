package me.dags.converse;

import com.google.common.base.Preconditions;

/**
 * Used as the key/identifier for ConversationNodes.
 * When the node's ConversationRouter is called, it returns a ConversationRoute pointing to the next node in the
 * conversation (or terminating the conversation if ConversationNode.end() or ConversationNode.exit() is returned).
 *
 * ConversationRoutes are used to control the flow of the conversation between nodes. The routes must only refer
 * to ConversationNodes registered under the same ConversationSpec. An exception is thrown if the conversation is
 * directed to a ConversationNode that does not exist.
 *
 * ConversationRoute.end() represents the completion of a conversation. No further ConversationNodes are called and
 * the parent ConversationSpec's 'onComplete' callback is called.
 *
 * ConversationRoute.exit() represents any other situation where the conversation should end immediately. No further
 * ConversationNodes are called and the parent ConversationSpec's 'onExit' callback is called.
 */
public final class ConversationRoute {

    private static final ConversationRoute END = new ConversationRoute("END");
    private static final ConversationRoute EXIT = new ConversationRoute("EXIT");

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
        Preconditions.checkNotNull(key);
        Preconditions.checkState(!key.isEmpty());
        return new ConversationRoute(key.toLowerCase());
    }

    public static ConversationRoute end() {
        return END;
    }

    public static ConversationRoute exit() {
        return EXIT;
    }
}
