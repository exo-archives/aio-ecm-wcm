String docBase = "/" + ServletContext.getServletContextName()

/***************************Login Form skin**************************************************/
SkinService.addSkin(
    "web/login",
    "Default",
    docBase + "/login/skin/Stylesheet.css",
    ServletContext
) ;

SkinService.addSkin(
    "web/login",
    "Vista",
    docBase + "/login/skin/Stylesheet.css",
    ServletContext
) ;
 
SkinService.addSkin(
    "web/login",
    "Mac",
    docBase + "/login/skin/Stylesheet.css",
    ServletContext
) ;

/////////////////////////////////////////////////////////////////////////////////////////////////////