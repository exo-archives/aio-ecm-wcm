function renderChildrenContainer(navigations) {
	var html =	'<div class="MenuItemContainer" style="display: none;">' +
								'<div class="MenuItemDecorator">' +
									'<div class="LeftTopMenuDecorator">' +
										'<div class="RightTopMenuDecorator">' +
											'<div class="CenterTopMenuDecorator"><span></span></div>' +
										'</div>' +
									'</div>' +
									'<div class="LeftMiddleMenuDecorator">' +
										'<div class="RightMiddleMenuDecorator">' +
											'<div class="CenterMiddleMenuDecorator">';
	
	for (var i in navigations) {
		if (Array.prototype[i]) continue;
		html += renderChildNode(navigations[i]);
	}
	
	html +=							'</div>' +
										'</div>' +
									'</div>' +
									'<div class="LeftBottomMenuDecorator">' +
										'<div class="RightBottomMenuDecorator">' +
											'<div class="CenterBottomMenuDecorator"><span></span></div>' +
										'</div>' +
									'</div>' +
								'</div>' +
							'</div>';
	return html;
}

function renderChildNode(navigation) {
	var html = '';
	var tabStyleNavigation = 'NormalItem';
	//if (navigation.isSelected)
	//	tabStyleNavigation = 'SelectedItem';
	
	if (navigation.icon == null)
		navigation.icon = 'PageNodeIcon';
	
	var title = navigation.resolvedLabel;
	if (navigation.resolvedLabel.length > 40) {
		navigation.resolvedLabel = navigation.resolvedLabel.substring(0,37) + "...";
	}
	
	var arrowIcon = '';	
	if (navigation.children && navigation.children.length)
		arrowIcon = 'ArrowIcon';
	
	html += '<div class="MenuItem ' + tabStyleNavigation + '">' + 
						'<div class="' + arrowIcon + '" title="' + title + '">' +
							'<div class="ItemIcon ' + navigation.icon + '">' +
								'<a href="' + navigation.uri + '">' + navigation.resolvedLabel + '</a>' +
							'</div>' +
						'</div>';
	if (navigation.children && navigation.children.length) {
		html += renderChildrenContainer(navigation.children);
	}
	
	html += '</div>';
	return html;
}

function renderNextLevelNode(navigations, intLevel) {
	var html = '';
	for (var i in navigations) {
		if (Array.prototype[i]) continue;
		var navigation = navigations[i];
		
		if (navigation.icon == null)
			navigation.icon = 'DefaultPageIcon';
		
		var title = navigation.resolvedLabel;
		if (navigation.resolvedLabel.length > 35) {
			navigation.resolvedLabel = navigation.resolvedLabel.substring(0,32) + "...";
		}
		
		html += '<div class="Item">' +
							'<div class="Level' + intLevel + '">' +
		        		'<div class="OverflowContainer">';
		
		if(navigation.uri)
			html += 		'<a class="IconItem ' + navigation.icon + '" href="' + navigation.uri + '" title="' + navigation.title + '">' + navigation.resolvedLabel + '</a>';
		else
			html +=			'<a class="IconItem ' + navigation.icon + '" title="' + navigation.title + '">' + navigation.resolvedLabel + '</a>';
		
		html +=			'</div>' +
		      		'</div>' +
						'</div>';
		
		if (navigation.children && navigation.children.length)
			html += renderNextLevelNode(navigation.children, intLevel + 1);
	}
	return html;
}

function getNavigations(renderFunction) {
	var http;
	var navigations;
	var serviceUrl = 'http://' + document.location.host + '/portal/rest/wcmNavigation/getPortalNavigations?portalName=classic&language=en';
	if (window.ActiveXObject)
		http = new ActiveXObject('Microsoft.XMLHTTP');
	else
		http = new XMLHttpRequest();
	http.open('GET', serviceUrl, true);
	http.onreadystatechange = function() {
		if (http.readyState == 4) {
			navigations = http.responseText;
			navigations = navigations.substring(1, navigations.length - 1);
			navigations = navigations.replace('"navigations":', 'var navigations = ');
			eval(navigations);
			renderFunction(navigations);
		}
	};
	http.send(null);
}
