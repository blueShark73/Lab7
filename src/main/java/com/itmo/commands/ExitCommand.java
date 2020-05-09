package com.itmo.commands;

import com.itmo.app.Application;
import com.itmo.server.Session;

/**
 * команда закрытия приложения
 */
public class ExitCommand extends Command {
    /**
     * установим флаг выхода, остальное условности
     */
    @Override
    public String execute(Application application, Session session) {
        application.removeSession(user, session);
        return "Выход из приложения...";
    }

    @Override
    String getCommandInfo() {
        return "exit : завершит программу";
    }

    @Override
    public String toString() {
        return "exit";
    }

    @Override
    public boolean withArgument() {
        return false;
    }
}