/*
 *  Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.esri.arcgisruntime.sample.annotationsublayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.layers.AnnotationLayer;
import com.esri.arcgisruntime.layers.AnnotationSublayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);

    // show current map scale in a text view at the bottom of the screen
    TextView currentMapScaleTextView = findViewById(R.id.mapScale);
    mMapView.addMapScaleChangedListener(mapScaleChangedEvent -> currentMapScaleTextView
        .setText(getString(R.string.map_scale, Math.round(mMapView.getMapScale()))));

    requestReadPermission();
  }

  /**
   * Load the mobile map package and get layer names for both annotation sublayers. Use those names to populate
   * checkboxes which are used to set the visibility of the associated annotation sublayers to a boolean value.
   */
  private void addSublayersWithAnnotation() {
    // get a reference to checkboxes
    CheckBox closedCheckBox = findViewById(R.id.closedCheckBox);
    CheckBox openCheckBox = findViewById(R.id.openCheckBox);

    // load the mobile map package
    MobileMapPackage mobileMapPackage = new MobileMapPackage(
        Environment.getExternalStorageDirectory() + getString(R.string.gas_device_anno_mmpk_path));
    mobileMapPackage.loadAsync();
    mobileMapPackage.addDoneLoadingListener(() -> {
      if (mobileMapPackage.getLoadStatus() == LoadStatus.LOADED) {
        // set the mobile map package's map to the map view
        mMapView.setMap(mobileMapPackage.getMaps().get(0));
        // find the annotation layer within the map
        for (Layer layer : mMapView.getMap().getOperationalLayers()) {
          if (layer instanceof AnnotationLayer) {
            // load the annotation layer. The layer must be loaded in order to access sub-layer contents
            layer.loadAsync();
            layer.addDoneLoadingListener(() -> {
              // get annotation sublayer name from sublayer contents
              AnnotationSublayer closedLayer = (AnnotationSublayer) layer.getSubLayerContents().get(0);
              AnnotationSublayer openLayer = (AnnotationSublayer) layer.getSubLayerContents().get(1);

              // set the layer name from the
              closedCheckBox.setText(buildLayerName(closedLayer));
              openCheckBox.setText(buildLayerName(openLayer));

              // toggle annotation sublayer visibility on check
              closedCheckBox.setOnCheckedChangeListener(
                  (checkBoxView, isChecked) -> closedLayer.setVisible(isChecked));
              openCheckBox.setOnCheckedChangeListener(
                  (checkBoxView, isChecked) -> openLayer.setVisible(isChecked));

              // when the map scale changes
              mMapView.addMapScaleChangedListener(mapScaleChangedEvent -> {
                // if the "open" layer is visible, set text color to black, otherwise set it to gray
                if (openLayer.isVisibleAtScale(mMapView.getMapScale())) {
                  openCheckBox.setTextColor(Color.BLACK);
                } else {
                  openCheckBox.setTextColor(Color.LTGRAY);
                }
              });

            });
          }
        }
      } else {
        String error = "Mobile map package failed load: " + mobileMapPackage.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  /**
   * Get name, and where relevant, append min and max scales of each annotation sublayer.
   *
   * @param annotationSublayer
   * @return the layer name with min max scales, where relevant
   */
  private String buildLayerName(AnnotationSublayer annotationSublayer) {
    StringBuilder layerNameBuilder = new StringBuilder(annotationSublayer.getName());
    if (!Double.isNaN(annotationSublayer.getMaxScale()) && !Double.isNaN(annotationSublayer.getMinScale())) {
      layerNameBuilder.append(" (1:").append((int) annotationSublayer.getMaxScale()).append(" - 1:")
          .append((int) annotationSublayer.getMinScale()).append(")");
    }
    return layerNameBuilder.toString();
  }

  /**
   * Request read external storage for API level 23+.
   */
  private void requestReadPermission() {
    // define permission to request
    String[] reqPermission = { Manifest.permission.READ_EXTERNAL_STORAGE };
    int requestCode = 2;
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      addSublayersWithAnnotation();
    } else {
      ActivityCompat.requestPermissions(this, reqPermission, requestCode);
    }
  }

  /**
   * Handle the permissions request response.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      addSublayersWithAnnotation();
    } else {
      // report to user that permission was denied
      Toast.makeText(this, getString(R.string.read_local_mmpk_denied_message), Toast.LENGTH_SHORT).show();
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
