package net.wizartinteractive.database;

import java.util.ArrayList;
import java.util.List;

import net.wizartinteractive.common.Utilities;
import net.wizartinteractive.dbmodels.Call;
import net.wizartinteractive.dbmodels.CallType;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBManager
{
	private static final String LOG_TAG = "Database";

	private static final String CALLS_TABLE = "CALLS";

	private Context appContext = null;

	private static DBContext databaseContext = null;

	private static SQLiteDatabase database = null;
	
	private static DBManager dbManager = null;

	public DBManager()
	{
	}
	
	public static DBManager getInstance()
	{
		if(dbManager == null)
		{
			dbManager = new DBManager();
		}
		
		return dbManager;
	}

	public boolean initializeDB(Context _context)
	{
		try
		{
			this.appContext = _context;
			this.databaseContext = new DBContext(this.appContext);

			return true;
		}
		catch (Exception ex)
		{
			Utilities.logErrorMessage(LOG_TAG, "Error initializing DB", ex);
			return false;
		}
	}

	public boolean openWritableDB()
	{
		try
		{
			if (this.database == null)
			{
				this.database = this.databaseContext.getWritableDatabase();
			}

			return true;
		}
		catch (Exception ex)
		{
			Utilities.logErrorMessage(LOG_TAG, "Error opening DB in W mode ", ex);
			return false;
		}
	}

	public boolean closeDatabase()
	{
		if (this.database.isOpen())
		{
			this.database.close();
			return true;
		}

		return false;
	}

	public Call getCall(long id)
	{
		Call call = null;

		this.openWritableDB();
		
		String[] columns = new String[] { "Id", "IncomingNumber", "Date", "Duration", "Type", "FilePath" };

		Cursor cursor = database.query(this.CALLS_TABLE, columns, String.format("Id = %s", id), null, null, null, null);

		if (cursor.moveToFirst())
		{
			call = new Call();

			call.setId(cursor.getLong(0));
			call.setIncomingNumber(cursor.getString(1));
			call.setDate(Utilities.ConvertStringToDate(cursor.getString(2)));
			call.setDuration(cursor.getLong(3));
			call.setType(CallType.values()[cursor.getInt(4)]);
			call.setFilePath(cursor.getString(5));
		}
		
		this.closeDatabase();

		return call;
	}

	public ArrayList<Call> getCalls()
	{
		ArrayList<Call> calls = new ArrayList<Call>();
		
		this.openWritableDB();

		String[] columns = new String[] { "Id" };

		Cursor cursor = this.database.query(this.CALLS_TABLE, columns, null, null, null, null, null);

		if (cursor.moveToFirst())
		{
			while (cursor.moveToNext())
			{
				Call call = this.getCall(cursor.getLong(0));

				if (call != null)
				{
					calls.add(call);
				}
			}
		}
		
		this.closeDatabase();

		return calls;
	}

	public boolean addCall(Call call)
	{
		this.openWritableDB();
		
		ContentValues values = new ContentValues();

		// values.put("Id", call.getId());
		values.put("IncomingNumber", call.getIncomingNumber());
		values.put("Date", Utilities.ConvertDateToString(call.getDate()));
		values.put("Duration", call.getDuration());
		values.put("Type", call.getType().getType());
		values.put("FilePath", call.getFilePath());
		
		long id = this.database.insert(this.CALLS_TABLE, null, values);

		if (id != -1)
		{
			this.closeDatabase();
			return true;
		}
		else
		{
			this.closeDatabase();
			Utilities.logErrorMessage(LOG_TAG, "Error inserting Call object into DB", null);
			return false;
		}
	}

	public boolean deleteCall(long id)
	{
		this.openWritableDB();
		
		int affectedRows = this.database.delete(CALLS_TABLE, String.format("Id=%s", id), null);
		
		this.closeDatabase();

		if (affectedRows != 0)
		{
			return true;
		}

		return false;
	}
}
