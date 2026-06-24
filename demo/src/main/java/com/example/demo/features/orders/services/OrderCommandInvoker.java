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
        // Automatically wire all beans implementing CommandHandler
        // Since Java type erasure prevents us from getting the exact C type easily without Reflection,
        // we can use a simpler approach or Reflection to find the generic type.
        // For simplicity, we can let handlers register themselves or use Spring's generics resolution.
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
        // Use reflection to find the generic parameter C of CommandHandler<C, R>
        java.lang.reflect.Type[] interfaces = handler.getClass().getGenericInterfaces();
        for (java.lang.reflect.Type type : interfaces) {
            if (type instanceof java.lang.reflect.ParameterizedType) {
                java.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) type;
                if (pt.getRawType().equals(CommandHandler.class)) {
                    return (Class<?>) pt.getActualTypeArguments()[0];
                }
            }
        }
        throw new IllegalStateException("Handler must implement CommandHandler with generic types: " + handler.getClass());
    }
}
