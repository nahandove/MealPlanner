package mealplanner;

import java.io.IOException;
import java.io.FileWriter;
import java.sql.*;
import java.util.*;

public class DbMealManagerDao implements MealManagerDao {
    private DbClient client;
    private static final String DROP_MEALS_TABLE = "DROP TABLE IF EXISTS meals";
    private static final String DROP_INGREDIENTS_TABLE = "DROP TABLE IF EXISTS ingredients";
    private static final String DROP_PLAN_TABLE = "DROP TABLE IF EXISTS plan";
    private static final String CREATE_MEALS_TABLE = "CREATE TABLE meals (" +
            "category VARCHAR NOT NULL," +
            "meal VARCHAR NOT NULL," +
            "meal_id INTEGER PRIMARY KEY" +
            ");";
    private static final String CREATE_INGREDIENTS_TABLE = "CREATE TABLE ingredients (" +
            "ingredient VARCHAR NOT NULL," +
            "ingredient_id INTEGER," +
            "meal_id INTEGER" +
            ");";
    private static final String CREATE_PLAN_TABLE = "CREATE TABLE plan (" +
            "day_of_week VARCHAR NOT NULL," +
            "meal VARCHAR NOT NULL," +
            "category VARCHAR NOT NULL," +
            "meal_id INTEGER" +
            ");";
    private static final String INSERT_MEAL = "INSERT INTO meals (category, meal, meal_id) VALUES (?, ?, ?)";
    private static final String INSERT_INGREDIENTS = "INSERT INTO ingredients (ingredient, ingredient_id, meal_id) VALUES (?, ?, ?)";
    private static final String INSERT_PLAN = "INSERT INTO plan (day_of_week, meal, category, meal_id) VALUES (?, ?, ?, ?)";
    private static final String QUERY_MEALS = "SELECT * FROM meals";
    private static final String QUERY_INGREDIENTS = "SELECT * FROM ingredients WHERE meal_id = ";
    private static final String QUERY_PLAN = "SELECT day_of_week, category, meal FROM plan WHERE day_of_week = '%s'";
    private static final String QUERY_ALL_PLAN = "SELECT * FROM plan";
    private static final String WRONG_FORMAT = "Wrong format. User letters only!";
    private static final String WRONG_CATEGORY = "Wrong meal category! Choose from: breakfast, lunch, dinner.";
    private static final String MEAL_NOT_EXISTS = "This meal doesn’t exist. Choose a meal from the list above.";
    private static final String CANNOT_SAVE = "Unable to save. Plan your meals first.";

    public DbMealManagerDao() throws IOException, SQLException {
        client = new DbClient();
        client.start(this);
    }

    private List<Meal> meals = new ArrayList<>();

    public List<Meal> getMeals() {
        return meals;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
    }

    public void createTable() {
        client.createStatement(DROP_INGREDIENTS_TABLE);
        client.createStatement(DROP_MEALS_TABLE);
        client.createStatement(CREATE_MEALS_TABLE);
        client.createStatement(CREATE_INGREDIENTS_TABLE);
    }

    public void createNewPlan() {
        client.createStatement(CREATE_PLAN_TABLE);
    }

    public void mainMenu() throws IOException, SQLException {
        ConsoleHelper.writeMessage("What would you like to do (add, show, plan, save, exit)?");
        String response = ConsoleHelper.readString();
        switch (response) {
            case "add":
                addMeal();
                break;
            case "show":
                if (meals.isEmpty()) {
                    ConsoleHelper.writeMessage("No meals saved. Add a meal first.");
                    mainMenu();
                }
                ConsoleHelper.writeMessage("Which category do you want to print (breakfast, lunch, dinner)?");
                showMeal();
                break;
            case "plan":
                client.createStatement(DROP_PLAN_TABLE);
                createNewPlan();
                planMeal();
                break;
            case "exit":
                ConsoleHelper.writeMessage("Bye!");
                System.exit(0);
            case "save":
                saveShoppingList();
                break;
            default:
                mainMenu();
        }
        mainMenu();
    }

    public void addMeal() throws IOException {
        int mealIndex = meals.size();
        int ingredientIndex = getAllIngredients(meals).size();
        ConsoleHelper.writeMessage("Which meal do you want to add (breakfast, lunch, dinner)?");
        MealType mealType = inputMealType();
        ConsoleHelper.writeMessage("Input the meal's name:");
        String mealName = inputMealName();
        try (PreparedStatement mealStatement = client.prepareStatement(INSERT_MEAL)) {
            mealStatement.setString(1, mealType.name().toLowerCase());
            mealStatement.setString(2, mealName);
            mealStatement.setInt(3, ++mealIndex);
            mealStatement.executeUpdate();
            ConsoleHelper.writeMessage("Input the ingredients:");
            List<String> ingredients = inputIngredients();
            for (String ingredient : ingredients) {
                try (PreparedStatement ingredientStatement = client.prepareStatement(INSERT_INGREDIENTS)) {
                    ingredientStatement.setString(1, ingredient);
                    ingredientStatement.setInt(2, ++ingredientIndex);
                    ingredientStatement.setInt(3, mealIndex);
                    ingredientStatement.executeUpdate();
                }
            }
            meals.add(new Meal(mealType, mealName, ingredients, mealIndex));
            ConsoleHelper.writeMessage("The meal has been added!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void showMeal() throws IOException, SQLException {
        try {
            String category = ConsoleHelper.readString();
            MealType mealType = getMealTypeByCategory(category);
            List<Meal> specificMeals = getSpecificMeals(mealType);

            if (specificMeals.isEmpty()) {
                ConsoleHelper.writeMessage("No meals found.");
                mainMenu();
            }

            ConsoleHelper.writeMessage("\nCategory: " + category);

            for (Meal meal : specificMeals) {
                ConsoleHelper.writeMessage("\nName: " + meal.getMealName());
                ConsoleHelper.writeMessage("Ingredients:");
                for (String ingredient : meal.getIngredients()) {
                    ConsoleHelper.writeMessage(ingredient.trim());
                }
            }
            ConsoleHelper.writeMessage("");
        } catch (IllegalArgumentException e) {
            ConsoleHelper.writeMessage(WRONG_CATEGORY);
            showMeal();
        }
    }

    public List<Meal> loadMeal(Statement statement1, Statement statement2) throws SQLException {
        ResultSet mealRS = statement1.executeQuery(QUERY_MEALS);

        while (mealRS.next()) {
            List<String> ingredients = new ArrayList<>();
            String category = mealRS.getString("category");
            MealType mealType = getMealTypeByCategory(category);
            String mealName = mealRS.getString("meal");
            int mealId = mealRS.getInt("meal_id");
            ResultSet ingredientRS = statement2.executeQuery(QUERY_INGREDIENTS + mealId);
            while (ingredientRS.next()) {
                String ingredient = ingredientRS.getString("ingredient");
                ingredients.add(ingredient);
            }
            meals.add(new Meal(mealType, mealName, ingredients, mealId));
        }
        return meals;
    }

    public void planMeal() throws IOException {
        for (DayOfWeek dayOfWeek: DayOfWeek.values()) {
            ConsoleHelper.writeMessage(dayOfWeek.getName());
            for (MealType mealType: MealType.values()) {
                List<Meal> mealsList = showSortedMeals(getSpecificMeals(mealType));
                ConsoleHelper.writeMessage(String.format("Choose the %s for %s from the list above:",
                        mealType.name().toLowerCase(), dayOfWeek.getName()));
                addMealToPlan(mealsList, dayOfWeek, mealType);
            }
            ConsoleHelper.writeMessage(String.format("Yeah! We planned the meals for %s.\n", dayOfWeek.getName()));
        }
        printPlannedMeals();
    }

    public void saveShoppingList() throws IOException, SQLException {
        try {
            if (client.nullCheck()) {
                throw new IllegalArgumentException();
            }

            List<String> shoppingList = getShoppingList();
            ConsoleHelper.writeMessage("Input a filename:");
            String fileName = ConsoleHelper.readString();

            try (FileWriter fileWriter = new FileWriter(fileName)) {
                for (String item : shoppingList) {
                    fileWriter.write(item + "\n");
                }
            }
            ConsoleHelper.writeMessage("Saved!");
        } catch (IllegalArgumentException | SQLException e) {
            ConsoleHelper.writeMessage(CANNOT_SAVE);
            mainMenu();
        }
    }

    private List<String> getShoppingList() throws SQLException {
        List<String> shoppingList = new ArrayList<>();
        Map<String, Integer> neededIngredients = new LinkedHashMap<>();
        List<Integer> allIds = new ArrayList<>();

        try (Statement planStatement = client.createStatement(QUERY_ALL_PLAN)) {
            ResultSet plannedSet = planStatement.executeQuery(QUERY_ALL_PLAN);
            while (plannedSet.next()) {
                int id = plannedSet.getInt("meal_id");
                allIds.add(id);
            }
        }

        for (int id: allIds) {
            try (Statement ingredientsStatement = client.createStatement(QUERY_INGREDIENTS)) {
                ResultSet ingredientRS = ingredientsStatement.executeQuery(QUERY_INGREDIENTS + id);
                while (ingredientRS.next()) {
                    String ingredient = ingredientRS.getString("ingredient");
                    if (neededIngredients.containsKey(ingredient)) {
                        int count = neededIngredients.get(ingredient);
                        neededIngredients.put(ingredient, ++count);
                    } else {
                        neededIngredients.put(ingredient, 1);
                    }
                }
            }
        }

        for (Map.Entry<String, Integer> pair : neededIngredients.entrySet()) {
            String shoppingItem = "";
            if (pair.getValue() == 1) {
                shoppingItem = pair.getKey();
            } else {
                shoppingItem = pair.getKey() + " x" + pair.getValue();
            }
            shoppingList.add(shoppingItem);
        }
        return shoppingList;
    }

    private List<Meal> showSortedMeals(List<Meal> specificMeals) {
        specificMeals.sort(Comparator.comparing(Meal::getMealName));
        for (Meal meal: specificMeals) {
            ConsoleHelper.writeMessage(meal.getMealName());
        }
        return specificMeals;
    }

    private List<Meal> getSpecificMeals(MealType mealType) {
        List<Meal> specificMeals = new ArrayList<>();
        for (Meal meal : meals) {
            if (meal.getMealType() == mealType) {
                specificMeals.add(meal);
            }
        }
        return specificMeals;
    }

    private void addMealToPlan(List<Meal> specificMeals, DayOfWeek dayOfWeek, MealType mealType) throws IOException {
        try {
            String choice = ConsoleHelper.readString();
            if (isChoiceInList(choice, specificMeals)) {
                try (PreparedStatement planStatement = client.prepareStatement(INSERT_PLAN)) {
                    planStatement.setString(1, dayOfWeek.getName());
                    planStatement.setString(2, choice);
                    planStatement.setString(3, mealType.name().toLowerCase());
                    planStatement.setInt(4, getMeal(choice, specificMeals).getId());
                    planStatement.executeUpdate();
                } catch (NullPointerException | SQLException e) {
                    e.printStackTrace();
                }
            } else {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            ConsoleHelper.writeMessage(MEAL_NOT_EXISTS);
            addMealToPlan(specificMeals, dayOfWeek, mealType);
        }
    }

    private void printPlannedMeals() {
        for (DayOfWeek dayOfWeek: DayOfWeek.values()) {
            ConsoleHelper.writeMessage(dayOfWeek.getName());
            try (Statement statement = client.createStatement(QUERY_PLAN)) {
                ResultSet planRS = statement.executeQuery(String.format(QUERY_PLAN, dayOfWeek.getName()));
                while(planRS.next()) {
                    ConsoleHelper.writeMessage(planRS.getString("category") + ": " + planRS.getString("meal"));
                }
                ConsoleHelper.writeMessage("");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private MealType getMealTypeByCategory(String category) {
        MealType mealType;
        switch (category) {
            case "breakfast" -> mealType = MealType.BREAKFAST;
            case "lunch" -> mealType = MealType.LUNCH;
            case "dinner" -> mealType = MealType.DINNER;
            default -> throw new IllegalArgumentException();
        }
        return mealType;
    }

    private boolean isChoiceInList(String choice, List<Meal> specificMeals) {
        for (Meal meal: specificMeals) {
            if (meal.getMealName().equals(choice)) {
                return true;
            }
        }
        return false;
    }

    private Meal getMeal(String choice, List<Meal> specificMeals) {
        for (Meal meal: specificMeals) {
            if (meal.getMealName().equals(choice)) {
                return meal;
            }
        }
        return null;
    }

    private MealType inputMealType() throws IOException {

        String choice = ConsoleHelper.readString();

        MealType mealType;

        try {
            mealType = getMealTypeByCategory(choice);
        } catch (IllegalArgumentException e) {
            ConsoleHelper.writeMessage(WRONG_CATEGORY);
            mealType = inputMealType();
        }
        return mealType;
    }

    private String inputMealName() throws IOException {
        String mealName = "";
        try {
            mealName = ConsoleHelper.readString();
            if (!mealName.matches("[A-Za-z ]+")) {
                throw new IllegalArgumentException();
            }
        } catch(IllegalArgumentException e) {
            ConsoleHelper.writeMessage(WRONG_FORMAT);
            mealName = inputMealName();
        }
        return mealName;
    }

    private List<String> inputIngredients() throws IOException {
        List<String> ingredients;
        try {
            String ingredientsString = ConsoleHelper.readString();
            String[] items = ingredientsString.split(",");
            for (String item : items) {
                if (!item.matches("[A-Za-z ]+")) {
                    throw new IllegalArgumentException();
                }
                if (item.matches("\\s+")) {
                    throw new IllegalArgumentException();
                }
            }
            ingredients = Arrays.asList(items);
        } catch (IllegalArgumentException e) {
            ConsoleHelper.writeMessage(WRONG_FORMAT);
            ingredients = inputIngredients();
        }
        return ingredients;
    }

    private List<String> getAllIngredients(List<Meal> meals) {
        List<String> ingredientsList = new ArrayList<>();
        for (Meal meal: meals) {
            ingredientsList.addAll(meal.getIngredients());
        }
        return ingredientsList;
    }
}
