package com.example.demo.features.orders.services;

import com.example.demo.features.orders.commands.Command;
import com.example.demo.features.orders.handlers.CommandHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderCommandInvoker {

    private final Map<Class<?>, CommandHandler> handlers;

    @SuppressWarnings("rawtypes")
    public OrderCommandInvoker(List<CommandHandler> handlerList) {
        this.handlers = handlerList.stream().collect(Collectors.toMap(
                this::getCommandType,
                h -> h
        ));
    }

    @SuppressWarnings("unchecked")
    public <R, C extends Command<R>> R execute(C command) {
        CommandHandler<C, R> handler = handlers.get(command.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("No handler found for command: " + command.getClass().getSimpleName());
        }
        return handler.handle(command);
    }

    private Class<?> getCommandType(CommandHandler<?, ?> handler) {
        Class<?>[] generics = org.springframework.core.GenericTypeResolver.resolveTypeArguments(handler.getClass(), CommandHandler.class);
        if (generics != null && generics.length > 0) {
            return generics[0];
        }
        throw new IllegalStateException("Handler must implement CommandHandler with generic types: " + handler.getClass());
    }
}
