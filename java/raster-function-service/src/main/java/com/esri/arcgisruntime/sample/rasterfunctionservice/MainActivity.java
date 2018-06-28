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

package com.esri.arcgisruntime.sample.rasterfunctionservice;

import java.util.List;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.ImageServiceRaster;
import com.esri.arcgisruntime.raster.Raster;
import com.esri.arcgisruntime.raster.RasterFunction;
import com.esri.arcgisruntime.raster.RasterFunctionArguments;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private Button mRasterFunctionButton;
  private ArcGISMap map;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);
    mRasterFunctionButton = findViewById(R.id.rasterButton);
    mRasterFunctionButton.setEnabled(false);
    // create a map with the BasemapType topographic
    map = new ArcGISMap(Basemap.createDarkGrayCanvasVector());
    final ImageServiceRaster imageServiceRaster = new ImageServiceRaster(getString(R.string.image_service_raster_url));
    final RasterLayer imageRasterLayer = new RasterLayer(imageServiceRaster);
    map.getOperationalLayers().add(imageRasterLayer);
    imageRasterLayer.addDoneLoadingListener(() -> {
      if (imageRasterLayer.getLoadStatus() == LoadStatus.LOADED) {
        // get the center point
        Point centerPnt = imageServiceRaster.getServiceInfo().getFullExtent().getCenter();
        mMapView.setViewpointCenterAsync(centerPnt, 55000000);
        mRasterFunctionButton.setEnabled(true);
      }
    });

    mRasterFunctionButton.setOnClickListener(v -> applyRasterFunction(imageServiceRaster));

    // set the map to be displayed in this view
    mMapView.setMap(map);
  }

  private void applyRasterFunction(Raster raster) {
    // create raster function from json string
    RasterFunction rasterFunction = RasterFunction.fromJson(getString(R.string.hillshade_simplified));
    // get parameter name value pairs used by hillside
    RasterFunctionArguments rasterFunctionArguments = rasterFunction.getArguments();
    // get a list of raster names associated with a raster function
    List<String> rasterName = rasterFunctionArguments.getRasterNames();
    rasterFunctionArguments.setRaster(rasterName.get(0), raster);
    // create raster as raster layer
    raster = new Raster(rasterFunction);
    RasterLayer hillshadeLayer = new RasterLayer(raster);
    map.getOperationalLayers().add(hillshadeLayer);
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