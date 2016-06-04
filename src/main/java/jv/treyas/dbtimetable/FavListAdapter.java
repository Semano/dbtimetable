package jv.treyas.dbtimetable;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * List adapter for main menu favorites list
 * Created by Johannes Holtz on 05.11.2015.
 */
public class FavListAdapter extends BaseAdapter {

    public static final int MAXITEMS = 10;
    public static final String FAVORTIE_SETTING_NAME = "fav";

    private static Context context;
    private static LayoutInflater inflater = null;
    private static DataBaseHelper mDB;
    private static DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINESE);
    private static int count = 0;


    public FavListAdapter(Context context, DataBaseHelper db) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        FavListAdapter.context = context;
        mDB = db;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.menu_fav_row, null);
        }

        SharedPreferences preferences = context.getSharedPreferences(context.getApplicationInfo().className, Context.MODE_PRIVATE);

        int favorite = preferences.getInt(FAVORTIE_SETTING_NAME + i, 0);
        if (favorite != 0) {
            TextView name = (TextView) view.findViewById(R.id.favoriteLineName);
            TextView timeLeft = (TextView) view.findViewById(R.id.favoriteLineTime);
            TextView depTime = (TextView) view.findViewById(R.id.favoriteLineDepTime);

            Date requestTime = new Date(0);
            Pair<String, Date[]> pair = mDB.getNextLineTimes(favorite, requestTime);
            if (pair == null) {
                return null;
            }

            name.setText(pair.first);
            //check if line just departed
            long diff = -1;
            Date last = null;
            for (Date t : pair.second) {
                // time difference in seconds
                diff = (requestTime.getTime() - t.getTime()) * 1000;
                if (diff >= 0) {
                    // departed within the last 5 minutes
                    last = t;
                } else {
                    // not yet departed, set text box values
                    timeLeft.setText(String.valueOf(diff));
                    depTime.setText(timeFormat.format(t));
                    break;
                }
            }
            if (last != null) {
                int min = (int) diff / 60;
                int sec = (int) diff % 60;
                name.append(" (" + R.string.warning_dep + " -" + min + ":" + sec + ")");
            }
        }

        return view;

    }


}
