package net.wizartinteractive.database;

import net.wizartinteractive.common.Utilities;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBContext extends SQLiteOpenHelper
{
	private static final String LOG_TAG = "Database";

	private static final String DATABASE_NAME = "AngelRecorderDB";

	private static final int DATABASE_VERSION = 1;

	private static final String CALLS_TABLE = "CALLS";

	private static final String[] DATABASE_TABLES = { CALLS_TABLE };

	private static final String COLUMN_ID = "Id";
	private static final String COLUMN_INCOMING_NUMBER = "IncomingNumber";
	private static final String COLUMN_DATE = "Date";
	private static final String COLUMN_DURATION = "Duration";
	private static final String COLUMN_TYPE = "Type";
	private static final String COLUMN_FILEPATH = "FilePath";

	private static final String QUERY_CREATE_TABLE = String.format("CREATE TABLE %s (" + // Table Name
	"%s INTEGER PRIMARY KEY AUTOINCREMENT, " + // Id
	"%s TEXT NOT NULL, " + // Incoming Number
	"%s TEXT NOT NULL, " + // Date
	"%s TEXT NOT NULL, " + // Duration
	"%s INTEGER NOT NULL, " + //Type 
	"%s TEXT NULL)", // FilePath
			CALLS_TABLE, COLUMN_ID, COLUMN_INCOMING_NUMBER, COLUMN_DATE, COLUMN_DURATION, COLUMN_TYPE, COLUMN_FILEPATH);

	public DBContext(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		try
		{
			db.execSQL(this.QUERY_CREATE_TABLE);
		}
		catch (Exception ex)
		{
			Utilities.logErrorMessage(LOG_TAG, "Error creating table", ex);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		for (String table : DATABASE_TABLES)
		{
			try
			{
				db.execSQL("DROP TABLE IF EXISTS " + table);
			}
			catch (Exception ex)
			{
				Utilities.logErrorMessage(LOG_TAG, "Error deleting table", ex);
			}
		}

		onCreate(db);
	}
}
