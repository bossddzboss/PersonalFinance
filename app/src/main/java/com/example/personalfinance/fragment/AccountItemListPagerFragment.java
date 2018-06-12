package com.example.personalfinance.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.personalfinance.R;
import com.example.personalfinance.adapter.MyPagerAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PagerFragmentListener} interface
 * to handle interaction events.
 */
public class AccountItemListPagerFragment extends Fragment {

    private static final String ARG_POSITION = "position";

    private int mPosition;
    private PagerFragmentListener mListener;

    public AccountItemListPagerFragment() {
        // Required empty public constructor
    }

    public static AccountItemListPagerFragment newInstance(int position) {
        AccountItemListPagerFragment fragment = new AccountItemListPagerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPosition = getArguments().getInt(ARG_POSITION);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity == null) return;
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar == null) return;

        actionBar.setTitle("รายการ");

        View accountBalanceLayout = activity.findViewById(R.id.account_balance_layout);
        accountBalanceLayout.setVisibility(View.GONE);

        TabLayout tabLayout = activity.findViewById(R.id.tab_layout);
        tabLayout.setVisibility(View.VISIBLE);

        setupViewPagerAndTabs(view, tabLayout);
    }

    private void setupViewPagerAndTabs(View view, TabLayout tabLayout) {
        if (getContext() == null) return;

        FragmentPagerAdapter adapter = new MyPagerAdapter(
                getContext(), getChildFragmentManager(), MyPagerAdapter.PAGER_TYPE_LIST
        );
        ViewPager viewPager = view.findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(mPosition);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PagerFragmentListener) {
            mListener = (PagerFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PagerFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface PagerFragmentListener {
        void onFragmentInteraction(Uri uri);
    }
}
