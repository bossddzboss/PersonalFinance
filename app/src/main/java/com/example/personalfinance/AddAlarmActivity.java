package com.example.personalfinance;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.personalfinance.adapter.CategoryListAdapter;
import com.example.personalfinance.db.FinanceDb;
import com.example.personalfinance.etc.ExpenseAlarmManager;
import com.example.personalfinance.etc.GridSpacingItemDecoration;
import com.example.personalfinance.etc.Utils;
import com.example.personalfinance.model.AccountItem;
import com.example.personalfinance.model.Category;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddAlarmActivity extends AppCompatActivity implements View.OnClickListener, CategoryListAdapter.CategoryListAdapterListener {

    private static final String TAG = AddAlarmActivity.class.getName();

    private Category mSelectedCategory = null;
    private Calendar mCalendar = Calendar.getInstance();

    private EditText mDescriptionEditText, mDateEditText, mTimeEditText;
    private RecyclerView mCategoryRecyclerView;

    private final String[] mShortMonthNames = {
            "ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.",
            "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(this);

        setupDescriptionEditText();
        setupCategoryRecyclerView(AccountItem.TYPE_EXPENSE);
        setupDateTimeEditText();

        updateDateEditText();
        updateTimeEditText();
    }

    private void setupDescriptionEditText() {
        mDescriptionEditText = findViewById(R.id.description_edit_text);
        mDescriptionEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        mDescriptionEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    View view = AddAlarmActivity.this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                    }
                }
                return true;
            }
        });
    }

    private void setupCategoryRecyclerView(int type) {
        List<Category> categoryList = new FinanceDb(this).getCategoryByType(type);
        CategoryListAdapter adapter = new CategoryListAdapter(
                this,
                categoryList,
                this
        );
        mCategoryRecyclerView = findViewById(R.id.category_recycler_view);
        calculateGridColumn();
        mCategoryRecyclerView.setAdapter(adapter);
    }

    private void setupDateTimeEditText() {
        mDateEditText = findViewById(R.id.date_edit_text);
        mDateEditText.setOnClickListener(this);
        mTimeEditText = findViewById(R.id.time_edit_text);
        mTimeEditText.setOnClickListener(this);
    }

    // คำนวณหาจำนวนคอลัมน์แสดงภาพ category ที่เหมาะสมกับความกว้างจอ
    private void calculateGridColumn() {
        final View rootLayout = findViewById(R.id.root_layout);

        rootLayout.post(new Runnable() {
            @Override
            public void run() {
                try {
                    int spacingInDp = 0;
                    int spacingInPx = (int) Math.ceil(Utils.convertDpToPixel(spacingInDp));
                    Log.d(TAG, "Spacing in px: " + spacingInPx);

                    int imageWidthInPx = (int) Utils.convertDpToPixel(76);
                    Log.d(TAG, "Image width in px: " + imageWidthInPx);

                    //int screenWidthInPx = Utils.getScreenDimensionInPixel(getContext())[0];
                    int screenWidthInPx = rootLayout.getWidth() - (int) Utils.convertDpToPixel(32);
                    Log.d(TAG, "Screen width in px: " + screenWidthInPx);

                    int columnCount = (int) (Math.floor(
                            (screenWidthInPx - spacingInPx) / (imageWidthInPx + spacingInPx)
                    ));

                    String msg = String.format(
                            Locale.getDefault(),
                            "Screen dimension: %d x %d",
                            Utils.getScreenDimensionInPixel(AddAlarmActivity.this)[0],
                            Utils.getScreenDimensionInPixel(AddAlarmActivity.this)[1]
                    );
                    Log.d(TAG, msg);
                    msg = String.format(
                            Locale.getDefault(),
                            "Root layout dimension: %d x %d",
                            rootLayout.getWidth(),
                            rootLayout.getHeight()
                    );
                    Log.d(TAG, msg);
                    Log.d(TAG, "Column count: " + columnCount);

                    GridLayoutManager layout = new GridLayoutManager(
                            AddAlarmActivity.this, columnCount, GridLayoutManager.VERTICAL, false
                    );
                    mCategoryRecyclerView.setLayoutManager(layout);
                    mCategoryRecyclerView.addItemDecoration(
                            new GridSpacingItemDecoration(columnCount, spacingInPx, true)
                    );

                } catch (Exception exception) {
                    Log.e(TAG, "Error calculating grid columns.");
                }
            }
        }); // End of rootLayout.post()
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save_button:
                if (validateInput()) {
                    long insertId = new FinanceDb(this).addAlarmItem(
                            mSelectedCategory.title,
                            mDescriptionEditText.getText().toString().trim(),
                            mSelectedCategory.id,
                            mCalendar.getTime()
                    );
                    if (insertId != -1) {
                        ExpenseAlarmManager.getInstance(this).setAlarm((int) insertId, mCalendar);
                        Toast.makeText(AddAlarmActivity.this, "บันทึกข้อมูลเรียบร้อย", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Utils.showModalOkDialog(
                                this,
                                null,
                                "เกิดข้อผิดพลาดในการเพิ่มการแจ้งเตือน",
                                null
                        );
                    }
                }
                break;

            case R.id.date_edit_text:
                final DatePickerDialog.OnDateSetListener dateSetListener =
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                mCalendar.set(Calendar.YEAR, year);
                                mCalendar.set(Calendar.MONTH, monthOfYear);
                                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                updateDateEditText();
                            }
                        };
                new DatePickerDialog(
                        AddAlarmActivity.this,
                        dateSetListener,
                        mCalendar.get(Calendar.YEAR),
                        mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH)
                ).show();
                break;

            case R.id.time_edit_text:
                final TimePickerDialog.OnTimeSetListener timeSetListener =
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker,
                                                  int selectedHour, int selectedMinute) {
                                mCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                mCalendar.set(Calendar.MINUTE, selectedMinute);
                                updateTimeEditText();
                            }
                        };
                new TimePickerDialog(
                        AddAlarmActivity.this,
                        timeSetListener,
                        mCalendar.get(Calendar.HOUR_OF_DAY),
                        mCalendar.get(Calendar.MINUTE),
                        true
                ).show();
                break;
        }
    }

    private boolean validateInput() {
        if ("".equals(mDescriptionEditText.getText().toString().trim())) {
            String errorText = "กรุณาป้อนบันทึกย่อ";
            Toast.makeText(this, errorText, Toast.LENGTH_LONG).show();
            mDescriptionEditText.setError(errorText);
            mDescriptionEditText.requestFocus();
            mDescriptionEditText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm =
                            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.showSoftInput(mDescriptionEditText, 0);
                }
            }, 200);
            return false;
        } else if (mSelectedCategory == null) {
            Toast.makeText(this, "กรุณาเลือกประเภทรายจ่าย", Toast.LENGTH_LONG).show();
            return false;
        } else {
            Date now = Calendar.getInstance().getTime();
            Date selectedDate = mCalendar.getTime();

            if (selectedDate.before(now)) {
                Toast.makeText(this, "กรุณาระบุวัน/เวลา หลังวัน/เวลาปัจจุบัน", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    private void updateDateEditText() {
        Date date =  mCalendar.getTime();

        SimpleDateFormat monthFormatter = new SimpleDateFormat("MM", Locale.US);
        String month = mShortMonthNames[Integer.valueOf(monthFormatter.format(date)) - 1];

        SimpleDateFormat yearFormatter = new SimpleDateFormat("yyyy", Locale.US);
        String yearInBe = String.valueOf(Integer.valueOf(yearFormatter.format(date)) + 543);

        SimpleDateFormat dayFormatter = new SimpleDateFormat("d", Locale.US);
        String day = dayFormatter.format(date);

        String dateText = String.format(
                Locale.getDefault(),
                "%s %s %s",
                day, month, yearInBe
        );
        mDateEditText.setText(dateText);
    }

    private void updateTimeEditText() {
        Date date =  mCalendar.getTime();

        SimpleDateFormat hourFormatter = new SimpleDateFormat("HH", Locale.US);
        String hour = hourFormatter.format(date);

        SimpleDateFormat minuteFormatter = new SimpleDateFormat("mm", Locale.US);
        String minute = minuteFormatter.format(date);

        String dateText = String.format(
                Locale.getDefault(),
                "%s.%s น.",
                hour, minute
        );
        mTimeEditText.setText(dateText);
    }

    @Override
    public void onCategoryClick(Category category) {
        mSelectedCategory = category;
        TextView titleTextView = findViewById(R.id.title_text_view);
        titleTextView.setText(category.title);
    }
}
