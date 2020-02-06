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

package com.esri.arcgisruntime.samples.rasterfunctionservice

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.layers.RasterLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.raster.ImageServiceRaster
import com.esri.arcgisruntime.raster.Raster
import com.esri.arcgisruntime.raster.RasterFunction
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a Dark Gray Vectory BaseMap
    val map = ArcGISMap(Basemap.createDarkGrayCanvasVector())
    // set the map to be displayed in this view
    mapView.map = map
    // create image service raster as raster layer
    val imageServiceRaster = ImageServiceRaster(
      resources.getString(R.string.image_service_raster_url)
    )
    val imageRasterLayer = RasterLayer(imageServiceRaster)
    map.operationalLayers.add(imageRasterLayer)
    // zoom to the extent of the raster service
    imageRasterLayer.addDoneLoadingListener {
      if (imageRasterLayer.loadStatus == LoadStatus.LOADED) {
        // zoom to extent of raster
        val centerPnt = imageServiceRaster.serviceInfo.fullExtent.center
        mapView.setViewpointCenterAsync(centerPnt, 55000000.0)
        // update raster with simplified hillshade
        applyRasterFunction(imageServiceRaster)
      }
    }
  }

  /**
   * Apply a raster function on the given Raster
   *
   * @param raster Input raster to apply function
   */
  private fun applyRasterFunction(raster: Raster) {
    // create raster function from json string
    val rasterFunction = RasterFunction.fromJson(resources.getString(R.string.hillshade_simplified))
    // get parameter name value pairs used by hillshade
    val rasterFunctionArguments = rasterFunction.arguments
    // get list of raster names associated with raster function
    val rasterName = rasterFunctionArguments.rasterNames
    // set raster to the raster name
    rasterFunctionArguments.setRaster(rasterName[0], raster)
    // create raster as raster layer
    val raster = Raster(rasterFunction)
    val hillshadeLayer = RasterLayer(raster)
    // add hillshade raster
    mapView.map.operationalLayers.add(hillshadeLayer)
  }

  override fun onPause() {
    super.onPause()
    mapView.pause()
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onDestroy() {
    super.onDestroy()
    mapView.dispose()
  }
}
