package com.itmo.commands;

import com.itmo.app.Application;
import com.itmo.server.Session;

/**
 * выводит последние 9 команд
 */
public class HistoryCommand extends Command {
    @Override
    public String execute(Application application, Session session) {
        return session.getHistory().getHistory();
    }

    @Override
    String getCommandInfo() {
        return "history : выведет последние 9 команд (без их аргументов)";
    }

    @Override
    public String toString() {
        return "history";
    }

    @Override
    public boolean withArgument() {
        return false;
    }
}
