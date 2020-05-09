package com.itmo.commands;

import com.itmo.app.Application;
import com.itmo.server.Session;

//вряд ли нужна в 7 лабе
/**
 * команда записывает коллекцию в файл в формате xml
 */
public class SaveCommand extends Command {
    /**
     * запись в файл
     */
    @Override
    public String execute(Application application, Session session) {
        //Collection collectionClass = new Collection();
        //collectionClass.set(application.getCollection());
        //if (FileWorker.saveCollection(collectionClass, "INPUT_PATH", "input.xml")) return "Коллекция сохранена в файл";
        return "Проблемы с переменной окружения и файлом по умолчанию на сервере, коллекция не сохранена...";
    }

    @Override
    String getCommandInfo() {
        return "save : сохранит коллекцию в файл";
    }

    @Override
    public String toString() {
        return "save";
    }

    @Override
    public boolean withArgument() {
        return false;
    }
}