import java.util.Iterator;
import java.util.Vector;

public class Main
{
	public static void main(String[] args)
	{
		System.out.println("Query ADO and Project DB to determine which projects to add. Please wait.");

		Vector<Project> notInAdo = new Vector<>();

		AdoIntegration adoIntegration = new AdoIntegration();
		DBIntegration db = new DBIntegration();

		//get all ado projects
		Vector<String> adoProjectList = adoIntegration.getProjectNumbers();

		//get all projects from the projectDB
		Vector<Project> readyTranslations = db.getReadyTranslations();

		//section for gathering all translations ready to be added to ADO.
		Iterator<Project> iterateValue = readyTranslations.iterator();
		while (iterateValue.hasNext())
		{
			//iterate over each translation from the project DB, see if it exists in ado
			Project currentTranslationFromDb = iterateValue.next();
			if(!adoProjectList.contains(currentTranslationFromDb.getProjectId()))
			{
				//add to new translation vector if it doesn't exist in ado
				notInAdo.add(currentTranslationFromDb);
			}
		}

		//pass final notInAdo vector to be created
		if(!adoProjectList.isEmpty())
		{
			boolean itemsCreatedFlag = adoIntegration.createAdoWorkItems(notInAdo);
			if (itemsCreatedFlag == false)
			{
				System.out.println("There were no new projects to bring in. No new ADO cards have been created.");
			}
		}
		else
		{
			System.out.println("There was an issue connecting to ADO, no new projects have been created.");
		}

		System.out.println("Finished");
	}
}
