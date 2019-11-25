import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Vector;

public class DBIntegration
{
	private static String username;
	private static String password;
	private static String url;

	static Connection setUpConnection(String url, String username, String password) throws ClassNotFoundException, SQLException
	{
		Class.forName("com.ibm.db2.jcc.DB2Driver");
		Connection connection = DriverManager.getConnection(url, username, password);
		return connection;
	}

	public void gatherConnectionProperties()
	{

		try
		{
			InputStream input = DBIntegration.class.getResourceAsStream("/main/resources/credentials.properties");
			Properties prop = new Properties();

			//load properties file
			prop.load(input);

			username = prop.getProperty("db.username");
			password = prop.getProperty("db.password");
			url = prop.getProperty("db.url");

		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	public Vector<Project> getReadyTranslations()
	{
		Vector<Project> readyTranslations = new Vector<>();
		Connection con = null;

		//gather query from text file
		String query = getQuery();

		try
		{
			//set connection variables through properties file
			gatherConnectionProperties();

			//create connection
			con = setUpConnection(url, username, password);
			Statement stmt = con.createStatement();
			//System.out.println(query);
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next())
			{
				readyTranslations.add(new Project(rs.getString("PROJECT ID"),
					rs.getString("TRANSLATION ID"),
					rs.getString("CUSTOMER"),
					rs.getString("PROJECT NAME"),
					rs.getString("PROJECT TYPE"),
					rs.getString("QUEUE"),
					rs.getString("READY FOR PRE-ANALYSIS DATE"),
					rs.getString("THIRD PARTY"),
					rs.getString("SYSTEM"),
					rs.getString("TRANSLATION_NAME"),
					rs.getString("STORY POINTS"),
					rs.getString("BREXIT"),
					rs.getString("COMMENT")));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return readyTranslations;
	}

	public String getQuery()
	{
		String query = "";
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(DBIntegration.class.getResourceAsStream("/main/resources/translationQuery.txt"))))
		{
			String fileText;
			while ((fileText = reader.readLine()) != null)
			{
				query += fileText + "\n";
			}
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return query;
	}
}
