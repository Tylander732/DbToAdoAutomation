import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;
import java.util.Scanner;
import java.util.Vector;
import org.apache.commons.codec.binary.Base64;
import org.json.*;

public class AdoIntegration
{

	private String adoToken;
	static String encodedPAT;
	static String serviceUrl = "https://<yourAdoBoard>";
	static String teamProjectName = "<YourTeamName>";
	static String urlEndGetWorkItemsQuery = "/_apis/wit/wiql?api-version=5.1";

	static String urlEndGetWorkItemById = "/_apis/wit/workitems?ids=";
	static String urlEndWorkItemByIdForPost = "/_apis/wit/workItems/";
	static String urlWorkItemFields = "&fields=System.Title,System.WorkItemType";

	static String urlWorkItemPostFeature = "/_apis/wit/workitems/$Feature?api-version=5.1";
	static String urlWorkItemPostUserStory = "/_apis/wit/workitems/$User%20Story?api-version=5.1";

	//Constructor
	//Gather Token for ADO connections
	public AdoIntegration()
	{
		gatherAdoToken();
	}

	public JSONArray getTranslationsCurrentlyInAdo()
	{
		JSONArray jsonIdArray = new JSONArray();

		try
		{
			String jsonInputString = "{\n" + "  \"query\": \"Select [System.Id] From WorkItems Where [System.WorkItemType] = 'User Story' AND [System.State] <> 'Closed'" + "\"\n" + "}";
			//System.out.println(jsonInputString);

			//create url for querying work items
			URL url = new URL(serviceUrl + teamProjectName + urlEndGetWorkItemsQuery);

			HttpURLConnection queryCon = setupConnection(url, "POST", true);

			//write POST message
			try (OutputStream os = queryCon.getOutputStream())
			{
				byte[] input = jsonInputString.getBytes("utf-8");
				os.write(input, 0, input.length);
			}

			//read response from connection for JSON data
			try (BufferedReader br = new BufferedReader(new InputStreamReader(queryCon.getInputStream(), "utf-8")))
			{
				StringBuilder response = new StringBuilder();
				String responseLine = null;
				while ((responseLine = br.readLine()) != null)
				{
					response.append(responseLine.trim());
				}

				String allResponse = response.toString();

				JSONObject jsonObject = new JSONObject(allResponse);
				jsonIdArray = jsonObject.getJSONArray("workItems");
			}

			queryCon.disconnect();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return jsonIdArray;
	}

	public Vector<String> getProjectNumbers()
	{
		Vector<String> finalListAdoProjectIds = new Vector<>();
		String currentListOfIds = "";

		JSONArray jsonIdArray = getTranslationsCurrentlyInAdo();

		int jsonIdArrayLength = jsonIdArray.length();

		int numberOfListsNeeded = jsonIdArrayLength / 200;
		int remainingIds = jsonIdArrayLength % 200;

		if (remainingIds != 0)
		{
			numberOfListsNeeded += 1;
		}

		Vector<String> vectorOfLists = new Vector<>();
		int totalIdCounter = 0;

		//Create lists of 200 workItemIds to be used for REST API GET request
		//The ADO API can only return 200 items at once
		for (int i = 0; i < numberOfListsNeeded; i++)
		{
			currentListOfIds = "";

			if (i < numberOfListsNeeded - 1)
			{
				for (int j = 0; j <= 199; j++)
				{
					if (j < 199)
					{
						JSONObject currentWorkItemId = jsonIdArray.getJSONObject(totalIdCounter);
						currentListOfIds += currentWorkItemId.getInt("id") + ",";
					}
					else
					{
						JSONObject currentWorkItemId = jsonIdArray.getJSONObject(totalIdCounter);
						currentListOfIds += currentWorkItemId.getInt("id");
					}
					totalIdCounter++;
				}

			}
			else if (i == numberOfListsNeeded - 1)
			{
				for (int j = 0; j <= remainingIds - 1; j++)
				{
					if (j < remainingIds - 1)
					{
						JSONObject currentWorkItemId = jsonIdArray.getJSONObject(totalIdCounter);
						currentListOfIds += currentWorkItemId.getInt("id") + ",";
					}
					else
					{
						JSONObject currentWorkItemId = jsonIdArray.getJSONObject(totalIdCounter);
						currentListOfIds += currentWorkItemId.getInt("id");
					}
					totalIdCounter++;
				}
			}
			vectorOfLists.add(currentListOfIds);
		}

		//If any lists were created, iterate over all lists and do a GET request for the Json data related to ID's
		if (!vectorOfLists.isEmpty())
		{
			try
			{
				Iterator<String> iterateValue = vectorOfLists.iterator();
				while (iterateValue.hasNext())
				{
					String currentIteratorListOfIds = iterateValue.next();
					URL url = new URL(serviceUrl + teamProjectName + urlEndGetWorkItemById + currentIteratorListOfIds + urlWorkItemFields);

					HttpURLConnection getIdsCon = setupConnection(url, "GET", false);

					int status = getIdsCon.getResponseCode();

					if (status == 200)
					{
						String responseBody;
						try (Scanner scanner = new Scanner(getIdsCon.getInputStream()))
						{
							responseBody = scanner.useDelimiter("\\A").next();
							JSONObject jsonObject = new JSONObject(responseBody);
							JSONArray jsonArray = jsonObject.getJSONArray("value");

							for (int i = 0; i < jsonArray.length(); i++)
							{
								JSONObject currentFields = jsonArray.getJSONObject(i);
								JSONObject childObject = (JSONObject) currentFields.get("fields");
								String currentTitle = childObject.getString("System.Title");
								String currentWorkItemType = childObject.getString("System.WorkItemType");
								if (!finalListAdoProjectIds.contains(currentTitle.substring(0, 8)) && (currentWorkItemType.equals("User Story")))
								{
									finalListAdoProjectIds.add(currentTitle.substring(0, 8));
								}
							}
						}
					}
					getIdsCon.disconnect();
				}
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return finalListAdoProjectIds;
	}


	public boolean createAdoWorkItems(Vector<Project> projects)
	{
		boolean itemsCreatedFlag = false;

		String currentProjectId;
		String currentTranslationId;
		String currentCustomer;
		String currentProjectName;
		String currentProjectType;
		String currentQueue;
		String currentReadyForPrelimDate;
		String currentThirdParty;
		String currentSystem;
		String currentTranslationName;
		String currentStoryPoints;
		String currentBrexit;
		String currentComment;

		Iterator<Project> iterateValue = projects.iterator();
		while (iterateValue.hasNext())
		{
			String jsonFeaturePost = "";
			String jsonUserStoryPost = "";

			Project currentTranslation = iterateValue.next();

			currentProjectId = currentTranslation.getProjectId();
			currentTranslationId = currentTranslation.getTranslationId();
			currentCustomer = currentTranslation.getCustomer();
			currentProjectName = currentTranslation.getProjectName();
			currentProjectType = currentTranslation.getProjectType();
			currentQueue = currentTranslation.getQueue();
			currentReadyForPrelimDate = currentTranslation.getReadyForPrelimDate();
			currentThirdParty = currentTranslation.getThirdParty();
			currentSystem = currentTranslation.getSystem();
			currentTranslationName = currentTranslation.getTranslationName();
			currentStoryPoints = currentTranslation.getStoryPoints();
			currentBrexit = currentTranslation.getBrexit();
			currentComment = currentTranslation.getComment();

			currentProjectId = replaceNull(currentProjectId);
			currentTranslationId = replaceNull(currentTranslationId);
			currentCustomer = replaceNull(currentCustomer);
			currentProjectName = replaceNull(currentProjectName);
			currentProjectType = replaceNull(currentProjectType);
			currentQueue = replaceNull(currentQueue);
			currentReadyForPrelimDate = replaceNull(currentReadyForPrelimDate);
			currentThirdParty = replaceNull(currentThirdParty);
			currentSystem = replaceNull(currentSystem);
			currentTranslationName = replaceNull(currentTranslationName);
			currentBrexit = replaceNull(currentBrexit);
			currentComment = replaceNull(currentComment);

			if (currentStoryPoints == null)
			{
				currentStoryPoints = "0";
			}

			String jsonWorkItemSystemTitle = new JSONObject()
				.put("op", "add")
				.put("path", "/fields/System.Title")
				.put("value", currentProjectId + " - " + currentCustomer + " - " + currentProjectName)
				.toString();

			String jsonWorkItemSystemDescription = new JSONObject()
				.put("op", "add")
				.put("path", "/fields/System.Description")
				.put("value", "Third Party: " + currentThirdParty + " | Translation Name: " + currentTranslationName
					+ " | System: " + currentSystem + " | Translation ID: " + currentTranslationId
					+ " | Comments from Project DB: " + currentComment)
				.toString();

			String jsonWorkItemSystemStoryPoints = new JSONObject()
				.put("op", "add")
				.put("path", "/fields/Microsoft.VSTS.Scheduling.StoryPoints")
				.put("value", currentStoryPoints)
				.toString();

			String jsonWorkItemSystemAreaPath = new JSONObject()
				.put("op", "add")
				.put("path", "/fields/System.AreaPath")
				.put("value", "<PutYourPathHere>")
				.toString();

			String jsonWorkItemSystemTags = new JSONObject()
				.put("op", "add")
				.put("path", "/fields/System.Tags")
				.put("value", currentProjectType + ";" + currentQueue + ";" + currentBrexit)
				.toString();

			String jsonWorkItemDate1 = new JSONObject()
				.put("op", "add")
				.put("path", "/fields/Custom.Date1")
				.put("value", currentReadyForPrelimDate)
				.toString();


			jsonFeaturePost = "[" + jsonWorkItemSystemTitle + "," + jsonWorkItemSystemDescription + "," + jsonWorkItemSystemAreaPath + "," + jsonWorkItemSystemTags + "]";
			//System.out.println(jsonFeaturePost);

			//send create feature post
			//if feature was created, read response, get ID and use that to link user story to feature
			try
			{
				//create the URL for making a feature workItem
				URL featureUrl = new URL(serviceUrl + teamProjectName + urlWorkItemPostFeature);

				HttpURLConnection featureCon = setupConnection(featureUrl, "POST", false);

				try (OutputStream os = featureCon.getOutputStream())
				{
					byte[] input = jsonFeaturePost.getBytes("utf-8");
					os.write(input, 0, input.length);
				}

				int status = featureCon.getResponseCode();

				//if the item was successfully created, then create the user story and link it with the feature just created
				if (status == 200)
				{
					try (BufferedReader br = new BufferedReader(new InputStreamReader(featureCon.getInputStream(), "utf-8")))
					{
						StringBuilder response = new StringBuilder();
						String responseLine = null;
						while ((responseLine = br.readLine()) != null)
						{
							response.append(responseLine.trim());
						}

						String allResponse = response.toString();

						JSONObject jsonObject = new JSONObject(allResponse);
						int featureId = jsonObject.getInt("id");

						String linkParentUrl = serviceUrl + teamProjectName + urlEndWorkItemByIdForPost + featureId;

						String jsonWorkItemLinkToParent = new JSONObject()
							.put("op", "add")
							.put("path", "/relations/-")
							.put("value", new JSONObject().put("rel", "System.LinkTypes.Hierarchy-Reverse").put("url", linkParentUrl))
							.toString();

						jsonUserStoryPost = "[" + jsonWorkItemSystemTitle + "," + jsonWorkItemSystemDescription + "," + jsonWorkItemSystemAreaPath + "," + jsonWorkItemSystemTags + "," + jsonWorkItemSystemStoryPoints + "," + jsonWorkItemLinkToParent + "," + jsonWorkItemDate1 + "]";


						URL userStoryUrl = new URL(serviceUrl + teamProjectName + urlWorkItemPostUserStory);

						HttpURLConnection userStoryCon = setupConnection(userStoryUrl, "POST", false);

						//write userstory item here
						try (OutputStream os = userStoryCon.getOutputStream())
						{
							byte[] input = jsonUserStoryPost.getBytes("utf-8");
							os.write(input, 0, input.length);
						}

						try (BufferedReader br1 = new BufferedReader(new InputStreamReader(featureCon.getInputStream(), "utf-8")))
						{
							StringBuilder response1 = new StringBuilder();
							String responseLine1 = null;
							while ((responseLine1 = br1.readLine()) != null)
							{
								response1.append(responseLine1.trim());
							}

							String allResponse1 = response1.toString();
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}

						if (userStoryCon.getResponseCode() == 200)
						{
							System.out.println(currentProjectId + " : " + currentQueue);
							itemsCreatedFlag = true;
						}

						//userstory finished, disconnect
						userStoryCon.disconnect();
					}
				}
				else
				{
					//disconnect from current feature url
					featureCon.disconnect();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return itemsCreatedFlag;
	}

	//Acceptable requestPostOrGet = "GET" or "POST"
	public HttpURLConnection setupConnection(URL url, String requestType, boolean query)
	{
		HttpURLConnection connection = null;

		try
		{
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Authorization", "Basic " + encodedPAT);

			if (requestType.equals("POST"))
			{
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Accept", "application/json");
				connection.setDoOutput(true);
				if (query == true)
				{
					connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
				}
				else
				{
					connection.setRequestProperty("Content-Type", "application/json-patch+json; utf-8");
				}
			}
			else if (requestType.equals("GET"))
			{
				connection.setRequestMethod("GET");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return connection;
	}

	public void gatherAdoToken()
	{
		try
		{
			//read token from properties file
			InputStream input = AdoIntegration.class.getResourceAsStream("/main/resources/credentials.properties");
			Properties prop = new Properties();

			//load properties file
			prop.load(input);
			setAdoToken(prop.getProperty("ado.token"));

			//after reading token from properties, create encoded version for url connection
			setEncodedPAT(this.adoToken);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	public String replaceNull(String value)
	{
		if (value == null)
		{
			value = "";
		}
		return value;
	}

	public void setAdoToken(String inputString)
	{
		this.adoToken = inputString;
	}

	public void setEncodedPAT(String PAT)
	{
		String AuthStr = ":" + PAT;
		Base64 base64 = new Base64();

		//base64 encode PAT to use for URL
		this.encodedPAT = new String(base64.encode(AuthStr.getBytes()));
	}

	public String getAdoToken()
	{
		return this.adoToken;
	}
}
