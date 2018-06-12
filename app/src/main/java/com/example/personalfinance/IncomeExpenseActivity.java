package com.example.personalfinance;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.personalfinance.adapter.CategoryListAdapter;
import com.example.personalfinance.db.FinanceDb;
import com.example.personalfinance.etc.DecimalDigitsInputFilter;
import com.example.personalfinance.etc.GridSpacingItemDecoration;
import com.example.personalfinance.etc.Utils;
import com.example.personalfinance.model.AccountItem;
import com.example.personalfinance.model.Category;

import java.util.List;
import java.util.Locale;

public class IncomeExpenseActivity extends AppCompatActivity implements CategoryListAdapter.CategoryListAdapterListener {

    private static final String TAG = IncomeExpenseActivity.class.getName();

    static final String KEY_INCOME_OR_EXPENSE = "income_or_expense";

    private int mType;
    private String mTypeText = "";
    private Category mSelectedCategory = null;

    private EditText mAmountEditText, mDescriptionEditText;
    private RecyclerView mCategoryRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_expense);

        Intent intent = getIntent();
        mType = intent.getIntExtra(KEY_INCOME_OR_EXPENSE, -1);

        TextView labelTextView = findViewById(R.id.label_text_view);
        View headerLayout = findViewById(R.id.header_layout);
        TextView titleTextView = findViewById(R.id.title_text_view);

        if (mType == AccountItem.TYPE_INCOME) {
            mTypeText = getResources().getString(R.string.income);
            headerLayout.setBackgroundResource(R.color.income);
        } else if (mType == AccountItem.TYPE_EXPENSE) {
            mTypeText = getResources().getString(R.string.expense);
            headerLayout.setBackgroundResource(R.color.expense);
        } else {
            throw new IllegalArgumentException("Invalid intent parameter");
        }
        labelTextView.setText(mTypeText);
        titleTextView.setHint("เลือกประเภท" + mTypeText);

        setupCategoryRecyclerView();

        mDescriptionEditText = findViewById(R.id.description_edit_text);
        mAmountEditText = findViewById(R.id.amount_edit_text);
        mAmountEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(2)});
        mAmountEditText.requestFocus();
        mAmountEditText.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(mAmountEditText, 0);
                String toastText = "";
                toastText = "ป้อนจำนวนเงิน" + mTypeText;
                Toast.makeText(IncomeExpenseActivity.this, toastText, Toast.LENGTH_LONG).show();
            }
        }, 200);

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateInput()) {
                    saveToDatabase();
                }
            }
        });
    }

    private boolean validateInput() {
        if ("".equals(mAmountEditText.getText().toString().trim())) {
            String errorText = "กรุณาป้อนจำนวนเงิน" + mTypeText;
            Toast.makeText(this, errorText, Toast.LENGTH_LONG).show();
            mAmountEditText.setError(errorText);
            return false;
        } else if ("".equals(mDescriptionEditText.getText().toString().trim())) {
            String errorText = "กรุณาป้อนบันทึกย่อ";
            Toast.makeText(this, errorText, Toast.LENGTH_LONG).show();
            mDescriptionEditText.setError(errorText);
            return false;
        } else if (mSelectedCategory == null) {
            Toast.makeText(this, "กรุณาเลือกประเภท" + mTypeText, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void saveToDatabase() {
        String amountText = mAmountEditText.getText().toString();
        int amount = (int) Math.floor(100 * Double.parseDouble(amountText));
        if (new FinanceDb(this).addAccountItem(
                mSelectedCategory.title,
                mDescriptionEditText.getText().toString().trim(),
                amount,
                mType,
                mSelectedCategory.id
        )) {
            // save success
            Toast.makeText(this, "บันทึกข้อมูลเรียบร้อย", Toast.LENGTH_LONG).show();
            finish();
        } else {
            // save failed
            Toast.makeText(this, "เกิดข้อผิดพลาดในการบันทึกข้อมูล!", Toast.LENGTH_LONG).show();
        }
    }

    private void setupCategoryRecyclerView() {
        List<Category> categoryList = new FinanceDb(this).getCategoryByType(mType);
        CategoryListAdapter adapter = new CategoryListAdapter(
                this,
                categoryList,
                this
        );

        mCategoryRecyclerView = findViewById(R.id.category_recycler_view);
        calculateGridColumn();
        mCategoryRecyclerView.setAdapter(adapter);
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
                            Utils.getScreenDimensionInPixel(IncomeExpenseActivity.this)[0],
                            Utils.getScreenDimensionInPixel(IncomeExpenseActivity.this)[1]
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
                            IncomeExpenseActivity.this, columnCount, GridLayoutManager.VERTICAL, false
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
    public void onCategoryClick(Category category) {
        mSelectedCategory = category;
        TextView titleTextView = findViewById(R.id.title_text_view);
        titleTextView.setText(category.title);
    }
}
