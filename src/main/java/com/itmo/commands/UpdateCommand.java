package com.itmo.commands;

import com.itmo.app.Application;
import com.itmo.server.Session;
import com.itmo.utils.FieldsValidator;
import com.itmo.app.StudyGroup;
import com.itmo.exceptions.IdNotFoundException;
import com.itmo.exceptions.InputFormatException;

import java.util.Scanner;

/**
 * команда обновит значения всех полей элемента с данным id
 */
public class UpdateCommand extends Command implements CommandWithInit{
    private StudyGroup studyGroup;

    public void init(String argument, Scanner scanner) {
        try {
            if (!FieldsValidator.checkStringParseToLong(argument, "id - это целое число!!!"))
                throw new InputFormatException();
            long id = Long.parseLong(argument);
            studyGroup = new StudyGroup();
            studyGroup.setId(id);
            if (scanner != null) studyGroup.setScanner(scanner);
            studyGroup.setAllFields();
            studyGroup.setScanner(null);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка ввода, id - число!!!");
        }
    }

    /**
     * обновляет поля
     */
    @Override
    public String execute(Application application, Session session) {
        studyGroup.setOwner(session.getUser());
        try {
            if (!application.getCollection().removeIf(studyGroup -> studyGroup.getId() == this.studyGroup.getId()) && studyGroup.getOwner().equals(session.getUser()) || !(application.getDataBaseManager().update(studyGroup.getId(), studyGroup)>0)) {
                if(application.getCollection().stream().noneMatch(studyGroup1 -> studyGroup1.getId()==this.studyGroup.getId())) throw new IdNotFoundException("Элемент нельзя обновить, т.к. элемента с таким id нет в коллекции");
                throw new IdNotFoundException("Элемент нельзя обновить, т.к. он не принадлежит вам");
            }
            application.getCollection().add(studyGroup);
            return "Элемент с id "+studyGroup.getId()+" обновлён";
        } catch (IdNotFoundException e) {
            return e.getMessage();
        }
    }

    @Override
    String getCommandInfo() {
        return "update id : обновит значение элемента коллекции, id которого равен заданному";
    }

    @Override
    public String toString() {
        return "update";
    }

    @Override
    public boolean withArgument() {
        return true;
    }
}