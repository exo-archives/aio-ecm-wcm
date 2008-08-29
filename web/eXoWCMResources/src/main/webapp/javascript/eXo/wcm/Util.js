
window.wcm = function() {}

wcm.insertCSSFromTextArea2FCK = function(Instance) {
	var eContentCSS = document.getElementById("ContentCSS");
	eContentCSS.style.border = "1px solid red";
	eContentCSS.onblur = function() {
		var sValue = eContentCSS.value;
		var iDoc = FCKeditorAPI.Instances[Instance].EditorWindow.document;
		var eHead = iDoc.getElementsByTagName("head")[0];
		var eStyle = iDoc.createElement("style");
		eStyle.setAttribute("type", "text/css");
		eStyle.innerHTML = sValue;
		eHead.appendChild(eStyle);
	}
}

//kill gadget :D
eXo.gadget = {
	UIGadget: {
		createGadget: function(){}
	}
}
