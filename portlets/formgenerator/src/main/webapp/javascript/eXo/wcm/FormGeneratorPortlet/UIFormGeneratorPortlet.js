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
	var formGenerator = "";
	formGenerator  +=					"<div class='BoxContentBoxStyle'>";
	formGenerator  +=						"<div class='TopContentBoxStyle'>";
	formGenerator  +=							"<div class='UIForm  UIFormEditBox'>";
	formGenerator  +=								"<div class='HorizontalLayout'>";
	formGenerator  +=									"<div class='FormContainer'>";
	formGenerator  +=										"<table class='UIFormGrid'>";
	formGenerator  +=											"<tr>";
	formGenerator  +=												"<td class='FieldLabel'>PropertyName</td>";
	formGenerator  +=												"<td class='FieldComponent'><input type='text' class='InputText' /></td>";
	formGenerator  += 											"<td class='FieldIcon'>";
	formGenerator  +=													"<div class='EditBox'>";
	formGenerator  +=														"<a class='ControlIcon DeleteIcon' onclick='eXo.ecm.UIFormGeneratorPortlet.removeComponent(this);' title='Click here to remove this component'><span></span></a>";
	formGenerator  +=														"<a class='ControlIcon EditIcon' onclick='eXo.ecm.UIFormGeneratorPortlet.showEditBox(this);' title='Click here to edit property'><span></span></a>";
	formGenerator  +=													"</div>";
	formGenerator  +=														"<a class='ControlIcon DownIcon' title='Move down component'><span></span></a>";
	formGenerator  +=														"<a class='ControlIcon UpIcon' title='Move up component'><span></span></a>";
	formGenerator  +=													"<div class='ClearRight'><span></span></div>";
	formGenerator  +=												"</td>";
	formGenerator  +=											"</tr>";
	formGenerator  +=										"</table>";
	formGenerator  +=									"</div>";
	formGenerator  +=								"</div>";
	formGenerator  +=							"</div>";
	formGenerator  +=						"</div>";
	formGenerator  +=						"<div class='MiddleContentBoxStyle' style='display:none'>";
	formGenerator  +=							"<div class='UIForm UIFormEditBox'>";
	formGenerator  +=								"<div class='HorizontalLayout'>";
	formGenerator  +=									"<div class='FormContainer'>";
	formGenerator  +=										"<table class='UIFormGrid'>";
	formGenerator  +=											"<tr>";
	formGenerator  +=												"<td class='FieldLabel'>Component Name</td>";
	formGenerator  +=												"<td class='FieldComponent'>";
	formGenerator  +=													"<input type='text' value='Component Name' class='InputText' />";
	formGenerator  +=												"</td>";
	formGenerator  +=											"</tr>";
	formGenerator  +=											"<tr>";
	formGenerator  +=												"<td class='FieldLabel'>File Size</td>";	
	formGenerator  +=												"<td class='FieldComponent'>";
	formGenerator  +=													"<input type='number' value='' class='InputText' style='width: 50%; float:left;' />";
	formGenerator  +=													"<div class='BoxRules'>";
	formGenerator  +=														"Rules: <input class='Requied' type='checkbox' checked='checked' value='0'>Requied</input>";
	formGenerator  +=													"</div>";
	formGenerator  +=												"</td>";
	formGenerator  +=												"<td class='FieldIcon'><span></span></td>";
	formGenerator  +=											"</tr>";
	formGenerator  +=											"<tr>";
	formGenerator  +=												"<td class='FieldLabel'>Default Value</td>";
	formGenerator  +=												"<td class='FieldComponent'><input type='text' class='InputText' name='FieldComponent' value='' /></td>";
	formGenerator  +=												"<td class='FieldIcon'>";
	formGenerator  +=													"<a class='AddIcon'><span></span></a>";
	formGenerator  +=													"<a class='RemoveIcon'><span></span></a>";
	formGenerator  +=													"<div class='ClearRight'><span></span></div>";
	formGenerator  +=												"</td>";
	formGenerator  +=											"</tr>";
	
/*================================================ advance options ==========================================
	formGenerator  +=									"<tr>";
	formGenerator  +=										"<td class='FieldLabel'>Advance Options</td>";
	formGenerator  +=										"<td class='FileComponent'>";
	formGenerator  +=											"this is component....";
	formGenerator  +=										"</td>";
	formGenerator  +=									"</tr>";
*/

	formGenerator  +=											"<tr>";
	formGenerator  +=                 			"<td class='FieldLabel'>Guidelines for User</td>";
	formGenerator  +=												"<td class='FieldComponent'><textarea class='Textarea'></textarea></td>";
	formGenerator  +=												"<td class='FieldIcon'><span></span></td>";
	formGenerator  +=											"</tr>";
	formGenerator  +=										"</table>";
	formGenerator  +=									"</div>";
	formGenerator  +=								"</div>";
	formGenerator +=							"</div>";
	formGenerator  +=						"</div>";	
	formGenerator  +=					"</div>";		


//============================================ Begin of render component ===============================================	
/*
	switch(typeComp){
		case "label"		: 
			formGenerator  +=									"<td class='FieldLabel'>Label Name</td>";
			formGenerator  +=									"<td class='FileComponent'><input type='text' name='labelName' value='Label Name' /></td>";
			break;
		
		case "input"		: 
			formGenerator  +=									"<td class='FieldLabel'>Input Name</td>";
			formGenerator  +=									"<td class='FileComponent'><input type='text' name='inputName' value='Input Name' /></td>";
			break;

		case "textarea"	:
			formGenerator  +=									"<td class='FieldLabel'>Textarea Name</td>";
			formGenerator  +=									"<td class='FileComponent'>Text Area</td>";
			break;			

		case "WYSWYG"		: 
			formGenerator  +=									"<td class='FieldLabel'>FCKEditor Name</td>";
			formGenerator  +=									"<td class='FileComponent'>Rich Text Editor</td>";
			break;			

		case "select"		: 
			formGenerator  +=									"<td class='FieldLabel'>Select Name</td>";
			formGenerator  +=									"<td class='FileComponent'>Select Name</td>";
			break;			
		
		case "checkbox"	: 
			formGenerator  +=									"<td class='FieldLabel'>Checkbox Name</td>";
			formGenerator  +=									"<td class='FileComponent'>Checkbox Name</td>";
			break;						

		case "radio"		: 
			formGenerator  +=									"<td class='FieldLabel'>Radio Name</td>";
			formGenerator  +=									"<td class='FileComponent'>Radio Name</td>";
			break;			

		case "datetime"	: 
			formGenerator  +=										"<td class='FieldLabel'>DateTime Name</td>";
			formGenerator  +=										"<td class='FileComponent'>DateTime Name</td>";
			break;
			
		case "upload"		: 
			formGenerator  +=										"<td class='FieldLabel'>Upload Name</td>";
			formGenerator  +=										"<td class='FileComponent'>Upload Name</td>";
			break;
	}
//============================================ End of render component ===============================================
*/

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

eXo.ecm.UIFormGeneratorPortlet = new UIFormGeneratorPortlet();