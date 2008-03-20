	var discoverEXO = function(object) {
		removeInfoDiv();
		if (!object) return;
		var infoDiv = document.createElement("div") ;
			infoDiv.setAttribute("id" , "show.EX0.0bject") ;
			infoDiv.style.background = "black" ;
			infoDiv.style.bottom = "0px" ;
			infoDiv.style.color = "white" ;
			infoDiv.style.padding = "3px" ;
			infoDiv.style.position = "absolute" ;
			infoDiv.style.zIndex = "9999" ;
		var closeButton = document.createElement("div") ;
			closeButton.setAttribute("id" , "Remove.Information.0bject") ;
			closeButton.style.paddingBottom = "10px" ;
			closeButton.style.textAlign = "right" ;
			closeButton.innerHTML = "<span style='color: red; font-weight: bold; cursor: pointer'>[ X ]</span>" ;
		var blockContent = 	document.createElement("div") ;
			blockContent.style.background = "#848484" ;
			blockContent.style.border = "1px solid green" ;
			blockContent.style.height = "200px" ;
			blockContent.style.overflow = "auto" ;
			blockContent.style.padding = "10px" ;
			blockContent.style.width = "400px" ;
		var csHTML = new String() ;
		var csObject = new String() ;
		if (typeof object == "object") {
			for (var exo in object) {
				if (typeof object[exo] == "string") csObject = object[exo].replace(/</g, "&lt;") ;
				else csObject = object[exo];
				csHTML +=  "<span style='color: #9b1a00'>" + exo + "</span> : " + csObject + "<br />" ;
			}
		} else if (typeof object == "string") {
			csHTML = object.replace(/</g, "&lt;") ;
		} else {
			csHTML = object.toString();
		}
		
		document.body.appendChild(infoDiv) ;
		infoDiv.appendChild(closeButton) ;
		infoDiv.appendChild(blockContent) ;
		blockContent.innerHTML = csHTML ;
		
		closeButton = document.getElementById("Remove.Information.0bject") ;
		closeButton.childNodes[0].onclick = removeInfoDiv ;
		
		function removeInfoDiv() {
		 if (document.getElementById("show.EX0.0bject")) {
			var infoDiv = document.getElementById("show.EX0.0bject") ;
			infoDiv.parentNode.removeChild(infoDiv) ;
			}
		}
	} ;
	
	var eUranium = new Object() ;
		eUranium.Name = 'Urani' ;
		
		eUranium.Execute = function() {
			var sumary = FCKeditorAPI.GetInstance("summary");
			var content = FCKeditorAPI.GetInstance("content");
			//discoverEXO(FCK.EditorWindow);
			if (document.selection) {
				var range = FCK.EditorWindow.document.selection.createRange();
				alert(range.text);
			} else  {
				var range = FCK.EditorWindow.getSelection();
				alert(range.getRangeAt(0))
			}
			
			//FCK.SetHTML("<sdfj klsdfl>".replace(/</g, "&lt;"));
		}
	
	eUranium.GetState = function() {}
	
	FCKCommands.RegisterCommand( 'Urani', eUranium ) ;
	
	var oElement = new FCKToolbarButton('Urani') ;
	oElement.IconPath = FCKConfig.eXoPlugins + 'urani/urani.gif' ;
	FCKToolbarItems.RegisterItem('Urani', oElement) ;	
