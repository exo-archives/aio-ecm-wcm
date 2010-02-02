eXo.require("eXo.projects.Module") ;
eXo.require("eXo.projects.Product") ;

function getProduct(version) {
  var product = new Product();
  
  product.name = "eXoWCM" ;
  product.portalwar = "portal.war" ;
  product.codeRepo = "ecm/wcm" ;
  product.useContentvalidation = true;
  product.version = "${project.version}" ;
  product.contentvalidationVersion = "${org.exoplatform.ecm.dms.version}";
  product.workflowVersion = "${org.exoplatform.ecm.workflow.version}" ;
  product.serverPluginVersion = "${org.exoplatform.portal.version}" ;
  product.workflowJbpmVersion = "${org.exoplatform.ecm.jbpm.version}";
  product.workflowBonitaVersion = "${org.bonita.version}";
  
  var kernel = Module.GetModule("kernel") ;
  var core = Module.GetModule("core") ;
  var ws = Module.GetModule("ws");
  var eXoJcr = Module.GetModule("jcr", {kernel : kernel, core : core, ws : ws}) ;
  var portal = Module.GetModule("portal", {kernel : kernel, ws:ws, core : core, eXoJcr : eXoJcr});         
  var dms = Module.GetModule("dms", {kernel : kernel, core : core, ws : ws, eXoJcr : eXoJcr , portal : portal});
  var wcm = Module.GetModule("wcm", {kernel : kernel, core : core, ws : ws, eXoJcr : eXoJcr, portal : portal, dms : dms});
	
	portal.starter = new Project("org.exoplatform.portal", "exo.portal.starter.war", "war", portal.version);
  portal.starter.deployName = "starter";
  product.addDependencies(portal.starter);
	
	product.addDependencies(portal.eXoGadgetServer) ;
	product.addDependencies(portal.eXoGadgets) ;
  product.addDependencies(portal.portlet.exoadmin) ;
  product.addDependencies(portal.portlet.web) ;
  product.addDependencies(portal.portlet.dashboard) ;  
  product.addDependencies(portal.web.eXoResources);
	product.addDependencies(portal.web.rest);
	product.addDependencies(portal.web.portal);
	product.addDependencies(portal.webui.portal);
	
	product.addDependencies(dms.extension.webapp);
	product.addDependencies(dms.gadgets);
	product.addDependencies(dms.portlet.dms);
	product.addDependencies(dms.web.eXoDMSResources) ;
  
	product.addDependencies(wcm.extension.war);
  product.addDependencies(wcm.portlet.webpresentation);
  product.addDependencies(wcm.portlet.websearches); 
  product.addDependencies(wcm.portlet.newsletter); 
  product.addDependencies(wcm.portlet.formgenerator);
  product.addDependencies(wcm.web.eXoWCMResources) ;

	product.addDependencies(wcm.demo.portal);
	product.addDependencies(wcm.demo.rest);
  
  product.addServerPatch("tomcat", wcm.server.tomcat.patch) ;
  product.addServerPatch("jboss",  portal.server.jboss.patch) ;
  product.addServerPatch("jbossear",  portal.server.jbossear.patch) ;  
  //product.addServerPatch("jonas",  portal.server.jonas.patch) ;
  //product.addServerPatch("ear",  portal.server.websphere.patch) ;

  product.module = wcm ;
  product.dependencyModule = [kernel, core, ws, eXoJcr, portal, dms];
    
  return product ;
}
