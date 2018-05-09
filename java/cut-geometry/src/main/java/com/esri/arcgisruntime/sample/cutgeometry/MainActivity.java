package com.esri.arcgisruntime.sample.cutgeometry;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.esri.arcgisruntime.geometry.*;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);
    // create a map with the BasemapType topographic
    ArcGISMap map = new ArcGISMap(Basemap.createTopographic());
    // set the map to be displayed in this view
    mMapView.setMap(map);

    // create a graphic overlay
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(graphicsOverlay);

    // create a blue polygon graphic to cut
    Graphic polygonGraphic = new Graphic(createLakeSuperiorPolygon(), new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID,
        0x220000FF, new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF0000FF, 2)));
    graphicsOverlay.getGraphics().add(polygonGraphic);

    // create a red polyline graphic to cut the polygon
    Graphic polylineGraphic = new Graphic(createBorder(), new SimpleLineSymbol(SimpleLineSymbol.Style.DOT,
        0xFFFF0000, 3));
    graphicsOverlay.getGraphics().add(polylineGraphic);

    // zoom to show the polygon graphic
    mMapView.setViewpointGeometryAsync(polygonGraphic.getGeometry());

    //create a button to perform the cut operation

  }
  /**
   * Creates a polyline along the US/Canada border over Lake Superior.
   *
   * @return poyline
   */
  private Polyline createBorder() {
    PointCollection points = new PointCollection(SpatialReferences.getWebMercator());
    points.add(new Point(-9981328.687124, 6111053.281447));
    points.add(new Point(-9946518.044066, 6102350.620682));
    points.add(new Point(-9872545.427566, 6152390.920079));
    points.add(new Point(-9838822.617103, 6157830.083057));
    points.add(new Point(-9446115.050097, 5927209.572793));
    points.add(new Point(-9430885.393759, 5876081.440801));
    points.add(new Point(-9415655.737420, 5860851.784463));
    return new Polyline(points);
  }

  /**
   * Creates a polygon of points around Lake Superior.
   *
   * @return polygon
   */
  private Polygon createLakeSuperiorPolygon() {
    PointCollection points = new PointCollection(SpatialReferences.getWebMercator());
    points.add(new Point(-10254374.668616, 5908345.076380));
    points.add(new Point(-10178382.525314, 5971402.386779));
    points.add(new Point(-10118558.923141, 6034459.697178));
    points.add(new Point(-9993252.729399, 6093474.872295));
    points.add(new Point(-9882498.222673, 6209888.368416));
    points.add(new Point(-9821057.766387, 6274562.532928));
    points.add(new Point(-9690092.583250, 6241417.023616));
    points.add(new Point(-9605207.742329, 6206654.660191));
    points.add(new Point(-9564786.389509, 6108834.986367));
    points.add(new Point(-9449989.747500, 6095091.726408));
    points.add(new Point(-9462116.153346, 6044160.821855));
    points.add(new Point(-9417652.665244, 5985145.646738));
    points.add(new Point(-9438671.768711, 5946341.148031));
    points.add(new Point(-9398250.415891, 5922088.336339));
    points.add(new Point(-9419269.519357, 5855797.317714));
    points.add(new Point(-9467775.142741, 5858222.598884));
    points.add(new Point(-9462924.580403, 5902686.086985));
    points.add(new Point(-9598740.325877, 5884092.264688));
    points.add(new Point(-9643203.813979, 5845287.765981));
    points.add(new Point(-9739406.633691, 5879241.702350));
    points.add(new Point(-9783061.694736, 5922896.763395));
    points.add(new Point(-9844502.151022, 5936640.023354));
    points.add(new Point(-9773360.570059, 6019099.583107));
    points.add(new Point(-9883306.649729, 5968977.105610));
    points.add(new Point(-9957681.938918, 5912387.211662));
    points.add(new Point(-10055501.612742, 5871965.858842));
    points.add(new Point(-10116942.069028, 5884092.264688));
    points.add(new Point(-10111283.079633, 5933406.315128));
    points.add(new Point(-10214761.742852, 5888134.399970));
    points.add(new Point(-10254374.668616, 5901877.659929));
    return new Polygon(points);
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
