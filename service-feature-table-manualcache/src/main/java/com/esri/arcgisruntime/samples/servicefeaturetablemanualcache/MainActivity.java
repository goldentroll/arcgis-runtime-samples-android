/* Copyright 2015 Esri
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

package com.esri.arcgisruntime.samples.servicefeaturetablemanualcache;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.datasource.FeatureQueryResult;
import com.esri.arcgisruntime.datasource.QueryParameters;
import com.esri.arcgisruntime.datasource.arcgis.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Map;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.Viewpoint;

public class MainActivity extends AppCompatActivity {


    MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // inflate MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);

        // create a map with the topographic basemap
        Map map = new Map(Basemap.createTopographic());

        // create feature layer with its service feature table
        // create the service feature table
        final ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(getResources().getString(R.string.sample_service_url));

        //explicitly set the mode to on manual cache (which means you need to call populate from service)
        serviceFeatureTable.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.MANUAL_CACHE);

        // create the feature layer using the service feature table
        FeatureLayer featureLayer = new FeatureLayer(serviceFeatureTable);

        // add the layer to the map
        map.getOperationalLayers().add(featureLayer);

        // load the table
        serviceFeatureTable.loadAsync();
        // add a done loading listener to call populate from service when the table is loaded is done
        serviceFeatureTable.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {

                // set up the query parameters
                QueryParameters params = new QueryParameters();
                // for a specific 311 request type
                params.setWhereClause("req_type = 'Tree Maintenance or Damage'");
                // set all outfields
                params.getOutFields().add("*");

                //populate the table based on the query, listen for result in a listenable future
                final ListenableFuture<FeatureQueryResult> future = serviceFeatureTable.populateFromServiceAsync(params, true);
                //add done listener to the future which fires when the async method is complete
                future.addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //call get on the future to get the result
                            FeatureQueryResult result = future.get();

                            //find out how many items there are in the result
                            int i = 0;
                            for (; result.iterator().hasNext(); ++i) {
                                result.iterator().next();
                            }
                            Toast.makeText(getApplicationContext(), i + " features returned", Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            Log.e(getResources().getString(R.string.app_name), "Populate from service failed: " + e.getMessage());
                        }
                    }
                });
            }
        });


        // set the map to be displayed in the mapview
        mMapView.setMap(map);

        //set a viewpoint on the mapview so it zooms to the features once they are cached.
        mMapView.setViewpoint(new Viewpoint(new Point(-13630484, 4545415, SpatialReferences.getWebMercator()), 500000));

    }

    @Override
    protected void onPause(){
        super.onPause();
        // pause MapView
        mMapView.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        // resume MapView
        mMapView.resume();
    }
}
