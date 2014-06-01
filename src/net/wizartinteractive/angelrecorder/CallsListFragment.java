package net.wizartinteractive.angelrecorder;

import java.util.ArrayList;
import java.util.Date;

import net.wizartinteractive.database.DBManager;
import net.wizartinteractive.dbmodels.Call;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CallsListFragment extends android.support.v4.app.Fragment implements OnItemClickListener
{
	public static final String FRAGMENT_NAME = "CALLS_LIST";

	private DBManager dbManager = null;

	private ArrayList<Call> calls = null;

	private Context appContext = null;

	private View contentView = null;
	private ListView callsListView = null;
	private CallsListAdapter callsListAdapter = null;

	public static boolean isDeleteMode;

	private ActionModeCallBack actionMode = null;

	private static ConfigurationManager configurationManager = null;

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

		if (this.actionMode == null)
		{
			this.actionMode = new ActionModeCallBack();
		}

		if (this.configurationManager == null)
		{
			ConfigurationManager.Init(this.appContext);
			this.configurationManager = ConfigurationManager.getInstance();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.contentView = inflater.inflate(R.layout.fragment_calls_list, container, false);

		this.refreshCallsList(this.contentView);

		if (this.configurationManager.getAutoDeleteEnabled()) // check autodelete execution
		{
			Date lastExecution = this.configurationManager.getAutoDeleteLastExecution();
			Date nextExecution = null;
			
			if(this.configurationManager.getAutoDeletePeriod() == 0) //daily
			{
				nextExecution = new Date(lastExecution.getTime() + (24 * 60 * 60 * 1000));
			}
			else if(this.configurationManager.getAutoDeletePeriod() == 1) //weekly
			{
				nextExecution = new Date(lastExecution.getTime() + (168 * 60 * 60 * 1000));
			}
			else if(this.configurationManager.getAutoDeletePeriod() == 2) //biweekly
			{
				nextExecution = new Date(lastExecution.getTime() + (336 * 60 * 60 * 1000));
			}

			if (new Date().after(nextExecution))
			{
				this.deleteAll();
				this.configurationManager.setAutoCleanLastExecution(new Date());
			}
		}

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
		case R.id.action_deleteSelected:

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
			this.callsListAdapter = new CallsListAdapter(getActivity(), R.layout.call_item, this.calls);

			noRecordsMessage.setVisibility(View.GONE);
			this.callsListView = (ListView) contentView.findViewById(R.id.calls_listView);

			// if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			// {
			// registerForContextMenu(this.callsListView);
			// }
			// else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			// {
			this.callsListView.setAdapter(this.callsListAdapter);
			this.callsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			this.callsListView.setOnItemClickListener(this);
			this.callsListView.setOnItemLongClickListener(new OnItemLongClickListener()
			{
				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long id)
				{
					MainActivity.getInstance().startSupportActionMode(actionMode);
					callsListAdapter.setNewSelection(position, true);
					
					CheckBox checkbox = (CheckBox) view.findViewById(R.id.delete_checkBox);
					checkbox.setChecked(true);
					
					// callsListView.setItemChecked(position, !callsListAdapter.isPositionChecked(position));
					return false;
				}
			});
			// }
		}
	}

	public void updateCalls()
	{
		this.calls = this.dbManager.getCalls();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		if (isDeleteMode)
		{
			CheckBox checkbox = (CheckBox) view.findViewById(R.id.delete_checkBox);
			checkbox.setChecked(!checkbox.isChecked());

			if (checkbox.isChecked())
			{
				callsListAdapter.setNewSelection(position, true);
			}
			else
			{
				callsListAdapter.removeSelection(position);
			}

			MainActivity.getInstance().setTitle(String.format("%s", callsListAdapter.getSelectedItemsCount()));
			callsListAdapter.notifyDataSetChanged();
		}
	}

	private void deleteSelected()
	{
		SparseBooleanArray selectedItems = this.callsListAdapter.getSelectedItems();

		int end = selectedItems.size();

		for (int i = 0; i < end; i++)
		{
			if (selectedItems.valueAt(i))
			{
				long id = selectedItems.keyAt(i);
				this.dbManager.deleteCall(id);
			}
		}

		this.refreshCallsList(this.contentView);
	}

	private void deleteAll()
	{
		ArrayList<Call> calls = this.dbManager.getCalls();

		int end = calls.size();

		for (int i = 0; i < end; i++)
		{
			Call call = calls.get(i);

			if (!call.getFavorite())
			{
				this.dbManager.deleteCall(call.getId());
			}
		}

		this.refreshCallsList(this.contentView);
	}

	private class ActionModeCallBack implements ActionMode.Callback
	{

		@Override
		public boolean onCreateActionMode(android.support.v7.view.ActionMode mode, Menu menu)
		{
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.contextual_calls_list, menu);

			isDeleteMode = true;

			return true;
		}

		@Override
		public boolean onPrepareActionMode(android.support.v7.view.ActionMode mode, Menu menu)
		{
			return false;
		}

		@Override
		public boolean onActionItemClicked(android.support.v7.view.ActionMode mode, MenuItem item)
		{
			switch (item.getItemId())
			{
			case R.id.action_deleteSelected:

				deleteSelected();
				callsListAdapter.clearSelection();
				isDeleteMode = false;
				mode.finish();

				return true;

			case R.id.action_delete:

				deleteAll();
				callsListAdapter.clearSelection();
				isDeleteMode = false;
				mode.finish();

				return true;

			default:

				return false;
			}
		}

		@Override
		public void onDestroyActionMode(android.support.v7.view.ActionMode paramActionMode)
		{
			isDeleteMode = false;
			callsListAdapter.clearSelection();
		}
	}
}
