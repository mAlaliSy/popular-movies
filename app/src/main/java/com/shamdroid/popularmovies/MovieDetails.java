package com.shamdroid.popularmovies;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MovieDetails extends AppCompatActivity {

    TextView txtTitle , txtReleaseDate , txtRate , txtOverview ;
    ImageView imgPoster ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        Movie movie = getIntent().getParcelableExtra("movie"); // Getting the Movie object passed with the intent

        // Binding the views

        txtTitle = (TextView) findViewById(R.id.txtDetailsTitle);
        txtReleaseDate = (TextView) findViewById(R.id.txtDetailsReleaseDate);
        txtRate = (TextView) findViewById(R.id.txtDetailsRate);
        txtOverview = (TextView) findViewById(R.id.txtDetailsOverview);
        imgPoster = (ImageView) findViewById(R.id.imgDetailsPoster);


        // Setting the texts
        txtTitle.setText(movie.getTitle());
        txtReleaseDate.setText(movie.getReleaseDate());
        txtOverview.setText(movie.getOverview());

        String rate = movie.getVoteAverage() + getString(R.string.outOf10);
        txtRate.setText(rate);

        // Load the poster
        Picasso.with(this)
                .load(movie.getPosterURL())
                .into(imgPoster);

    }
}
