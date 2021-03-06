package com.example.personalfinance.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.personalfinance.model.AccountBalance;
import com.example.personalfinance.model.AccountItem;
import com.example.personalfinance.model.AlarmItem;
import com.example.personalfinance.model.Category;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FinanceDb {

    private static final String TAG = FinanceDb.class.getSimpleName();
    private static final String STORED_DATE_FORMAT = "yyyy-MM-dd";
    private static final String STORED_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final String DATABASE_NAME = "personal_finance.db";
    private static final int DATABASE_VERSION = 4;

    // เทเบิล account
    // +-----+-------+-------------+--------+------+----------+------+
    // | _id | title | description | amount | type | category | date |
    // +-----+-------+-------------+--------+------+----------+------+
    // |     |       |             |        |      |          |      |

    private static final String TABLE_ACCOUNT = "account";
    private static final String COL_ID = "_id";
    private static final String COL_TITLE = "title";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_AMOUNT = "amount";
    private static final String COL_TYPE = "type";
    private static final String COL_CATEGORY = "category";
    private static final String COL_DATE = "date";

    private static final String SQL_CREATE_TABLE_ACCOUNT = "CREATE TABLE " + TABLE_ACCOUNT + "("
            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_TITLE + " TEXT, "
            + COL_DESCRIPTION + " TEXT, "
            + COL_AMOUNT + " INTEGER, "   // เก็บแบบ fixed-point โดยใช้จำนวนเต็ม เช่น 78.50 บาท จะเก็บข้อมูลเป็น 7850
            // หรือเก็บหน่วยเป็นสตางค์ นั่นเอง
            // เพราะการเก็บแบบทศนิยม floating-point มีโอกาสทำให้ค่าผิดเพี้ยนได้เมื่อนำมาคำนวณ
            + COL_TYPE + " INTEGER, "     // 0 = รายรับ, 1 = รายจ่าย, 2 = ลงทุน
            + COL_CATEGORY + " INTEGER, " // หมวดหมู่ของรายรับ (เงินเดือน, ถูกหวย ฯลฯ), รายจ่าย (ค่าอาหาร, ค่าน้ำ/ไฟ ฯลฯ), การลงทุน
            + COL_DATE + " TEXT "         // yyyy-mm-dd
            + ")";

    // เทเบิล category
    // +-----+-------+-------+------+
    // | _id | title | image | type |
    // +-----+-------+-------+------+
    // |     |       |       |      |

    private static final String TABLE_CATEGORY = "category";
    private static final String COL_IMAGE = "image";

    private static final String SQL_CREATE_TABLE_CATEGORY = "CREATE TABLE " + TABLE_CATEGORY + "("
            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_TITLE + " TEXT, "      // category title
            + COL_IMAGE + " TEXT, "      // image filename in assets folder
            + COL_TYPE + " INTEGER "     // 0 = รายรับ, 1 = รายจ่าย, 2 = ลงทุน
            + ")";

    // เทเบิล balance
    // +-----+---------+-------------+
    // | _id | balance | last_update |
    // +-----+---------+-------------+
    // |     |         |             |

    private static final String TABLE_BALANCE = "balance";
    private static final String COL_BALANCE = "balance";
    private static final String COL_LAST_UPDATE = "last_update";

    private static final String SQL_CREATE_TABLE_BALANCE = "CREATE TABLE " + TABLE_BALANCE + "("
            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_BALANCE + " INTEGER, "
            + COL_LAST_UPDATE + " TEXT "  // yyyy-mm-dd hh:mm:ss
            + ")";

    // เทเบิล alarm
    // +-----+-------+-------------+----------+----------+
    // | _id | title | description | category | due_date |
    // +-----+-------+-------------+----------+----------+
    // |     |       |             |          |          |

    private static final String TABLE_ALARM = "alarm";
    private static final String COL_DUE_DATE = "due_date";

    private static final String SQL_CREATE_TABLE_ALARM = "CREATE TABLE " + TABLE_ALARM + "("
            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_TITLE + " TEXT, "
            + COL_DESCRIPTION + " TEXT, "
            + COL_CATEGORY + " INTEGER, "  // หมวดหมู่ของรายจ่าย (ค่าอาหาร, ค่าน้ำ/ไฟ ฯลฯ)
            + COL_DUE_DATE + " TEXT "      // yyyy-mm-dd hh:mm:ss
            + ")";

    private static DatabaseHelper sDbHelper;
    private SQLiteDatabase mDatabase;

    public FinanceDb(Context context) {
        context = context.getApplicationContext(); // เข้าถึง App-level context เพื่อป้องกัน memory leak

        if (sDbHelper == null) {
            sDbHelper = new DatabaseHelper(context);
        }
        mDatabase = sDbHelper.getWritableDatabase();
    }

    public List<AlarmItem> getAlarmItemList() {
        String sql = "SELECT alarm.*, category._id AS cat_id, category.title AS cat_title, category.image AS cat_image, category.type AS cat_type "
                + " FROM " + TABLE_ALARM + " INNER JOIN " + TABLE_CATEGORY
                + " ON alarm.category = category._id "
                //+ " WHERE " + COL_DATE + "=? "
                + " ORDER BY " + COL_DUE_DATE + " DESC ";

        Cursor cursor = mDatabase.rawQuery(sql, null);

        List<AlarmItem> alarmItemList = new ArrayList<>();
        while (cursor.moveToNext()) {
            Category category = new Category(
                    cursor.getInt(cursor.getColumnIndex("cat_id")),
                    cursor.getString(cursor.getColumnIndex("cat_title")),
                    cursor.getString(cursor.getColumnIndex("cat_image")),
                    cursor.getInt(cursor.getColumnIndex("cat_type"))
            );
            AlarmItem accountItem = new AlarmItem(
                    cursor.getInt(cursor.getColumnIndex(COL_ID)),
                    cursor.getString(cursor.getColumnIndex(COL_TITLE)),
                    cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION)),
                    category,
                    cursor.getString(cursor.getColumnIndex(COL_DUE_DATE))
            );
            alarmItemList.add(accountItem);
        }
        cursor.close();
        return alarmItemList;
    }

    public List<AccountItem> getAccountItemListByDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(STORED_DATE_FORMAT, Locale.getDefault());
        String dateString = dateFormat.format(date);

/*
        Cursor cursor = mDatabase.query(
                TABLE_ACCOUNT,
                null,
                COL_DATE + "=?",
                new String[]{dateString},
                null,
                null,
                COL_DATE + " DESC"
        );
*/

        String sql = "SELECT account.*, category._id AS cat_id, category.title AS cat_title, category.image AS cat_image "
                + " FROM " + TABLE_ACCOUNT + " INNER JOIN " + TABLE_CATEGORY
                + " ON account.category = category._id "
                + " WHERE " + COL_DATE + "=? "
                + " ORDER BY " + COL_ID + " DESC ";

        Cursor cursor = mDatabase.rawQuery(sql, new String[]{dateString});

        List<AccountItem> accountItemList = new ArrayList<>();
        while (cursor.moveToNext()) {
            Category category = new Category(
                    cursor.getInt(cursor.getColumnIndex("cat_id")),
                    cursor.getString(cursor.getColumnIndex("cat_title")),
                    cursor.getString(cursor.getColumnIndex("cat_image")),
                    cursor.getInt(cursor.getColumnIndex(COL_TYPE))
            );
            AccountItem accountItem = new AccountItem(
                    cursor.getInt(cursor.getColumnIndex(COL_ID)),
                    cursor.getString(cursor.getColumnIndex(COL_TITLE)),
                    cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION)),
                    cursor.getInt(cursor.getColumnIndex(COL_AMOUNT)),
                    cursor.getInt(cursor.getColumnIndex(COL_TYPE)),
                    category,
                    cursor.getString(cursor.getColumnIndex(COL_DATE))
            );
            accountItemList.add(accountItem);
        }
        cursor.close();
        return accountItemList;
    }

    public List<AccountItem> getAccountItemListByMonth(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM", Locale.getDefault());
        String monthString = dateFormat.format(date);

/*
        Cursor cursor = mDatabase.query(
                TABLE_ACCOUNT,
                null,
                COL_DATE + " LIKE ?",
                new String[]{"____-" + monthString + "-__"},
                null,
                null,
                COL_DATE + " DESC"
        );
*/

        String sql = "SELECT account.*, category._id AS cat_id, category.title AS cat_title, category.image AS cat_image "
                + " FROM " + TABLE_ACCOUNT + " INNER JOIN " + TABLE_CATEGORY
                + " ON account.category = category._id "
                + " WHERE " + COL_DATE + " LIKE ? "
                + " ORDER BY " + COL_DATE + " DESC, " + COL_ID + " DESC ";

        Cursor cursor = mDatabase.rawQuery(sql, new String[]{"____-" + monthString + "-__"});

        List<AccountItem> accountItemList = new ArrayList<>();
        while (cursor.moveToNext()) {
            Category category = new Category(
                    cursor.getInt(cursor.getColumnIndex("cat_id")),
                    cursor.getString(cursor.getColumnIndex("cat_title")),
                    cursor.getString(cursor.getColumnIndex("cat_image")),
                    cursor.getInt(cursor.getColumnIndex(COL_TYPE))
            );
            AccountItem accountItem = new AccountItem(
                    cursor.getInt(cursor.getColumnIndex(COL_ID)),
                    cursor.getString(cursor.getColumnIndex(COL_TITLE)),
                    cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION)),
                    cursor.getInt(cursor.getColumnIndex(COL_AMOUNT)),
                    cursor.getInt(cursor.getColumnIndex(COL_TYPE)),
                    category,
                    cursor.getString(cursor.getColumnIndex(COL_DATE))
            );
            accountItemList.add(accountItem);
        }
        cursor.close();
        return accountItemList;
    }

    public AccountBalance getCurrentAccountBalance() {
        Cursor cursor = mDatabase.query(
                TABLE_BALANCE,
                null,
                null,
                null,
                null,
                null,
                null
        );
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            AccountBalance accountBalance = new AccountBalance(
                    cursor.getInt(cursor.getColumnIndex(COL_BALANCE)),
                    cursor.getString(cursor.getColumnIndex(COL_LAST_UPDATE))
            );
            cursor.close();
            return accountBalance;
        } else {
            cursor.close();
            return null;
        }
    }

    public List<Category> getCategoryByType(int type) {
        Cursor cursor = mDatabase.query(
                TABLE_CATEGORY,
                null,
                COL_TYPE + "=?",
                new String[]{String.valueOf(type)},
                null,
                null,
                COL_ID + " ASC"
        );

        List<Category> categoryList = new ArrayList<>();
        while (cursor.moveToNext()) {
            Category category = new Category(
                    cursor.getInt(cursor.getColumnIndex(COL_ID)),
                    cursor.getString(cursor.getColumnIndex(COL_TITLE)),
                    cursor.getString(cursor.getColumnIndex(COL_IMAGE)),
                    cursor.getInt(cursor.getColumnIndex(COL_TYPE))
            );
            categoryList.add(category);
        }
        cursor.close();
        return categoryList;
    }

    public boolean addAccountItem(String title /* ประเภทรายรับ/รายจ่าย */,
                               String description /* บันทึกย่อ */,
                               int amount, int type, int category) {

        Date now = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormatter = new SimpleDateFormat(STORED_DATE_FORMAT, Locale.getDefault());
        SimpleDateFormat dateTimeFormatter = new SimpleDateFormat(STORED_DATE_TIME_FORMAT, Locale.getDefault());
        String dateString = dateFormatter.format(now);

        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE, title);
        cv.put(COL_DESCRIPTION, description);
        cv.put(COL_AMOUNT, amount);
        cv.put(COL_TYPE, type);
        cv.put(COL_CATEGORY, category);
        cv.put(COL_DATE, dateString);

        mDatabase.beginTransaction();
        if (mDatabase.insert(TABLE_ACCOUNT, null, cv) != -1) {
            // update account balance
            String updateSql = "UPDATE " + TABLE_BALANCE
                    + " SET " + COL_BALANCE + " = "
                    + COL_BALANCE + (type == AccountItem.TYPE_INCOME ? "+" : "-") + amount
                    + ", " + COL_LAST_UPDATE + " = '" + dateTimeFormatter.format(now) + "'";
            try {
                mDatabase.execSQL(updateSql);
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
                mDatabase.endTransaction();
                return false;
            }
        } else {
            mDatabase.endTransaction();
            return false;
        }
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_TABLE_ACCOUNT);
            db.execSQL(SQL_CREATE_TABLE_CATEGORY);
            db.execSQL(SQL_CREATE_TABLE_BALANCE);
            db.execSQL(SQL_CREATE_TABLE_ALARM);
            insertInitialData(db);
        }

        private void insertInitialData(SQLiteDatabase db) {
            //-----------------------------------------------------------
            // ใส่ข้อมูลตัวอย่าง เพื่อทดสอบตอน dev
            List<AccountItem> accountItemList = new ArrayList<>();
            accountItemList.add(
                    new AccountItem(0, "รายรับประเภท 2", "บันทึกย่อรายรับ", 50000, AccountItem.TYPE_INCOME,
                            2, "2018-04-26")
            );
            accountItemList.add(
                    new AccountItem(0, "รายจ่ายประเภท 1", "บันทึกย่อรายจ่าย", 299000, AccountItem.TYPE_EXPENSE,
                            9, "2018-04-26")
            );
            accountItemList.add(
                    new AccountItem(0, "รายรับประเภท 5", "บันทึกย่อรายรับ", 175000, AccountItem.TYPE_INCOME,
                            5, "2018-04-26")
            );
            accountItemList.add(
                    new AccountItem(0, "รายรับประเภท 8", "บันทึกย่อรายรับ", 250000, AccountItem.TYPE_INCOME,
                            8, "2018-04-27")
            );
            accountItemList.add(
                    new AccountItem(0, "รายจ่ายประเภท 3", "บันทึกย่อรายจ่าย", 6000, AccountItem.TYPE_EXPENSE,
                            11, "2018-04-27")
            );

            ContentValues cv;
            for (AccountItem accountItem : accountItemList) {
                cv = new ContentValues();
                cv.put(COL_TITLE, accountItem.title);
                cv.put(COL_DESCRIPTION, accountItem.description);
                cv.put(COL_AMOUNT, accountItem.amount);
                cv.put(COL_TYPE, accountItem.type);
                cv.put(COL_CATEGORY, accountItem.categoryId);
                cv.put(COL_DATE, accountItem.date);
                db.insert(TABLE_ACCOUNT, null, cv);
            }

            //-----------------------------------------------------------
            // ใส่ข้อมูล category
            List<Category> categoryList = new ArrayList<>();
            categoryList.add(
                    new Category(0, "รายรับประเภท 1", "income_category_01.png", AccountItem.TYPE_INCOME)
            );
            categoryList.add(
                    new Category(0, "รายรับประเภท 2", "income_category_02.png", AccountItem.TYPE_INCOME)
            );
            categoryList.add(
                    new Category(0, "รายรับประเภท 3", "income_category_03.png", AccountItem.TYPE_INCOME)
            );
            categoryList.add(
                    new Category(0, "รายรับประเภท 4", "income_category_04.png", AccountItem.TYPE_INCOME)
            );
            categoryList.add(
                    new Category(0, "รายรับประเภท 5", "income_category_05.png", AccountItem.TYPE_INCOME)
            );
            categoryList.add(
                    new Category(0, "รายรับประเภท 6", "income_category_06.png", AccountItem.TYPE_INCOME)
            );
            categoryList.add(
                    new Category(0, "รายรับประเภท 7", "income_category_07.png", AccountItem.TYPE_INCOME)
            );
            categoryList.add(
                    new Category(0, "รายรับประเภท 8", "income_category_08.png", AccountItem.TYPE_INCOME)
            );
            categoryList.add(
                    new Category(0, "รายจ่ายประเภท 1", "expense_category_01.png", AccountItem.TYPE_EXPENSE)
            );
            categoryList.add(
                    new Category(0, "รายจ่ายประเภท 2", "expense_category_02.png", AccountItem.TYPE_EXPENSE)
            );
            categoryList.add(
                    new Category(0, "รายจ่ายประเภท 3", "expense_category_03.png", AccountItem.TYPE_EXPENSE)
            );
            categoryList.add(
                    new Category(0, "รายจ่ายประเภท 4", "expense_category_04.png", AccountItem.TYPE_EXPENSE)
            );
            categoryList.add(
                    new Category(0, "รายจ่ายประเภท 5", "expense_category_05.png", AccountItem.TYPE_EXPENSE)
            );
            categoryList.add(
                    new Category(0, "รายจ่ายประเภท 6", "expense_category_06.png", AccountItem.TYPE_EXPENSE)
            );
            categoryList.add(
                    new Category(0, "รายจ่ายประเภท 7", "expense_category_07.png", AccountItem.TYPE_EXPENSE)
            );
            categoryList.add(
                    new Category(0, "รายจ่ายประเภท 8", "expense_category_08.png", AccountItem.TYPE_EXPENSE)
            );

            for (Category category : categoryList) {
                cv = new ContentValues();
                cv.put(COL_TITLE, category.title);
                cv.put(COL_IMAGE, category.image);
                cv.put(COL_TYPE, category.type);
                db.insert(TABLE_CATEGORY, null, cv);
            }

            //-----------------------------------------------------------
            // กำหนด balance เริ่มต้น 1,000 บาท (เอาไว้ทดสอบตอน dev เท่านั้น พอ release แอพ ให้กำหนดเป็น 0)
            cv = new ContentValues();
            cv.put(COL_BALANCE, 100000);
            cv.put(COL_LAST_UPDATE, "2018-04-25 16:00:00");
            db.insert(TABLE_BALANCE, null, cv);

            //-----------------------------------------------------------
            // ใส่ข้อมูล alarm ตัวอย่าง เพื่อทดสอบตอน dev
            cv = new ContentValues();
            cv.put(COL_TITLE, "รายจ่ายประเภท 1");
            cv.put(COL_DESCRIPTION, "บันทึกย่อรายจ่าย");
            cv.put(COL_CATEGORY, 9);
            cv.put(COL_DUE_DATE, "2018-04-28 15:00:00");
            db.insert(TABLE_ALARM, null, cv);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BALANCE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARM);
            onCreate(db);
        }
    }
}
