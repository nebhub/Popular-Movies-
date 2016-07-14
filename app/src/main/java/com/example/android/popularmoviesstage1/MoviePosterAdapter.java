package com.example.android.popularmoviesstage1;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;


public class MoviePosterAdapter extends BaseAdapter {


    private Context context;
    private ArrayList<Movie> movieList;

    public MoviePosterAdapter(Context context, ArrayList<Movie> movieList){
        this.context = context;
        this.movieList = movieList;
    }

    public void setMovieList(ArrayList<Movie> movieList) {
        this.movieList = movieList;
    }

    public ArrayList<Movie> getMovieList() {
        return movieList;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        if (movieList == null){
            return 0;
        }
        return movieList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView==null){
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
        }

        ImageView moviePoster = (ImageView)convertView.findViewById(R.id.movie_poster);
        if (movieList!=null) {
            Picasso.with(context).load(movieList.get(position).getPosterUrl()).fit()
                    .into(moviePoster);
        }




        return convertView;
    }
}
