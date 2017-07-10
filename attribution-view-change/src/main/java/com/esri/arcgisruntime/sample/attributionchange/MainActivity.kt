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
package com.esri.arcgisruntime.sample.attributionchange

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // create a map with the BasemapType topographic
        val map = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 47.495052, -121.786863, 12)
        // set the map to be displayed in this view
        mapView.map = map

        // create a FAB to respond to attribution bar
        fab.setOnClickListener { view ->
            Snackbar.make(view, resources.getString(R.string.message), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        // set attribution bar listener
        val params = fab.layoutParams as CoordinatorLayout.LayoutParams
        mapView.addAttributionViewLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            val heightDelta = bottom - oldBottom
            params.bottomMargin += heightDelta
            Toast.makeText(this, "new bounds [" + left + "," + top + "," + right + "," + bottom + "]" +
                    " old bounds [" + oldLeft + "," + oldTop + "," + oldRight + "," + oldBottom + "]", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.pause()
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }
}
