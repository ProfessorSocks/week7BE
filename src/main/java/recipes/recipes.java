package recipes;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

import recipes.dao.DbConnection;
import recipes.entity.Category;
import recipes.entity.Ingredient;
import recipes.entity.Recipe;
import recipes.entity.Step;
import recipes.entity.Unit;
import recipes.exception.DbException;
import recipes.service.RecipeService;

public class recipes {
	
	private Scanner scanner = new Scanner(System.in);
	private RecipeService recipeService = new RecipeService();
	private Recipe curRecipe;
	
	private List<String> operations = List.of(
			"1) Create and populate all tables",
			"2) Add a recipe",
			"3) List recipes",
			"4) Select a recipe",
			"5) add ingredient to current recipe",
			"6) add step to current recipe",
			"7) add category to current recipe",
			"8) modify step in curent recipe",
			"9) Delete Recipe"
			);
	
	
	
	public static void main(String[] args) {
//		DbConnection.getConnection();
		new recipes().displayMenu();
		
	}

	private void displayMenu() {
		boolean done = false;
		
		while(!done) {
			
			
			try {
				int operation = getOperation();
				
				switch(operation) {
				case -1:
					done = exitMenu();
					break;
				case 1:
					createTables();
					break;
				case 2:
					addRecipe();
					break;
				case 3:
					listRecipes();
					break;
				case 4:
					setCurrentRecipe();
					break;
				case 5:
					addIngredientToCurrentRecipe();
					break;
				case 6:
					addStepToCurrentRecipe();
					break;
				case 7:
					addCategoryToCurrentRecipe();
					break;
				case 8:
					modifyStepInCurrentRecipe();
					break;
				case 9:
					deleteRecipe();
					break;
					
				default:
					System.out.println("\n" + operation + " is not vaild");
					break;
					
				}
			}catch(Exception e) {
				System.out.println("\nError: " + e.toString() + " Try again.");
			}
		}
		
	}

	private void deleteRecipe() {
		listRecipes();
		
		Integer recipeId = getIntInput("Enter the id of the recipe you wish to delete");
		
		if(Objects.nonNull(recipeId)) {
			recipeService.deleteRecipe(recipeId);
			
			if(Objects.nonNull(curRecipe) && curRecipe.getRecipeId().equals(recipeId)) {
				curRecipe = null;
			}
		}
		
		
	}

	private void modifyStepInCurrentRecipe() {
		if(Objects.isNull(curRecipe)) {
			System.out.println("\nSelect a recipe first");
			return;
		}
		
		List<Step> steps = recipeService.fetchSteps(curRecipe.getRecipeId());
		
		System.out.println("\nSteps for recipe");
		steps.forEach(step -> System.out.println("   " + step));
		
		Integer stepId = getIntInput("Enter step Id");
		
		if(Objects.nonNull(stepId)) {
			String stepText = getStringInput("Enter new step text");
			
			if(Objects.nonNull(stepText)) {
				Step step = new Step();
				
				step.setStepId(stepId);
				step.setStepText(stepText);
				
				recipeService.modifyStep(step);
				curRecipe = recipeService.fetchRecipeById(curRecipe.getRecipeId());
				
			}
			
		}
		
	}

	private void addCategoryToCurrentRecipe() {
		if(Objects.isNull(curRecipe)) {
			System.out.println("\nSelect a recipe first");
			return;
		}
		
		List<Category> cate = recipeService.fetchCategories();
		
		cate.forEach(cat -> System.out.println("    " + cat.getCategoryName()));;
		
		String cateName = getStringInput("Enter Category ");
		
		if(Objects.nonNull(cateName)) {
			recipeService.addCategoryToRecipe(curRecipe.getRecipeId(), cateName);
			curRecipe = recipeService.fetchRecipeById(curRecipe.getRecipeId());
		}
		
	}

	private void addStepToCurrentRecipe() {
		if(Objects.isNull(curRecipe)) {
			System.out.println("\nSelect a recipe first");
			return;
		}
		
		String stepText = getStringInput("Enter step text");
		
		if(Objects.nonNull(stepText)) {
			Step step = new Step();
;
			step.setRecipeId(curRecipe.getRecipeId());
			step.setStepText(stepText);
			
			recipeService.addStep(step);
			
			curRecipe = recipeService.fetchRecipeById(step.getRecipeId());
		}
		
	}

	private void addIngredientToCurrentRecipe() {
		if(Objects.isNull(curRecipe)) {
			System.out.println("\nSelect a recipe first");
			return;
		}
		
		String name = getStringInput("Enter name");
		String instruct = getStringInput("Enter Instructions");
		Double inputAmount = getDoubleInput("Enter amount");
		List<Unit> units = recipeService.fetchUnits();
		
		BigDecimal amount = Objects.isNull(inputAmount) ? null : new BigDecimal(inputAmount).setScale(2);
		
		System.out.println("Units: ");
		
		units.forEach(unit -> System.out.println("    " + unit.getUnitId() + ": " + unit.getUnitNameSingular() + " (" + unit.getUnitNamePlural() + ")"));
		
		Integer unitId = getIntInput("Enter a unit id");
		
		Unit unit = new Unit();
		
		unit.setUnitId(unitId);
		
		Ingredient ingr = new Ingredient();
		
		ingr.setRecipeId(curRecipe.getRecipeId());
		ingr.setUnit(unit);
		ingr.setIngredientName(name);
		ingr.setInstruction(instruct);
		ingr.setAmount(amount);
		
		recipeService.addIngredient(ingr);
		
		curRecipe = recipeService.fetchRecipeById(ingr.getRecipeId());
		
		
		
	}

	private void setCurrentRecipe() {
		List<Recipe> recipes = listRecipes();
		
		Integer recipeId = getIntInput("Select an id!");
		
		curRecipe = null;
		
		for(Recipe recipe : recipes) {
			if(recipe.getRecipeId().equals(recipeId)) {
				curRecipe = recipeService.fetchRecipeById(recipeId);
				break;
			}
		
		}
		if (Objects.isNull(curRecipe)) {
			System.out.println("\nYou are not working with a recipe.");
		} else {
			System.out.println("\nYou are working with recipe " + curRecipe);
		}
	}

	private List<Recipe> listRecipes() {
		List<Recipe> recipes = recipeService.fetchRecipes();
		
		System.out.println("\nRecipes:");
		
		recipes.forEach(recipe -> System.out.println("   " + recipe.getRecipeId() + ": " + recipe.getRecipeName()));
		
		return recipes;
	}

	private void addRecipe() {
		String name = getStringInput("Enter the recipe name");
		String notes = getStringInput("Enter recipe notes");
		Integer numServings = getIntInput("Enter # of servings");
		Integer prepMinutes = getIntInput("Enter prep time");
		Integer cookMinutes = getIntInput("Enter cook time");
		
		LocalTime prepTime = minutesToLocalTime(prepMinutes);
		LocalTime cookTime = minutesToLocalTime(cookMinutes);
		
		Recipe recipe = new Recipe();
		
		recipe.setRecipeName(name);
		recipe.setNotes(notes);
		recipe.setNumServings(numServings);
		recipe.setPrepTime(prepTime);
		recipe.setCookTime(cookTime);
		
		Recipe dbRecipe = recipeService.addRecipe(recipe);
		System.out.println("Added a recipe");
		
//		curRecipe = recipeService.fetchRecipeById(dbRecipe.getRecipeId());
	}

	private LocalTime minutesToLocalTime(Integer numMinutes) {
		int min =  Objects.isNull(numMinutes) ? 0 : numMinutes;
		int hours = min / 60;
		int minutes = min % 60;
		
		return LocalTime.of(hours, minutes);
		
	}

	private void createTables() {
		recipeService.createAndPopulateTables();
		System.out.println("\nTables created!");
	}

	private boolean exitMenu() {
		System.out.println("\nExiting BYE!!!!!!!!!!");
		return true;
	}

	private int getOperation() {
		printOperations();
		Integer op = getIntInput("\n Enter an operation number (press enter to quit)");
		
		return Objects.isNull(op) ? -1 : op;
		
	}

	

	private void printOperations() {
		System.out.println();
		System.out.println("Here's what you can do:");
		
		operations.forEach(op -> System.out.println("   " + op));
		
	}
	
	private Integer getIntInput(String prompt) {
		String input = getStringInput(prompt);
		
		if(Objects.isNull(input)) {
			return null;
		}
		
		try {
			return Integer.parseInt(input);
		}catch(NumberFormatException e) {
			throw new DbException(input + " is not a vaild number.");
		}
	}
	
	private Double getDoubleInput(String prompt) {
		String input = getStringInput(prompt);
		
		if(Objects.isNull(input)) {
			return null;
		}
		
		try {
			return Double.parseDouble(input);
		}catch(NumberFormatException e) {
			throw new DbException(input + " is not a vaild number.");
		}
	}

	private String getStringInput(String prompt) {
		System.out.print(prompt + ": ");
		String line = scanner.nextLine();
		
		return line.isBlank() ? null : line.trim();
	}
}
