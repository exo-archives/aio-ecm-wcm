function showMoreAction(expandItem, toolBarClazz, moreActionBlockId, hiddenActionClazz, evt) {
	var root = eXo.core.DOMUtil.findAncestorByClass(expandItem, toolBarClazz);
	var moreActionContainer = eXo.core.DOMUtil.findDescendantById(root, moreActionBlockId);
	if (moreActionContainer.style.display == "block") {
		moreActionContainer.style.display = "none";
	} else {
		moreActionContainer.style.display = "block";
	}
	evt = window.event || evt ;
	evt.cancelBubble = true ;
	eXo.core.DOMUtil.hideElementList.push(moreActionContainer);	
}