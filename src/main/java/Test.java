import me.dags.converse.api.ConversationNode;
import me.dags.converse.api.ConversationRoute;
import me.dags.converse.api.ConversationSpec;
import me.dags.converse.dummy.Spunge;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.util.concurrent.TimeUnit;

/**
 * @author dags <dags@dags.me>
 */
@Plugin(id = "test", name = "Test", version = "0.1")
public class Test {

    @Listener
    public void init(GameInitializationEvent event) {
        Spunge.init(this);

        ConversationNode name = ConversationNode.builder("name")
                .executor((source, context) -> ConversationRoute.next("age"))
                .prompt((source, context) -> Text.of("What is your name?"))
                .parameter(GenericArguments.remainingJoinedStrings(Text.of("name")))
                .build();

        ConversationNode age = ConversationNode.builder("age")
                .executor((source, context) -> ConversationRoute.next("nowhere"))
                .prompt((source, context) -> Text.of("What is your age?"))
                .parameter(GenericArguments.integer(Text.of("age")))
                .build();

        ConversationNode location = ConversationNode.builder("location")
                .executor((source, context) -> ConversationRoute.complete())
                .prompt((source, context) -> Text.of("Where do you live?"))
                .parameter(GenericArguments.remainingJoinedStrings(Text.of("location")))
                .build();

        ConversationSpec details = ConversationSpec.builder()
                .onComplete(conversation -> conversation.getSource().ifPresent(source -> {
                    CommandContext context = conversation.getContext();
                    source.sendMessage(Text.of("Finished!"));
                    context.getOne("name").ifPresent(System.out::println);
                    context.getOne("age").ifPresent(System.out::println);
                    context.getOne("location").ifPresent(System.out::println);
                }))
                .timeOut(30, TimeUnit.SECONDS)
                .nodes(name, age, location)
                .root(name)
                .build();

        CommandSpec conv = CommandSpec.builder()
                .executor(details)
                .build();

        CommandSpec chat = CommandSpec.builder()
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("args")))
                .executor((src, args) -> {
                    Spunge.getConversationManager().getConversation(src.getIdentifier()).ifPresent(conversation -> {
                        conversation.processSafely(args.<String>getOne("args").orElse(""));
                    });
                    return CommandResult.success();
                }).build();

        Sponge.getCommandManager().register(this, conv, "conv");
        Sponge.getCommandManager().register(this, chat, "chat");
    }
}
