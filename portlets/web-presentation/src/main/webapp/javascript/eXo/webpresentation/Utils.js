var isOpen = false;
function showDivBlock(checkpoint, div1Class, div2Class) {
	var root = eXo.core.DOMUtil.findAncestorsByClass(checkpoint, 'UIWizard')[0];
	var oDiv1Style = eXo.core.DOMUtil.findDescendantsByClass(root, 'div', div1Class)[0].style;
	var oDiv2Style = eXo.core.DOMUtil.findDescendantsByClass(root, 'div', div2Class)[0].style;
	
	if (isOpen) {
		oDiv1Style.display = 'none';
		oDiv2Style.marginLeft = '0';
		isOpen = false;
	} else {
		oDiv1Style.display = 'block';
		oDiv2Style.marginLeft = '250px';
		isOpen = true;
	}
}