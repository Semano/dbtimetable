// DB Timetable
// Copyright (C) 2011  Justin Vogel

// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

package jv.treyas.dbtimetable;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String
            KEY_ROWID = "_id",
            KEY_ROUTEID = "RoutesID",
            KEY_TIME = "Time",
            KEY_DAYID = "DaysID",
            KEY_ORIGINID = "OriginID",
            KEY_NAME = "Name",
            KEY_HOLIDAY = "Holiday",
            KEY_ROUTETYPE = "RouteType",
            KEY_ORDERBY = "OrderBy";
    public static final int
            FERRYROUTE = 0,
            BUSROUTE = 1;
    private static final String TAG = "DBHELPER";

    private static final String
            ROUTES_TABLE = "Routes",
            TIMES_TABLE = "Times",
            ORIGINS_TABLE = "Origin",
            HOLIDAYS_TABLE = "Holidays";
    // 1.1 Original Version = 15
    // 1.2 Original Version = 20
    private static final int DATABASE_VERSION = 20;
    private static String DB_PATH = "/data/data/jv.treyas.dbtimetable/databases/";
    private static String DB_NAME = "timetable.sqlite";
    private static DataBaseHelper myInstance;
    private final Context myContext;
    DateFormat timeFormat = new SimpleDateFormat("HH:mm");
    DateFormat dateFormat = new SimpleDateFormat("yyyy-DD-MM");
    private static SQLiteDatabase myDataBase;

    private DataBaseHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
        this.myContext = context;
    }

    public static DataBaseHelper getInstanace(Context ctx) {
        if (myInstance == null) {
            myInstance = new DataBaseHelper(ctx.getApplicationContext());
        }
        return myInstance;
    }

    /**
     * check if database exists and creates and fills a new instance
     *
     * @throws IOException
     */
    public void createDataBase() throws IOException {
        boolean dbExist = checkDatabase();
        if (dbExist) {
            // do nothing - DB already exists
            // DB Exists
            this.getWritableDatabase();
        } else {
            this.getReadableDatabase();
            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    /**
     * Check if DB already Exists to avoid copying again.
     *
     * @return true if exists, false if not.
     */
    private boolean checkDatabase() {
        SQLiteDatabase checkDB = null;
        try {
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            Log.d(TAG, "checkDatabase: Database doesn't exist yet...");
        }

        if (checkDB != null) {
            checkDB.close();
            Log.d(TAG, "checkDatabase: DB exists");
        }

        return checkDB != null ? true : false;
    }

    /**
     * Copies DB from Assets folder to the just created empty DB in the system folder
     *
     * @throws IOException
     */
    private void copyDataBase() throws IOException {
        File fileTest = myContext.getFileStreamPath(DB_NAME);
        boolean exists = fileTest.exists();
        if (!exists) {
            Log.d(TAG, "COPY DB : DB !EXIST");
            // Open the empty db as the output stream
            OutputStream databaseOutputStream = new FileOutputStream(DB_PATH + DB_NAME);
            InputStream databaseInputStream;

            databaseInputStream = myContext.getAssets().open(DB_NAME);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = databaseInputStream.read(buffer)) > 0) {
                databaseOutputStream.write(buffer);
            }

            // Close the streams
            databaseInputStream.close();
            databaseOutputStream.flush();
            databaseOutputStream.close();
            Log.d(TAG, "DATABASE COPIED");
        }
    }

    /**
     * opens a R/O database handle
     *
     * @throws SQLException
     */
    public void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
        if (myDataBase == null) {
            myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
            if (myDataBase.isOpen())
                Log.d(TAG, "openDataBase: database opened " + myDataBase);
            else
                Log.d(TAG, "openDataBase: unable to open database");
        } else
            Log.d(TAG, "openDataBase: database is open already. myDatabase is " + myDataBase);
    }

    /**
     * checks if global database handle is opened or not
     *
     * @return true if open; else false
     */
    public boolean isOpen() {
        if (myDataBase != null) {
            return myDataBase.isOpen();
        } else return false;
    }

    @Override
    public synchronized void close() {
        Log.d(TAG, "close() called");
        if (myDataBase != null)
            myDataBase.close();

        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade Called oldVersion =" + oldVersion + " newVersion = " + newVersion);
        if (newVersion > oldVersion)
            Log.d(TAG, "Database Version Higher than Saved Version");
        if (myContext.deleteDatabase(DB_NAME)) {
            Log.d(TAG, "DB UPGRADE CALLED, DELETED DB");
        } else Log.d(TAG, "DB UPGRADE DELETE FAILED");
    }

    public Cursor fetchRoutes(int routeType) {
        String selection = KEY_ROUTETYPE + "=" + routeType;
        return myDataBase.query(ROUTES_TABLE, new String[]{KEY_ROWID, KEY_NAME, KEY_ORDERBY}, selection, null, null, null, KEY_ORDERBY);
    }

    public Cursor fetchTimes(Long route, String day, Long origin, String currentTime) throws SQLException {
        String whereClause = KEY_ROUTEID + "=" + route + " AND " + day + " AND " + KEY_ORIGINID + "=" + origin;
        String OrderBy = "Time";
        Log.d(TAG, whereClause);
        if (currentTime != null)
            whereClause = whereClause + " AND " +
                    KEY_TIME + ">=" + "'" + currentTime + "'";

        Cursor mCursor =

                myDataBase.query(true, TIMES_TABLE, new String[]{KEY_ROWID, KEY_TIME},
                        whereClause, null, null, null, OrderBy, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor fetchRouteName(Long route) throws SQLException {
        Cursor mCursor =
                myDataBase.query(true, ROUTES_TABLE, new String[]{KEY_NAME},
                        KEY_ROWID + "=" + route,
                        null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public Cursor fetchOriginsID(Long route) throws SQLException {
        if (!myDataBase.isOpen()) {
            Log.d(TAG, "fetchOriginsID: database is not open route is " + route);
            return null;
        }
        Cursor originId = myDataBase.query(true, TIMES_TABLE, new String[]{KEY_ORIGINID}, KEY_ROUTEID + "=" + route, null, null, null, null, null);
        if (originId != null) {
            originId.moveToFirst();
        }
        return originId;
    }

    // Fetch single OriginName for use in Title bar
    public Cursor fetchOriginName(Long originId) throws SQLException {
        Cursor originName = myDataBase.query(true, ORIGINS_TABLE, new String[]{KEY_ROWID, KEY_NAME}, KEY_ROWID + "=" + originId, null, null, null, null, null);
        originName.moveToFirst();
        return originName;
    }

    // Fetch multiple OriginNames for use in Origin Selection
    public Cursor fetchOriginName(String[] originId) {
        String originSelection = "";

        for (int i = 0; i < originId.length; i++) { // Format Selection for number of SelectionArgs.. Pain in the Ass!
            if (!originSelection.equals("")) {
                originSelection = originSelection + " OR ";
            }
            originSelection = originSelection + KEY_ROWID + "=" + '?';
        }
        Cursor result = null;
        try {
            result = myDataBase.query(true, ORIGINS_TABLE, new String[]{KEY_ROWID, KEY_NAME}, originSelection, originId, null, null, null, null);
        } catch (SQLException sqlEx) {
            Log.d(TAG, "fetchOriginName: SQL Exception " + sqlEx.getMessage());
        }

        return result;
    }

    public Boolean checkHoliday(String currentDay) {
        Cursor holidayCursor =
                myDataBase.query(true, HOLIDAYS_TABLE, new String[]{KEY_HOLIDAY}, KEY_HOLIDAY + "='" + currentDay + "'", null, null, null, null, null);

        if (holidayCursor.getCount() > 0) {
            holidayCursor.close();
            return true;
        } else {
            holidayCursor.close();
            return false;
        }
    }

    public Pair<String, Date[]> getNextLineTimes(int line_id, Date requestTime) {

        boolean holiday = checkHoliday(dateFormat.format(requestTime));

        String query = "SELECT DISTINCT Name, Time " +
                "       FROM " +
                "           (SELECT Name, _id" +
                "           FROM Routes" +
                "           WHERE _id = ?) AS r " +
                "       INNER LEFT JOIN " +
                "           (SELECT *" +
                "           FROM Times " +
                "           WHERE Times.Time >= time('?' , '-5 minutes') " +
                "               AND ? Times.DaysID = 3) as t" +
                "           ON r._id = t.RoutesID" +
                "       ORDER BY Time ASC";
        Cursor cursor = myDataBase.rawQuery(query, new String[]{String.valueOf(line_id), (holiday ? "" : "NOT"), "now"});
        if (cursor.getCount() <= 0) {
            Calendar c = Calendar.getInstance();
            c.setTime(requestTime);
            c.add(Calendar.DAY_OF_YEAR, 1);
            holiday = checkHoliday(dateFormat.format(c.getTime()));
            cursor = myDataBase.rawQuery(query, new String[]{String.valueOf(line_id), (holiday ? "" : "NOT"), "00:05"});
        }
        if (cursor.getCount() <= 0) {
            Log.e(this.toString(), "No results for next departure for line " + line_id + " at request time " + requestTime.toString());
            return null;
        } else {
            cursor.moveToFirst();
            ArrayList<Date> departTimes = new ArrayList<>(cursor.getCount());
            do {
                try {
                    departTimes.add(timeFormat.parse(cursor.getString(1)));
                } catch (ParseException pe) {
                    pe.printStackTrace();
                    departTimes.add(null);
                }
                cursor.moveToNext();
            } while (cursor.isLast());
            // Format Time String from Database to a Calendar Object (to Compare to Current Time)
            //Calendar departTime = Calendar.getInstance();

                /*
                departTime.setTime(departDateTime);
                departTime.set(Calendar.SECOND, 59); // Fix for minute calculation appearing to be early.

                // Check tomorrow's time if next day.  Calendar class handles leap years and 31+1 day of the month, luckily.
                departTime.set(
                        requestTime.get(Calendar.YEAR),
                        requestTime.get(Calendar.MONTH),
                        ( !nextDay ? requestTime.get(Calendar.DATE) : requestTime.get(Calendar.DATE) + 1 ) );
                        */

            return new Pair<>(cursor.getString(0), (Date[]) departTimes.toArray());
        }
    }
}
