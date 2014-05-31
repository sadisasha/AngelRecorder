package net.wizartinteractive.angelrecorder;

import java.util.ArrayList;

import net.wizartinteractive.database.DBManager;
import net.wizartinteractive.dbmodels.Call;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CallsListFragment extends android.support.v4.app.Fragment implements MultiChoiceModeListener
{
	public static final String FRAGMENT_NAME = "CALLS_LIST";

	private DBManager dbManager = null;

	private ArrayList<Call> calls = null;

	private Context appContext = null;

	private View contentView = null;
	private ListView callsListView = null;
	private CallsListAdapter callsListAdapter = null;

	public static boolean isDeleteMode;

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

			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			{
				registerForContextMenu(this.callsListView);
			}
			else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			{
				this.callsListView.setAdapter(this.callsListAdapter);
				this.callsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
				this.callsListView.setMultiChoiceModeListener(this);
				this.callsListView.setOnItemLongClickListener(new OnItemLongClickListener()
				{
					@Override
					public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3)
					{

						callsListView.setItemChecked(position, !callsListAdapter.isPositionChecked(position));
						return false;
					}
				});
			}
		}
	}

	public void updateCalls()
	{
		this.calls = this.dbManager.getCalls();
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu)
	{
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.contextual_calls_list, menu);

		this.isDeleteMode = true;

		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu)
	{
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.action_deleteSelected:

			this.deleteSelected();
			this.callsListAdapter.clearSelection();
			this.isDeleteMode = false;
			mode.finish();

			return true;

		case R.id.action_delete:

			this.deleteAll();
			this.callsListAdapter.clearSelection();
			this.isDeleteMode = false;
			mode.finish();

			return true;

		default:

			return false;
		}
	}

	@Override
	public void onDestroyActionMode(ActionMode mode)
	{
		this.isDeleteMode = false;
		this.callsListAdapter.clearSelection();
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
	{
		int selectedItems = this.callsListView.getCheckedItemCount();
		mode.setTitle(String.format("%s", selectedItems));

		if (checked)
		{
			this.callsListAdapter.setNewSelection(position, checked);
		}
		else
		{
			this.callsListAdapter.removeSelection(position);
		}

		this.callsListAdapter.notifyDataSetChanged();
	}

	private void deleteSelected()
	{
		SparseBooleanArray selectedItems = this.callsListAdapter.getSelectedItems();

		int end = selectedItems.size();

		for (int i = 0; i < end; i++)
		{
			if (selectedItems.valueAt(i))
			{
				// long id = this.callsListAdapter.getItemId(i);
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
}
