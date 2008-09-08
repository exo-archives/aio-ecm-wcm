eXo.wcm = {
	
};

Utils = function(){
	Utils.prototype.removeQuickeditingBlock = function(portletID, quickEditingBlockId) {
		var presentation = document.getElementById(portletID);
		var parentNode = presentation.parentNode;
		var quickEditingBlock = document.getElementById(quickEditingBlockId);
		if(quickEditingBlock != null) {
			quickEditingBlock.parentNode.removeChild(quickEditingBlock);
		}
	};
		
	Utils.prototype.insertQuickeditingBlock = function(portletID, quickEditingBlockId) {
		var presentation = document.getElementById(portletID);
		var parentNode = presentation.parentNode;
		var quickEditingBlock = document.getElementById(quickEditingBlockId);
		if(quickEditingBlock == null) {
			parentNode.insertBefore(newEditingBlock, presentation);
		}
	};
}

eXo.wcm = new Utils();
	
var isOpen = false;
function showDivBlock(checkpoint, div1Class, div2Class) {
	var root = eXo.core.DOMUtil.findAncestorsByClass(checkpoint, 'UIWizard')[0];
	var oDiv1Style = eXo.core.DOMUtil.findDescendantsByClass(root, 'div', div1Class)[0].style;
	var oDiv2Style = eXo.core.DOMUtil.findDescendantsByClass(root, 'div', div2Class)[0].style;
	
	if (isOpen) {
		oStepInfoStyle.display = 'none';
		oSelectInfoStyle.marginLeft = '0';
		isOpen = false;
	} else {
		oStepInfoStyle.display = 'block';
		oSelectInfoStyle.marginLeft = '250px';
		isOpen = true;
	}
}