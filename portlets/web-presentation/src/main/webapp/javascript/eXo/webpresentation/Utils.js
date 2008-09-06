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
function showStepInfo() {
	var oStepInfoStyle = document.getElementById('PageNodeContainer').style;
	var oSelectInfoStyle = document.getElementById('SelectedPageInfo').style;
	
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