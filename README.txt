Project assignment from JetBrains Academy (www.hyperskill.org), Java Backend Developer track.

	This program uses JDBC and the PostgreSQL database to aid in storing and planning meals. Moreover, 
it is able to generate grocery shopping lists based on existing meal plans.

	The databases are created to be permanent. Upon starting the program, the data in the stored 
"meals_db" database are automatically loaded. If the databases don't exist or are empty, they are created 
anew.

	The following databases are used in the program:

1. The meals database:
	The database consists of three columns: category, meal, meal_id.
	category: There are only three items in category: breakfast, lunch, and dinner, representing the 
	type of meal the meal belongs to.
	meal: The name of the meal. Only Latin alphabets and the space characters are allowed as meal names.
	meal_id: An interger that grants a unique key for each meal and referenced by other databases.

2. The ingredients database:
	The database consists of three columns: ingredient, ingredient_id, and meal_id.
	ingredient: The name of the ingredient that is used in a specific meal. Only Latin alphabets and
	the space character are allowed as ingredient names.
	ingredient_id: A unique integer id that is used as reference for a specific ingredient.
	meal_id: Identical to meal_id in the meals and plan database. Represents the foreign key that can be
	used to query items of other databases.

3. The plan database:
	The database consists of four columns: day_of_week, meal, category, and meal_id.
	day_of_week: Represents the day of the week a specific meal is planned.
	meal: Represents the meal that gets featured in the plan.
	category: Represents which type of meal (breakfast, lunch, dinner) the meal belongs to.
	meal_id: Identical to meal_id in the meals and ingredients database. Represents the foreign key that 
	can be used to query items of other databases.

	As the program begins, the user is prompted to choose among several inputs: add, show, plan, save, exit. 
Depending on the current state of the databases, the user would be informed if they can perform a particular 
operation or not. If they entered invalid operation, the program repeats the prompt.

1.add operation:
	The user can add new meals to the database. The program prompts the user first which meal type 
	(breakfast, lunch, or dinner) to add. If the user enters something other than the three choices, they 
	are informed of their errors ("Wrong meal category!") and asked to re-input.
	Once valid input is received, the user is then asked to input the meal name. If the meal name contains
	non-latin alphabets, the user is informed of their error ("Wrong format. Use letters only!) until they 
	enter valid input.
	The user is then asked to input the list of ingredients that make up the meal. The list consists of
	items separated by a comma. Again, similar to the meal name, if any of the items contains non-latin 
	alphabets or is empty, the user is informed of their error("Wrong format. Use letters only!) until the 
	user enters a valid list.

2.show operation:
	The program prompts the user to enter the meal type (breakfast, lunch, dinner) to be shown. The 
	program then queries the meals database to check if meals of this category exist. If not, the program 
	prints an error message("No meals found") and returns the user to the main menu. Otherwise, the program	
	groups meals of the queried category and show the existing meals in the following format:

	Category: {name of category}
	
	Name: {meal name 1}
	Ingredients:
	{ingredients 1}
	...
	{ingredients n}

	Name: {meal name 2}
	Ingredients:
	{ingredients 1}
	...
	{ingredients n}
	
	...

	Name: {meal name n}
	Ingredients:
	{ingredients 1}
	...
	{ingredients n}

3. plan operation:
	The program generates the weekly meal plan for the user. If a plan currently exists, the plan is 
	discarded and a new plan is generated. The user is then prompted to design the meal plan for the 
	following week. 
	The program following the pattern of starting from Monday and ending on Sunday, and always in the
	sequence of breakfast-lunch-dinner. Therefore, first a list of stored breakfast entries are shown for 
	the user, then lunch entries, then dinner entries, for Monday, and then similarly for Tuesday, and so 
	forth. after the entries for each meal are shown, the user inputs their choices. If the choice do not 
	correspond to the stored entries, the user is informed of their error("This meal doesn’t exist. Choose a 
	meal from the list above.") and asked to re-input their choice. Once the user finished their plan for the
	whole week, the program prints the full plan in the following format:

	Monday
	Breakfast: {meal name}
	Lunch: {meal name}
	Dinner: [meal name}

	Tuesday
	Breakfast: {meal name}
	Lunch: {meal name}
	Dinner: [meal name}

	...

	and so forth.

4. save operation:
	The program generates a grocery shopping list for the week that suits the meal plan. If the plan database 
	is empty, the user is informed of their error and asked to make their weekly meal plan first. Else,
	the user is first asked to supply a file to store their shopping list. Then, the program reads through the
	plan database to figure out the needed servings of each ingredient of meals that make up the weekly plans, 
	and the shopping list is stored in the following format:

	eggs x12
	chicken x2
	avocado
	salmon
	milk x5
	carrot x10
	...

	and so forth.

	That is, if only one serving of ingredient is needed, the item is listed alone. But if more than one
	servings are needed, the shopping list shows "x{servings}" after the ingredient name. 

5. exit operation:
	The program ends. However, the items saved in the databases are accessible for future uses and can be 
	called upon any time.


9. September, 2023--description by E. Hsu (nahandove@gmail.com)