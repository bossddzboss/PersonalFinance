package com.example.personalfinance;

import android.animation.Animator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.personalfinance.fragment.AccountItemListFragment;
import com.example.personalfinance.fragment.AccountItemListPagerFragment;
import com.example.personalfinance.fragment.AlarmListFragment;
import com.example.personalfinance.fragment.GraphPagerFragment;
import com.example.personalfinance.fragment.HomeFragment;
import com.example.personalfinance.model.AccountItem;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import static com.example.personalfinance.IncomeExpenseActivity.KEY_INCOME_OR_EXPENSE;

/*
* แก้ไข-ลบ
* เพิ่มความสูง bottom nav
* ปิดคีย์บอร์ด ตอนกรอกข้อมูล
* แยกแท็บรายรับ-รายจ่าย
* เปลี่ยนตำแหน่ง วันนี้ เมื่อวาน เดือนนี้ (วันนี้ตรงกลาง)
* ลบข้อมูลทั้งหมด ในเมนูข้าง
* */

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        HomeFragment.HomeFragmentListener,
        AccountItemListPagerFragment.PagerFragmentListener,
        AccountItemListFragment.AccountItemListFragmentListener,
        AlarmListFragment.AlarmListFragmentListener,
        BottomNavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();
    private static final String TAG_FRAGMENT_HOME = "home_fragment";
    private static final String TAG_FRAGMENT_LIST_PAGER = "list_pager_fragment";
    private static final String TAG_FRAGMENT_GRAPH_PAGER = "graph_pager_fragment";
    private static final String TAG_FRAGMENT_ALARM_LIST = "alarm_list_fragment";
    private static final int ANIMATION_DURATION = 250;

    private FloatingActionButton mIncomeFab, mExpenseFab, mInvestmentFab;
    private View mMaskView;
    private boolean mIsFabMenuOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupFab();
        setupToolbarAndDrawer();
        setupBottomNav();

        loadHomeFragment();
    }

    private void setupFab() {
        mIncomeFab = findViewById(R.id.income_fab);
        mIncomeFab.setOnClickListener(this);
        mExpenseFab = findViewById(R.id.expense_fab);
        mExpenseFab.setOnClickListener(this);
        mInvestmentFab = findViewById(R.id.investment_fab);
        mInvestmentFab.setOnClickListener(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mIsFabMenuOpen) {
                    openFabMenu();
                } else {
                    closeFabMenu();
                }
            }
        });

        mMaskView = findViewById(R.id.mask_view);
        mMaskView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsFabMenuOpen) {
                    closeFabMenu();
                }
            }
        });
    }

    private void setupToolbarAndDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupBottomNav() {
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottom_navigation);
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        //FragmentManager fm = getSupportFragmentManager();

                        switch (item.getItemId()) {
                            case R.id.action_home:
                                loadHomeFragment();
                                break;
                            case R.id.action_summary:
                                loadGraphPagerFragment(0);
                                break;
                            case R.id.action_alarm:
                                loadAlarmListFragment();
                                break;
                        }
                        return true;
                    }
                });
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.income_fab:
                intent = new Intent(this, IncomeExpenseActivity.class);
                intent.putExtra(KEY_INCOME_OR_EXPENSE, AccountItem.TYPE_INCOME);
                break;
            case R.id.expense_fab:
                intent = new Intent(this, IncomeExpenseActivity.class);
                intent.putExtra(KEY_INCOME_OR_EXPENSE, AccountItem.TYPE_EXPENSE);
                break;
            case R.id.investment_fab:
                Toast.makeText(this, "under construction", Toast.LENGTH_SHORT).show();
                break;
        }
        if (intent != null) startActivity(intent);
        if (mIsFabMenuOpen) closeFabMenu();
    }

    private void openFabMenu() {
        mIsFabMenuOpen = true;
        mMaskView.setVisibility(View.VISIBLE);
        mMaskView.animate().alpha(1.0f).setDuration(ANIMATION_DURATION).setListener(null);
        mIncomeFab.animate().translationX(-getResources().getDimension(R.dimen.translate_x_fab)).setDuration(ANIMATION_DURATION);
        mIncomeFab.animate().translationY(-getResources().getDimension(R.dimen.translate_y_fab)).setDuration(ANIMATION_DURATION);
        mExpenseFab.animate().translationY(-getResources().getDimension(R.dimen.translate_y_fab)).setDuration(ANIMATION_DURATION);
        mInvestmentFab.animate().translationX(getResources().getDimension(R.dimen.translate_x_fab)).setDuration(ANIMATION_DURATION);
        mInvestmentFab.animate().translationY(-getResources().getDimension(R.dimen.translate_y_fab)).setDuration(ANIMATION_DURATION);
    }

    private void closeFabMenu() {
        mIsFabMenuOpen = false;
        mMaskView.animate().alpha(0.0f).setDuration(500).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}

            @Override
            public void onAnimationEnd(Animator animator) {
                mMaskView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });
        mIncomeFab.animate().translationX(0).setDuration(ANIMATION_DURATION);
        mIncomeFab.animate().translationY(0).setDuration(ANIMATION_DURATION);
        mExpenseFab.animate().translationX(0).setDuration(ANIMATION_DURATION);
        mExpenseFab.animate().translationY(0).setDuration(ANIMATION_DURATION);
        mInvestmentFab.animate().translationX(0).setDuration(ANIMATION_DURATION);
        mInvestmentFab.animate().translationY(0).setDuration(ANIMATION_DURATION);
    }

    private void loadHomeFragment() {
        getSupportFragmentManager().beginTransaction().replace(
                R.id.fragment_container,
                new HomeFragment(),
                TAG_FRAGMENT_HOME
        ).commit();
    }

    private void loadListPagerFragment(int position) {
        getSupportFragmentManager().beginTransaction().replace(
                R.id.fragment_container,
                AccountItemListPagerFragment.newInstance(position),
                TAG_FRAGMENT_LIST_PAGER
        ).commit();
    }

    private void loadGraphPagerFragment(int position) {
        getSupportFragmentManager().beginTransaction().replace(
                R.id.fragment_container,
                GraphPagerFragment.newInstance(position),
                TAG_FRAGMENT_GRAPH_PAGER
        ).commit();
    }

    private void loadAlarmListFragment() {
        getSupportFragmentManager().beginTransaction().replace(
                R.id.fragment_container,
                new AlarmListFragment(),
                TAG_FRAGMENT_ALARM_LIST
        ).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mIsFabMenuOpen) {
            closeFabMenu();
        } else if (getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_HOME) == null) {
            loadHomeFragment();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClickScope(int scope) {
        loadListPagerFragment(scope);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }
}
