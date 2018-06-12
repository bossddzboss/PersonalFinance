package com.example.personalfinance.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.personalfinance.R;
import com.example.personalfinance.adapter.AlarmItemListAdapter;
import com.example.personalfinance.db.FinanceDb;
import com.example.personalfinance.model.AlarmItem;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AlarmListFragmentListener} interface
 * to handle interaction events.
 */
public class AlarmListFragment extends Fragment {

    private List<AlarmItem> mAlarmItemList = new ArrayList<>();
    private AlarmListFragmentListener mListener;

    private ListView mItemListView;

    public AlarmListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_alarm_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity == null) return;
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar == null) return;

        actionBar.setTitle("การแจ้งเตือน");

        View tabLayout = activity.findViewById(R.id.tab_layout);
        tabLayout.setVisibility(View.GONE);

        View accountBalanceLayout = activity.findViewById(R.id.account_balance_layout);
        accountBalanceLayout.setVisibility(View.GONE);

        mItemListView = view.findViewById(R.id.item_list_view);
        TextView emptyView = view.findViewById(R.id.empty_view);
        mItemListView.setEmptyView(emptyView);

        AlarmItemListAdapter adapter = new AlarmItemListAdapter(
                getContext(),
                mAlarmItemList
        );
        mItemListView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAlarmItemList.clear();
        mAlarmItemList.addAll(loadDataFromDb());
        ((AlarmItemListAdapter) mItemListView.getAdapter()).notifyDataSetChanged();
    }

    private List<AlarmItem> loadDataFromDb() {
        FinanceDb db = new FinanceDb(getContext());
        return db.getAlarmItemList();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AlarmListFragmentListener) {
            mListener = (AlarmListFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AlarmListFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface AlarmListFragmentListener {
        void onFragmentInteraction(Uri uri);
    }
}
