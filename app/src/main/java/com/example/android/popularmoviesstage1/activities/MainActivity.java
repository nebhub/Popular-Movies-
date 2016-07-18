package com.example.android.popularmoviesstage1.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.android.popularmoviesstage1.BuildConfig;
import com.example.android.popularmoviesstage1.models.Movie;
import com.example.android.popularmoviesstage1.adapters.MoviePosterAdapter;
import com.example.android.popularmoviesstage1.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public final static String TAG = "Main Activity";
    private MoviePosterAdapter adapter;
    private ProgressDialog progressDialog;
    private NetworkStateReceiver networkStateReceiver;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.progress_dialog_title));
        progressDialog.setMessage(getString(R.string.progress_dialog_message));
        progressDialog.setIndeterminate(true);
        progressDialog.dismiss();

        //setup GridView adapter
        adapter = new MoviePosterAdapter(this, null);

        //setup movieList GridView
        GridView movieList = (GridView) findViewById(R.id.gridView);
        movieList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent detailActivity = new Intent(MainActivity.this, MovieDetailActivity.class);
                detailActivity.putExtra(getString(R.string.extra_key_movies), adapter.getMovieList().get(position));

                startActivity(detailActivity);
            }
        });
        movieList.setAdapter(adapter);

        //setup spinner
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.movie_sort_order_tags, R.layout.spinner_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //download movie data
                fetchData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }

        });
        //download movie data
        fetchData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (networkStateReceiver != null) {
            registerReceiver(networkStateReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (networkStateReceiver != null) {
            unregisterReceiver(networkStateReceiver);
        }
    }

    /**
     * Starts the MovieDataDownloadTask if there is internet connection. If there is no connection
     * registers a broadcast receiver, if not already registered.
     */
    public void fetchData() {
       if (internetAvailable()) {
            //Download movie data
            new MovieDataDownloaderTask().execute(getResources().
                    getStringArray(R.array.movie_sort_order_values)[spinner.getSelectedItemPosition()]);
            }
        else {
            Toast.makeText(this, getString(R.string.message_no_internet), Toast.LENGTH_SHORT).show();
            if (networkStateReceiver == null){
            networkStateReceiver = new NetworkStateReceiver();
            registerReceiver(networkStateReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
            }

        }
    }

    /**
     * updates The adapter with the specified ArrayList
     *
     * @param updatedList The new ArrayList to replace the old one.
     */
    public void updateView(ArrayList<Movie> updatedList) {

        if (updatedList != null) {
            adapter.setMovieList(updatedList);
            adapter.notifyDataSetChanged();
        }

    }

    /** Code from developer.android.com/training/monitoring-device-state/connectivity-monitoring.html
     * Checks if internet connection is available or not.
     *
     * @return returs true if connection is available, false if not.
     */
    public boolean internetAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    class MovieDataDownloaderTask extends AsyncTask<String, Void, String> {

        public static final String HTTP_REQUEST_METHOD_GET = "GET";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            return download(params[0]);
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            updateView(parse(data));

            //dismiss the progress dialog
            progressDialog.dismiss();

            //unregister broadcast receiver if it was registered.
            if (networkStateReceiver != null) {
                unregisterReceiver(networkStateReceiver);
                networkStateReceiver = null;
            }
        }

        /**
         * @param category a movie sort(filter) category that will be appended to the base URL
         * @return list of movies in a json format
         */
        public String download(String category) {

            String baseUrl = getString(R.string.movie_db_base_url);

            //API KEY
            String APIkey = BuildConfig.MOVIE_DB_API_KEY;

            //setup URL
            String PARAM_API_KEY = getString(R.string.movie_db_param_api_key);
            Uri uri = Uri.parse(baseUrl).buildUpon().appendPath(category)
                    .appendQueryParameter(PARAM_API_KEY, APIkey).build();

            HttpURLConnection connection;
            BufferedReader reader;
            String jsonData = null;

            try {
                URL url = new URL(uri.toString());
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(HTTP_REQUEST_METHOD_GET);

                connection.connect();
                InputStream is = connection.getInputStream();
                StringBuilder buffer = new StringBuilder();

                if (is == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {

                    buffer.append(line).append("\n");
                }
                if (buffer.length() == 0) {
                    // Stream was empty.
                    return null;
                }
                jsonData = buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return jsonData;

        }

        /**
         * Parses a list of movies from a json format to an ArrayList of Movie objects
         *
         * @param jsonData list of movies in a json format
         * @return array list of Movie object
         */
        public ArrayList<Movie> parse(String jsonData) {

            if (jsonData == null) {
                Toast.makeText(MainActivity.this, getString(R.string.message_no_data), Toast.LENGTH_SHORT).show();
                return null;
            }

            ArrayList<Movie> movies = null;
            try {
                JSONObject root = new JSONObject(jsonData);
                JSONArray result = root.getJSONArray(getString(R.string.api_tag_results));
                movies = new ArrayList<>();
                for (int i = 0; i < result.length(); i++) {

                    Movie movie = new Movie();
                    movie.setOriginalTitle(result.getJSONObject(i).getString(getString(R.string.api_tag_original_title)));

                    //set up URL
                    String url = getResources().getString(R.string.api_poster_base_url) +
                            getResources().getString(R.string.api_poster_size_default) +
                            result.getJSONObject(i).getString(getString(R.string.api_tag_poster_path));

                    movie.setPosterUrl(url);
                    movie.setPlotSynopsis(result.getJSONObject(i).getString(getString(R.string.api_tag_overview)));
                    movie.setReleaseDate(result.getJSONObject(i).getString(getString(R.string.api_tag_release_date)));
                    movie.setUserRating(result.getJSONObject(i).getString(getString(R.string.api_tag_vote_average)));
                    movies.add(movie);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return movies;
        }

    }

    public class NetworkStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            /**
             * since the app is only concerned about whether there is internet connection or not,
             * its going to call fetchData method. Which checks for internet connectivity before
             * downloading data.
             */
            fetchData();
        }
    }
}