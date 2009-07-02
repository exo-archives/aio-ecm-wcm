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
			fieldComponent  +=		"<td class='FieldLabel'>Input field</td>";
			fieldComponent  +=		"<td class='FieldComponent'><input type='text' class='InputText' value='Input value'/></td>";
			break;
		case "textarea"	:
			fieldComponent  +=		"<td class='FieldLabel'>Textarea field</td>";
			fieldComponent  +=		"<td class='FieldComponent'><textarea class='Textarea'>Textarea value</textarea></td>";
			break;			
		case "WYSWYG"		: 
			fieldComponent  +=		"<td class='FieldLabel'>WYSIWYG field</td>";
			fieldComponent  +=		"<td class='FieldComponent'><textarea class='Textarea'>WYSIWYG value</textarea></td>";
			
			advancedOption  +=	"<tr>";
			advancedOption  +=		"<td class='FieldLabel'>Advance Options</td>";
			advancedOption  +=		"<td class='FileComponent'>";
			advancedOption  += 			"Toolbar: <select><option>Basic</option><option>Advanced</option></select>";
			advancedOption  +=		"</td>";
			advancedOption  +=	"</tr>";

			break;			
		case "select"		: 
			fieldComponent  +=		"<td class='FieldLabel'>Select field</td>";
			fieldComponent  +=		"<td class='FieldComponent'><select class='SelectBox'><option></option></select></td>";
			
			multivalue		= true;
			
			break;			
		case "checkbox"	: 
			fieldComponent  +=		"<td class='FieldLabel'>Checkbox field</td>";
			fieldComponent  +=		"<td class='FieldComponent'><input type='checkbox' class='CheckBox' value='checkbox1'/><input type='checkbox' class='CheckBox' value='checkbox2'/><input type='checkbox' class='CheckBox' value='checkbox3'/></td>";
			break;						
		case "radio"		: 
			fieldComponent  +=		"<td class='FieldLabel'>Radio field</td>";
			fieldComponent  +=		"<td class='FieldComponent'><input type='radio' class='Radio' value='radio1'/><input type='radio' class='Radio' value='radio2'/><input type='radio' class='Radio' value='radio3'/></td>";
			break;			
		case "datetime"	: 
			fieldComponent  +=		"<td class='FieldLabel'>Datetime field</td>";
			fieldComponent  +=		"<td class='FieldComponent'><input type='text' class='InputText' value='Datetime value'/></td>";
			
			advancedOption  +=	"<tr>";
			advancedOption  +=		"<td class='FieldLabel'>Advance Options</td>";
			advancedOption  +=		"<td class='FileComponent'>";
			advancedOption  += 			"Format: <select><option>dd/mm/yyyy</option><option>dd-mm-yyyy</option></select>";
			advancedOption  +=		"</td>";
			advancedOption  +=	"</tr>";

			break;
		case "upload"		: 
			fieldComponent  +=		"<td class='FieldLabel'>Upload field</td>";
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
	formGenerator  +=									"<a class='ControlIcon DownIcon' title='Move down component'><span></span></a>";
	formGenerator  +=									"<a class='ControlIcon UpIcon' title='Move up component'><span></span></a>";
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
	formGenerator  +=								"<td class='FieldComponent'><input type='text' class='InputText' name='FieldComponent' value='' onkeyup='eXo.ecm.UIFormGeneratorPortlet.updateValue(this);'/></td>";
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
	var DOMUtil = eXo.core.DOMUtil;
	var parentNode = DOMUtil.findAncestorByClass(obj, "BoxContentBoxStyle");
	if(parentNode) {
		document.getElementById('MiddleCenterViewBoxStyle').removeChild(parentNode);			
	}
};

UIFormGeneratorPortlet.prototype.updateLabel = function(obj) {
	var DOMUtil = eXo.core.DOMUtil;
	var parentNode = DOMUtil.findAncestorByClass(obj, 'BoxContentBoxStyle');
	var labelNode = DOMUtil.findFirstDescendantByClass(parentNode, 'td', 'FieldLabel');
	labelNode.innerHTML = obj.value;
};

UIFormGeneratorPortlet.prototype.updateWidth = function(obj) {
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
	var DOMUtil = eXo.core.DOMUtil;
	var parentNode = DOMUtil.findAncestorByClass(obj, 'BoxContentBoxStyle');
	var containerNode = DOMUtil.findFirstDescendantByClass(parentNode, 'div', 'TopContentBoxStyle');
	var componentNode = DOMUtil.findFirstDescendantByClass(containerNode, 'td', 'FieldComponent');
	var inputNode = componentNode.childNodes[0];
	if (inputNode) inputNode.style.width = width;
};

UIFormGeneratorPortlet.prototype.updateHeight = function(obj) {
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
	var DOMUtil = eXo.core.DOMUtil;
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
	if (!componentNode) return false;
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

UIFormGeneratorPortlet.prototype.updateValue = function(obj) {
	var DOMUtil = eXo.core.DOMUtil;
	var parentNode = DOMUtil.findAncestorByClass(obj, 'BoxContentBoxStyle');
	var containerNode = DOMUtil.findFirstDescendantByClass(parentNode, 'div', 'TopContentBoxStyle');
	var componentNode = DOMUtil.findFirstDescendantByClass(containerNode, 'td', 'FieldComponent');
	if (!componentNode) return false;
	var inputNode = componentNode.childNodes[0];
	if (inputNode.type.indexOf('select') >= 0) {
		var index = 0;
		var brotherNode = DOMUtil.findAncestorByClass(obj, 'FieldComponent');
		var brotherChildNodes = brotherNode.childNodes;
		for (var i in brotherChildNodes) {
			if (brotherChildNodes[i] == obj) index = i; 
			break;
		}
		inputNode[i].innerHTML = obj.value;
	}
	inputNode.value = obj.value;
};

UIFormGeneratorPortlet.prototype.updateGuide = function(obj) {
	
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
	optionInputNode.onkeyup = 'eXo.ecm.UIFormGeneratorPortlet.updateValue(this);';
	
	brotherNode.appendChild(optionInputNode);
};

UIFormGeneratorPortlet.prototype.removeOption = function(obj) {

};

eXo.ecm.UIFormGeneratorPortlet = new UIFormGeneratorPortlet();