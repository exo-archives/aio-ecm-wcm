function AjaxRequest(){
}

AjaxRequest.prototype.requestXMLHttp = function(url) {
	var XMLHttp = null;
  if (window.XMLHttpRequest) {
    try {
      XMLHttp = new XMLHttpRequest();
    } catch (e) { }
  } else if (window.ActiveXObject) {
    try {
      XMLHttp = new ActiveXObject("Msxml2.XMLHTTP");
    } catch (e) {
      try {
        XMLHttp = new ActiveXObject(
          "Microsoft.XMLHTTP");
      } catch (e) { }
    }
  }
  XMLHttp.onreadystatechange = eXo.wcm.AjaxRequest.responseXMLHttp;
  XMLHttp.open('GET', url, true);
  XMLHttp.send(null);
};

AjaxRequest.prototype.responseXMLHttp = function() {
	var xmlHttpRequest = eXo.wcm.AjaxRequest.requestXMLHttp();
	if(xmlHttpRequest.readyState == 4) {
		if(xmlHttpRequest.status == 200) {
			return xmlHttpRequest.responseXML;
		}
	}
};

if(!window.wcm) eXo.wcm = new Object();
eXo.wcm.AjaxRequest = new AjaxRequest();