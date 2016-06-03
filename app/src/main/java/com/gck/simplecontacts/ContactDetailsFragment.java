package com.gck.simplecontacts;


import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


public class ContactDetailsFragment extends Fragment {
    private static final String KEY_CONTACT_ID = "param1";
    private static final String TAG = "ContactDetailsFragment";

    private int contactId;
    private ListView listView;
    private NumbersAdapter numbersAdapter;


    public ContactDetailsFragment() {
    }

    public static ContactDetailsFragment newInstance(int contactId) {
        ContactDetailsFragment fragment = new ContactDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_CONTACT_ID, contactId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            contactId = getArguments().getInt(KEY_CONTACT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "Contact id " + contactId);
        View view = inflater.inflate(R.layout.fragment_contact_details, container, false);

        listView = (ListView) view.findViewById(R.id.numbers_list_view);
        numbersAdapter = new NumbersAdapter(getActivity(), ContactUtils.getContactPhoneNumber(getActivity(), contactId));
        listView.setAdapter(numbersAdapter);
        return view;
    }

}
