package com.brandactif.scandemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

class Movie {
    String title;
    String videoName;
    int thumbnailId;
    int videoId;

    Movie(String title, String videoName, int thumbnailId, int videoId) {
        this.title = title;
        this.videoName = videoName;
        this.thumbnailId = thumbnailId;
        this.videoId = videoId;
    }
}

class CustomAdapter implements ListAdapter {
    ArrayList<Movie> arrayList;
    Context context;

    public CustomAdapter(Context context, ArrayList<Movie> arrayList) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Movie movie = arrayList.get(position);
        if(convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.list_row, null);
            TextView title = convertView.findViewById(R.id.tvTitle);
            TextView link = convertView.findViewById(R.id.tvLink);
            ImageView image = convertView.findViewById(R.id.imgPoster);
            title.setText(movie.title);
            image.setImageResource(movie.thumbnailId);
        }
        return convertView;
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }
    @Override
    public int getViewTypeCount() {
        return arrayList.size();
    }
    @Override
    public boolean isEmpty() {
        return false;
    }
}

public class VideoListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        final ListView listView = findViewById(R.id.listView);
        ArrayList<Movie> arrayList = new ArrayList<>();
        arrayList.add(new Movie("The Wolf Of Wall Street",
                "b5823bd3-aaf3-4031-a6a3-a7331c835e52",
                R.mipmap.wows,
                R.raw.the_wolf_of_wall_street));

        arrayList.add(new Movie("The Matrix",
                "516e4592-d18b-4f3f-85e7-36c300bd07cd",
                R.mipmap.matrix,
                R.raw.matrix));

        arrayList.add(new Movie("Mediacorp",
                getString(R.string.video_uuid),
                R.mipmap.matrix,
                R.raw.mediacorp));

        CustomAdapter customAdapter = new CustomAdapter(this, arrayList);
        listView.setAdapter(customAdapter);

        listView.setOnItemClickListener((adapterView, view, position, l) -> {
            Movie selectedMovie = arrayList.get(position);
            if (selectedMovie == null) {
                return;
            }

            // Open VideoActivity
            Intent intent = new Intent(getApplicationContext(), VideoActivity.class);
            intent.putExtra("videoTitle", selectedMovie.title);
            intent.putExtra("videoName", selectedMovie.videoName);
            intent.putExtra("videoId", selectedMovie.videoId);
            startActivity(intent);
        });
    }
}
