package me.dags.converse;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.TextMessageException;

import javax.annotation.Nonnull;

/**
 * @author dags <dags@dags.me>
 */
public class ConversationException extends TextMessageException {

    ConversationException(Text message) {
        super(message);
    }

    @Nonnull
    @Override
    public Text getText() {
        return super.getText() == null ? Text.EMPTY : super.getText();
    }
}
