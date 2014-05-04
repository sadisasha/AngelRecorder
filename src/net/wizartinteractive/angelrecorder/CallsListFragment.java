package net.wizartinteractive.angelrecorder;

import java.util.ArrayList;

import net.wizartinteractive.database.DBManager;
import net.wizartinteractive.dbmodels.Call;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CallsListFragment extends android.support.v4.app.Fragment
{
	public static final String FRAGMENT_NAME = "CALLS_LIST";

	private DBManager dbManager = null;

	private ArrayList<Call> calls = null;

	private Context appContext = null;

	private View contentView = null;
	private ListView callsListView = null;

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

		if (this.dbManager == null)
		{
			DBManager.initializeDB(this.appContext);
			this.dbManager = DBManager.getInstance();
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.contentView = inflater.inflate(R.layout.fragment_calls_list, container, false);

		this.refreshCallsList(this.contentView);
		return contentView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);

		MenuInflater inflater = this.getActivity().getMenuInflater();
		inflater.inflate(R.menu.contextual_calls_list, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		switch (item.getItemId())
		{
		case R.id.action_delete:

			Toast toast = null;

			if (this.dbManager.deleteCall(info.targetView.getId()))
			{
				toast = Toast.makeText(appContext, appContext.getString(R.string.messageDeleteSuccesfull), Toast.LENGTH_LONG);
				this.refreshCallsList(this.contentView);
			}
			else
			{
				toast = Toast.makeText(appContext, appContext.getString(R.string.messageDeleteError), Toast.LENGTH_LONG);
			}

			toast.show();

			return true;

		default:
			return super.onContextItemSelected(item);
		}
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

	public void refreshCallsList(View contentView)
	{
		this.updateCalls();

		TextView noRecordsMessage = (TextView) contentView.findViewById(R.id.noData_textView);

		if (this.calls.size() <= 0)
		{
			noRecordsMessage.setVisibility(View.VISIBLE);
		}
		else
		{
			noRecordsMessage.setVisibility(View.GONE);
			this.callsListView = (ListView) contentView.findViewById(R.id.calls_listView);
			this.registerForContextMenu(this.callsListView);
			callsListView.setAdapter(new CallsListAdapter(getActivity(), R.layout.call_item, this.calls));
			
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			{
				
			}
		}
	}

	public void updateCalls()
	{
		this.calls = this.dbManager.getCalls();
	}
}
