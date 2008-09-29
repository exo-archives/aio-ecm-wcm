/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.wcm.skin;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.BaseWebSchemaHandler;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * May 28, 2008  
 */
public class CSSFileHandler extends BaseWebSchemaHandler {  
  protected String getHandlerNodeType() { return "nt:file"; }
  protected String getParentNodeType() { return "exo:cssFolder" ; }
  private boolean isInPortalCSSFolder = false;

  public CSSFileHandler() { }

  public boolean matchHandler(Node node, SessionProvider sessionProvider) throws Exception {
    if(!matchNodeType(node))
      return false;
    if(!matchMimeType(node))
      return false;
    isInPortalCSSFolder = matchPortalCSSFolder(node,sessionProvider);
    if(!matchParentNodeType(node)) {      
      if(isInPortalCSSFolder)
        return true;
    }
    return true;        
  }

  private boolean matchNodeType(Node node) throws Exception{        
    return node.getPrimaryNodeType().getName().equals("nt:file");
  }

  private boolean matchMimeType(Node node) throws Exception{
    String mimeType = getFileMimeType(node);
    if("text/css".equals(mimeType))
      return true;
    if("text/plain".equals(mimeType))
      return true;
    return false;
  }

  private boolean matchParentNodeType(Node file) throws Exception{
    return file.getParent().isNodeType("exo:cssFolder");
  }

  private boolean matchPortalCSSFolder(Node file, SessionProvider sessionProvider) throws Exception{
    Node portal = findPortalNode(file,sessionProvider);    
    if(portal == null) return false;
    WebSchemaConfigService schemaConfigService = getService(WebSchemaConfigService.class);
    PortalFolderSchemaHandler schemaHandler = schemaConfigService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);            
    Node cssFolder = schemaHandler.getCSSFolder(portal);
    return file.getPath().startsWith(cssFolder.getPath());     
  }
  
  @SuppressWarnings("unused")
  public void onCreateNode(Node file, SessionProvider sessionProvider) throws Exception {  
    addMixin(file, "exo:cssFile");
    addMixin(file,"exo:owneable");
    file.setProperty("exo:presentationType","exo:cssFile");
    //If this cssFile belong to cssFolder of portal, the cssFile will be shared cssFile    
    if(isInPortalCSSFolder) {
      file.setProperty("exo:sharedCSS",true);
    }
  }  

  public void onModifyNode(Node file, SessionProvider sessionProvider) throws Exception {
    if(isInPortalCSSFolder) {
      Node portal = findPortalNode(file,sessionProvider);
      XSkinService skinService = getService(XSkinService.class);      
      skinService.updatePortalSkinOnModify(file,portal); 
    }          
  }

  public void onRemoveNode(Node file, SessionProvider sessionProvider) throws Exception {
    if(isInPortalCSSFolder){          
      XSkinService skinService = getService(XSkinService.class);
      Node portal = findPortalNode(file,sessionProvider);
      skinService.updatePortalSkinOnRemove(file,portal);
    }
  }    

}
