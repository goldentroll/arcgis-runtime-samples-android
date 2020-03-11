/*
 * Copyright 2020 Esri
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

package com.esri.arcgisruntime.sample.featurelayerselection

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

  private val TAG: String = MainActivity::class.java.simpleName

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create service feature table and a feature layer from it
    val serviceFeatureTable = ServiceFeatureTable(getString(R.string.gdp_per_capita_url))
    val featureLayer = FeatureLayer(serviceFeatureTable)

    // create a map with the streets base map type
    val streetsMap = ArcGISMap(Basemap.createStreets()).apply {
      // set an initial view point
      initialViewpoint = Viewpoint(Envelope(
        -1131596.019761, 3893114.069099, 3926705.982140, 7977912.461790,
        SpatialReferences.getWebMercator()))
      // add the feature layer to the map's operational layers
      operationalLayers.add(featureLayer)
    }

    mapView.let {
      // set the map to be displayed in the layout's map view
      it.map = streetsMap
      // give any item selected on the map view a red selection halo
      it.selectionProperties.color = Color.RED
      // set an on touch listener on the map view
      it.onTouchListener = object : DefaultMapViewOnTouchListener(this, it) {
        override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
          // get the point that was tapped and convert it to a point in map coordinates
          val tappedPoint = it.screenToLocation(android.graphics.Point(motionEvent.x.roundToInt(),
            motionEvent.y.roundToInt()))
          // set a tolerance for accuracy of returned selections from point tapped
          val tolerance = 25
          val mapTolerance = tolerance * it.unitsPerDensityIndependentPixel
          // create objects required to do a selection with a query
          val envelope = Envelope(tappedPoint.x - mapTolerance, tappedPoint.y - mapTolerance,
            tappedPoint.x + mapTolerance, tappedPoint.y + mapTolerance, streetsMap.spatialReference)
          // define query parameters for the extent tapped
          val queryParameters = QueryParameters()
          queryParameters.geometry = envelope
          // call select features
          val featureQueryResultFuture =
            featureLayer.selectFeaturesAsync(queryParameters, FeatureLayer.SelectionMode.NEW)
          // add done loading listener to fire when the selection returns
          featureQueryResultFuture.addDoneListener{
            try {
              // call get on the future to get the result
              val featureQueryResult = featureQueryResultFuture.get()
              // create an Iterator
              val iterator = featureQueryResult.iterator()
              // cycle through selections
              var counter = 0
              // count the features selected
              while (iterator.hasNext()) {
                counter++
              }
              Toast.makeText(applicationContext, "$counter features selected", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
              val errorMessage = "Select feature failed: " + e.message
              Log.e(TAG, errorMessage)
              Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_LONG).show()
            }
          }
          return super.onSingleTapConfirmed(motionEvent)
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onPause() {
    mapView.pause()
    super.onPause()
  }

  override fun onDestroy() {
    mapView.dispose()
    super.onDestroy()
  }
}
