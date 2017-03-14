# Converse

### Example:
```Java
ConversationNode name = ConversationNode.route("name_node")
        .parameters(GenericArguments.remainingJoinedStrings(Text.of("name")))
        .prompt(Text.of("Whats your name bro?"))
        .inputTemplate(TextTemplate.of("> Name:", TextTemplate.arg("name")))
        .router(ConversationRoute.goTo("age_node"))
        .build();

ConversationNode age = ConversationNode.route("age_node")
        .parameters(GenericArguments.integer(Text.of("age")))
        .prompt((src, contexts) -> Text.of(
                "How old are you, ",
                contexts.getLast("name_node", "name").orElse("bruv"),
                "?")
        )
        .router(ConversationRoute.goTo("location_node"))
        .build();

ConversationNode location = ConversationNode.route("location_node")
        .parameters(GenericArguments.remainingJoinedStrings(Text.of("location")))
        .prompt(Text.of("Where you at?"))
        .router(ConversationRoute.end())
        .suppressInput()
        .build();

ConversationSpec spec = ConversationSpec.builder()
        .timeOut(30, TimeUnit.SECONDS)
        .nodes(name, age, location)
        .first(name)
        .onComplete(conversation -> {
            conversation.getSource().ifPresent(source -> {
                Text result = Text.of("Details: ",
                    "name=", conversation.getContext().getLast("name_node", "name").orElse("error"),
                    ", age=", conversation.getContext().getLast("age_node", "age").orElse(0),
                    ", location=", conversation.getContext().getLast("location_node", "location").orElse("nowhere")
                );
                source.sendMessage(result);
            });
        })
        .build();

Sponge.getCommandManager().register(this, spec.toCommand().permission("command.deets.use").build(), "deets");
```
