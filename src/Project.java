public class Project
{
	private String projectId;
	private String translationId;
	private String customer;
	private String projectName;
	private String projectType;
	private String queue;
	private String readyForPrelimDate;
	private String thirdParty;
	private String system;
	private String translationName;
	private String storyPoints;
	private String brexit;
	private String comment;

	public Project(String projectId, String translationId, String customer, String projectName, String projectType, String queue, String readyForPrelimDate, String thirdParty,
				   String system, String translationName, String storyPoints, String brexit, String comment)
	{
		this.projectId = projectId;
		this.translationId = translationId;
		this.customer = customer;
		this.projectName = projectName;
		this.projectType = projectType;
		this.queue = queue;
		this.readyForPrelimDate = readyForPrelimDate;
		this.thirdParty = thirdParty;
		this.system = system;
		this.translationName = translationName;
		this.storyPoints = storyPoints;
		this.brexit = brexit;
		this.comment = comment;
	}

	public String getTranslationName()
	{
		return translationName;
	}

	public String getProjectId()
	{
		return projectId;
	}

	public String getTranslationId()
	{
		return translationId;
	}

	public String getCustomer()
	{
		return customer;
	}

	public String getProjectName()
	{
		return projectName;
	}

	public String getProjectType()
	{
		return projectType;
	}

	public String getQueue()
	{
		return queue;
	}

	public String getReadyForPrelimDate()
	{
		return readyForPrelimDate;
	}

	public String getThirdParty()
	{
		return thirdParty;
	}

	public String getSystem()
	{
		return system;
	}

	public String getStoryPoints()
	{
		return storyPoints;
	}

	public String getBrexit()
	{
		return brexit;
	}

	public String getComment()
	{
		return comment;
	}
}
