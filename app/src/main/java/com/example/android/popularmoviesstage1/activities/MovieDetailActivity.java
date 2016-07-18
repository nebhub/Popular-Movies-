package com.example.android.popularmoviesstage1.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmoviesstage1.models.Movie;
import com.example.android.popularmoviesstage1.R;
import com.squareup.picasso.Picasso;

public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView poster = (ImageView)findViewById(R.id.movie_poster_detail);
        TextView title = (TextView)findViewById(R.id.movie_title);
        TextView plotSynopsis = (TextView)findViewById(R.id.movie_plot_synopsis);
        TextView releaseDate = (TextView)findViewById(R.id.movie_releaseDate);
        TextView userRating = (TextView)findViewById(R.id.movie_user_rating);

        Movie movie =  getIntent().getParcelableExtra(getString(R.string.extra_key_movies));

        Picasso.with(this).load(movie.getPosterUrl()).fit()
                .into(poster);
        title.setText(movie.getOriginalTitle());
        plotSynopsis.setText(movie.getPlotSynopsis());
        releaseDate.setText(movie.getReleaseDate());
        userRating.setText(movie.getUserRating());

    }

}