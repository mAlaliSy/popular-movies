package com.shamdroid.popularmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import Utilities.MyPreferencesManager;
import Utilities.VolleySingleton;

public class MainActivity extends AppCompatActivity {


    RecyclerView recyclerView ;

    RequestQueue requestQueue ;
    ArrayList<Movie> movies ;

    String currentSortingBy ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.mainRecyclerView); // Binding the RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this , getResources().getInteger(R.integer.mainNumberOfColumns))); // Setting the layout to GridLayout. The number of columns depends on the device type ( Mobile - Tablet )


        requestQueue = VolleySingleton.getInstance().getRequestQueue(this);
        movies = new ArrayList<>();

        MyRecyclerViewAdapter myRecyclerViewAdapter = new MyRecyclerViewAdapter(movies); // Initializing the RecyclerView adapter with empty ArrayList . *After we get the data , we will notify the adapter about the changes
        recyclerView.setAdapter(myRecyclerViewAdapter);


        currentSortingBy = MyPreferencesManager.getSortingSetting(this) ; // Getting the sorting method stored in the SharedPreferences

        getMoviesFromAPI(currentSortingBy); // Getting the data with the selected sorting method and notify the adapter about the changes

    }


    public void getMoviesFromAPI (String sortBy){ // Getting the movies with the selected sorting method passed and update the data in the recyclerview's adapter

        if (!movies.isEmpty()) // Make sure that the movies ArrayList is empty
            movies.clear();  // If it is not empty , clear it

        //## Start Building the URL for getting the movies depending on the order chosen by the user ##//
        String urlRequest; // The url for getting the movies depending on the order chosen by the user


        if (sortBy.equals(MyPreferencesManager.POPULAR_VALUE)){ // Determine the URL of the sorting by method chosen by the user
            urlRequest = VolleySingleton.POPULAR_URL ;
        }else{
            urlRequest = VolleySingleton.TOP_RATER_URL ;
        }

        urlRequest+= "?" + VolleySingleton.API_KEY_GET_REQUEST_NAME + "=" + VolleySingleton.API_KEY ; // Appending the API Key required by themoviedb

        //## End Building the URL for getting the movies depending on the order chosen by the user ##//

        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlRequest, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {

                    // Getting the data from the response and fill the ArrayList movies

                    JSONObject mResponse = new JSONObject(response);
                    JSONArray moviesArray = mResponse.getJSONArray("results");

                    for (int i = 0; i < moviesArray.length(); i++) {

                        JSONObject oneMovie = moviesArray.getJSONObject(i);

                        String title = oneMovie.getString("original_title");
                        String posterURL = VolleySingleton.IMAGES_URL +  oneMovie.getString("poster_path");
                        String releaseDate = oneMovie.getString("release_date");
                        String overview = oneMovie.getString("overview");
                        float voteAverage = (float) oneMovie.getDouble("vote_average");

                        int id = oneMovie.getInt("id");

                        Movie movie = new Movie (id , title , posterURL , overview , releaseDate , voteAverage);
                        movies.add(movie);
                    }


                    recyclerView.getAdapter().notifyDataSetChanged(); // Update the recycler view data


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleySingleton.buildConnectionErrorDialog(MainActivity.this).show(); // Show the connection error dialog
            }
        });

        requestQueue.add(stringRequest);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id){
            case R.id.mainMenuMostPopular :
                if (!currentSortingBy.equals(MyPreferencesManager.POPULAR_VALUE)) { // Make sure that the user changed the sorting method before updating the data
                    MyPreferencesManager.setSortingSetting(this, MyPreferencesManager.POPULAR_VALUE); // Update the sorting method stored in the ShredPreferences
                    getMoviesFromAPI(MyPreferencesManager.POPULAR_VALUE); // Get the movies from the API with the new selected sorting method
                    currentSortingBy = MyPreferencesManager.POPULAR_VALUE; // Update the current selected sorting method
                }
                break ;

            case R.id.mainMenuTopRated :
                if (!currentSortingBy.equals(MyPreferencesManager.TOP_RATED_VALUE)) { // Make sure that the user changed the sorting method  before updating the data
                    MyPreferencesManager.setSortingSetting(this, MyPreferencesManager.TOP_RATED_VALUE); // Update the sorting method stored in the ShredPreferences
                    getMoviesFromAPI(MyPreferencesManager.TOP_RATED_VALUE); // Get the movies from the API with the new selected sorting method
                    currentSortingBy = MyPreferencesManager.TOP_RATED_VALUE; // Update the current selected sorting method
                }
                break;
        }

        return true;
    }


    class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder> {


        ArrayList<Movie> moviesData ;

        public MyRecyclerViewAdapter(ArrayList<Movie> movies) {
            moviesData = movies;
        }


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item , parent , false);

            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            String imageURL = moviesData.get(position).getPosterURL();

            Picasso.with(MainActivity.this) // Load the poster image to the image view
                    .load(imageURL)
                    .into(holder.poster);

            holder.poster.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { // Go to the details activity when the poster is clicked
                    Intent intent = new Intent(MainActivity.this , MovieDetails.class);
                    intent.putExtra("movie" , moviesData.get(position)); // Since the class Movie implements the Parcelable class , we can pass it with the intent
                    startActivity(intent);
                }
            });


        }

        @Override
        public int getItemCount() {
            return moviesData.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder{

            ImageView poster ;

            public MyViewHolder(View itemView) {
                super(itemView);
                poster = (ImageView) itemView ;
            }
        }
    }
}
