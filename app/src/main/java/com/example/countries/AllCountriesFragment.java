package com.example.countries;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by Юлька on 29.11.2014.
 */
public class AllCountriesFragment extends Fragment {

    private ActionBar actionBar;
    public ListView list;
    DownloadCountriesTask downloadCountries;

    ArrayList<HashMap<String, String>> generalList;
    ArrayList<String> countries;
    ArrayList<String> codes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_all_countries, container, false);
        list = (ListView) view.findViewById(R.id.list_with_countries);
        initActionBar();

        generalList = new ArrayList<HashMap<String, String>>();
        countries = new ArrayList<String>();
        codes = new ArrayList<String>();

        if (isOnline()) {
            downloadCountries = new DownloadCountriesTask();
            downloadCountries.execute("http://api.theprintful.com/countries");
            try {
                generalList = downloadCountries.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            alertDialogForInternet();
        }

        countries = getArrayOf("name", generalList);
        codes = getArrayOf("code", generalList);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                countries);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onItemClickListener(position);
            }
        });

        return view;
    }

    private void initActionBar() {
        getActivity().getActionBar().setHomeButtonEnabled(true);
        getActivity().getActionBar().setDisplayShowCustomEnabled(true);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().getActionBar().setTitle("Страны");
    }

    private ArrayList<String> getArrayOf(String key, ArrayList<HashMap<String, String>> arrayList) {
        ArrayList<String> array = new ArrayList<String>();

        for (int i = 0; i < arrayList.size(); i++) {
            String name = arrayList.get(i).get(key);
            array.add(name);
        }

        return array;
    }

    private boolean isOnline() {

        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) {
            return false;
        }

        NetworkInfo[] netInfo = cm.getAllNetworkInfo();

        if (netInfo == null) {
            return false;
        }

        for (NetworkInfo ni : netInfo) {

            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected()) {

                    return true;
                }

            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected()) {

                    return true;
                }
        }
        return false;
    }

    private void alertDialogForInternet() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Интернет соединение отсутствует")
                .setMessage("Пожалуйста, поключитесь к Интернету и повторите попытку")
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
    }

    private void onItemClickListener(int position) {
        Intent intent = new Intent(Constants.INTENT_NAME_CODE);
        String countryCode = codes.get(position);
        String countryName = countries.get(position);

        Log.d(Constants.LOG_TAG, "Country " + countryName + ", code " + countryCode);

        intent.putExtra(Constants.COUNTRY_CODE, countryCode);
        intent.putExtra(Constants.COUNTRY_NAME, countryName);
        getActivity().sendBroadcast(intent);
    }

    public class DownloadCountriesTask extends AsyncTask<String, Void, ArrayList<HashMap<String, String>>> {

        private ArrayList<HashMap<String, String>> countriesList;
        private HashMap<String, String> temp;

        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(String... params) {

            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet post = new HttpGet(params[0]);
                HttpResponse response = client.execute(post);

                int status = response.getStatusLine().getStatusCode();
                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);

                    countriesList = new ArrayList<HashMap<String, String>>();
                    countriesList = parseJSON(data);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return countriesList;
        }

        private ArrayList<HashMap<String, String>> parseJSON(String data) {

            ArrayList<HashMap<String, String>> countriesList =
                    new ArrayList<HashMap<String, String>>();

            try{
                JSONObject allCountries = new JSONObject(data);
                JSONArray result = allCountries.getJSONArray("result");

                for (int i = 0; i < result.length(); i++) {
                    JSONObject country = result.getJSONObject(i);

                    temp = new HashMap<String, String>();
                    temp.put("code", country.getString("code"));
                    temp.put("name", country.getString("name"));
                    if (country.getString("states").equals("null")) {
                        temp.put("states", country.getString("states"));
                    } else {
                        temp.put("states", "notNull");
                    }
                    countriesList.add(temp);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return countriesList;
        }
    }
}
