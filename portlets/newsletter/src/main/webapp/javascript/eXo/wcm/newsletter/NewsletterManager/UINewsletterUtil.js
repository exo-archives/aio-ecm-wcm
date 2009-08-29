function UINewsletterUtil() {
}

UINewsletterUtil.prototype.checkAllSelected = function(checkBox) {
	var elements = checkBox.form.elements;
	var checked = checkBox.checked;
	for (i = 0; i < elements.length; i++) {
		if (elements[i].type == "checkbox") 
			elements[i].checked = checked;
	}
};

eXo.ecm.UINewsletterUtil = new UINewsletterUtil();