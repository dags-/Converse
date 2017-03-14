package me.dags.converse;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Holds all ConversationContexts produced during a Conversation.
 * ConversationContexts are mapped against the route of the ConversationNode that created them.
 * The most recently parsed context can be retrieved via the 'getCurrent' method.
 *
 * A single ConversationNode may be visited multiple times during a conversation with each ConversationContext
 * being stored in order of insertion.
 * The are convenience methods for retrieving the first and last inserted contexts.
 */
public final class ContextCollection {

    private final ArrayListMultimap<String, ConversationContext> contexts = ArrayListMultimap.create();
    private ConversationContext current = new ConversationContext();

    public Collection<ConversationRoute> getRoutes() {
        return contexts.keySet().stream().map(ConversationRoute::goTo).collect(Collectors.toList());
    }

    public ConversationContext getCurrent() {
        return current;
    }

    public boolean hasRoute(Object route) {
        return contexts.containsKey(route.toString());
    }

    public Optional<ConversationContext> getFirstContext(Object route) {
        List<ConversationContext> list = contexts.get(route.toString());
        if (list.size() > 0) {
            return Optional.ofNullable(list.get(0));
        }
        return Optional.empty();
    }

    public Optional<ConversationContext> getLastContext(Object route) {
        List<ConversationContext> list = contexts.get(route.toString());
        if (list.size() > 0) {
            return Optional.ofNullable(list.get(list.size() - 1));
        }
        return Optional.empty();
    }

    public List<ConversationContext> getAllContexts(Object route) {
        return ImmutableList.copyOf(contexts.get(route.toString()));
    }

    public <T> Optional<T> getOne(Object route, String key) {
        return getFirstContext(route).flatMap(routeContext -> routeContext.getFirst(key));
    }

    public <T> Optional<T> getOne(Object route, Text key) {
        return getFirstContext(route).flatMap(routeContext -> routeContext.getFirst(key));
    }

    public <T> Optional<T> getLast(Object route, String key) {
        return getLastContext(route).flatMap(routeContext -> routeContext.getLast(key));
    }

    public <T> Optional<T> getLast(Object route, Text key) {
        return getLastContext(route).flatMap(routeContext -> routeContext.getLast(key));
    }

    public <T> Collection<T> getAll(Object route, String key) {
        return contexts.get(route.toString()).stream().flatMap(context -> context.<T>stream(key)).collect(Collectors.toList());
    }

    public <T> Collection<T> getAll(Object route, Text key) {
        return contexts.get(route.toString()).stream().flatMap(context -> context.<T>stream(key)).collect(Collectors.toList());
    }

    public void putContext(Object route, ConversationContext context) {
        contexts.put(route.toString(), context);
    }

    void setCurrent(ConversationContext current) {
        this.current = current;
    }
}
