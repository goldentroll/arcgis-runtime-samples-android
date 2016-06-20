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

package com.esri.arcgisruntime.sample.picturemarkersymbols;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;


public class MainActivity extends AppCompatActivity {

    MapView mMapView;
    String mArcGISTempFolderPath;
    String mPinBlankOrangeFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setContentView(R.layout.activity_main);

        // inflate MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);

        // create a map with the imagery basemap
        ArcGISMap map = new ArcGISMap(Basemap.createTopographic());

        // set the map to be displayed in the mapview
        mMapView.setMap(map);

        // create an initial viewpoint using an envelope (of two points, bottom left and top right)
        Envelope envelope = new Envelope(new Point(-228835, 6550763, SpatialReferences.getWebMercator()), new Point(-223560, 6552021, SpatialReferences.getWebMercator()));
        //set viewpoint on mapview
        mMapView.setViewpointGeometryWithPaddingAsync(envelope, 100.0);

        // create a new graphics overlay and add it to the mapview
        final GraphicsOverlay graphicOverlay = new GraphicsOverlay();
        mMapView.getGraphicsOverlays().add(graphicOverlay);

        //Add graphics using different types of picture marker symbols

        //[DocRef: Name=Picture Marker Symbol URL, Category=Fundamentals, Topic=Symbols and Renderers]
        //Create a picture marker symbol from a URL resource
        //When using a URL, you need to call load to fetch the remote resource
        final PictureMarkerSymbol campsiteSymbol = new PictureMarkerSymbol("http://sampleserver6.arcgisonline.com/arcgis/rest/services/Recreation/FeatureServer/0/images/e82f744ebb069bb35b234b3fea46deae");
        //Optionally set the size (if not set, the size in pixels of the image will be used)
        campsiteSymbol.setHeight(18);
        campsiteSymbol.setWidth(18);
        campsiteSymbol.loadAsync();
        //[DocRef: END]
        campsiteSymbol.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                //Once the symbol has loaded, add a new graphic to the graphic overlay
                Point campsitePoint = new Point(-223560, 6552021, SpatialReferences.getWebMercator());
                Graphic campsiteGraphic = new Graphic(campsitePoint, campsiteSymbol);
                graphicOverlay.getGraphics().add(campsiteGraphic);
            }
        });

        //[DocRef: Name=Picture Marker Symbol Drawable-android, Category=Fundamentals, Topic=Symbols and Renderers]
        //Create a picture marker symbol from an app resource
        BitmapDrawable pinStarBlueDrawable = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.pin_star_blue);
        final PictureMarkerSymbol pinStarBlueSymbol = new PictureMarkerSymbol(pinStarBlueDrawable);
        pinStarBlueSymbol.loadAsync();
        //[DocRef: END]
        pinStarBlueSymbol.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                //add a new graphic with the same location as the initial viewpoint
                Point pinStarBluePoint = new Point(-226773, 6550477, SpatialReferences.getWebMercator());
                Graphic pinStarBlueGraphic = new Graphic(pinStarBluePoint, pinStarBlueSymbol);
                graphicOverlay.getGraphics().add(pinStarBlueGraphic);
            }
        });

        //Check sample has access to external storage
        if (saveResourceToExternalStorage()){

            //[DocRef: Name=Picture Marker Symbol File-android, Category=Fundamentals, Topic=Symbols and Renderers]
            //Create a picture marker symbol from a file on disk
            BitmapDrawable pinBlankOrangeDrawable = (BitmapDrawable) Drawable.createFromPath(mPinBlankOrangeFilePath);
            final PictureMarkerSymbol pinBlankOrangeSymbol = new PictureMarkerSymbol(pinBlankOrangeDrawable);
            pinBlankOrangeSymbol.loadAsync();
            //[DocRef: END]
            pinBlankOrangeSymbol.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    //add a new graphic with the same location as the initial viewpoint
                    Point pinBlankOrangePoint = new Point(-228835, 6550763, SpatialReferences.getWebMercator());
                    Graphic pinBlankOrangeGraphic = new Graphic(pinBlankOrangePoint, pinBlankOrangeSymbol);
                    graphicOverlay.getGraphics().add(pinBlankOrangeGraphic);
                }
            });
        }


    }

    /**
     * Helper method to save an image which is within this sample as a drawable resource to the sdcard so that it can be used as the basis of a PictureMarkerSymbol created from a file on disc
     */
    private boolean saveResourceToExternalStorage() {

        //handle no sdcard
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return false;
        } else {

            //build paths
            mArcGISTempFolderPath = Environment.getExternalStorageDirectory() + File.separator + this.getResources().getString(R.string.pin_blank_orange_folder_name);
            mPinBlankOrangeFilePath = mArcGISTempFolderPath + File.separator + this.getResources().getString(R.string.pin_blank_orange_file_name);

            //get drawable resource
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.pin_blank_orange);

            //create new ArcGIS temp folderf
            File folder = new File(mArcGISTempFolderPath);
            folder.mkdirs();

            //create file on disk
            File file = new File(mPinBlankOrangeFilePath);

            try{
                OutputStream outStream = new FileOutputStream(file);
                bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                outStream.flush();
                outStream.close();

                return true;

            } catch (Exception e){
                Log.e("picture-marker-symbol", "Failed to write image to external directory: message = " + e.getMessage());
                return false;
            }
        }
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        //Clean up file and folders we saved to disk
        try{
            File file = new File(mPinBlankOrangeFilePath);
            file.delete();
            File tempFolder = new File(mArcGISTempFolderPath);
            tempFolder.delete();
        } catch (Exception e){
            Log.e("picture-marker-symbol", "Failed to delete temp files and directory written to external storage: message = " + e.getMessage());
        }
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
