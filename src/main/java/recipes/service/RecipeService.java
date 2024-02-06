package recipes.service;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import recipes.dao.RecipeDao;
import recipes.entity.Recipe;
import recipes.exception.DbException;

public class RecipeService {
	private static final String SCHEMA_FILE = "recipe_schema.sql";
	private static final String DATA_FILE = "recipe_data.sql";
	
	private RecipeDao recipeDao = new RecipeDao();
	
	
	
	public void createAndPopulateTables() {
		loadFromFile(SCHEMA_FILE);
		loadFromFile(DATA_FILE);
	}

	private void loadFromFile(String file) {
		String content = readFileContent(file);
		List<String> sqlStatements = convertContentToSqlStatements(content);
		
		sqlStatements.forEach(line -> System.out.println(line));
		
		recipeDao.executeBatch(sqlStatements);
	}

	private List<String> convertContentToSqlStatements(String content) {
		content = removeComments(content);
		content = replaceWhitespaceSequencesWithSingleSpace(content);
		
		return extractLinesFromContent(content);
	}

	private List<String> extractLinesFromContent(String content) {
		List<String> lines = new LinkedList<>();
		while(!content.isEmpty()) {
			int semicolon = content.indexOf(";");
			
			if(semicolon == -1) {
				if(!content.isBlank()) {
					lines.add(content);
				}
				
				content = "";
			}
			else {
				lines.add(content.substring(0, semicolon).trim());
				content = content.substring(semicolon +1);
			}
		}	
		
		return lines;
	}

	private String replaceWhitespaceSequencesWithSingleSpace(String content) {
		return content.replaceAll("\\s+", " ");
	}

	private String removeComments(String content) {
		StringBuilder builder = new StringBuilder(content);
		int commentPos = 0;
		
		while((commentPos =  builder.indexOf("-- ", commentPos)) != -1) {
			int eolPos = builder.indexOf("\n", commentPos +1);
			
			if(eolPos == -1) {
				builder.replace(commentPos,builder.length(), "");
			}
			else {
				builder.replace(commentPos, eolPos + 1, "");
			}
		}
		
		
		return builder.toString();
	}

	private String readFileContent(String file) {
		try {
			Path path = Paths.get(getClass().getClassLoader().getResource(file).toURI());
			return Files.readString(path);
			
		} catch (Exception e) {
			throw new DbException(e);
		}
	}

	public Recipe addRecipe(Recipe recipe) {
		return recipeDao.insertRecipe(recipe);
	}

	public List<Recipe> fetchRecipes() {
		return recipeDao.fetchAllRecipes();
	}
	
	public Optional<Recipe> fetchRecipeById(Integer RecipeId) {
		try{
			return recipeDao.fetchRecipeById(RecipeId);
		}catch(Exception e) {
			throw new DbException(e);
		}
		
	}
	
//	public static void main(String[] args) {
//		 new RecipeService().createAndPopulateTables();
//	}
	
	
}
