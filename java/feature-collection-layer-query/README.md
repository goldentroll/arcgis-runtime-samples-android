# Feature collection layer (query)
### Category: Search and Query
This sample demonstrates how to create a feature collection layer to show a query result from a service feature table. The feature collection is then displayed on a map with a feature collection layer.

![Feature Collection Layer (Query) App](feature-collection-layer-query.png)

## How it works

A query is performed using the `.queryFeaturesAsync(queryParameters)` method on `FeatureTable`. The result of the query is used to instantiate an `FeatureCollectionTable`. The table is used to instantiate an `FeatureCollection` which is then use to initialize a `FeatureCollectionLayer`. The layer is then displayed on the map by adding it to the operational layers array.
