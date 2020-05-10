package com.itmo.utils;

import com.itmo.app.*;
import com.itmo.client.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Класс для работы с базой данных
 * Исполнение запросов и т.п.
 */
public class DataBaseManager {
    //For Database
    private static final String DB_URL = "jdbc:postgresql://pg:5432/studs";
    private static String USER;
    private static String PASS;
    private static final String FILE_WITH_ACCOUNT = "account";
    private static final String TABLE_NAME = "studygroups";
    private static final String USERS_TABLE = "users";
    private static final String pepper = "1@#$&^%$)3";


    //читаем данные аккаунта для входа подключения к бд, ищем драйвер
    static {
        try (FileReader fileReader = new FileReader(FILE_WITH_ACCOUNT);
             BufferedReader reader = new BufferedReader(fileReader)) {
            USER = reader.readLine();
            PASS = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Connection to PostgreSQL JDBC");
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("PostgreSQL JDBC Driver successfully connected");
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found. Include it in your library path");
            e.printStackTrace();
        }
    }

    private Statement statement;
    private PassEncoder passEncoder;

    //оборачиваем в кавычки для запросов
    private String decorate(String string) {
        return string == null ? null : "'" + string + "'";
    }

    public DataBaseManager(String dbUrl, String user, String pass) {
        try {
            Connection connection = DriverManager.getConnection(dbUrl, user, pass);
            statement = connection.createStatement();
            passEncoder = new PassEncoder(pepper);
        } catch (SQLException e) {
            System.out.println("Connection to database failed");
            e.printStackTrace();
        }
    }

    public DataBaseManager(String address, int port, String dbName, String user, String pass) {
        this("jdbc:postgresql://" + address + ":" + port + "/" + dbName, user, pass);
    }

    public DataBaseManager() {
        this(DB_URL, USER, PASS);
    }

    //загрузка коллекции в память
    public CopyOnWriteArraySet<StudyGroup> getCollectionFromDatabase() throws SQLException {
        ResultSet resultSet = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
        CopyOnWriteArraySet<StudyGroup> collection = new CopyOnWriteArraySet<>();
        while (resultSet.next()) {
            Coordinates coordinates = new Coordinates(resultSet.getLong("coordinate_x"), resultSet.getLong("coordinate_y"));
            Long studentsCount = resultSet.getLong("students_count") == 0 ? null : resultSet.getLong("students_count");
            Long height = resultSet.getLong("height") == 0 ? null : resultSet.getLong("height");
            Person person = new Person(
                    resultSet.getString("admin_name"),
                    height,
                    resultSet.getLong("weight"),
                    resultSet.getString("passport_id"),
                    new Location(resultSet.getDouble("x_admin"), resultSet.getLong("y_admin"), resultSet.getString("location_name"))
            );
            StudyGroup studyGroup = new StudyGroup(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    coordinates,
                    DateTimeAdapter.parseToZonedDateTime(resultSet.getDate("creation_date")),
                    studentsCount,
                    FormOfEducation.getValue(resultSet.getString("form_of_education"), ""),
                    Semester.getValue(resultSet.getString("semester"), ""),
                    person,
                    resultSet.getString("owner"),
                    new Scanner(System.in)
            );
            collection.add(studyGroup);
        }
        return collection;
    }

    //добаление нового элемента
    public synchronized boolean addGroup(StudyGroup studyGroup) {
        try {
            long id = generate_id();
            String query = "INSERT INTO " + TABLE_NAME + " VALUES(" + id + ", "
                    + decorate(studyGroup.getName()) + ", "
                    + studyGroup.getCoordinates().getX() + ", "
                    + studyGroup.getCoordinates().getY() + ", "
                    + decorate(DateTimeAdapter.parseToSQLDate(studyGroup.getCreationDate())) + ", "
                    + studyGroup.getStudentsCount() + ", "
                    + decorate(studyGroup.getFormOfEducation().getEnglish()) + ", "
                    + studyGroup.getSemesterEnum().getInt() + ", "
                    + decorate(studyGroup.getGroupAdmin().getName()) + ", "
                    + studyGroup.getGroupAdmin().getHeight() + ", "
                    + studyGroup.getGroupAdmin().getWeight() + ", "
                    + decorate(studyGroup.getGroupAdmin().getPassportID()) + ", "
                    + studyGroup.getGroupAdmin().getLocation().getX() + ", "
                    + studyGroup.getGroupAdmin().getLocation().getY() + ", "
                    + decorate(studyGroup.getGroupAdmin().getLocation().getName()) + ", "
                    + decorate(studyGroup.getOwner()) + ")";
            statement.execute(query);
            studyGroup.setId(id);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //удаление элемента по id
    public synchronized int remove(long id) {
        String query = "DELETE FROM " + TABLE_NAME + " WHERE ID=" + id;
        try {
            return statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    //добавление нового пользователя
    public synchronized void addUser(User user) {
        String hash = passEncoder.getHash(user.getPass());
        String query = "INSERT INTO " + USERS_TABLE + " VALUES(" + decorate(user.getName()) + ", " + decorate(hash) + ")";
        try {
            statement.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //ищем пользователя
    public synchronized boolean containsUser(User user) {
        String hash = passEncoder.getHash(user.getPass());
        String query = "SELECT * FROM " + USERS_TABLE + " WHERE name=" + decorate(user.getName()) + " AND password=" + decorate(hash);
        try {
            return statement.executeQuery(query).next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //ищем пользователя только по имени
    public synchronized boolean containsUserName(String name) {
        String query = "SELECT * FROM " + USERS_TABLE + " WHERE NAME=" + decorate(name);
        try {
            ResultSet resultSet = statement.executeQuery(query);
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //генерируем id с помощью sequence
    public synchronized long generate_id() throws SQLException {
        String query = "SELECT NEXTVAL('GENERATE_ID')";
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        return resultSet.getLong("nextval");
    }

    //удаляем все элементы, принадлежащие пользователю
    public synchronized int removeAll(String userName) {
        String query = "DELETE FROM " + TABLE_NAME + " WHERE OWNER=" + decorate(userName);
        try {
            return statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    //обновляем поля элемента
    public synchronized int update(long id, StudyGroup studyGroup) {
        String query = "UPDATE " + TABLE_NAME + " SET name=" + decorate(studyGroup.getName()) +
                ", coordinate_x=" + studyGroup.getCoordinates().getX() +
                ", coordinate_y=" + studyGroup.getCoordinates().getY() +
                ", creation_date=" + decorate(DateTimeAdapter.parseToSQLDate(studyGroup.getCreationDate())) +
                ", students_count=" + studyGroup.getStudentsCount() +
                ", form_of_education=" + decorate(studyGroup.getFormOfEducation().getEnglish()) +
                ", semester=" + studyGroup.getSemesterEnum().getInt() +
                ", admin_name=" + decorate(studyGroup.getGroupAdmin().getName()) +
                ", height=" + studyGroup.getGroupAdmin().getHeight() +
                ", weight=" + studyGroup.getGroupAdmin().getWeight() +
                ", passport_id=" + decorate(studyGroup.getGroupAdmin().getPassportID()) +
                ", x_admin=" + studyGroup.getGroupAdmin().getLocation().getX() +
                ", y_admin=" + studyGroup.getGroupAdmin().getLocation().getY() +
                ", location_name=" + decorate(studyGroup.getGroupAdmin().getLocation().getName()) + " WHERE ID=" + id;
        try {
            return statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
