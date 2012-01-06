from com.raytheon.uf.common.message.response import ResponseMessageGeneric
from gov.noaa.nws.ncep.edex.uengine.tasks.profile import NcSoundingDrv

class NcSoundingDataRequest():

    def __init__(self):
        self.NcSoundingDrv = NcSoundingDrv()
        
    def setLat(self, lat):
        self.NcSoundingDrv.setLat(lat)
        self.lat = lat
        
    def setLon(self, lon):
        self.NcSoundingDrv.setLon(lon)
        self.lon = lon
        
    def setRefTime(self, refTime):
        self.NcSoundingDrv.setRefTime(refTime)
        self.refTime = refTime
        
    def setRefTimeStr(self, refTimeStr):
        self.NcSoundingDrv.setRefTimeStr(refTimeStr)
        
    def setValidTimeStart(self, validTimeStart):
        self.NcSoundingDrv.setValidTimeStart(validTimeStart)
        self.validTimeStart = validTimeStart
        
    def setValidTimeEnd(self, validTimeEnd):
        self.NcSoundingDrv.setValidTimeEnd(validTimeEnd)
        self.validTimeEnd = validTimeEnd

    def setValidTimeStartStr(self, validTimeStartStr):
        self.NcSoundingDrv.setValidTimeStartStr(validTimeStartStr)

    def setValidTimeEndStr(self, validTimeEndStr):
        self.NcSoundingDrv.setValidTimeEndStr(validTimeEndStr)

    def setSndType(self, sndType):
        self.NcSoundingDrv.setSndType(sndType)
        
    def setDataType(self, dataType):
        self.NcSoundingDrv.setDataType(dataType)
        
    def setQueryType(self, queryType):
        self.NcSoundingDrv.setQueryType(queryType)
        
    def setTableName(self, tableName):
        self.NcSoundingDrv.setTableName(tableName)
        
    def setMerge(self, mergeFlag):
        self.NcSoundingDrv.setMerge(mergeFlag)
        
    def setLevel(self, level):
        self.NcSoundingDrv.setLevel(level)
 
    def setDbIdList(self, dbIdList):
        #print dbIdList
        from jep import jarray, JINT_ID
         # create a one dim jarray of length len(dbIdList) of JINT_ID type
        jA = jarray(len(dbIdList), JINT_ID, 0)
         # fill the values
        for i in range(len(dbIdList)):
            jA[i] = int(dbIdList[i])
        self.NcSoundingDrv.setDbIdList(jA)
        
    def setLatLonList(self, LatLonArr):
        from jep import jarray, JFLOAT_ID
        jA = jarray(len(LatLonArr)*len(LatLonArr[0]), JFLOAT_ID, 0)
        for i in range(len(LatLonArr)):
            for j in range(len(LatLonArr[0])):
                jA[i*len(LatLonArr)+j] = float(LatLonArr[i][j])
        self.NcSoundingDrv.setLatLons(jA)
        
    def setTimeLine(self, timeLine):
        self.NcSoundingDrv.setTimeLine(timeLine)
        

    def makeResponse(self):
        return ResponseMessageGeneric(self.result)

    def makeNullResponse(self):
        return ResponseMessageGeneric("No data is available")

 
    def getSoundingRangeTimeLine(self):
        self.result = self.NcSoundingDrv.getSoundingRangeTimeLine()
        if self.result is None:
            return self.makeNullResponse()
        else:
            return self.makeResponse()
       
    def getSoundingTimeLine(self):
        self.result = self.NcSoundingDrv.getSoundingTimeLine()
        if self.result is None:
            return self.makeNullResponse()
        else:
            return self.makeResponse()
    def getMdlSoundingRangeTimeLine(self):
        self.result = self.NcSoundingDrv.getMdlSoundingRangeTimeLine()
        if self.result is None:
            return self.makeNullResponse()
        else:
            return self.makeResponse()
       
    def getMdlSoundingTimeLine(self):
        self.result = self.NcSoundingDrv.getMdlSoundingTimeLine()
        if self.result is None:
            return self.makeNullResponse()
        else:
            return self.makeResponse()
       
    def getSoundingStnInfoCol(self):
        self.result = self.NcSoundingDrv.getSoundingStnInfoCol()
        if self.result is None:
            return self.makeNullResponse()
        else:
            return self.makeResponse()
    
    
    def getSoundingDataByLatLonArray(self, LatLonArr):
        self.NcSoundingDrv.setQueryType("LATLON")
        from jep import jarray, JFLOAT_ID
        jA = jarray(len(LatLonArr), JFLOAT_ID)
        for i in range(len(LatLonArr)):
            jA[i] = float(LatLonArr[i])
        self.NcSoundingDrv.setLatLons(jA)

        self.result = self.NcSoundingDrv.getSoundingDataByLatLonArray()
        if self.result is None:
            return self.makeNullResponse()
        else:
            return self.makeResponse()

    def getSoundingDataByStnIdArray(self, StnIdArr):
        self.NcSoundingDrv.setQueryType("STNID")
        from jep import jarray
        from java.lang import String
        jA = jarray(len(StnIdArr), String)
        for i in range(len(StnIdArr)):
            jA[i] = String(StnIdArr[i])
        self.NcSoundingDrv.setStnIdArr(jA)

        self.result = self.NcSoundingDrv.getSoundingDataByStnArray()
        if self.result is None:
            return self.makeNullResponse()
        else:
            return self.makeResponse()

    def getSoundingDataByStnNumArray(self, StnNumArr):
        self.NcSoundingDrv.setQueryType("STNNUM")
        from jep import jarray
        from java.lang import String
        jA = jarray(len(StnNumArr), String)
        for i in range(len(StnNumArr)):
            jA[i] = String(StnNumArr[i])
        self.NcSoundingDrv.setStnNumArr(jA)

        self.result = self.NcSoundingDrv.getSoundingDataByStnArray()
        if self.result is None:
            return self.makeNullResponse()
        else:
            return self.makeResponse()
        
    def getSoundingModelNames (self, plugin):
        from java.util import ArrayList
        from com.raytheon.edex.uengine.tasks.query import TableQuery
        from com.raytheon.uf.edex.database.plugin import PluginFactory
        
        recordClass = PluginFactory.getInstance().getPluginRecordClassName(plugin)
        
        query = TableQuery("metadata",recordClass)
        query.setDistinctField("modelInfo.modelName")
        queryResults = query.execute()
        if queryResults is None or queryResults.size() == 0:
            return self.makeNullResponse()
        else:
            response = ArrayList()
            for i in range(queryResults.size()):
                currentQuery = queryResults.get(i)
                response.add(ResponseMessageGeneric(currentQuery))
            return response
    
    def setModelName (self, modelName):
        self.NcSoundingDrv.setModelName(modelName)
        self.modelName = modelName
        
    def setPluginName (self, plugin):
        self.NcSoundingDrv.setPluginName(plugin)
        self.plugin = plugin
        
    def getModelSoundingDataProto (self, aModel ):       
        import GempakDataPointRequest
        dpr = GempakDataPointRequest.GempakDataPointRequest(self.plugin)
        dpr.setTime(self.refTime)
        dpr.setGrid(self.modelName)
        dpr.setLocationLatlon (self.lon, self.lat)
        dpr.setParm(parm)
        return dpr.execute()
    
    def getModelSoundingData (self):
        self.result = self.NcSoundingDrv.getMdlSoundingData()
        print "result=", self.result
        if self.result is None:
            return self.makeNullResponse()
        else:
            return self.makeResponse()
        
