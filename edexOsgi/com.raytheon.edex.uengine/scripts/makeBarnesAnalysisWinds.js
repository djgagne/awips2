/* define a class to find winds and generate wind speed grid */
function GribWinds(plugin,speed) {
  this.plugin = plugin;
  this.speed = speed;
  this.params = new Object();
  this.count = 1;
  this.sortby = "basetime";
  this.query = new TermQuery(this.plugin);
  this.geom = null;
  this.crs = null;
  this.dataURI = null;
}
function _executeGWS() {
  var response;
  /* get the U wind grib */
  var query = new TermQuery(this.plugin);
  for (name in this.params) {
    query.addParameter(name,this.params[name]);
  }
  query.addParameter("paramid","U%wind","like");
  query.setCount(this.count);
  query.setSortBy(this.sortby);
  var uResult = query.execute();
  if (uResult.size() == 0) {
    return this.makeError("Query for Wind-U returned 0 results.",this.query);
  }
  /* get the V wind grib */
  var query = new TermQuery(this.plugin);
  for (name in this.params) {
    query.addParameter(name,this.params[name]);
  }
  query.addParameter("paramid","V%wind","like");
  query.setCount(this.count);
  query.setSortBy(this.sortby);
  var vResult = query.execute();
  if (vResult.size() == 0) {
    return this.makeError("Query for Wind-V returned 0 results.",this.query);
  }
    /* read the data from the data store */
  this.geom = uResult.get(0).getGrid().getGridGeom();
  this.crs = uResult.get(0).getGrid().getCrs();
  this.dataURI = uResult.get(0).getDataURI();
  var uFile = new FileIn(this.plugin,uResult.get(0));
  var vFile = new FileIn(this.plugin,vResult.get(0));
  var uData = uFile.execute();
  var vData = vFile.execute();
    /* combine the data into wind speed data */
  var windSpeed = new ConvertWindsData(uData,vData,this.speed);
  return windSpeed.execute();
  

}
/* GWS accessors */
function _setGWSSortValue(sortValue) {
  this.sortby = sortValue;
}
function _setGWSCount(count) {
  this.count = count;
}
function _addGWSParameter(name,value) {
  this.params[name] = value;
}
function _getGWSGeom() {
  return this.geom;
}
function _getGWSCRS () {
  return this.crs;
}
function _getGWSDataURI() {
  return this.dataURI;
}

/* mapping functions to the GWS object */
GribWinds.prototype.execute = _executeGWS;
GribWinds.prototype.addParameter = _addGWSParameter;
GribWinds.prototype.getGeom = _getGWSGeom;
GribWinds.prototype.getCRS = _getGWSCRS;
GribWinds.prototype.getDataURI = _getGWSDataURI;
GribWinds.prototype.setCount = _setGWSCount;
GribWinds.prototype.setSortby = _setGWSSortValue;
GribWinds.prototype.makeError = _makeError;

/* define a query to get the temperature data */
function ObsSpatialQuery(plugin,obParam) {
  this.icao = "icao";
  this.geometry = "geometry";
  this.plugin = plugin;
  this.obParam = obParam;
  this.oQuery = new TermQuery(this.plugin);
  this.sQuery = new SpatialQuery();
}
function _executeOSQ() {
  /* get the spatial results */
  var sResult = this.sQuery.execute();
  if(sResult == null || sResult.size() == 0) {
    return this.makeError("Spatial query returned no results.",this.spatial);
  }
  /* get and geolocate the wind speed observations */
  this.addList("stationid",MEUtils.changeArrayListToString(sResult.get(this.icao)));
  var oResult = this.oQuery.execute();
  if(oResult == null || oResult.size() == 0) {
    return this.makeError("Ob query returned no results.",oQuery);
  }
  var oMap = new MapAsciiData(this.obParam,
                              oResult,
                              sResult.get(this.icao),
                              sResult.get(this.geometry));
  
  return oMap.execute();
}
/* OSQ accessors */
function _setOSQSortValue(sortValue) {
  this.oQuery.setSortBy(sortValue);
}
function _setOSQCount(count) {
  this.oQuery.setCount(count);
}
function _addOSQParameter(name,value) {
  this.oQuery.addParameter(name,value);
}
function _addOSQList(name,value) {
  this.oQuery.addParameter(name,value,"in");
}
/* setter for the spatial bounds */
function _setOSQSpatialBounds(ulLat, ulLon, lrLat, lrLon, icao, geom) {
  this.icao = icao;
  this.geometry = geom;
  this.sQuery.setUpperLeftLat(ulLat);
  this.sQuery.setUpperLeftLon(ulLon);
  this.sQuery.setLowerRightLat(lrLat);
  this.sQuery.setLowerRightLon(lrLon);
  /* preload fields of the query */
  this.sQuery.addField(this.icao);
  this.sQuery.addField(this.geometry);
}

/* mapping functions to the  OSQ object */
ObsSpatialQuery.prototype.execute = _executeOSQ;
ObsSpatialQuery.prototype.addParameter = _addOSQParameter;
ObsSpatialQuery.prototype.setCount = _setOSQCount;
ObsSpatialQuery.prototype.setSortby = _setOSQSortValue;
ObsSpatialQuery.prototype.makeError = _makeError;
ObsSpatialQuery.prototype.setSpatialBounds = _setOSQSpatialBounds;
ObsSpatialQuery.prototype.addList = _addOSQList;

/* define a class to generate a image from a GRIB */
function GribImage(plugin) {
  /* names of things */
  this.plugin = plugin;
  /* grid attributes */
  this.grid = null;
  this.geom = null;
  this.crs = null;
  /* image attributes */
  this.colormap = "GribRGB";
  this.format = "png";
  this.scaleFactor = 3;
  this.reproject = false;
}
function _executeGI() {
  var gribMap = new GribMap(this.grib, this.colormap, this.grid, this.geom);
  gribMap.setScaleFactor(this.scaleFactor);
  var imageData = gribMap.execute();
  this.geom = gribMap.getGridGeometry();
  var colorMap = new ColorMapImage(this.colormap, imageData, this.geom);
  var imageOut = null;
  if(this.reproject){
    var reproject = new ReprojectImage(colorMap.execute(), this.geom, this.crs);
    var reprojectedImage = reproject.execute();
    imageOut = new ImageOut(reprojectedImage, this.format, reproject.getGridGeometry());
  }
  else
  {
    imageOut = new ImageOut(colorMap.execute(), this.format,this.geom);
  }
  return imageOut.execute();
}
/* setters */
function _setGIGrid(grid) {
  this.grid = grid;
}
function _setGIGeom(geom) {
  this.geom = geom;
}
function _setGICrs(crs) {
  this.crs = crs;
}
function _setGIColormap(colormap) {
  this.colormap = colormap;
}
function _setGIFormat(format) {
  this.format = format;
}
function _setGIScaleFactor(scale) {
  this.scaleFactor = scale;
}
function _setGIReproject(reproject) {
  this.reproject = reproject;
}
/* getters */
function _getGIGeom() {
  return this.geom;
}
function _getGIFormat() {
  return this.format;
}

/* map the functions to the class prototype */
GribImage.prototype.execute = _executeGI;
GribImage.prototype.setGrid = _setGIGrid;
GribImage.prototype.setGeom = _setGIGeom;
GribImage.prototype.getGeom = _getGIGeom;
GribImage.prototype.setCrs = _setGICrs;
GribImage.prototype.setColormap = _setGIColormap;
GribImage.prototype.setFormat = _setGIFormat;
GribImage.prototype.getFormat = _getGIFormat;
GribImage.prototype.setScaleFactor = _setGIScaleFactor;
GribImage.prototype.setReproject = _setGIReproject;

/* Define the class to perform the analysis */
function BarnesAnalysisWinds() {
  /* names of things */
  this.grib = "grib";
  this.obs = "obs";
  this.icao = "icao";
  this.geometry = "geometry";
  this.obParam = "windSpeed";
  /* settings for Barnes Analysis */
  this.radius = "50000.0";
  this.weight = "0.50";
  this.stations = "1";
  this.passes = "1";
  /* query objects */
  this.gQuery = new GribWinds(this.grib,true);
  this.oQuery = new ObsSpatialQuery(this.obs,this.obParam);
  /* the image creator */
  this.iMaker = new GribImage(this.grib);
  /* the logger */
  this.logger = new SystemLog();
}

function _execute() {
  var response;
  /* get the wind speed as a grib */
  var wResult = this.gQuery.execute();
  if (wResult.getClass().getSimpleName() != "FloatDataRecord") {
    this.logger.log("warn","GRIB Winds creation failed.");
    return wResult;
  }
  this.logger.log("info","GRIB Winds creation successful.");
  var geom = this.gQuery.getGeom();
  var crs = this.gQuery.getCRS();

  /* get and geolocate the wind speed observations */
  var oResult = this.oQuery.execute();
  
  if(oResult.getClass().getSimpleName() != "ArrayList") {
    this.logger.log("warn","Spatial ob query for " + this.obParam + " failed.");
    return oResult;
  }
  this.logger.log("info","Spatial ob query for " + this.obParam + " successful.");

  /* setup the analyzer and perform the analysis */
  var analyzer = new ObjectiveAnalysis(wResult,
                                       geom,
                                       crs,
                                       oResult);
  analyzer.addParameter("searchRadius",this.radius);
  analyzer.addParameter("weight",this.weight);
  analyzer.addParameter("minNoStns",this.stations);
  analyzer.addParameter("numPasses",this.passes);
  analyzer.addParameter("extrapolate","true");
  var analyzed = analyzer.execute();
  
  /* create the derived image */
  this.iMaker.setGrid(analyzed);
  this.iMaker.setCrs(crs);
  this.iMaker.setGeom(geom);
  var imageOut = this.iMaker.execute();
  geom = this.iMaker.getGeom();
  var format = this.iMaker.getFormat();
  
  /* write the image to the file and return the response */
  var fileOut = new FileOut(imageOut, format);
  var writeFile = fileOut.execute();
  var makeResponse = new MakeResponseUri(writeFile, 
                                         null, 
                                         this.gQuery.getDataURI(), 
                                         this.format);
  return makeResponse.execute();  
}

/* helper methods */
function _makeError(message,query) {
   var response = new MakeResponseNull(message,query);
   return response.execute();
}

/* setters for query objects */
function _addParameter(grid,name,value) {
  var query = (grid)?this.gQuery:this.oQuery;
  query.addParameter(name,value);
}
function _addList(grid,name,value) {
  var query = (grid)?this.gQuery:this.oQuery;
  query.addParameter(name,value,"in");
}
function _setSortValue(grid,sortValue) {
  var query = (grid)?this.gQuery:this.oQuery;
  query.setSortby(sortValue);
}
function _setCount(grid,count) {
  var query = (grid)?this.gQuery:this.oQuery;
  query.setCount(count);
}
/* setters for image creation parameters */
function _setScaleFactor(scale) {
  this.iMaker.setScaleFactor(scale);
}

function _reprojectImage(reproject) {
  this.iMaker.setReproject(reproject);
}

function _setColorMap(colormap){
  this.iMaker.setColormap(colormap);
}

function _setFormat(format){
  this.iMaker.setFormat(format);
}

/* setters for the spatial query */
function _setSpatialBounds(ulLat, ulLon, lrLat, lrLon) {
  this.oQuery.setSpatialBounds(ulLat, 
                               ulLon,
                               lrLat,
                               lrLon,
                               this.icao,
                               this.geometry);
}

/* setters for Barnes Analysis */
function _setBarnesParameters(radius,weight,stations,passes) {
  this.radius = radius;
  this.weight = weight;
  this.stations = stations;
  this.passes = passes;
}

/* mapping functions to the object */
BarnesAnalysisWinds.prototype.execute = _execute;
BarnesAnalysisWinds.prototype.addParameter = _addParameter;
BarnesAnalysisWinds.prototype.addList = _addList;
BarnesAnalysisWinds.prototype.setSortValue = _setSortValue;
BarnesAnalysisWinds.prototype.setCount = _setCount;
BarnesAnalysisWinds.prototype.setScaleFactor = _setScaleFactor;
BarnesAnalysisWinds.prototype.reprojectImage = _reprojectImage;
BarnesAnalysisWinds.prototype.setColorMap = _setColorMap;
BarnesAnalysisWinds.prototype.setFormat = _setFormat;
BarnesAnalysisWinds.prototype.setSpatialBounds = _setSpatialBounds;
BarnesAnalysisWinds.prototype.setBarnesParameters = _setBarnesParameters;
BarnesAnalysisWinds.prototype.makeError = _makeError;

var runner = new BarnesAnalysisWinds();
/* setup the basic grib queries */
runner.addParameter(true,"levelinfo","10.0_m");
runner.addParameter(true,"forecasttime","0");
runner.addParameter(true,"gridid",212);
runner.setSortValue(true,"basetime");
runner.setCount(true,1);
/* setup the basic obs query */
runner.addParameter(false,"refhour","20070601190000");
runner.setSortValue(false,"timeobs");
runner.setCount(false,0);
/* setup the spatial search parameters */
runner.setSpatialBounds(43.00, -98.00, 37.00, -92.00);
/* setup the Barnes analysis paramters */
runner.setBarnesParameters("50000.0","0.50","1","1");
/* set image properties */
runner.setColorMap("GribRGB");
runner.setFormat("png");
runner.setScaleFactor(3.0);
/* execute the script */
runner.execute();

