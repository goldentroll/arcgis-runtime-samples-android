/* Copyright 2016 Esri
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

package com.esri.arcgisruntime.sample.displaydrawingstatus;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.esri.arcgisruntime.datasource.arcgis.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DrawStatus;
import com.esri.arcgisruntime.mapping.view.DrawStatusChangedEvent;
import com.esri.arcgisruntime.mapping.view.DrawStatusChangedListener;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // inflate MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);
        // create a map with the Basemap Type topographic
        ArcGISMap map = new ArcGISMap(Basemap.createTopographic());
        // create an envelope
        Envelope targetExtent = new Envelope(-13639984.0, 4537387.0, -13606734.0, 4558866.0, SpatialReferences.getWebMercator());
        // use envelope to set initial viewpoint
        Viewpoint initViewpoint = new Viewpoint(targetExtent);
        // set the initial viewpoint in the map
        map.setInitialViewpoint(initViewpoint);

        // create a feature table from a service url
        ServiceFeatureTable svcFeaturetable = new ServiceFeatureTable(getResources().getString(R.string.service_feature_table_url));
        // create a feature layer
        FeatureLayer featureLayer = new FeatureLayer(svcFeaturetable);
        // add feature layer to map
        map.getOperationalLayers().add(featureLayer);

        // set the map to be displayed in this view
        mMapView.setMap(map);

        //[DocRef: Name=Monitor map drawing, Category=Work with maps, Topic=Display a map]
        mMapView.addDrawStatusChangedListener(new DrawStatusChangedListener() {
            @Override
            public void drawStatusChanged(DrawStatusChangedEvent drawStatusChangedEvent) {
                if(drawStatusChangedEvent.getDrawStatus() == DrawStatus.IN_PROGRESS){
                    progressBar.setVisibility(View.VISIBLE);
                    Log.d("drawStatusChanged", "spinner visible");
                }else if (drawStatusChangedEvent.getDrawStatus() == DrawStatus.COMPLETED){
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
        //[DocRef: END]
    }

    @Override
    protected void onPause(){
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mMapView.resume();
    }
}
