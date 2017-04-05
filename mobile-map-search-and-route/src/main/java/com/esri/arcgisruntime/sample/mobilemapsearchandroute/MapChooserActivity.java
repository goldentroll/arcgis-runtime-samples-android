/* Copyright 2017 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgisruntime.sample.mobilemapsearchandroute;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MapChooserActivity extends AppCompatActivity {
    private RecyclerView mMapPreviewRecyclerView;
    private MapPreviewAdapter mMapPreviewAdapter;
    private List<MapPreview> mMapPreviews;
    private String mMMPkTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_chooser);
        //get intent extras
        Bundle intentExtras = getIntent().getExtras();
        if (intentExtras != null) {
            mMapPreviews = (List<MapPreview>) intentExtras.get("map_previews");
            mMMPkTitle = (String) intentExtras.get("MMPk_title");
        }
        TextView nameMMPkView = (TextView) findViewById(R.id.MMPk_title);
        nameMMPkView.setText(mMMPkTitle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //setup recycler view
        mMapPreviewRecyclerView = (RecyclerView) findViewById(R.id.map_preview_list);
        mMapPreviewRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        loadMapPreviews();
    }

    //create and load adapter
    private void loadMapPreviews() {
        if (mMapPreviewAdapter == null) {
            mMapPreviewAdapter = new MapPreviewAdapter(mMapPreviews);
            mMapPreviewRecyclerView.setAdapter(mMapPreviewAdapter);
        } else {
            mMapPreviewAdapter.setMapPreviews(mMapPreviews);
            mMapPreviewAdapter.notifyDataSetChanged();
        }
    }
    //implements recycler view to be extensible for MMPks with many maps
    private class MapPreviewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private MapPreview mMapPreview;
        private final TextView mTitleTextView;
        private final TextView mTransportView;
        private final TextView mGeotaggingView;
        private final TextView mDescTextView;
        private final ImageView mThumbnailImageView;

        //inflate views within holder
        private MapPreviewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.map_preview, parent, false));
            itemView.setOnClickListener(this);
            mTitleTextView = (TextView) itemView.findViewById(R.id.mapTitle);
            mTransportView = (TextView) itemView.findViewById(R.id.mapTransportNetwork);
            mGeotaggingView = (TextView) itemView.findViewById(R.id.mapGeotagging);
            mDescTextView = (TextView) itemView.findViewById(R.id.mapDesc);
            mThumbnailImageView = (ImageView) itemView.findViewById(R.id.mapThumbnail);
        }
        //bind data to views
        private void bind(MapPreview mapPreview) {
            mMapPreview = mapPreview;
            mTitleTextView.setText(mapPreview.getTitle());
            if (mapPreview.hasTransportNetwork()) {
                mTransportView.setText(R.string.has_transport);
            } else {
                mTransportView.setText(R.string.no_transport);
            }
            if (mapPreview.hasGeocoding()) {
                mGeotaggingView.setText(R.string.has_geotag);
            } else {
                mGeotaggingView.setText(R.string.no_geotag);
            }

            mDescTextView.setText(mapPreview.getDesc());
            // decode thumbnail from byte stream to bitmap
            Bitmap thumbnail = BitmapFactory.decodeByteArray(
                    mapPreview.getThumbnailByteStream(),
                    0,
                    mapPreview.getThumbnailByteStream().length);
            mThumbnailImageView.setImageBitmap(thumbnail);
        }

        @Override
        public void onClick(View view) {
            final int MAP_CHOSEN_RESULT = 1;
            Intent mapChosenIntent = new Intent(getApplicationContext(),
                    MobileMapViewActivity.class);
            //pass map number chosen back to mobile map view activity
            mapChosenIntent.putExtra("map_num", mMapPreview.getMapNum());
            setResult(MAP_CHOSEN_RESULT, mapChosenIntent);
            finish();
        }
    }

    private class MapPreviewAdapter extends RecyclerView.Adapter<MapPreviewHolder> {
        private List<MapPreview> mMapPreviews;

        private MapPreviewAdapter(List<MapPreview> mapPreviews) {
            mMapPreviews = mapPreviews;
        }

        @Override
        public MapPreviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
            return new MapPreviewHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(MapPreviewHolder holder, int position) {
            MapPreview mapPreview = mMapPreviews.get(position);
            holder.bind(mapPreview);
        }

        @Override
        public int getItemCount() {
            return mMapPreviews.size();
        }

        private void setMapPreviews(List<MapPreview> mapPreviews) {
            mMapPreviews = mapPreviews;
        }
    }
}
