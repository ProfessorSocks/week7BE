package recipes;

import recipes.dao.DbConnection;

public class recipes {
	
	public static void main(String[] args) {
		DbConnection.getConnection();
	}
}
