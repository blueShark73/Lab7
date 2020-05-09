package com.itmo.commands;

import com.itmo.app.Application;
import com.itmo.server.Session;

/**
 * команда очищает коллекцию
 */
public class ClearCommand extends Command {
    @Override
    public String execute(Application application, Session session) {
        int result = application.getDataBaseManager().removeAll(session.getUser());
        if(result>0){
            application.getCollection().forEach(studyGroup -> {
                if(studyGroup.getOwner().equals(session.getUser())){
                    application.getIdList().remove(studyGroup.getId());
                    application.getCollection().remove(studyGroup);
                }
            });
            return "Все элементы принадлежашие пользователю "+session.getUser()+" удалены.\n" +
                    "Были удалены "+result+" элементов";
        }
        return "Удаление не удалось";
    }

    @Override
    String getCommandInfo() {
        return "clear : удалит те элементы коллекции, которые принадлежат вам";
    }

    @Override
    public String toString() {
        return "clear";
    }

    @Override
    public boolean withArgument() {
        return false;
    }
}
