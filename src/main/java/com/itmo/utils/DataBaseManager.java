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
    //private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
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

    private Connection connection;
    private PassEncoder passEncoder;

    //оборачиваем в кавычки для запросов
    //private String decorate(String string) {return string == null ? null : "'" + string + "'";}

    public DataBaseManager(String dbUrl, String user, String pass) {
        try {
            connection = DriverManager.getConnection(dbUrl, user, pass);
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
        PreparedStatement statement = connection.prepareStatement("select * from " + TABLE_NAME);
        ResultSet resultSet = statement.executeQuery();
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
                    FormOfEducation.valueOf(resultSet.getString("form_of_education")),
                    Semester.valueOf(resultSet.getString("semester")),
                    person,
                    resultSet.getString("owner"),
                    new Scanner(System.in)
            );
            collection.add(studyGroup);
        }
        return collection;
    }

    //добаление нового элемента
    public boolean addGroup(StudyGroup studyGroup) {
        try {
            long id = generate_id();
            Person admin = studyGroup.getGroupAdmin();
            Coordinates coordinates = studyGroup.getCoordinates();
            Location location = admin.getLocation();
            PreparedStatement statement = connection.prepareStatement("insert into " + TABLE_NAME + " values (?, ?, ?, ?, ?, ?, cast (? as form_of_education), cast (? as semester), ?, ?, ?, ?, ?, ?, ?, ?)");
            statement.setLong(1, id);
            statement.setString(2, studyGroup.getName());
            statement.setLong(3, coordinates.getX());
            statement.setLong(4, coordinates.getY());
            statement.setObject(5, studyGroup.getCreationDate().toLocalDate());
            statement.setLong(6, studyGroup.getStudentsCount());
            statement.setObject(7, studyGroup.getFormOfEducation().getEnglish());
            statement.setObject(8, studyGroup.getSemesterEnum().getEnglish());
            statement.setString(9, admin.getName());
            statement.setLong(10, admin.getHeight());
            statement.setLong(11, admin.getWeight());
            statement.setString(12, admin.getPassportID());
            statement.setDouble(13, location.getX());
            statement.setLong(14, location.getY());
            statement.setString(15, location.getName());
            statement.setString(16, studyGroup.getOwner());
            /*String query = "INSERT INTO " + TABLE_NAME + " VALUES(" + id + ", "
                    + decorate(studyGroup.getName()) + ", "
                    + studyGroup.getCoordinates().getX() + ", "
                    + studyGroup.getCoordinates().getY() + ", "
                    + decorate(DateTimeAdapter.parseToSQLDate(studyGroup.getCreationDate())) + ", "
                    + studyGroup.getStudentsCount() + ", "
                    + decorate(studyGroup.getFormOfEducation().getEnglish()) + ", "
                    + decorate(studyGroup.getSemesterEnum().getEnglish()) + ", "
                    + decorate(studyGroup.getGroupAdmin().getName()) + ", "
                    + studyGroup.getGroupAdmin().getHeight() + ", "
                    + studyGroup.getGroupAdmin().getWeight() + ", "
                    + decorate(studyGroup.getGroupAdmin().getPassportID()) + ", "
                    + studyGroup.getGroupAdmin().getLocation().getX() + ", "
                    + studyGroup.getGroupAdmin().getLocation().getY() + ", "
                    + decorate(studyGroup.getGroupAdmin().getLocation().getName()) + ", "
                    + decorate(studyGroup.getOwner()) + ")";
            statement.execute(query);
             */
            studyGroup.setId(id);
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //удаление элемента по id
    public int remove(long id) {
        //String query = "DELETE FROM " + TABLE_NAME + " WHERE ID=" + id;
        try {
            PreparedStatement statement = connection.prepareStatement("delete from " + TABLE_NAME + " where id=?");
            statement.setLong(1, id);
            return statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    //добавление нового пользователя
    public void addUser(User user) {
        String hash = passEncoder.getHash(user.getPass());
        //String query = "INSERT INTO " + USERS_TABLE + " VALUES(" + decorate(user.getName()) + ", " + decorate(hash) + ")";
        try {
            PreparedStatement statement = connection.prepareStatement("insert into " + USERS_TABLE + " values (?, ?)");
            statement.setString(1, user.getName());
            statement.setString(2, hash);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //ищем пользователя
    public boolean containsUser(User user) {
        String hash = passEncoder.getHash(user.getPass());
        //String query = "SELECT * FROM " + USERS_TABLE + " WHERE name=" + decorate(user.getName()) + " AND password=" + decorate(hash);
        try {
            PreparedStatement statement = connection.prepareStatement("select * from " + USERS_TABLE + " where name = ? and password = ?");
            statement.setString(1, user.getName());
            statement.setString(2, hash);
            return statement.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //ищем пользователя только по имени
    public boolean containsUserName(String name) {
        //String query = "SELECT * FROM " + USERS_TABLE + " WHERE NAME=" + decorate(name);
        try {
            PreparedStatement statement = connection.prepareStatement("select * from " + USERS_TABLE + " where name = ?");
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //генерируем id с помощью sequence
    public long generate_id() throws SQLException {
        //String query = "SELECT NEXTVAL('GENERATE_ID')";
        PreparedStatement statement = connection.prepareStatement("select nextval('generate_id')");
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        return resultSet.getLong("nextval");
    }

    //удаляем все элементы, принадлежащие пользователю
    public boolean removeAll(String userName) {
        //String query = "DELETE FROM " + TABLE_NAME + " WHERE OWNER=" + decorate(userName);
        try {
            PreparedStatement statement = connection.prepareStatement("select from " + TABLE_NAME + " where owner=?");
            statement.setString(1, userName);
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //обновляем поля элемента
    public int update(long id, StudyGroup studyGroup) {
        Person admin = studyGroup.getGroupAdmin();
        Location location = admin.getLocation();
        Coordinates coordinates = studyGroup.getCoordinates();
        /*String query = "UPDATE " + TABLE_NAME + " SET name=" + decorate(studyGroup.getName()) +
                ", coordinate_x=" + studyGroup.getCoordinates().getX() +
                ", coordinate_y=" + studyGroup.getCoordinates().getY() +
                ", creation_date=" + decorate(DateTimeAdapter.parseToSQLDate(studyGroup.getCreationDate())) +
                ", students_count=" + studyGroup.getStudentsCount() +
                ", form_of_education=" + decorate(studyGroup.getFormOfEducation().getEnglish()) +
                ", semester=" + decorate(studyGroup.getSemesterEnum().getEnglish()) +
                ", admin_name=" + decorate(studyGroup.getGroupAdmin().getName()) +
                ", height=" + studyGroup.getGroupAdmin().getHeight() +
                ", weight=" + studyGroup.getGroupAdmin().getWeight() +
                ", passport_id=" + decorate(studyGroup.getGroupAdmin().getPassportID()) +
                ", x_admin=" + studyGroup.getGroupAdmin().getLocation().getX() +
                ", y_admin=" + studyGroup.getGroupAdmin().getLocation().getY() +
              ", location_name=" + decorate(studyGroup.getGroupAdmin().getLocation().getName()) + " WHERE ID=" + id;
              */
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "update " + TABLE_NAME + " set name=?, coordinate_x=? , coordinate_y=?, creation_date=?, students_count=?, form_of_education=cast (? as form_of_education)," +
                            " semester=cast (? as semester), admin_name=?, height=?, weight=?, passport_id=?, x_admin=?, y_admin=?, location_name=? where id = ?");
            statement.setLong(15, id);
            statement.setString(1, studyGroup.getName());
            statement.setLong(2, coordinates.getX());
            statement.setLong(3, coordinates.getY());
            statement.setObject(4, studyGroup.getCreationDate().toLocalDate());
            statement.setLong(5, studyGroup.getStudentsCount());
            statement.setObject(6, studyGroup.getFormOfEducation().getEnglish());
            statement.setObject(7, studyGroup.getSemesterEnum().getEnglish());
            statement.setString(8, admin.getName());
            statement.setLong(9, admin.getHeight());
            statement.setLong(10, admin.getWeight());
            statement.setString(11, admin.getPassportID());
            statement.setDouble(12, location.getX());
            statement.setLong(13, location.getY());
            statement.setString(14, location.getName());
            return statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
