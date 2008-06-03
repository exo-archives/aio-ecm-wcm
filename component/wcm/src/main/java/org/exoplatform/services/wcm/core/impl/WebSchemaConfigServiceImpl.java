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
package org.exoplatform.services.wcm.core.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.core.WebSchemaHandler;
import org.exoplatform.services.wcm.core.WebSchemaHandlerNotFoundException;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 3, 2008  
 */
public class WebSchemaConfigServiceImpl implements WebSchemaConfigService, Startable {

  private HashMap<String, WebSchemaHandler> webSchemaHandlers = new HashMap<String, WebSchemaHandler>();
  private List<SharedResourceFolderPlugin> sharedFolderPlugins = new ArrayList<SharedResourceFolderPlugin>(0) ;    
  private Log log = ExoLogger.getLogger("wcm:WebSchemaConfigService") ;
  
  public WebSchemaConfigServiceImpl() { }
  
  public void addSharedResourcePlugin(ComponentPlugin plugin) {
    if(plugin instanceof SharedResourceFolderPlugin) {
      sharedFolderPlugins.add((SharedResourceFolderPlugin)plugin) ;
    }
  }
  
  public void addWebSchemaHandler(ComponentPlugin plugin) throws Exception {
    if(plugin instanceof WebSchemaHandler) {
      String clazz = plugin.getClass().getName() ;
      webSchemaHandlers.put(clazz, (WebSchemaHandler)plugin);
    }
  }

  public Collection<WebSchemaHandler> getAllWebSchemaHandler() throws Exception {
    return webSchemaHandlers.values();    
  }

  public <T extends WebSchemaHandler> T getWebSchemaHandlerByType(Class<T> clazz) throws Exception {    
    WebSchemaHandler schemaHandler = webSchemaHandlers.get(clazz.getName()) ;
    if(schemaHandler == null) throw new WebSchemaHandlerNotFoundException() ;
    return clazz.cast(schemaHandler);    
  }

  public void createSchema(Node node) throws Exception {
    for(WebSchemaHandler handler: getAllWebSchemaHandler()) {
      if(handler.matchHandler(node)) {
        handler.process(node) ;
        return ;
      }
    }    
  }

  public void start() { 
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    RepositoryService repositoryService = 
      (RepositoryService)container.getComponentInstanceOfType(RepositoryService.class) ;
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    for(SharedResourceFolderPlugin plugin: sharedFolderPlugins) {
      try {
        plugin.createSharedResourceFolder(sessionProvider, repositoryService) ;
      } catch (Exception e) {
        log.error("Can not create shared resource folder for plugin"+plugin.getName(), e);
      }
    }
    sessionProvider.close();
  }

  public void stop() { }  
}
