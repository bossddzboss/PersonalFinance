package com.example.personalfinance.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.personalfinance.R;
import com.example.personalfinance.adapter.AccountItemListAdapter;
import com.example.personalfinance.db.FinanceDb;
import com.example.personalfinance.etc.Const;
import com.example.personalfinance.model.AccountItem;
import com.example.personalfinance.model.DateHeaderItem;
import com.example.personalfinance.model.Item;
import com.example.personalfinance.model.TotalItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AccountItemListFragmentListener} interface
 * to handle interaction events.
 * Use the {@link AccountItemListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountItemListFragment extends Fragment {

    private static final String ARG_SCOPE = "scope";

    private int mScope;
    private List<Item> mItemList = new ArrayList<>();
    private AccountItemListFragmentListener mListener;

    private ListView mItemListView;

    public AccountItemListFragment() {
        // Required empty public constructor
    }

    public static AccountItemListFragment newInstance(int scope) {
        AccountItemListFragment fragment = new AccountItemListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SCOPE, scope);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mScope = getArguments().getInt(ARG_SCOPE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account_item_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mItemListView = view.findViewById(R.id.item_list_view);
        TextView emptyView = view.findViewById(R.id.empty_view);
        mItemListView.setEmptyView(emptyView);

        AccountItemListAdapter adapter = new AccountItemListAdapter(
                getContext(),
                mItemList
        );
        mItemListView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        mItemList.clear();
        mItemList.addAll(loadDataFromDb());
        ((AccountItemListAdapter) mItemListView.getAdapter()).notifyDataSetChanged();
    }

    private List<Item> loadDataFromDb() {
        FinanceDb db = new FinanceDb(getContext());
        Calendar calendar = Calendar.getInstance();
        List<Item> itemList = new ArrayList<>();
        List<AccountItem> accountItemList = new ArrayList<>();

        switch (mScope) {
            case Const.SCOPE_TODAY:
                accountItemList = db.getAccountItemListByDate(calendar.getTime());
                break;
            case Const.SCOPE_YESTERDAY:
                calendar.add(Calendar.DATE, -1);
                accountItemList = db.getAccountItemListByDate(calendar.getTime());
                break;
            case Const.SCOPE_THIS_MONTH:
                accountItemList = db.getAccountItemListByMonth(calendar.getTime());
                break;
        }

        int totalAmount = 0;
        String previousDate = "";

        if (!accountItemList.isEmpty()) {
            for (AccountItem accountItem : accountItemList) {
                switch (accountItem.type) {
                    case AccountItem.TYPE_INCOME:
                        totalAmount += accountItem.amount;
                        break;
                    case AccountItem.TYPE_EXPENSE:
                        totalAmount -= accountItem.amount;
                        break;
                    case AccountItem.TYPE_INVESTMENT:
                        totalAmount -= accountItem.amount;
                        break;
                }

                if (!accountItem.date.equals(previousDate)) {
                    itemList.add(new DateHeaderItem(accountItem.date));
                    previousDate = accountItem.date;
                }
                itemList.add(accountItem);
            }

            itemList.add(new TotalItem(totalAmount));
        }
        return itemList;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AccountItemListFragmentListener) {
            mListener = (AccountItemListFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AccountItemListFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface AccountItemListFragmentListener {
        void onFragmentInteraction(Uri uri);
    }
}
