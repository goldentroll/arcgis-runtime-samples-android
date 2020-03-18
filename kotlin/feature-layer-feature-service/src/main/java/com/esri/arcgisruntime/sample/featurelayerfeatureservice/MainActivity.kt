/* Copyright 2020 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the Sample code usage restrictions document for further information.
 *
 */
package com.esri.arcgisruntime.sample.featurelayerfeatureservice

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create the service feature table
    val serviceFeatureTable = ServiceFeatureTable(resources.getString(R.string.sample_service_url))
    // create the feature layer using the service feature table
    val featureLayer = FeatureLayer(serviceFeatureTable)

    // create a map with the terrain with labels basemap
    ArcGISMap(Basemap.createTerrainWithLabels()).let { map ->
      // set an initial viewpoint
      map.initialViewpoint = Viewpoint(
        Point(
          -13176752.0,
          4090404.0,
          SpatialReferences.getWebMercator()
        ), 500000.0
      )

      // add the feature layer to the map
      map.operationalLayers.add(featureLayer)

      // set the map to be displayed in the mapview
      mapView.map = map
    }
  }

  override fun onPause() {
    mapView.pause()
    super.onPause()
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onDestroy() {
    mapView.dispose()
    super.onDestroy()
  }
}
