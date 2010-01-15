package org.exoplatform.wcm.connector.handler;

import java.text.SimpleDateFormat;

import javax.jcr.Node;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.ecm.connector.fckeditor.FCKUtils;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.wcm.core.NodetypeConstant;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FCKFileHandler {

  public static Element createFileElement(
  		Document document, String fileType, Node sourceNode, Node displayNode, String currentPortal) throws Exception {   
  	Element file = document.createElement("File");
    file.setAttribute("name", displayNode.getName());     
    SimpleDateFormat formatter = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);    
    file.setAttribute("dateCreated", formatter.format(sourceNode.getProperty("exo:dateCreated").getDate().getTime()));    
    file.setAttribute("dateModified", formatter.format(sourceNode.getProperty("exo:dateModified").getDate().getTime()));      
    file.setAttribute("creator", sourceNode.getProperty("exo:owner").getString());
    if (sourceNode.isNodeType("nt:file")) {
    	Node content = sourceNode.getNode("jcr:content");
    	file.setAttribute("nodeType", content.getProperty("jcr:mimeType").getString());
    } else {
    	file.setAttribute("nodeType", sourceNode.getPrimaryNodeType().getName());
    }
//    if (displayNode.hasProperty("exo:uuid")
//    		&& displayNode.getProperty("exo:uuid").getString().equals(sourceNode.getUUID())) {
    if (sourceNode.isNodeType(NodetypeConstant.EXO_WEBCONTENT)
    		|| sourceNode.isNodeType(NodetypeConstant.EXO_ARTICLE)){
    	file.setAttribute("url",getDocURL(displayNode, currentPortal));        
    } else {
    file.setAttribute("url",getFileURL(displayNode));        
    }
    if(sourceNode.isNodeType(FCKUtils.NT_FILE)) {
      long size = sourceNode.getNode("jcr:content").getProperty("jcr:data").getLength();
      file.setAttribute("size", "" + size / 1000);      
    }else {
      file.setAttribute("size", "");
    }
    return file;
  }
  
  /**
   * Gets the file url.
   * 
   * @param file the file
   * @return the file url
   * @throws Exception the exception
   */
  protected static String getFileURL(final Node file) throws Exception {   
    return FCKUtils.createWebdavURL(file);
  }  
  
  private static String getDocURL(final Node node, String currentPortal) throws Exception {
  	String baseURI = "/portal";
    String accessMode = "private";
    AccessControlList acl = ((ExtendedNode) node).getACL();
    for (AccessControlEntry entry : acl.getPermissionEntries()) {
      if (entry.getIdentity().equalsIgnoreCase(SystemIdentity.ANY)
          && entry.getPermission().equalsIgnoreCase(PermissionType.READ)) {
        accessMode = "public";
        break;
      }
    }
    String repository = ((ManageableRepository) node.getSession().getRepository())
    .getConfiguration().getName();
    String workspace = node.getSession().getWorkspace().getName();
    String nodePath = node.getPath();
    StringBuilder builder = new StringBuilder();
    if(node.isNodeType(NodetypeConstant.NT_FILE)) {
      if("public".equals(accessMode)) {
        return builder.append(baseURI).append("/jcr/").append(repository).append("/")
        .append(workspace).append(nodePath).toString();
      }     
      return builder.append(baseURI).append("/private/jcr/").append(repository).append("/")
      .append(workspace).append(nodePath).toString();
    }    
    WCMConfigurationService configurationService = (WCMConfigurationService) ExoContainerContext
    .getCurrentContainer().getComponentInstanceOfType(WCMConfigurationService.class);
    String parameterizedPageViewerURI = configurationService.getRuntimeContextParam(WCMConfigurationService.PARAMETERIZED_PAGE_URI);    
    return baseURI.replace("/rest", "") + "/" + accessMode + "/" + currentPortal + parameterizedPageViewerURI + "/"
    + repository + "/" + workspace + nodePath;
  }
}
