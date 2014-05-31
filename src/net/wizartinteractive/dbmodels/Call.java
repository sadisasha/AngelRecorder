package net.wizartinteractive.dbmodels;

import java.util.Date;

public class Call
{
	private long Id;

	private String IncomingNumber;

	private Date Date;

	private long Duration;

	private CallType Type;

	private String FilePath;
	
	private boolean Favorite = false;

	public long getId()
	{
		return Id;
	}

	public void setId(long id)
	{
		Id = id;
	}

	public String getIncomingNumber()
	{
		return IncomingNumber;
	}

	public void setIncomingNumber(String incommingNumber)
	{
		IncomingNumber = incommingNumber;
	}

	public Date getDate()
	{
		return Date;
	}

	public void setDate(Date date)
	{
		Date = date;
	}

	public long getDuration()
	{
		return Duration;
	}

	public void setDuration(long duration)
	{
		Duration = duration;
	}

	public CallType getType()
	{
		return Type;
	}

	public void setType(CallType type)
	{
		Type = type;
	}

	public String getFilePath()
	{
		return FilePath;
	}

	public void setFilePath(String filePath)
	{
		FilePath = filePath;
	}
	
	public boolean getFavorite()
	{
		return this.Favorite;
	}
	
	public void setFavorite(boolean favorite)
	{
		Favorite = favorite;
	}

}
