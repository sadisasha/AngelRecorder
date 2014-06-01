package net.wizartinteractive.database;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

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

	// private static Context appContext = null;

	private static DBManager dbManager = null;
	private static DBContext databaseContext = null;
	private static SQLiteDatabase database = null;

	private AtomicInteger openCounter = new AtomicInteger();

	public synchronized static DBManager getInstance()
	{
		if (dbManager == null)
		{
			throw new IllegalStateException(String.format("%s is not initialized, call initializeDB(..) method first.", DBManager.class.getSimpleName()));
		}

		return dbManager;
	}

	public static synchronized boolean initializeDB(Context context)
	{
		try
		{
			dbManager = new DBManager();
			databaseContext = new DBContext(context);

			return true;
		}
		catch (Exception ex)
		{
			Utilities.logErrorMessage(LOG_TAG, "Error initializing DB", ex);
			return false;
		}
	}

	public synchronized SQLiteDatabase getWritableDB()
	{

		try
		{
			if (openCounter.incrementAndGet() == 1)
			{
				Utilities.logDebugMessage(LOG_TAG, "Opening DB...");
				this.database = this.databaseContext.getWritableDatabase();
			}

			return this.database;
		}
		catch (Exception ex)
		{
			Utilities.logErrorMessage(LOG_TAG, "Error opening DB in W mode ", ex);
			return null;
		}
	}

	public synchronized boolean closeDatabase()
	{

		if (this.openCounter.decrementAndGet() == 0)
		{
			Utilities.logDebugMessage(LOG_TAG, "Closing DB...");
			this.database.close();
		}
		// if (this.database != null && this.database.isOpen())
		// {
		// this.database.close();
		// return true;
		// }

		return false;
	}

	public synchronized Call getCall(long id)
	{
		Call call = null;

		String[] columns = new String[] { "Id", "IncomingNumber", "Date", "Duration", "Type", "FilePath", "Favorite" };

		// Cursor cursor = database.query(this.CALLS_TABLE, columns, String.format("Id = %s", id), null, null, null, null);
		Cursor cursor = this.getInstance().getWritableDB().query(this.CALLS_TABLE, columns, String.format("Id = %s", id), null, null, null, null);

		if (cursor.moveToFirst())
		{
			call = new Call();

			call.setId(cursor.getLong(0));
			call.setIncomingNumber(cursor.getString(1));
			call.setDate(Utilities.ConvertStringToDate(cursor.getString(2)));
			call.setDuration(cursor.getLong(3));
			call.setType(CallType.values()[cursor.getInt(4)]);
			call.setFilePath(cursor.getString(5));
			call.setFavorite(cursor.getInt(6) > 0);
		}

		this.getInstance().closeDatabase();

		return call;
	}

	public synchronized ArrayList<Call> getCalls()
	{
		ArrayList<Call> calls = new ArrayList<Call>();

		String[] columns = new String[] { "Id" };

		// Cursor cursor = this.database.query(this.CALLS_TABLE, columns, null, null, null, null, null);

		Cursor cursor = this.getInstance().getWritableDB().query(this.CALLS_TABLE, columns, null, null, null, null, "Id DESC");

		if (cursor.moveToFirst())
		{

			do
			{
				Call call = this.getCall(cursor.getLong(0));

				if (call != null)
				{
					calls.add(call);
				}
			}
			while (cursor.moveToNext());
		}

		Utilities.logDebugMessage(this.LOG_TAG, String.format("Calls in DB: %s", calls.size()));

		this.getInstance().closeDatabase();

		return calls;
	}

	public synchronized boolean addCall(Call call)
	{
		ContentValues values = new ContentValues();

		// values.put("Id", call.getId());
		values.put("IncomingNumber", call.getIncomingNumber());
		values.put("Date", Utilities.ConvertDateToString(call.getDate()));
		values.put("Duration", call.getDuration());
		values.put("Type", call.getType().getType());
		values.put("FilePath", call.getFilePath());
		values.put("Favorite", call.getFavorite() ? 1 : 0);

		// long id = this.database.insert(this.CALLS_TABLE, null, values);

		Utilities.logDebugMessage(LOG_TAG, "Inserting Call Object into DB");
		long id = this.getInstance().getWritableDB().insert(this.CALLS_TABLE, null, values);

		if (id != -1)
		{
			Utilities.logDebugMessage(LOG_TAG, "Call Object sucesfully inserted!!!");
			this.getInstance().closeDatabase();
			return true;
		}
		else
		{
			this.getInstance().closeDatabase();
			Utilities.logErrorMessage(LOG_TAG, "Error inserting Call object into DB", null);
			return false;
		}
	}

	public boolean updateCall(long id, ContentValues contentValues)
	{
		int affectedRows = this.getInstance().getWritableDB().update(CALLS_TABLE, contentValues, String.format("Id=%s", id), null);

		this.getInstance().closeDatabase();

		if (affectedRows > 0)
		{
			return true;
		}

		return false;
	}

	private boolean deleteFile(long id)
	{
		Call call = this.getCall(id);

		File file = new File(call.getFilePath());

		if (file.exists())
		{
			if (file.delete())
			{
				return true;
			}
		}

		return false;
	}

	public synchronized boolean deleteCall(long id)
	{
		this.deleteFile(id);

		int affectedRows = this.getInstance().getWritableDB().delete(CALLS_TABLE, String.format("Id=%s", id), null);

		this.getInstance().closeDatabase();

		if (affectedRows != 0)
		{
			return true;
		}

		return false;
	}

	public synchronized boolean deleteAllCalls()
	{
		ArrayList<Call> calls = this.getCalls();

		for (Call call : calls)
		{
			if (!call.getFavorite())
			{
				this.deleteFile(call.getId());

				this.getInstance().getWritableDB().delete(CALLS_TABLE, String.format("Id=%s", call.getId()), null);
			}
		}

		this.getInstance().closeDatabase();

		return true;
	}
}
