package com.example.countries;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.Calendar;


public class MainActivity extends Activity {

    private ListView listForSlidingMenu;

    private SlidingMenu slidingMenu;

    private SharedPreferences sharedPreferences;

    private BroadcastReceiver broadcastReceiverForDetails;
    private BroadcastReceiver broadcastReceiverForMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firstAlertDialog();

        initActionBar();

        onCreateSlidingMenu();
        fillSlidingMenu();
        slidingMenuOnItemClickListener();
        sharedPreferencesForTime(getCurrentTime());
    }

    @Override
    protected void onResume() {
        broadcastReceiverForDetails = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                showFragment(new DetailsFragment());

                String name = intent.getStringExtra(Constants.COUNTRY_NAME);
                String code = intent.getStringExtra(Constants.COUNTRY_CODE);

                Log.d(Constants.LOG_TAG, "Country " + name + ", code " + code);

                sharedPreferencesForFragment(Constants.COUNTRY_NAME, name, Constants.COUNTRY_CODE, code);
            }
        };

        broadcastReceiverForMap = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                showFragment(new GoMap());

                String latitude = intent.getStringExtra(Constants.LATITUDE);
                String longitude = intent.getStringExtra(Constants.LONGITUDE);

                Log.d(Constants.LOG_TAG, "Latitude: " + latitude + ", longitude: " + longitude);

                sharedPreferencesForFragment(Constants.LATITUDE, latitude, Constants.LONGITUDE, longitude);
            }
        };

        registerReceiver(broadcastReceiverForDetails, new IntentFilter(Constants.INTENT_NAME_CODE));
        registerReceiver(broadcastReceiverForMap, new IntentFilter(Constants.INTENT_LAT_LNG));

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiverForDetails);
        unregisterReceiver(broadcastReceiverForMap);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (slidingMenu.isMenuShowing()) {
            slidingMenu.toggle();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            this.slidingMenu.toggle();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                menuToggle();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void firstAlertDialog() {

        sharedPreferences = getPreferences(MODE_PRIVATE);
        String savedTime = sharedPreferences.getString(Constants.SAVED_TIME, "");

        if (savedTime.equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Добро пожаловать!")
                    .setMessage("Узнайте мир с нашим приложением :)")
                    .setCancelable(false)
                    .setIcon(R.drawable.ic_launcher)
                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Добро пожаловать!")
                    .setIcon(R.drawable.ic_launcher)
                    .setCancelable(false)
                    .setMessage("В последний раз вы заходили в приложение " + savedTime)
                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void onCreateSlidingMenu() {
        slidingMenu = new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        slidingMenu.setMenu(R.layout.sidemenu);
        slidingMenu.setBehindWidthRes(R.dimen.slidingmenu_behind_width);
        slidingMenu.setSelectorDrawable(R.drawable.sidemenu_items_background);
    }

    private void fillSlidingMenu() {
        String[] items = {"Все страны","Сохраненные", "Выход"};
        ((ListView) findViewById(R.id.sidemenu)).setAdapter(
                new ArrayAdapter<Object>(
                        this,
                        R.layout.sidemenu_item,
                        R.id.text,
                        items
                )
        );
    }

    private void slidingMenuOnItemClickListener() {
        listForSlidingMenu = ((ListView) findViewById(R.id.sidemenu));
        listForSlidingMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                menuToggle();
                changeFragment(position);
            }
        });
    }

    private void initActionBar() {
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle("Страны");
    }

    private void menuToggle() {
        if (slidingMenu.isMenuShowing()) {
            slidingMenu.showContent();
        } else {
            slidingMenu.showMenu();
        }
    }

    private void changeFragment(int position) {
        switch (position) {
            case 0:
                showFragment(new AllCountriesFragment());
                break;
            case 1:
                showFragment(new SavedCountriesFragment());
                break;
            case 2:
                super.onBackPressed();
                break;
        }
    }

    private void showFragment(Fragment currentFragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, currentFragment);
        fragmentTransaction.commit();
        Log.d(Constants.LOG_TAG, "Commit fragment");
    }

    private String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return String.format("%01d " + monthInStringFormat(month) + " в %02d:%02d", day, hour, minute);
    }

    private void sharedPreferencesForTime(String currentTime) {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.SAVED_TIME, currentTime);
        editor.commit();
        Log.d(Constants.LOG_TAG, "Time saved");
    }

    private void sharedPreferencesForFragment(String firstKey, String firstValue, String secondKey, String secondValue) {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(firstKey, firstValue);
        editor.putString(secondKey, secondValue);
        editor.commit();

        Log.d(Constants.LOG_TAG, "Saved");
    }

    private String monthInStringFormat(int month) {
        switch(month) {
            case 0:
                return "Января";
            case 1:
                return "Февраля";
            case 2:
                return "Марта";
            case 3:
                return "Апреля";
            case 4:
                return "Мая";
            case 5:
                return "Июня";
            case 6:
                return "Июля";
            case 7:
                return "Августа";
            case 8:
                return "Сентября";
            case 9:
                return "Октября";
            case 10:
                return "Ноября";
            case 11:
                return "Декабря";
        }
        return "";
    }
}
