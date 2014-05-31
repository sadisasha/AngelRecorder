package net.wizartinteractive.angelrecorder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import net.wizartinteractive.common.Utilities;
import net.wizartinteractive.database.DBManager;
import net.wizartinteractive.dbmodels.Call;
import net.wizartinteractive.dbmodels.CallType;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * the array adapter for the events list
 * 
 * @author Streetwizard
 * 
 */
public class CallsListAdapter extends ArrayAdapter<Call>
{
	// application context
	private Context appContext = null;

	private ArrayList<Call> calls = null;

	// a layout inflater to inflate the events list
	private LayoutInflater layoutInflater;

	private DBManager dbManager = null;

	private SparseBooleanArray selectedItems = null;

	public CallsListAdapter(Context context, int resource, ArrayList<Call> objects)
	{
		super(context, resource, objects);
		this.appContext = context;
		this.calls = objects;
		this.layoutInflater = (LayoutInflater) this.appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (this.dbManager == null)
		{
			DBManager.initializeDB(this.appContext);
			this.dbManager = DBManager.getInstance();
		}

		this.selectedItems = new SparseBooleanArray();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder viewHolder;

		// final Call tempItemList = getItem(position);
		final Call tempItemList = this.calls.get(position);

		// this check improves performance to only create the elements to fill the device screen
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.call_item, parent, false);

			viewHolder = new ViewHolder();
			viewHolder.contact = (ImageView) convertView.findViewById(R.id.contact_imageView);
			viewHolder.phoneNumber = (TextView) convertView.findViewById(R.id.phoneNumber_textView);
			viewHolder.phoneDirection = (ImageView) convertView.findViewById(R.id.phone_imageView);
			viewHolder.details = (TextView) convertView.findViewById(R.id.callDetails_textView);
			viewHolder.date = (TextView) convertView.findViewById(R.id.date_textView);
			viewHolder.popup = (ImageButton) convertView.findViewById(R.id.popup_imageButton);
			viewHolder.popup.setFocusable(false);
			viewHolder.popup.setClickable(false);
			viewHolder.popup.setOnClickListener(new PopupClickListener(tempItemList));
			viewHolder.popup.setTag(tempItemList.getId());
			viewHolder.deletecheck = (CheckBox) convertView.findViewById(R.id.delete_checkBox);

			String contactName = this.getContactName(tempItemList.getIncomingNumber());
			InputStream photo = this.getContactPhoto(tempItemList.getIncomingNumber());

			// contact photo
			if (photo != null)
			{
				viewHolder.contact.setImageBitmap(BitmapFactory.decodeStream(photo));
			}
			else
			{
				viewHolder.contact.setImageResource(R.drawable.ic_default_contact_photo);
			}

			// contact name
			if (contactName != null)
			{
				viewHolder.phoneNumber.setText(contactName);
			}
			else
			{
				viewHolder.phoneNumber.setText(tempItemList.getIncomingNumber());
			}

			// call type
			if (tempItemList.getType().getType() == CallType.INCOMING.getType())
			{
				viewHolder.phoneDirection.setImageResource(android.R.drawable.sym_call_incoming);
			}
			else
			{
				viewHolder.phoneDirection.setImageResource(android.R.drawable.sym_call_outgoing);
			}

			viewHolder.details.setText(String.format("%s", Utilities.ConvertMilisecondsToHMS(tempItemList.getDuration())));
			viewHolder.date.setText(String.format("%s", Utilities.ConvertDateToShortDateString(tempItemList.getDate())));

			convertView.setId((int) tempItemList.getId());

			convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (ViewHolder) convertView.getTag();
		}

		// if (CallsListFragment.isDeleteMode)
		// {
		// viewHolder.deletecheck.setVisibility(View.VISIBLE);
		// viewHolder.popup.setVisibility(View.GONE);
		// }
		// else
		// {
		// viewHolder.deletecheck.setVisibility(View.GONE);
		// viewHolder.popup.setVisibility(View.VISIBLE);
		// }

		if (this.selectedItems.get(position))
		{
			convertView.setBackgroundResource(R.drawable.abc_list_pressed_holo_dark);
		}
		else
		{
			if (tempItemList.getFavorite())
			{
				convertView.setBackgroundResource(R.drawable.list_item_focused);
			}
			else
			{
				convertView.setBackgroundResource(R.drawable.abc_list_divider_holo_dark);
			}
		}

		return convertView;
	}

	public void setNewSelection(int position, boolean value)
	{
		this.selectedItems.put(position, value);

		notifyDataSetChanged();
	}

	public boolean isPositionChecked(int position)
	{
		boolean result = this.selectedItems.get(position);

		return result;
	}

	public void removeSelection(int position)
	{
		this.selectedItems.delete(position);

		notifyDataSetChanged();
	}

	public void clearSelection()
	{
		this.selectedItems = new SparseBooleanArray();

		notifyDataSetChanged();
	}

	public SparseBooleanArray getSelectedItems()
	{
		return this.selectedItems;
	}

	private String getContactName(String phoneNumber)
	{

		String contactName = null;

		Uri contactUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		Cursor cursor = this.appContext.getContentResolver().query(contactUri, new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);

		if (cursor == null)
		{
			return null;
		}

		try
		{
			if (cursor.moveToFirst())
			{
				contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			}
		}
		finally
		{
			cursor.close();
		}

		return contactName;
	}

	private InputStream getContactPhoto(String phoneNumber)
	{

		if (this.getContactName(phoneNumber) != null)
		{
			Uri contactUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
			Cursor cursor = this.appContext.getContentResolver().query(contactUri, new String[] { PhoneLookup._ID }, null, null, null);

			long contactId = 0;

			if (cursor == null)
			{
				return null;
			}

			try
			{
				if (cursor.moveToFirst())
				{
					contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
				}
			}
			finally
			{
				cursor.close();
			}

			return this.getContactPhoto(contactId);
		}

		return null;
	}

	private InputStream getContactPhoto(long contactId)
	{
		Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
		Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
		Cursor cursor = this.appContext.getContentResolver().query(photoUri, new String[] { ContactsContract.CommonDataKinds.Photo.PHOTO }, null, null, null);

		if (cursor == null)
		{
			return null;
		}
		try
		{
			if (cursor.moveToFirst())
			{
				byte[] data = cursor.getBlob(0);
				if (data != null)
				{
					return new ByteArrayInputStream(data);
				}
			}
		}
		finally
		{
			cursor.close();
		}

		return null;
	}

	private class ViewHolder
	{
		ImageView contact;
		TextView phoneNumber;
		ImageView phoneDirection;
		TextView details;
		TextView date;
		ImageButton popup;
		CheckBox deletecheck;
	}

	private class PopupClickListener implements OnClickListener, OnMenuItemClickListener
	{
		private Call call;

		public PopupClickListener(Call call)
		{
			this.call = call;
		}

		@Override
		public void onClick(View view)
		{
			PopupMenu popup = new PopupMenu(appContext, view);
			popup.setOnMenuItemClickListener(this);

			MenuInflater inflater = popup.getMenuInflater();
			inflater.inflate(R.menu.list_popup, popup.getMenu());

			popup.show();
		}

		@Override
		public boolean onMenuItemClick(MenuItem menuItem)
		{
			switch (menuItem.getItemId())
			{
			case R.id.action_play:

				Intent viewIntent = new Intent(Intent.ACTION_VIEW);
				File file = new File(this.call.getFilePath());

				if (file.exists())
				{
					viewIntent.setDataAndType(Uri.fromFile(file), "audio/*");
					appContext.startActivity(Intent.createChooser(viewIntent, "Complete action using..."));
				}
				else
				{
					Toast toast = Toast.makeText(appContext, appContext.getString(R.string.mediaPlayingFileError), Toast.LENGTH_LONG);
					toast.show();
				}

				return true;

			case R.id.action_favourite:

				ContentValues values = new ContentValues();
				this.call.setFavorite(!this.call.getFavorite());
				values.put("Favorite", this.call.getFavorite());

				dbManager.updateCall(this.call.getId(), values);

				calls = dbManager.getCalls();

				notifyDataSetChanged();

				return true;

			default:

				return false;
			}
		}
	}
}
