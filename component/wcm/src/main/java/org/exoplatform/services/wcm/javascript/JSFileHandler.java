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
package org.exoplatform.services.wcm.javascript;

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
public class JSFileHandler extends BaseWebSchemaHandler {

  protected String getHandlerNodeType() { return "nt:file"; }
  protected String getParentNodeType() { return "exo:jsFolder" ;}
  private boolean isPortalJSFile = false;

  public boolean matchHandler(Node node) throws Exception {
    String handlerNodeType = getHandlerNodeType();    
    if (!node.getPrimaryNodeType().isNodeType(handlerNodeType)) 
      return false;   
    isPortalJSFile = isPortalJSFile(node);
    if(isPortalJSFile)        
      return true;     
    return node.getParent().isNodeType(getParentNodeType());
  }

  public void onCreateNode(Node file) throws Exception {
    addMixin(file, "exo:jsFile") ;
    addMixin(file,"exo:owneable");
    file.setProperty("exo:presentationType","exo:jsFile");
    //If this jsFile belong to jsFolder of portal, the jsFile will be shared jsFile
    if(isPortalJSFile) {
      file.setProperty("exo:sharedJS",true); 
    }    
  }

  private boolean isPortalJSFile(Node file) throws Exception{    
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
    if(portalName == null) return false;
    Node portal = livePortalManagerService.getLivePortal(portalName,sessionProvider);
    Node jsFolder = schemaHandler.getJSFolder(portal);
    return file.getPath().startsWith(jsFolder.getPath());
  }
  public void onModifyNode(Node file) throws Exception {
    if(file.hasProperty("exo:sharedJS") && file.getProperty("exo:sharedJS").getBoolean()) { 
      XJavascriptService javascriptService = getService(XJavascriptService.class);
      javascriptService.updatePortalJSOnModify(file);
    }
  }

  public void onRemoveNode(Node file) throws Exception { 
    if(file.hasProperty("exo:sharedJS") && file.getProperty("exo:sharedJS").getBoolean()) { 
      XJavascriptService javascriptService = getService(XJavascriptService.class);
      javascriptService.updatePortalJSOnRemove(file);
    }
  }

}
