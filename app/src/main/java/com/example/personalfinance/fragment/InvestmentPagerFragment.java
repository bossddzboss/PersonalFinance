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

import com.example.personalfinance.R;

public class InvestmentPagerFragment extends Fragment {

    private InvestmentPagerFragmentListener mListener;

    public InvestmentPagerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_investment_pager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity == null) return;
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar == null) return;

        actionBar.setTitle("การลงทุน");

        View tabLayout = activity.findViewById(R.id.tab_layout);
        tabLayout.setVisibility(View.GONE);

        View accountBalanceLayout = activity.findViewById(R.id.account_balance_layout);
        accountBalanceLayout.setVisibility(View.GONE);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof InvestmentPagerFragmentListener) {
            mListener = (InvestmentPagerFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement InvestmentPagerFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface InvestmentPagerFragmentListener {
        void onFragmentInteraction(Uri uri);
    }
}
