package com.example.personalfinance.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.personalfinance.R;
import com.example.personalfinance.fragment.AccountItemListFragment;
import com.example.personalfinance.fragment.GraphFragment;

import static com.example.personalfinance.etc.Const.SCOPE_THIS_MONTH;
import static com.example.personalfinance.etc.Const.SCOPE_TODAY;
import static com.example.personalfinance.etc.Const.SCOPE_YESTERDAY;

public class MyPagerAdapter extends FragmentPagerAdapter {

    public static final int PAGER_TYPE_LIST = 0;
    public static final int PAGER_TYPE_GRAPH = 1;

    private Context mContext;
    private int mPagerTye;

    public MyPagerAdapter(Context context, FragmentManager fm, int pagerType) {
        super(fm);
        mContext = context.getApplicationContext();
        mPagerTye = pagerType;
    }

    @Override
    public Fragment getItem(int position) {
        switch (mPagerTye) {
            case PAGER_TYPE_LIST:
                return AccountItemListFragment.newInstance(position);
            case PAGER_TYPE_GRAPH:
                return GraphFragment.newInstance(position);
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case SCOPE_TODAY:
                return mContext.getString(R.string.today);
            case SCOPE_YESTERDAY:
                return mContext.getString(R.string.yesterday);
            case SCOPE_THIS_MONTH:
                return mContext.getString(R.string.this_month);
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
