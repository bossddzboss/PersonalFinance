package com.example.personalfinance.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.personalfinance.R;
import com.example.personalfinance.db.FinanceDb;
import com.example.personalfinance.etc.Utils;
import com.example.personalfinance.model.AccountBalance;
import com.example.personalfinance.model.AccountItem;

import java.util.Calendar;
import java.util.List;

import static com.example.personalfinance.etc.Const.SCOPE_THIS_MONTH;
import static com.example.personalfinance.etc.Const.SCOPE_TODAY;
import static com.example.personalfinance.etc.Const.SCOPE_YESTERDAY;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragmentListener} interface
 * to handle interaction events.
 */
public class HomeFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = HomeFragment.class.getName();

    private HomeFragmentListener mListener;

    TextView mAccountBalanceTextView, mTotalTodayTextView;
    TextView mTotalYesterdayTextView, mTotalThisMonthTextView;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity == null) return;
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar == null) return;

        actionBar.setTitle(activity.getString(R.string.app_name));

        View tabLayout = activity.findViewById(R.id.tab_layout);
        tabLayout.setVisibility(View.GONE);

        View accountBalanceLayout = activity.findViewById(R.id.account_balance_layout);
        accountBalanceLayout.setVisibility(View.VISIBLE);

        mAccountBalanceTextView = activity.findViewById(R.id.account_balance_text_view);
        mTotalTodayTextView = view.findViewById(R.id.total_today_text_view);
        mTotalYesterdayTextView = view.findViewById(R.id.total_yesterday_text_view);
        mTotalThisMonthTextView = view.findViewById(R.id.total_this_month_text_view);

        setCardListener(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUi();
    }

    private void updateUi() {
        FinanceDb db = new FinanceDb(getContext());
        List<AccountItem> accountItemList;

        // Account balance
        AccountBalance accountBalance = db.getCurrentAccountBalance();
        mAccountBalanceTextView.setText(Utils.formatCurrency(accountBalance.balance));

        String lastUpdateText = "Last update: " + accountBalance.lastUpdate;
        Log.d(TAG, lastUpdateText);
        //Toast.makeText(getActivity(), lastUpdateText, Toast.LENGTH_SHORT).show();

        // Today
        accountItemList = db.getAccountItemListByDate(Calendar.getInstance().getTime());
        calculateTotalAndSetUi(accountItemList, mTotalTodayTextView);

        // Yesterday
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        accountItemList = db.getAccountItemListByDate(calendar.getTime());
        calculateTotalAndSetUi(accountItemList, mTotalYesterdayTextView);

        // This month
        accountItemList = db.getAccountItemListByMonth(Calendar.getInstance().getTime());
        calculateTotalAndSetUi(accountItemList, mTotalThisMonthTextView);
    }

    private void setCardListener(View view) {
        View totalTodayCardView = view.findViewById(R.id.total_today_card_view);
        View totalYesterdayCardView = view.findViewById(R.id.total_yesterday_card_view);
        View totalThisMonthCardView = view.findViewById(R.id.total_this_month_card_view);

        totalTodayCardView.setOnClickListener(this);
        totalYesterdayCardView.setOnClickListener(this);
        totalThisMonthCardView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.total_today_card_view:
                mListener.onClickScope(SCOPE_TODAY);
                break;
            case R.id.total_yesterday_card_view:
                mListener.onClickScope(SCOPE_YESTERDAY);
                break;
            case R.id.total_this_month_card_view:
                mListener.onClickScope(SCOPE_THIS_MONTH);
                break;
        }
    }

    private void calculateTotalAndSetUi(List<AccountItem> accountItemList, TextView textView) {
        int total = 0;
        for (AccountItem accountItem : accountItemList) {
            if (accountItem.type == AccountItem.TYPE_INCOME) {
                total += accountItem.amount;
            } else if (accountItem.type == AccountItem.TYPE_EXPENSE) {
                total -= accountItem.amount;
            } else if (accountItem.type == AccountItem.TYPE_INVESTMENT) {
                total -= accountItem.amount;
            }
        }
        String totalString = Utils.formatCurrency(total);
        if (total > 0) {
            textView.setTextColor(getResources().getColor(R.color.income));
        } else if (total < 0) {
            textView.setTextColor(getResources().getColor(R.color.expense));
        } else {
            textView.setTextColor(getResources().getColor(R.color.grey));
        }
        textView.setText(totalString);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof HomeFragmentListener) {
            mListener = (HomeFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement HomeFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface HomeFragmentListener {
        void onClickScope(int scope);
    }
}
