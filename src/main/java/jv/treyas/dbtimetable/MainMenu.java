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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;

public class MainMenu extends Activity implements View.OnClickListener {

    public static final int ACTIVITY_NEXT = 1;
    private static final String TAG = "MAIN MENU";
    private DataBaseHelper mDb;
    private Button ferry, bus;
    private TextView ferryText, busText;
    private ListView favList;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu);
        mDb = new DataBaseHelper(this);

        ferry = (Button) findViewById(R.id.Ferry);
        ferryText = (TextView) findViewById(R.id.TextView01);
        bus = (Button) findViewById(R.id.Bus);
        busText = (TextView) findViewById(R.id.TextView02);
        favList = (ListView) findViewById(R.id.menu_fav);

        // Click Listeners
        ferry.setOnClickListener(this);
        ferryText.setOnClickListener(this);
        bus.setOnClickListener(this);
        busText.setOnClickListener(this);
        favList.setOnClickListener(this);

        try {
            mDb.createDataBase();
        } catch (IOException ioe) {
            Log.e(TAG, "Unable to create database:" + ioe);
        }

        // create favorite list
        Adapter adapter = new FavListAdapter(this, mDb);


        //mDb.close();
    }

    public void onClick(View v) {
        if (v == ferry || v == ferryText) {
            Intent i = new Intent(this, RouteList.class);
            i.putExtra(DataBaseHelper.KEY_ROUTETYPE, DataBaseHelper.FERRYROUTE);
            startActivityForResult(i, MainMenu.ACTIVITY_NEXT);
        } else if (v == bus || v == busText) {
            Intent i = new Intent(this, RouteList.class);
            i.putExtra(DataBaseHelper.KEY_ROUTETYPE, DataBaseHelper.BUSROUTE);
            startActivityForResult(i, MainMenu.ACTIVITY_NEXT);
        }
    }

    public void onSelectFavorite(View v) throws Exception {
        throw new Exception("Not yet Implemented");
    }

    public void onSelectFavoriteUnsubscripe(View v) throws Exception {
        throw new Exception("Not yet Implemented");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about_menu:
                Intent i = new Intent(this, AboutPage.class);
                startActivityForResult(i, MainMenu.ACTIVITY_NEXT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}