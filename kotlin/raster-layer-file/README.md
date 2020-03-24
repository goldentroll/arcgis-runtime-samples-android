# Raster layer (file)

Create and use a raster layer made from a local raster file.

![Image of raster layer file](raster-layer-file.png)

## Use case

Rasters can be digital aerial photographs, imagery from satellites, digital pictures, or even scanned maps. An end-user will frequently need to import raster files acquired through various data-collection methods into their map to view and analyze the data.

## How to use the sample

When the sample starts, a raster will be loaded from a file and displayed in the map view.

## How it works

1. Create a `Raster` from a raster file.
1. Create a `RasterLayer` from the raster.
1. Add it as an operational layer with `map.operationalLayers.add(rasterLayer)`.

## Relevant API

* Raster
* RasterLayer

## Additional information

See the topic [What is raster data?](http://desktop.arcgis.com/en/arcmap/10.3/manage-data/raster-and-images/what-is-raster-data.htm) in the *ArcMap* documentation for more information about raster images.

## Offline data

1. Download the data from [ArcGIS Online](https://www.arcgis.com/home/item.html?id=34da965ca51d4c68aa9b3a38edb29e00).
2. Extract the contents of the downloaded zip files to disk.
3. Open your command prompt and navigate to the folder where you extracted the contents of the data from step 1 & 2.
4. Execute the following command:

  `adb push Shasta.tif /Android/data/com.esri.arcgisruntime.sample.rasterlayerfile/files/raster-file/Shasta.tif`

Link | Local Location
---------|-------|
|[raster-file.zip](https://arcgisruntime.maps.arcgis.com/home/item.html?id=7c4c679ab06a4df19dc497f577f111bd)| /Android/data/com.esri.arcgisruntime.sample.rasterlayerfile/files/raster-file/Shasta.tif |

## Tags

data, image, import, layer, raster, visualization
