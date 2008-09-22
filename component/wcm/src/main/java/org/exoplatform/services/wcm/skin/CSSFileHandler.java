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
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * May 28, 2008  
 */
public class CSSFileHandler extends BaseWebSchemaHandler {  
  protected String getHandlerNodeType() { return "nt:file"; }
  protected String getParentNodeType() { return "exo:cssFolder" ;}  

  private String portalContainCSSFile = null;
  public CSSFileHandler() { }

  public boolean matchHandler(Node node) throws Exception {
    String handlerNodeType = getHandlerNodeType();    
    if (!node.getPrimaryNodeType().isNodeType(handlerNodeType)) 
      return false;    
    portalContainCSSFile = findPortalContainCSSFile(node);
    //this css file belong to a portal
    if(portalContainCSSFile != null) 
      return true;
    return node.getParent().isNodeType(getParentNodeType());
  }

  public void onCreateNode(Node file) throws Exception {  
    addMixin(file, "exo:cssFile");
    addMixin(file,"exo:owneable");
    file.setProperty("exo:presentationType","exo:cssFile");
    //If this cssFile belong to cssFolder of portal, the cssFile will be shared cssFile       
    if(portalContainCSSFile != null) {
      file.setProperty("exo:sharedCSS",true);
    }
  }

  private String findPortalContainCSSFile(Node file) throws Exception{    
    LivePortalManagerService livePortalManagerService = getService(LivePortalManagerService.class);
    WebSchemaConfigService schemaConfigService = getService(WebSchemaConfigService.class);
    PortalFolderSchemaHandler schemaHandler = schemaConfigService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);    
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    String portalName = null;
    for(String portalPath: livePortalManagerService.getLivePortalsPath()) {
      if(file.getPath().startsWith(portalPath)) {
        portalName = livePortalManagerService.getPortalNameByPath(portalPath);
        break;
      }      
    }
    if(portalName == null) return null;
    Node portal = livePortalManagerService.getLivePortal(portalName,sessionProvider);
    Node cssFolder = schemaHandler.getCSSFolder(portal);
    if (file.getPath().startsWith(cssFolder.getPath())) 
      return portalName;
    return null;
  }

  public void onModifyNode(Node file) throws Exception {
    if(file.hasProperty("exo:sharedCSS") && file.getProperty("exo:sharedCSS").getBoolean() && portalContainCSSFile != null) {      
      XSkinService skinService = getService(XSkinService.class);      
      skinService.updatePortalSkinOnModify(file,portalContainCSSFile);
    }
  }

  public void onRemoveNode(Node file) throws Exception {
    if(file.hasProperty("exo:sharedCSS") && file.getProperty("exo:sharedCSS").getBoolean() && portalContainCSSFile != null) {      
      XSkinService skinService = getService(XSkinService.class);
      skinService.updatePortalSkinOnRemove(file,portalContainCSSFile);
    }
  }    

}
