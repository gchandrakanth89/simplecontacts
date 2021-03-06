package com.gck.simplecontacts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;


public class ContactsListFragment extends Fragment {


    private static final String TAG = "ContactsListFragment";
    private ListView listView;
    private ContactsAdapter contactsAdapter;
    private Cursor searchCursor;
    private LooperThread looperThread;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (contactsAdapter == null) {
                contactsAdapter = new ContactsAdapter(getActivity(), searchCursor);
                listView.setAdapter(contactsAdapter);
            } else {
                String searchStr = (String) msg.obj;
                contactsAdapter.changeCursor(searchCursor, searchStr);
            }
        }
    };
    private EditText editText;


    public ContactsListFragment() {
    }

    public static ContactsListFragment newInstance() {
        ContactsListFragment fragment = new ContactsListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            looperThread = new LooperThread();
            looperThread.start();
            Log.d(TAG, "Chandu onAttach context");
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            looperThread = new LooperThread();
            looperThread.start();
            Log.d(TAG, "Chandu onAttach Activity");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_contacts_list, container, false);

        listView = (ListView) view.findViewById(R.id.list_view);
        looperThread.post(null);
        listView.setOnItemClickListener(listListener);
        listView.setOnItemLongClickListener(onItemLongClickListener);

        editText = (EditText) view.findViewById(R.id.edit_text);
        editText.addTextChangedListener(textWatcher);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return view;
    }

    AdapterView.OnItemClickListener listListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cursor = (Cursor) parent.getItemAtPosition(position);
            int contactId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));

            ContactDetailsActivity.startActivity(getActivity(), contactId, contactName);

        }
    };

    AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

            Cursor cursor = (Cursor) parent.getItemAtPosition(position);
            final int contactId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            final String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
            String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Alert");
            builder.setMessage("Do you want to delete " + contactName);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, Uri.encode(lookupKey));
                    int delete = getActivity().getContentResolver().delete(uri, null, null);
                    Log.d(TAG, delete + "");
                }
            });
            builder.setNegativeButton("Cancel", null);

            builder.show();

            return true;
        }
    };

    @Override
    public void onDetach() {
        if (searchCursor != null) {
            Log.d(TAG, "Closing cursor");
            searchCursor.close();
        }

        looperThread.quitLooper();
        handler.removeCallbacksAndMessages(null);
        super.onDetach();
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (count == 0) {
                editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.search_unselected, 0, 0, 0);
            } else {
                editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.search_selected, 0, 0, 0);
            }
            looperThread.post(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private class MyRunnable implements Runnable {

        private String search;

        public MyRunnable(String search) {
            this.search = search;
        }

        @Override
        public void run() {
            if (TextUtils.isEmpty(search)) {
                searchCursor = ContactUtils.getAllContactsCursor(getActivity());
            } else {
                searchCursor = ContactUtils.getSearchCursor(getActivity(), search);
            }
            Message message = Message.obtain();
            message.obj = search;
            handler.sendMessage(message);
        }
    }

    private class LooperThread extends Thread {
        private Handler bgHandler;

        @Override
        public void run() {
            Looper.prepare();
            bgHandler = new Handler();
            Looper.loop();
        }

        public void post(String search) {

            //Sometimes it takes time to prepare the loop. So calling post()
            // immediately after run() method will give NPE
            if (bgHandler != null) {
                bgHandler.removeCallbacksAndMessages(null);
                bgHandler.post(new MyRunnable(search));
            }
        }

        public void quitLooper() {
            bgHandler.getLooper().quit();
        }
    }

}


