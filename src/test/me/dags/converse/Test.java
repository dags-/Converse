package me.dags.converse;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author dags <dags@dags.me>
 */
@Plugin(id = "test", name = "Test", version = "0.1")
public class Test {

    @Listener
    public void init(GamePostInitializationEvent event) {
        ConversationNode name = ConversationNode.builder()
                .prompt((source, context) -> Text.of("What's your name?"))
                .parameter(GenericArguments.remainingJoinedStrings(Text.of("name")))
                .executor((source, args) -> ConversationResult.of(Text.of("age")))
                .build("name");

        ConversationNode age = ConversationNode.builder()
                .prompt((source, context) -> {
                    String username = context.<String>getOne("name").orElse("buddy");
                    return Text.of(String.format("How old are you, %s?", username));
                })
                .parameter(GenericArguments.integer(Text.of("age")))
                .executor((source, args) -> ConversationResult.of(Text.of("location")))
                .build("age");

        ConversationNode location = ConversationNode.builder()
                .prompt((source, context) -> Text.of("Where do you live?"))
                .parameter(GenericArguments.remainingJoinedStrings(Text.of("location")))
                .executor((source, args) -> ConversationResult.of("end"))
                .build("location");

        ConversationNode end = ConversationNode.builder()
                .preprocessor((source, args) -> {
                    Optional<String> nameOp = args.getOne("name");
                    Optional<Integer> ageOp = args.getOne("age");
                    Optional<String> locationOp = args.getOne("location");

                    if (!nameOp.isPresent()) {
                        return ConversationResult.of("name");
                    }
                    if (!ageOp.isPresent()) {
                        return ConversationResult.of("age");
                    }
                    if (!locationOp.isPresent()) {
                        return ConversationResult.of("location");
                    }

                    Text message = Text.of("name=", nameOp.get(), ", age=", ageOp.get(), ", location=", locationOp.get());
                    source.sendMessage(message);

                    return ConversationResult.finish();
                }).build("end");

        ConversationSpec conversation = ConversationSpec.builder()
                .first(name)
                .child(name, age, location, end)
                .timeout(15L, TimeUnit.SECONDS)
                .build(this);

        CommandSpec command = CommandSpec.builder().executor(conversation).build();
        Sponge.getCommandManager().register(this, command, "convo");
    }
}
