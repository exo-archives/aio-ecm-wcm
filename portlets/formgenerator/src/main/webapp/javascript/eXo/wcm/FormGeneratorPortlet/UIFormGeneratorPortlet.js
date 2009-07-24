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
			 eXo.ecm.UIFormGeneratorPortlet.renderComponent(this.getAttribute('elementType'));	
		}
	}
};

UIFormGeneratorPortlet.prototype.renderComponent = function(typeComp) {
	var formGenerator 	= "";
	var fieldComponent 	= "";
	var advancedOption 	= "";
	var multivalue 		= false;
	
//============================================ Begin of render component ===============================================	

	switch(typeComp){
		case "label"		: 
			fieldComponent  +=		"<td class='FieldLabel' colspan='2'>Label</td>";
			break;
		case "input"		: 
			fieldComponent  +=		"<td class='FieldLabel' value='Input Text'>Input field</td>";
			fieldComponent  +=		"<td class='FieldComponent'><input type='text' class='InputText' value='Input value'/></td>";
			break;
		case "textarea"	:
			fieldComponent  +=		"<td class='FieldLabel' value='Textarea'>Textarea field</td>";
			fieldComponent  +=		"<td class='FieldComponent'><textarea class='Textarea'>Textarea value</textarea></td>";
			break;			
		case "WYSIWYG"		: 
			fieldComponent  +=		"<td class='FieldLabel' value='WYSIWYG'>WYSIWYG field</td>";
			fieldComponent  +=		"<td class='FieldComponent'><textarea class='Textarea'>WYSIWYG value</textarea></td>";
			
			advancedOption  +=	"<tr>";
			advancedOption  +=		"<td class='FieldLabel'>Advance Options</td>";
			advancedOption  +=		"<td class='FileComponent'>";
			advancedOption  += 			"Toolbar: <select><option>Basic</option><option>Advanced</option></select>";
			advancedOption  +=		"</td>";
			advancedOption  +=	"</tr>";

			break;			
		case "select"		: 
			fieldComponent  +=		"<td class='FieldLabel' value='Select'>Select field</td>";
			fieldComponent  +=		"<td class='FieldComponent'><select class='SelectBox'><option></option></select></td>";
			
			multivalue		= true;
			
			break;			
		case "checkbox"	: 
			fieldComponent  +=		"<td class='FieldLabel' value='Checkbox'>Checkbox field</td>";
			fieldComponent  +=		"<td class='FieldComponent'><input type='checkbox' class='CheckBox' value='checkbox1'/><input type='checkbox' class='CheckBox' value='checkbox2'/><input type='checkbox' class='CheckBox' value='checkbox3'/></td>";
			break;						
		case "radio"		: 
			fieldComponent  +=		"<td class='FieldLabel' value='Radio'>Radio field</td>";
			fieldComponent  +=		"<td class='FieldComponent'><input type='radio' class='Radio' value='radio1'/><input type='radio' class='Radio' value='radio2'/><input type='radio' class='Radio' value='radio3'/></td>";
			break;			
		case "datetime"	: 
			fieldComponent  +=		"<td class='FieldLabel' value='DateTime'>Datetime field</td>";
			fieldComponent  +=		"<td class='FieldComponent'><input type='text' class='InputText' value='Datetime value'/></td>";
			
			advancedOption  +=	"<tr>";
			advancedOption  +=		"<td class='FieldLabel'>Advance Options</td>";
			advancedOption  +=		"<td class='FileComponent'>";
			advancedOption  += 			"Format: <select><option>dd/mm/yyyy</option><option>dd-mm-yyyy</option></select>";
			advancedOption  +=		"</td>";
			advancedOption  +=	"</tr>";

			break;
		case "upload"		: 
			fieldComponent  +=		"<td class='FieldLabel' value='Upload'>Upload field</td>";
			fieldComponent  +=		"<td class='FieldComponent'><input type='file' class='Upload'/><img src='/eXoResources/skin/sharedImages/Blank.gif' alt='' class='UploadButton'/></td>";
			break;
	}

	formGenerator  +=		"<div class='TopContentBoxStyle'>";
	formGenerator  +=			"<div class='UIForm UIFormEditBox'>";
	formGenerator  +=				"<div class='HorizontalLayout'>";
	formGenerator  +=					"<div class='FormContainer'>";
	formGenerator  +=						"<table class='UIFormGrid'>";
	formGenerator  +=							"<tr>";
	formGenerator  += 								fieldComponent;
	formGenerator  +=								"<td class='FieldIcon'>";
	formGenerator  +=									"<div class='EditBox'>";
	formGenerator  +=										"<a class='ControlIcon DeleteIcon' onclick='eXo.ecm.UIFormGeneratorPortlet.removeComponent(this);' title='Click here to remove this component'><span></span></a>";
	formGenerator  +=										"<a class='ControlIcon EditIcon' onclick='eXo.ecm.UIFormGeneratorPortlet.showEditBox(this);' title='Click here to edit property'><span></span></a>";
	formGenerator  +=									"</div>";
	formGenerator  +=									"<a class='ControlIcon DownIcon' onclick='eXo.ecm.UIFormGeneratorPortlet.moveDownElement(this);' title='Move down component'><span></span></a>";
	formGenerator  +=									"<a class='ControlIcon UpIcon' onclick='eXo.ecm.UIFormGeneratorPortlet.moveUpElement(this);' title='Move up component'><span></span></a>";
	formGenerator  +=									"<div class='ClearRight'><span></span></div>";
	formGenerator  +=								"</td>";
	formGenerator  +=							"</tr>";
	formGenerator  +=						"</table>";
	formGenerator  +=					"</div>";
	formGenerator  +=				"</div>";
	formGenerator  +=			"</div>";
	formGenerator  +=		"</div>";
	formGenerator  +=		"<div class='MiddleContentBoxStyle' style='display:none'>";
	formGenerator  +=			"<div class='UIForm UIFormEditBox'>";
	formGenerator  +=				"<div class='HorizontalLayout'>";
	formGenerator  +=					"<div class='FormContainer'>";
	formGenerator  +=						"<table class='UIFormGrid'>";
	formGenerator  +=							"<tr>";
	formGenerator  +=								"<td class='FieldLabel'>Component Name</td>";
	formGenerator  +=								"<td class='FieldComponent'>";
	formGenerator  +=									"<input type='text' class='InputText' onkeyup='eXo.ecm.UIFormGeneratorPortlet.updateLabel(this);'/>";
	formGenerator  +=								"</td>";
	formGenerator  +=							"</tr>";
	formGenerator  +=							"<tr>";
	formGenerator  +=								"<td class='FieldLabel'>Width</td>";	
	formGenerator  +=								"<td class='FieldComponent'>";
	formGenerator  +=									"<input type='number' value='' class='InputText' style='width: 50%; float:left;' onkeyup='eXo.ecm.UIFormGeneratorPortlet.updateWidth(this);' />";
	formGenerator  +=									"<div class='BoxRules'>";
	formGenerator  +=										"Rules: <input class='Requied' type='checkbox' onchange='eXo.ecm.UIFormGeneratorPortlet.updateRequired(this);'>Required</input>";
	formGenerator  +=									"</div>";
	formGenerator  +=								"</td>";
	formGenerator  +=								"<td class='FieldIcon'><span></span></td>";
	formGenerator  +=							"</tr>";
	formGenerator  +=							"<tr>";
	formGenerator  +=								"<td class='FieldLabel'>Height</td>";	
	formGenerator  +=								"<td class='FieldComponent'>";
	formGenerator  +=									"<input type='number' value='' class='InputText' style='width: 50%; float:left;' onkeyup='eXo.ecm.UIFormGeneratorPortlet.updateHeight(this);' />";
	formGenerator  +=								"</td>";
	formGenerator  +=								"<td class='FieldIcon'><span></span></td>";
	formGenerator  +=							"</tr>";
	formGenerator  +=							"<tr>";
	formGenerator  +=								"<td class='FieldLabel'>Default Value</td>";
	formGenerator  +=								"<td class='FieldComponent'><input type='text' class='InputText' onkeyup='eXo.ecm.UIFormGeneratorPortlet.updateValue(event);'/></td>";
	formGenerator  +=								"<td class='FieldIcon'>";
	if (multivalue) {
		formGenerator  +=								"<a class='AddIcon' onclick='eXo.ecm.UIFormGeneratorPortlet.addOption(this);'><span></span></a>";
		formGenerator  +=								"<a class='RemoveIcon' onclick='eXo.ecm.UIFormGeneratorPortlet.removeOption(this);'><span></span></a>";
		formGenerator  +=								"<div class='ClearRight'><span></span></div>";
	} else {
		formGenerator  += 								"<span></span>";
	}
	formGenerator  +=								"</td>";
	formGenerator  +=							"</tr>";
	formGenerator  +=							advancedOption
	formGenerator  +=							"<tr>";
	formGenerator  +=								"<td class='FieldLabel'>Guidelines for User</td>";
	formGenerator  +=								"<td class='FieldComponent'><textarea class='Textarea' onkeyup='eXo.ecm.UIFormGeneratorPortlet.updateGuide(this);'></textarea></td>";
	formGenerator  +=								"<td class='FieldIcon'><span></span></td>";
	formGenerator  +=							"</tr>";
	formGenerator  +=						"</table>";
	formGenerator  +=					"</div>";
	formGenerator  +=				"</div>";
	formGenerator  +=			"</div>";
	formGenerator  +=		"</div>";		

//============================================ End of render component ===============================================

	var node = document.createElement('div');
	node.innerHTML = formGenerator;
	node.className = 'BoxContentBoxStyle';
	node.setAttribute('typeComponent', typeComp);
	document.getElementById('MiddleCenterViewBoxStyle').appendChild(node);
};


UIFormGeneratorPortlet.prototype.showEditBox = function(obj) {
	var DOMUtil = eXo.core.DOMUtil;
	var parentNode = DOMUtil.findAncestorByClass(obj, "TopContentBoxStyle");
	var boxContent = DOMUtil.findNextElementByTagName(parentNode, "div");
	if(boxContent && boxContent.style.display !='block') {
		boxContent.style.display = 'block';
	} else {
		boxContent.style.display = 'none';
	}
};

UIFormGeneratorPortlet.prototype.removeComponent = function(obj) {
	var parentNode = eXo.core.DOMUtil.findAncestorByClass(obj, "BoxContentBoxStyle");
	if(parentNode) {
		var confirmDelete = confirm("Are you sure to remove?");
		if(confirmDelete == true) {
				document.getElementById('MiddleCenterViewBoxStyle').removeChild(parentNode);			
		} else {
			return;
		}
	}
};

UIFormGeneratorPortlet.prototype.moveDownElement = function(obj) {
	var DOMUtil = eXo.core.DOMUtil;
	var parentNode = DOMUtil.findAncestorByClass(obj, "BoxContentBoxStyle");
	var middContainer = document.getElementById('MiddleCenterViewBoxStyle');
	if(!middContainer || !parentNode) return;
	var tmpNode = '';
	nextElt = DOMUtil.findNextElementByTagName(parentNode, 'div');
	if(nextElt) {
		tmpNode = nextElt.cloneNode(true);
		middContainer.removeChild(nextElt);
		middContainer.insertBefore(tmpNode, parentNode);
	}
};

UIFormGeneratorPortlet.prototype.moveUpElement = function(obj) {
	var DOMUtil = eXo.core.DOMUtil;
	var parentNode = DOMUtil.findAncestorByClass(obj, "BoxContentBoxStyle");
	var middContainer = document.getElementById('MiddleCenterViewBoxStyle');
	if(!middContainer || !parentNode) return;
	previousElt = DOMUtil.findPreviousElementByTagName(parentNode, 'div');
	if(!previousElt) return;
	var tmpNode = parentNode.cloneNode(true);
	middContainer.removeChild(parentNode);
	middContainer.insertBefore(parentNode, previousElt);
};

UIFormGeneratorPortlet.prototype.updateLabel = function(obj) {
	var DOMUtil = eXo.core.DOMUtil;
	var parentNode = DOMUtil.findAncestorByClass(obj, 'BoxContentBoxStyle');
	var labelNode = DOMUtil.findFirstDescendantByClass(parentNode, 'td', 'FieldLabel');
	labelNode.innerHTML = obj.value;
};

UIFormGeneratorPortlet.prototype.updateWidth = function(obj) {
	var DOMUtil = eXo.core.DOMUtil;
	var width = '';
	if (obj.value == '') {
		width = null;
	} else if (isNaN(parseFloat(obj.value))) {
		alert('Number only');
		obj.value = '';
		return false;
	} else {
		width = obj.value + 'px';
	}

	var parentNode = DOMUtil.findAncestorByClass(obj, 'BoxContentBoxStyle');
	var containerNode = DOMUtil.findFirstDescendantByClass(parentNode, 'div', 'TopContentBoxStyle');
	var componentNode = DOMUtil.findFirstDescendantByClass(containerNode, 'td', 'FieldComponent');
	var inputNode = componentNode.childNodes[0];
	if (inputNode) inputNode.style.width = width;
};

UIFormGeneratorPortlet.prototype.updateHeight = function(obj) {
	var DOMUtil = eXo.core.DOMUtil;
	var height = '';
	if (obj.value == '') {
		height = null;
	} else if (isNaN(parseFloat(obj.value))) {
		alert('Number only');
		obj.value = '';
		return false;
	} else {
		height = obj.value + 'px';
	}

	var parentNode = DOMUtil.findAncestorByClass(obj, 'BoxContentBoxStyle');
	var containerNode = DOMUtil.findFirstDescendantByClass(parentNode, 'div', 'TopContentBoxStyle');
	var componentNode = DOMUtil.findFirstDescendantByClass(containerNode, 'td', 'FieldComponent');
	var inputNode = componentNode.childNodes[0];
	if (inputNode) inputNode.style.height = height;
};

UIFormGeneratorPortlet.prototype.updateRequired = function(obj) {
	var DOMUtil = eXo.core.DOMUtil;
	var parentNode = DOMUtil.findAncestorByClass(obj, 'BoxContentBoxStyle');
	var containerNode = DOMUtil.findFirstDescendantByClass(parentNode, 'div', 'TopContentBoxStyle');
	var componentNode = DOMUtil.findFirstDescendantByClass(containerNode, 'td', 'FieldComponent');
	var labelNode = DOMUtil.findFirstDescendantByClass(containerNode, 'td', 'FieldLabel');	
	if (!componentNode) return false;
	if(obj.checked)	{
		labelNode.setAttribute('mandatory', 'true');
	} else {
		labelNode.setAttribute('mandatory', 'false');
	}
	var requiredNode = DOMUtil.getChildrenByTagName(componentNode, 'span')[0];
	if (!requiredNode) {
		requiredNode = document.createElement('span');
		requiredNode.style.color = 'red';
		requiredNode.innerHTML = ' *';
		componentNode.appendChild(requiredNode);
	} else {
		componentNode.removeChild(requiredNode);
	}
};

UIFormGeneratorPortlet.prototype.updateValue = function(evt) {
	var DOMUtil = eXo.core.DOMUtil;
	var srcEle = eXo.core.Browser.getEventSource(evt);
	if(!srcEle) return;
	var root = DOMUtil.findAncestorByClass(srcEle, 'BoxContentBoxStyle');
	var componentNode = DOMUtil.findFirstDescendantByClass(root, 'div', 'TopContentBoxStyle');
	var eltName = DOMUtil.findFirstDescendantByClass(componentNode, 'td', 'FieldLabel').getAttribute('value');
	if(!eltName) return;
	switch(eltName) {
		case "Label" :
			break;
		case "Input Text" :
			var inputNode = DOMUtil.findFirstDescendantByClass(componentNode, 'input', 'InputText');
			inputNode.value = srcEle.value;
			break;
		case "Select" :
			var selectNode = DOMUtil.findFirstDescendantByClass(componentNode, 'select', 'SelectBox');
			var fieldNode = DOMUtil.findAncestorByClass(srcEle, 'FieldComponent');
			var inputNodes = DOMUtil.getChildrenByTagName(fieldNode, 'input');
			for(var i = 0 ; i < inputNodes.length; i++) {
				if(inputNodes[i] == srcEle){
					selectNode.options[i].value = srcEle.value;
					selectNode.options[i].innerHTML = srcEle.value;
				}
			}
			break;
		case "textarea" :	
			var textarea = DOMUtil.findFirstDescendantByClass(componentNode, 'textarea', 'Textarea');
			textarea.value = srcEle.value;
			break
		case "WYSIWYG" :
			var rte = DOMUtil.findFirstDescendantByClass(componentNode, 'textarea', 'Textarea');
			rte.value = srcEle.value;
			break;
		case "upload" : 
			break;
	}
};

UIFormGeneratorPortlet.prototype.updateGuide = function(objGuide) {
	var DOMUtil = eXo.core.DOMUtil;
	var root = DOMUtil.findAncestorByClass(objGuide, 'BoxContentBoxStyle');
	var componentNode = DOMUtil.findFirstDescendantByClass(root, 'div', 'TopContentBoxStyle');
	var fieldLabel = DOMUtil.findFirstDescendantByClass(componentNode, 'td', 'FieldLabel');
	fieldLabel.setAttribute("desc", objGuide.value);
};

UIFormGeneratorPortlet.prototype.addOption = function(obj) {
	var DOMUtil = eXo.core.DOMUtil;
	var parentNode = DOMUtil.findAncestorByClass(obj, 'BoxContentBoxStyle');
	var containerNode = DOMUtil.findFirstDescendantByClass(parentNode, 'div', 'TopContentBoxStyle');
	var componentNode = DOMUtil.findFirstDescendantByClass(containerNode, 'td', 'FieldComponent');
	var inputNode = componentNode.childNodes[0];
	var optionNode = document.createElement('option');
	inputNode.appendChild(optionNode);
	var rowNode = DOMUtil.findAncestorByTagName(obj, 'tr');
	var brotherNode = DOMUtil.findFirstDescendantByClass(rowNode, 'td', 'FieldComponent');
	var optionInputNode = document.createElement('input');
	optionInputNode.className = 'InputText';
	optionInputNode.type = 'text';
	optionInputNode.onkeyup = this.updateValue;	
	brotherNode.appendChild(optionInputNode);
};

UIFormGeneratorPortlet.prototype.removeOption = function(obj) {
	var DOMUtil = eXo.core.DOMUtil;
	var parentNode = DOMUtil.findAncestorByTagName(obj, 'tr');
	var componentNode = DOMUtil.findFirstDescendantByClass(parentNode, 'td', 'FieldComponent');
	var inputNodes = DOMUtil.getChildrenByTagName(componentNode, 'input');
	
	var root = DOMUtil.findAncestorByClass(obj, 'BoxContentBoxStyle');
	var topContainerNode = DOMUtil.findFirstDescendantByClass(root, 'div', 'TopContentBoxStyle');
	var topFieldComponent = DOMUtil.findFirstDescendantByClass(topContainerNode, 'td', 'FieldComponent');
	var selectNode = DOMUtil.findFirstDescendantByClass(topFieldComponent, 'select', 'SelectBox');
	var options =	 DOMUtil.getChildrenByTagName(selectNode, 'options');
	for(var i = 0 ; i < inputNodes.length; i++) {
		var index = inputNodes.length -1;
		if(i == index) {
			componentNode.removeChild(inputNodes[i]);			
			selectNode.remove(i);
		}
	}
};

UIFormGeneratorPortlet.prototype.getStringJsonObject = function() {
	var DOMUtil = eXo.core.DOMUtil;
	var root = document.getElementById('MiddleCenterViewBoxStyle');
	var boxsContent = DOMUtil.findDescendantsByClass(root, 'div', 'BoxContentBoxStyle');
	var strJsonObject = '{';	
	for(var i = 0; i < boxsContent.length; i++) {
		strJsonObject += eXo.ecm.UIFormGeneratorPortlet.getProperties(boxsContent[i]);
		if(i != (boxsContent.length-1)) {
			strJsonObject += ',';
		}
	}
	alert(strJsonObject);
};

UIFormGeneratorPortlet.prototype.getProperties = function(comp) {
	var DOMUtil = eXo.core.DOMUtil;
	var strObject = '{';
	strObject += '"nodeName":'	
	strObject += '"type":"'+comp.getAttribute("typeComponent")+'",';
	var topContent = DOMUtil.findFirstDescendantByClass(comp, 'div', 'TopContentBoxStyle');
	var fieldLabel = DOMUtil.findFirstDescendantByClass(topContent, 'td', 'FieldLabel');
	var defaultValue = fieldLabel.getAttribute('value'); 
	var nameComp = '';		
	if(fieldLabel && fieldLabel.textContent != '') {
		nameComp = fieldLabel.textContent;
	} else {
		nameComp = defaultValue;
	}
	
	strObject += 'name:"'+nameComp+'",';
	switch(comp.getAttribute("typeComponent")) {
		case "input" :
			inputNode = DOMUtil.findFirstDescendantByClass(topContent, 'input', "InputText");
			var width	= inputNode.offsetWidth;
			var mandatory = fieldLabel.getAttribute('mandatory');
			var height  = inputNode.offsetHeight;
			strObject +=  'value:"'+inputNode.value+'",width:"'+width+'",mandatory:"'+mandatory+'",height:"'+height+'",';
			break;
		case "label" :
			break;
		case "textarea" :
			var textareaNode = DOMUtil.findFirstDescendantByClass(topContent, 'textarea', "Textarea");
			var width	= textareaNode.offsetWidth;
			var mandatory = fieldLabel.getAttribute('mandatory');
			var height  = textareaNode.offsetHeight;
			strObject +=  'value:"'+textareaNode.value+'",width:"'+width+'",mandatory:"'+mandatory+'",height:"'+height+'",';	
			break;
		case "WYSIWYG" : 
				break;
		case "select" :
			var selectNode = DOMUtil.findFirstDescendantByClass(topContent, 'select', "SelectBox");
			var width	= selectNode.offsetWidth;
			var mandatory = fieldLabel.getAttribute('mandatory');
			var height  = selectNode.offsetHeight;
			strObject +=  'value:"'+selectNode.value+'",width:"'+width+'",mandatory:"'+mandatory+'",height:"'+height+'",';		
			var options = DOMUtil.getChildrenByTagName(selectNode, 'option');
			var advOptions = '';
			strObject += 	'advanced:"';
			for(var i = 0; i < options.length; i++) {
				strObject += options[i].value;
				if(i != (options.length-1)) {
					strObject += ",";				
				}
			}
			strObject += '",';
			break;
		case "upload" :
			break;
	}

	strObject += 'guildline:"'+fieldLabel.getAttribute('desc')+'"';
	strObject += "}";
	return strObject;
};

eXo.ecm.UIFormGeneratorPortlet = new UIFormGeneratorPortlet();