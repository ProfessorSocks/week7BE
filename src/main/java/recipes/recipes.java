package recipes;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import recipes.dao.DbConnection;
import recipes.entity.Recipe;
import recipes.exception.DbException;
import recipes.service.RecipeService;

public class recipes {
	
	private Scanner scanner = new Scanner(System.in);
	private RecipeService recipeService = new RecipeService();
	
	private List<String> operations = List.of(
			"1) Create and populate all tables",
			"2) Add a recipe"
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
					
				default:
					System.out.println("\n" + operation + " is not vaild");
					break;
					
				}
			}catch(Exception e) {
				System.out.println("\nError: " + e.toString() + " Try again.");
			}
		}
		
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
