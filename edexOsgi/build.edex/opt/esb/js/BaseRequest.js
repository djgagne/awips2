/**
 * Performs a TermQuery for plugin data.
 * 
 *   
 *   
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 20080516                njensen     Initial Creation
 */
function BaseRequest(aPlugin){
  this.plugin = aPlugin;
  this.subscribe = false;
  this.subscription = null;
  this.queryResults = null;  
  this.query = new TermQuery(this.plugin, subscriptionDataFieldId, subscriptionDataQueryId);
}

function _addParameter(name,value,operand){
	if(arguments.length==2){
		this.query.addParameter(name,value);
	} else{
		this.query.addParameter(name,value,operand);
	}
}

function _addList(name, value){
  this.query.addList(name, value);
}

function _setCount(count){
  this.query.setCount(count);
}

function _setSortValue(sortValue){
  this.query.setSortBy(sortValue);
}

function _enableSubscription(){
  this.subscribe = true;
}

function _execute()
{
  if(this.subscribe){
    this.subscription = new Subscription();
    this.subscription.setup(this.query);
  }
  
  this.queryResults = this.query.execute();
  if(this.queryResults == null || this.queryResults.size() == 0)
  {
  	var response = new MakeResponseNull("Query returned 0 results.",
  	                                        this.query);
    return response.execute();
  }
  else
  {
       return this.makeXmlResponse();
  }
}

function _makeXmlResponse()
{
  var xmlResults = new Array();
  var response = new Array();
  for(i=0; i < this.queryResults.size(); i++)
  {    
    var makeResponse = new MakeResponseXml(this.queryResults.get(i));
    response[i] = makeResponse.execute();
  }
  return response;
}

BaseRequest.prototype.execute = _execute;
BaseRequest.prototype.makeXmlResponse = _makeXmlResponse;
BaseRequest.prototype.addParameter = _addParameter;
BaseRequest.prototype.addList = _addList;
BaseRequest.prototype.setCount = _setCount;
BaseRequest.prototype.setSortValue = _setSortValue;
BaseRequest.prototype.enableSubscription = _enableSubscription;
