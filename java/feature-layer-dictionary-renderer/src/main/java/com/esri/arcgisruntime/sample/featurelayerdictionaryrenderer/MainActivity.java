/* Copyright 2018 Esri
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

package com.esri.arcgisruntime.sample.featurelayerdictionaryrenderer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.esri.arcgisruntime.data.Geodatabase;
import com.esri.arcgisruntime.data.GeodatabaseFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.DictionaryRenderer;
import com.esri.arcgisruntime.symbology.DictionarySymbolStyle;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get the reference to the map view
    mMapView = findViewById(R.id.mapView);

    ArcGISMap map = new ArcGISMap(Basemap.createTopographic());
    // set the map to the map view
    mMapView.setMap(map);

    // for API level 23+ request permission at runtime
    requestReadPermission();
  }

  /**
   * Load Geo-Database and display features from layer using mil2525d symbols.
   */
  private void loadGeodatabaseSymbolDictionary() {
    // load geo-database from local location
    Geodatabase geodatabase = new Geodatabase(
        Environment.getExternalStorageDirectory() + getString(R.string.militaryoverlay_geodatabase));
    geodatabase.loadAsync();

    // render tells layer what symbols to apply to what features
    DictionarySymbolStyle symbolDictionary = DictionarySymbolStyle
        .createFromFile(Environment.getExternalStorageDirectory() + getString(R.string.mil2525d_stylx));
    symbolDictionary.loadAsync();

    geodatabase.addDoneLoadingListener(() -> {
      if (geodatabase.getLoadStatus() == LoadStatus.LOADED) {

        for (GeodatabaseFeatureTable table : geodatabase.getGeodatabaseFeatureTables()) {
          // add each layer to map
          FeatureLayer featureLayer = new FeatureLayer(table);
          featureLayer.loadAsync();
          // features no longer show after this scale
          featureLayer.setMinScale(1000000);
          mMapView.getMap().getOperationalLayers().add(featureLayer);

          symbolDictionary.addDoneLoadingListener(() -> {
            if (symbolDictionary.getLoadStatus() == LoadStatus.LOADED) {
              // displays features from layer using mil2525d symbols
              DictionaryRenderer dictionaryRenderer = new DictionaryRenderer(symbolDictionary);
              featureLayer.setRenderer(dictionaryRenderer);

              featureLayer.addDoneLoadingListener(() -> {
                if (featureLayer.getLoadStatus() == LoadStatus.LOADED) {
                  // initial viewpoint to encompass all graphics displayed on the map view
                  mMapView.setViewpointGeometryAsync(featureLayer.getFullExtent());
                } else {
                  String error = "Feature Layer Failed to Load: " + featureLayer.getLoadError().getMessage();
                  Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                  Log.e(TAG, error);
                }
              });
            } else {
              String error = "Dictionary Symbol Failed to Load: " + symbolDictionary.getLoadError().getMessage();
              Toast.makeText(this, error, Toast.LENGTH_LONG).show();
              Log.e(TAG, error);
            }
          });
        }
      } else {
        String error = "Geodatabase Failed to Load: " + geodatabase.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  /**
   * Request read external storage for API level 23+.
   */
  private void requestReadPermission() {
    // define permission to request
    String[] reqPermission = { Manifest.permission.READ_EXTERNAL_STORAGE };
    int requestCode = 2;
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      loadGeodatabaseSymbolDictionary();
    } else {
      // request permission
      ActivityCompat.requestPermissions(this, reqPermission, requestCode);
    }
  }

  /**
   * Handle the permissions request response
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      loadGeodatabaseSymbolDictionary();
    } else {
      // report to user that permission was denied
      Toast.makeText(this, getResources().getString(R.string.write_permission_denied),
          Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  protected void onPause() {
    mMapView.pause();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onDestroy() {
    mMapView.dispose();
    super.onDestroy();
  }
}
