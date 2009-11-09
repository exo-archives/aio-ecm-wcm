package org.exoplatform.wcm.connector.handler;

import java.text.SimpleDateFormat;

import javax.jcr.Node;

import org.exoplatform.ecm.connector.fckeditor.FCKUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FCKFileHandler {

  public static Element createFileElement(Document document, String fileType, Node sourceNode, Node displayNode) throws Exception {   
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
    file.setAttribute("url",getFileURL(displayNode));        
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
}
