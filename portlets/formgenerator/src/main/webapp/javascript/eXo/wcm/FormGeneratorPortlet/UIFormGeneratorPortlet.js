function UIFormGeneratorPortlet() {
}

UIFormGeneratorPortlet.prototype.init = function() {
	//alert('This is test Form generator portlet....');
	var DOMUtil = eXo.core.DOMUtil;	
	var uiTabContentContainer = document.getElementById('UITabContentContainer');
	var uiTabContent = DOMUtil.findFirstDescendantByClass(uiTabContentContainer, 'div', 'UITabContent');
	var menuitems = DOMUtil.findDescendantsByClass(uiTabContent, 'div', 'LeftMenu');
	for(var i = 0; i < menuitems.length; i++) {
		menuitems[i].onclick = function() {
			 eXo.ecm.UIFormGeneratorPortlet.insertComponent(this.getAttribute('elementType'));	
		}
	}
};

UIFormGeneratorPortlet.prototype.insertComponent = function(type) {
	switch(type){
		case "input"	: eXo.ecm.UIFormGeneratorPortlet.addComponent(this.input); break;
		case "label"	: eXo.ecm.UIFormGeneratorPortlet.addComponent(this.label); break;
		case "textarea"	: eXo.ecm.UIFormGeneratorPortlet.addComponent(this.textarea); break;
		case "WYSWYG"	: eXo.ecm.UIFormGeneratorPortlet.addComponent(this.WYSWYG); break;	
		case "select"	: eXo.ecm.UIFormGeneratorPortlet.addComponent(this.select); break;
		case "checkbox"	: eXo.ecm.UIFormGeneratorPortlet.addComponent(this.checkbox); break;
		case "radio"	: eXo.ecm.UIFormGeneratorPortlet.addComponent(this.radio); break;
		case "datetime"	: eXo.ecm.UIFormGeneratorPortlet.addComponent(this.datetime); break;
		case "upload"	: eXo.ecm.UIFormGeneratorPortlet.addComponent(this.upload); break;		
	}
};

UIFormGeneratorPortlet.prototype.addComponent = function(html, id) {
//	var node = document.createElement("div");
//	node.className = "UIComponentItem";
//	node.style.width = "100%";
//	node.style.border = "solid 1px #000";
//	node.style.padding = "5px";
//	node.style.marginTop = "10px";
//	node.innerHTML = html ;
//	document.getElementById("DropContainer").appendChild(node);
};

eXo.ecm.UIFormGeneratorPortlet = new UIFormGeneratorPortlet();