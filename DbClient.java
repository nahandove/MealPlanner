package mealplanner;

import java.io.IOException;
import java.sql.*;

public class DbClient {
    static String DB_URL = "jdbc:postgresql://localhost:5432/meals_db";
    static String USER = "postgres";
    static String PASSWORD = "1111";
    static Connection connection;
    static {
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public DbClient() {

    }

    public void start(DbMealManagerDao mealManager) throws IOException, SQLException {
        Statement statement1 = connection.createStatement();
        Statement statement2 = connection.createStatement();

        try {
            mealManager.setMeals(mealManager.loadMeal(statement1, statement2));
        } catch (SQLException e) {
            mealManager.createTable();
        }
        if (mealManager.getMeals().isEmpty()) {
            mealManager.createTable();
        }
        mealManager.createNewPlan();
        try {
            mealManager.mainMenu();
        } catch (IllegalArgumentException | IOException e) {
            mealManager.mainMenu();
        }
    }

    public Statement createStatement(String sqlString) {
        Statement statement = null;

        try {
            statement = connection.createStatement();
            statement.executeUpdate(sqlString);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return statement;
    }

    public PreparedStatement prepareStatement(String sqlString) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sqlString);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statement;
    }

    public boolean nullCheck() {

        PreparedStatement statement = null;
        ResultSet results = null;
        String query = "SELECT * From plan";

        try {
            statement = (PreparedStatement) connection.prepareStatement(query);
            results =  statement.executeQuery();
            int count = 0;
            while(results.next()){
                count++;
            }
            if(count == 0){
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
