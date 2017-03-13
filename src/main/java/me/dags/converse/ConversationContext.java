package me.dags.converse;

import com.google.common.collect.ArrayListMultimap;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TranslatableText;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Similar to a CommandContext, the ConversationContext holds the arguments parsed from a CommandSource's input.
 */
public final class ConversationContext {

    private final ArrayListMultimap<String, Object> values;

    public ConversationContext() {
        values = ArrayListMultimap.create();
    }

    public boolean hasAny(String key) {
        return values.containsKey(key);
    }

    public <T> Optional<T> getFirst(Text key) {
        return getFirst(textToArgKey(key));
    }

    public <T> Optional<T> getFirst(String key) {
        List<Object> list = values.get(key);
        if (list.size() > 0) {
            return Optional.ofNullable(cast(list.get(0)));
        }
        return Optional.empty();
    }

    public <T> Optional<T> getLast(Text key) {
        return getLast(textToArgKey(key));
    }

    public <T> Optional<T> getLast(String key) {
        List<Object> list = values.get(key);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(cast(list.get(list.size() - 1)));
    }

    public <T> Collection<T> getAll(String key) {
        return Collections.unmodifiableCollection(cast(values.get(key)));
    }

    public <T> Collection<T> getAll(Text key) {
        return getAll(textToArgKey(key));
    }

    public void putArg(String key, Object value) {
        checkNotNull(value, "value");
        values.put(key, value);
    }

    public void putArg(Text key, Object value) {
        putArg(textToArgKey(key), value);
    }

    <T> Stream<T> stream(Text key) {
        return values.get(textToArgKey(key)).stream().map(ConversationContext::cast);
    }

    <T> Stream<T> stream(String key) {
        return values.get(key).stream().map(ConversationContext::cast);
    }

    void putAll(String key, Iterable<Object> value) {
        checkNotNull(value, "value");
        values.putAll(key, value);
    }

    void putAll(Text key, Iterable<Object> value) {
        putAll(textToArgKey(key), value);
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object in) {
        return (T) in;
    }

    private static String textToArgKey(@Nullable Text key) {
        if (key == null) {
            return null;
        }

        if (key instanceof TranslatableText) { // Use translation key
            return ((TranslatableText) key).getTranslation().getId();
        }

        return key.toPlain();
    }
}
