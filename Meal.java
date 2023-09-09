package mealplanner;

import java.util.List;

public class Meal {
    private MealType mealType;
    private String mealName;
    private List<String> ingredients;
    private int id;

    public Meal(MealType mealType, String mealName, List<String> ingredients, int id) {
        this.mealType = mealType;
        this.mealName = mealName;
        this.ingredients = ingredients;
        this.id = id;
    }

    public MealType getMealType() {
        return mealType;
    }

    public String getMealName() {
        return mealName;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public int getId() {
        return id;
    }
}
