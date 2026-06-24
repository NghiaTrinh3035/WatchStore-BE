package com.example.demo.features.orders.handlers;

import com.example.demo.features.orders.commands.Command;

public interface CommandHandler<C extends Command<R>, R> {
    R handle(C command);
}
