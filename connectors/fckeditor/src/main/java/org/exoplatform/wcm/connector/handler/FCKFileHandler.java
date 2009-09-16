package org.exoplatform.wcm.connector.handler;

import java.text.SimpleDateFormat;

import javax.jcr.Node;

import org.exoplatform.ecm.connector.fckeditor.FCKUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FCKFileHandler {

  public static Element createFileElement(Document document, Node child, String fileType) throws Exception {   
  	Element file = document.createElement("File");
    file.setAttribute("name", child.getName());     
    SimpleDateFormat formatter = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);    
    file.setAttribute("dateCreated", formatter.format(child.getProperty("exo:dateCreated").getDate().getTime()));    
    file.setAttribute("dateModified", formatter.format(child.getProperty("exo:dateModified").getDate().getTime()));      
    file.setAttribute("creator", child.getProperty("exo:owner").getString());
    if (child.getProperty("exo:presentationType") == null ) {
    	file.setAttribute("nodeType", child.getPrimaryNodeType().getName());
    } else {
    	file.setAttribute("nodeType", child.getProperty("exo:presentationType").getString());
    }
    file.setAttribute("url",getFileURL(child));        
    if(child.isNodeType(FCKUtils.NT_FILE)) {
      long size = child.getNode("jcr:content").getProperty("jcr:data").getLength();
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
