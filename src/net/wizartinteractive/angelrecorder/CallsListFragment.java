package net.wizartinteractive.angelrecorder;

import java.util.ArrayList;

import net.wizartinteractive.database.DBManager;
import net.wizartinteractive.dbmodels.Call;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class CallsListFragment extends android.support.v4.app.Fragment
{
	public static final String FRAGMENT_NAME = "CALLS_LIST";

	private DBManager dbManager = null;

	private ArrayList<Call> calls = null;

	private Context appContext = null;

	public CallsListFragment()
	{
		this.appContext = this.getActivity();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (this.appContext == null)
		{
			this.appContext = this.getActivity();
		}

		this.dbManager = DBManager.getInstance();
		this.dbManager.initializeDB(this.appContext);

		// this.dbManager = new DBManager();
		// this.dbManager.initializeDB(this.appContext);

		// for (int i = 0; i <= 10; i++)
		// {
		// Call call = new Call();
		//
		// // call.setId();
		// call.setIncomingNumber("123456789");
		// call.setDate(new Date());
		// call.setDuration(111111);
		// call.setType(CallType.INCOMING);
		// call.setFilePath("file:\\" + i);
		//
		// dbManager.addCall(call);
		// }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		View contentView = inflater.inflate(R.layout.fragment_calls_list, container, false);

		this.updateCalls();

		ListView callsList = (ListView) contentView.findViewById(R.id.calls_listView);

		callsList.setAdapter(new CallsListAdapter(getActivity(), R.layout.call_item, this.calls));
		// callsList.setOnItemClickListener(CallsListFragment.this);

		return contentView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onStart()
	{
		super.onStart();
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}

	@Override
	public void onStop()
	{
		super.onStop();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
	}

	public void updateCalls()
	{
		// this.dbManager.openWritableDB();
		this.calls = dbManager.getCalls();
		// dbManager.closeDatabase();
	}
}
