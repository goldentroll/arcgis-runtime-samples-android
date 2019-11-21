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

package com.esri.arcgisruntime.sample.generategeodatabase;

import java.io.File;
import java.util.concurrent.ExecutionException;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.esri.arcgisruntime.concurrent.Job;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Geodatabase;
import com.esri.arcgisruntime.data.GeodatabaseFeatureTable;
import com.esri.arcgisruntime.data.TileCache;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.tasks.geodatabase.GenerateGeodatabaseJob;
import com.esri.arcgisruntime.tasks.geodatabase.GenerateGeodatabaseParameters;
import com.esri.arcgisruntime.tasks.geodatabase.GeodatabaseSyncTask;

public class MainActivity extends AppCompatActivity {

  private final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  private TextView mProgressTextView;
  private RelativeLayout mProgressLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // define permission to request
    String[] reqPermission = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
    int requestCode = 2;
    // For API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(MainActivity.this, reqPermission[0]) != PackageManager.PERMISSION_GRANTED) {
      // request permission
      ActivityCompat.requestPermissions(MainActivity.this, reqPermission, requestCode);
    }

    // use local tile package for the base map
    TileCache sanFrancisco = new TileCache(
        Environment.getExternalStorageDirectory() + getString(R.string.san_francisco_tpk));
    ArcGISTiledLayer tiledLayer = new ArcGISTiledLayer(sanFrancisco);

    // create a map view and add a map
    mMapView = (MapView) findViewById(R.id.mapView);
    final ArcGISMap map = new ArcGISMap(new Basemap(tiledLayer));
    mMapView.setMap(map);

    // create a graphics overlay and symbol to mark the extent
    final GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(graphicsOverlay);
    final SimpleLineSymbol boundarySymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 5);

    // inflate button and progress layout
    final Button genGeodatabaseButton = (Button) findViewById(R.id.genGeodatabaseButton);
    mProgressLayout = (RelativeLayout) findViewById(R.id.progressLayout);
    final ProgressBar progressBar = (ProgressBar) findViewById(R.id.taskProgressBar);
    mProgressTextView = (TextView) findViewById(R.id.progressTextView);

    // create a geodatabase sync task
    final GeodatabaseSyncTask geodatabaseSyncTask = new GeodatabaseSyncTask(getString(R.string.wildfire_sync));
    geodatabaseSyncTask.loadAsync();
    geodatabaseSyncTask.addDoneLoadingListener(new Runnable() {
      @Override public void run() {

        // generate the geodatabase sync task
        genGeodatabaseButton.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View v) {

            // show the progress layout
            progressBar.setProgress(0);
            mProgressLayout.setVisibility(View.VISIBLE);

            // clear any previous operational layers and graphics if button clicked more than once
            map.getOperationalLayers().clear();
            graphicsOverlay.getGraphics().clear();

            // show the extent used as a graphic
            Envelope extent = mMapView.getVisibleArea().getExtent();
            Graphic boundary = new Graphic(extent, boundarySymbol);
            graphicsOverlay.getGraphics().add(boundary);

            // create generate geodatabase parameters for the current extent
            final ListenableFuture<GenerateGeodatabaseParameters> defaultParameters = geodatabaseSyncTask
                .createDefaultGenerateGeodatabaseParametersAsync(extent);
            defaultParameters.addDoneListener(new Runnable() {
              @Override public void run() {
                try {
                  // set parameters and don't include attachments
                  GenerateGeodatabaseParameters parameters = defaultParameters.get();
                  parameters.setReturnAttachments(false);

                  // define the local path where the geodatabase will be stored
                  final String localGeodatabasePath =
                      getCacheDir().toString() + File.separator + getString(R.string.wildfire_geodatabase);

                  // create and start the job
                  final GenerateGeodatabaseJob generateGeodatabaseJob = geodatabaseSyncTask
                      .generateGeodatabaseAsync(parameters, localGeodatabasePath);
                  generateGeodatabaseJob.start();
                  mProgressTextView.setText(getString(R.string.progress_started));

                  // update progress
                  generateGeodatabaseJob.addProgressChangedListener(new Runnable() {
                    @Override public void run() {
                      progressBar.setProgress(generateGeodatabaseJob.getProgress());
                      mProgressTextView.setText(getString(R.string.progress_fetching));
                    }
                  });

                  // get geodatabase when done
                  generateGeodatabaseJob.addJobDoneListener(new Runnable() {
                    @Override public void run() {
                      mProgressLayout.setVisibility(View.INVISIBLE);
                      if (generateGeodatabaseJob.getStatus() == Job.Status.SUCCEEDED) {
                        final Geodatabase geodatabase = generateGeodatabaseJob.getResult();
                        geodatabase.loadAsync();
                        geodatabase.addDoneLoadingListener(new Runnable() {
                          @Override public void run() {
                            if (geodatabase.getLoadStatus() == LoadStatus.LOADED) {
                              mProgressTextView.setText(getString(R.string.progress_done));
                              for (GeodatabaseFeatureTable geodatabaseFeatureTable : geodatabase
                                  .getGeodatabaseFeatureTables()) {
                                geodatabaseFeatureTable.loadAsync();
                                map.getOperationalLayers().add(new FeatureLayer(geodatabaseFeatureTable));
                              }
                              genGeodatabaseButton.setVisibility(View.GONE);
                              Log.i(TAG, "Local geodatabase stored at: " + localGeodatabasePath);
                            } else {
                              Log.e(TAG, "Error loading geodatabase: " + geodatabase.getLoadError().getMessage());
                            }
                          }
                        });
                        // unregister since we're not syncing
                        ListenableFuture unregisterGeodatabase = geodatabaseSyncTask
                            .unregisterGeodatabaseAsync(geodatabase);
                        unregisterGeodatabase.addDoneListener(new Runnable() {
                          @Override public void run() {
                            Log.i(TAG, "Geodatabase unregistered since we wont be editing it in this sample.");
                            Toast.makeText(MainActivity.this,
                                "Geodatabase unregistered since we wont be editing it in this sample.",
                                Toast.LENGTH_LONG).show();
                          }
                        });
                      } else if (generateGeodatabaseJob.getError() != null) {
                        Log.e(TAG, "Error generating geodatabase: " + generateGeodatabaseJob.getError().getMessage());
                      } else {
                        Log.e(TAG, "Unknown Error generating geodatabase");
                      }
                    }
                  });
                } catch (InterruptedException | ExecutionException e) {
                  Log.e(TAG, "Error generating geodatabase parameters : " + e.getMessage());
                }
              }
            });
          }
        });
      }
    });
  }

  @Override
  protected void onPause() {
    super.onPause();
    mMapView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mMapView.dispose();
  }
}
