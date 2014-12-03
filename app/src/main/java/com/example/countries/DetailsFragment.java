package com.example.countries;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by Юлька on 02.12.2014.
 */
public class DetailsFragment extends Fragment {

    private ImageView flag;
    private TextView tvCapital, tvRegion, tvArea, tvLatitude, tvLongitude, tvCallingCodes;
    private Button btnGoMap;

    private SharedPreferences sharedPreferences;

    DownloadDetailsTask downloadDetailsTask;
    ArrayList<HashMap<String, String>> detailsList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_details, container, false);
        flag = (ImageView) view.findViewById(R.id.flag);

        tvCapital = (TextView) view.findViewById(R.id.capital);
        tvRegion = (TextView) view.findViewById(R.id.region);
        tvArea = (TextView) view.findViewById(R.id.area);
        tvLatitude = (TextView) view.findViewById(R.id.latitude);
        tvLongitude = (TextView) view.findViewById(R.id.longitude);
        tvCallingCodes = (TextView) view.findViewById(R.id.callingCodes);

        btnGoMap = (Button) view.findViewById(R.id.go_map);

        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        String name = sharedPreferences.getString(Constants.COUNTRY_NAME,"");
        String code = sharedPreferences.getString(Constants.COUNTRY_CODE, "");

        Log.d(Constants.LOG_TAG, "Country " + name + ", code " + code);

        initActionBar(name);

        flag = (ImageView) view.findViewById(R.id.flag);
        new DownloadImageTask(flag).execute("http://www.geognos.com/api/en/countries/flag/" +
                code.toLowerCase() + ".png");

        downloadDetailsTask = new DownloadDetailsTask();
        downloadDetailsTask.execute("http://restcountries.eu/rest/v1/alpha/" + code);
        detailsList = new ArrayList<HashMap<String, String>>();

        try {
            detailsList = downloadDetailsTask.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        final String latitude = detailsList.get(0).get("latitude");
        final String longitude= detailsList.get(0).get("longitude");
        tvLatitude.setText("Latitude: " + latitude);
        tvLongitude.setText("Longitude: " + longitude);
        tvCapital.setText("Capital: " + detailsList.get(0).get("capital"));
        tvRegion.setText("Region: " + detailsList.get(0).get("region"));
        tvArea.setText("Area: " + detailsList.get(0).get("area"));
        tvCallingCodes.setText("CallingCodes: " + detailsList.get(0).get("callingCodes0"));

        btnGoMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickGoMap(latitude, longitude);
            }
        });

        return view;
    }

    private void initActionBar(String name) {
        getActivity().getActionBar().setHomeButtonEnabled(true);
        getActivity().getActionBar().setDisplayShowCustomEnabled(true);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().getActionBar().setTitle(name);
    }

    private void onClickGoMap(String lat, String lng) {
        Log.d(Constants.LOG_TAG, "Latitude: "+ lat + ", longitude: " + lng);
        Intent intent = new Intent(Constants.INTENT_LAT_LNG);
        intent.putExtra(Constants.LATITUDE, lat);
        intent.putExtra(Constants.LONGITUDE, lng);
        getActivity().sendBroadcast(intent);
    }

    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String urlDisplay = params[0];
            Bitmap icon = null;
            try {
                InputStream in = new java.net.URL(urlDisplay).openStream();
                icon = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return icon;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            bmImage.setImageBitmap(bitmap);
        }
    }

    public class DownloadDetailsTask extends AsyncTask<String, Void, ArrayList<HashMap<String, String>>> {

        ArrayList<HashMap<String, String>> detailsList;

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

                    detailsList = parseJSON(data);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return detailsList;
        }

        private ArrayList<HashMap<String,String>> parseJSON(String data) {

            ArrayList<HashMap<String, String>> detailsList =
                    new ArrayList<HashMap<String, String>>();
            HashMap<String, String> hashMap = new HashMap<String, String>();

            try {
                JSONObject detail = new JSONObject(data);

                hashMap.put("capital", detail.getString("capital"));
                hashMap.put("region", detail.getString("region"));
                hashMap.put("area", detail.getString("area"));

                String[] latlngArray = new String[2];
                JSONArray latlng = detail.getJSONArray("latlng");
                for (int i = 0; i < latlng.length(); i++) {
                    latlngArray[i] = latlng.get(i).toString();
                }

                hashMap.put("latitude", latlngArray[0]);
                hashMap.put("longitude", latlngArray[1]);

                ArrayList<String> callingCodes = new ArrayList<String>();
                JSONArray callingCodesJSON = detail.getJSONArray("callingCodes");
                for (int i = 0; i < callingCodesJSON.length(); i++) {
                    callingCodes.add(callingCodesJSON.get(i).toString());
                }
                for (int i = 0; i < callingCodes.size(); i++) {
                    hashMap.put("callingCodes" + i, callingCodes.get(i));
                }

                detailsList.add(hashMap);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return detailsList;
        }

    }
}
