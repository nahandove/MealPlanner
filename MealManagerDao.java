package mealplanner;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public interface MealManagerDao {
    void createTable();
    void createNewPlan();
    void addMeal() throws IOException;

    List<Meal> loadMeal(Statement statement1, Statement statement2) throws SQLException;
    void showMeal() throws IOException, SQLException;
    void planMeal() throws IOException, SQLException;
}
