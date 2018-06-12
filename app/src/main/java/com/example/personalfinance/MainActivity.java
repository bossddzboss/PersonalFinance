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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.personalfinance.db.FinanceDb;
import com.example.personalfinance.fragment.AccountItemListFragment;
import com.example.personalfinance.fragment.AccountItemListPagerFragment;
import com.example.personalfinance.fragment.AlarmListFragment;
import com.example.personalfinance.fragment.GraphPagerFragment;
import com.example.personalfinance.fragment.HomeFragment;
import com.example.personalfinance.fragment.InvestmentPagerFragment;
import com.example.personalfinance.model.AccountItem;
import com.example.personalfinance.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.personalfinance.IncomeExpenseActivity.KEY_INCOME_OR_EXPENSE;

/*
* แก้ไข-ลบ
* ---เพิ่มความสูง bottom nav
* ---ปิดคีย์บอร์ด ตอนกรอกข้อมูล
* ---แยกแท็บรายรับ-รายจ่าย
* ---เปลี่ยนตำแหน่ง วันนี้ เมื่อวาน เดือนนี้ (วันนี้ตรงกลาง)
* ลบข้อมูลทั้งหมด ในเมนูข้าง
* ---สถานะปุ่มใน bottom nav
* */

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        HomeFragment.HomeFragmentListener,
        AccountItemListPagerFragment.PagerFragmentListener,
        AccountItemListFragment.AccountItemListFragmentListener,
        AlarmListFragment.AlarmListFragmentListener,
        InvestmentPagerFragment.InvestmentPagerFragmentListener,
        BottomNavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener {

    public static final List<AccountItem> gAccountItemList = new ArrayList<>();

    private static final String TAG = MainActivity.class.getName();
    private static final String TAG_FRAGMENT_HOME = "home_fragment";
    private static final String TAG_FRAGMENT_LIST_PAGER = "list_pager_fragment";
    private static final String TAG_FRAGMENT_GRAPH_PAGER = "graph_pager_fragment";
    private static final String TAG_FRAGMENT_INVESTMENT_PAGER = "investment_pager_fragment";
    private static final String TAG_FRAGMENT_ALARM_LIST = "alarm_list_fragment";
    private static final int ANIMATION_DURATION = 250;

    NavigationView mNavView;
    BottomNavigationViewEx mBottomNav;
    private FloatingActionButton mIncomeFab, mExpenseFab, mInvestmentFab;
    private View mMaskView;
    private boolean mIsFabMenuOpen = false;

    private DatabaseReference mDatabase;
    private boolean mFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        //getAccountItemListByDateFirebase();



        setupFab();
        setupToolbarAndDrawer();
        setupBottomNav();

        loadHomeFragment();
        //getUserData();
    }

    private void getAccountItemListByDateFirebase() {
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) return;

        String uId = account.getId();

        DatabaseReference db = FirebaseDatabase.getInstance().getReference("account/" + uId);
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String, AccountItem>> genericTypeIndicator =
                        new GenericTypeIndicator<Map<String, AccountItem>>(){};
                Map<String, AccountItem> accountItemMap = dataSnapshot.getValue(genericTypeIndicator);

                gAccountItemList.clear();
                for (Map.Entry<String, AccountItem> accountItem : accountItemMap.entrySet()) {
                    String msg = "";
                    String key = accountItem.getKey();
                    AccountItem value = accountItem.getValue();
                    msg += key + ":\n";
                    msg += "\tid: " + value.id + "\n";
                    msg += "\ttitle: " + value.title + "\n";
                    msg += "\tdescription: " + value.description + "\n";
                    msg += "\tamount: " + value.amount + "\n";
                    msg += "\ttype: " + value.type + "\n";
                    msg += "\tcategoryId: " + value.categoryId + "\n";
                    msg += "\tdate: " + value.date + "\n\n";

                    Log.i(TAG, msg);
                    gAccountItemList.add(value);
                    value.category = new FinanceDb(MainActivity.this).getCategoryById(value.categoryId);
                }

                if (!mFlag) {
                    loadHomeFragment();
                    mFlag = true;
                }

                //Utils.showModalOkDialog(mContext, null, msg, null);

                //HashMap<GenericTypeIndicator, GenericTypeIndicator> value = dataSnapshot.getValue(HashMap.class);
                //Log.i(TAG, "Value is " + value.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read value.", databaseError.toException());
            }
        });

    }

    private void getUserData() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            String personName = account.getDisplayName();
            String personEmail = account.getEmail();
            Uri personPhoto = account.getPhotoUrl();
            String personId = account.getId();

            /*Utils.showModalOkDialog(
                    this,
                    "User Profile",
                    String.format(
                            Locale.getDefault(),
                            "Name: %s, Photo URI: %s",
                            personName, personPhoto
                    ),
                    null
            );*/

            Log.i(TAG, "Firebase UID: " + personId);

            View headerLayout = mNavView.getHeaderView(0);
            ImageView profileImageView = headerLayout.findViewById(R.id.profile_image_view);
            Glide.with(this)
                    .load(personPhoto)
                    .into(profileImageView);

            TextView nameTextView = headerLayout.findViewById(R.id.name_text_view);
            nameTextView.setText(personName);

            TextView emailTextView = headerLayout.findViewById(R.id.email_text_view);
            emailTextView.setText(personEmail);
        }
    }

    private void test() {
        User user = new User("promlert", "promlert@gmail.com");

        mDatabase.child("users").child("0001").setValue(user);
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
                Intent intent = new Intent(MainActivity.this, IncomeExpenseActivity.class);
                startActivity(intent);

                /*if (!mIsFabMenuOpen) {
                    openFabMenu();
                } else {
                    closeFabMenu();
                }*/
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

        mNavView = findViewById(R.id.nav_view);
        mNavView.getMenu().getItem(0).setChecked(true);
        mNavView.setNavigationItemSelectedListener(this);
    }

    private void setupBottomNav() {
        mBottomNav = findViewById(R.id.bottom_navigation);
        mBottomNav.enableAnimation(false);
        mBottomNav.enableShiftingMode(false);
        mBottomNav.enableItemShiftingMode(false);
        mBottomNav.setIconSize(30, 30);
        mBottomNav.setTextSize(14);
        mBottomNav.setIconsMarginTop(8);
        mBottomNav.setItemBackground(0, R.color.color_primary_dark);
        mBottomNav.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        //FragmentManager fm = getSupportFragmentManager();
                        mBottomNav.setItemBackground(0, R.color.color_primary);
                        mBottomNav.setItemBackground(1, R.color.color_primary);
                        mBottomNav.setItemBackground(2, R.color.color_primary);
                        mBottomNav.setItemBackground(3, R.color.color_primary);
                        mBottomNav.setItemBackground(4, R.color.color_primary);

                        switch (item.getItemId()) {
                            case R.id.action_home:
                                mBottomNav.setItemBackground(0, R.color.color_primary_dark);
                                mNavView.getMenu().getItem(0).setChecked(true);
                                loadHomeFragment();
                                break;
                            case R.id.action_summary:
                                mBottomNav.setItemBackground(1, R.color.color_primary_dark);
                                mNavView.getMenu().getItem(1).setChecked(true);
                                loadGraphPagerFragment(0);
                                break;
                            case R.id.action_investment:
                                mBottomNav.setItemBackground(3, R.color.color_primary_dark);
                                mNavView.getMenu().getItem(2).setChecked(true);
                                loadInvestmentPagerFragment(0);
                                break;
                            case R.id.action_alarm:
                                mBottomNav.setItemBackground(4, R.color.color_primary_dark);
                                mNavView.getMenu().getItem(3).setChecked(true);
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

    private void loadInvestmentPagerFragment(int position) {
        getSupportFragmentManager().beginTransaction().replace(
                R.id.fragment_container,
                new InvestmentPagerFragment(),
                TAG_FRAGMENT_INVESTMENT_PAGER
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

        mBottomNav.setItemBackground(0, R.color.color_primary);
        mBottomNav.setItemBackground(1, R.color.color_primary);
        mBottomNav.setItemBackground(2, R.color.color_primary);
        mBottomNav.setItemBackground(3, R.color.color_primary);
        mBottomNav.setItemBackground(4, R.color.color_primary);

        if (id == R.id.nav_home) {
            mBottomNav.getMenu().getItem(0).setChecked(true);
            mBottomNav.setItemBackground(0, R.color.color_primary_dark);
            loadHomeFragment();
        } else if (id == R.id.nav_summary) {
            mBottomNav.getMenu().getItem(1).setChecked(true);
            mBottomNav.setItemBackground(1, R.color.color_primary_dark);
            loadGraphPagerFragment(0);
        } else if (id == R.id.nav_investment) {
            mBottomNav.getMenu().getItem(3).setChecked(true);
            mBottomNav.setItemBackground(3, R.color.color_primary_dark);
            loadInvestmentPagerFragment(0);
        } else if (id == R.id.nav_alarm) {
            mBottomNav.getMenu().getItem(4).setChecked(true);
            mBottomNav.setItemBackground(4, R.color.color_primary_dark);
            loadAlarmListFragment();
        } else if (id == R.id.nav_clear_data) {
            Toast.makeText(MainActivity.this, "Under construction!", Toast.LENGTH_LONG).show();
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
