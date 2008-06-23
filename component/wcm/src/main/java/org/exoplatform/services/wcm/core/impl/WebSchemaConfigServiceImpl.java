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

import java.util.Collection;
import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.core.WebSchemaHandler;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 3, 2008  
 */
public class WebSchemaConfigServiceImpl implements WebSchemaConfigService, Startable {

  private HashMap<String, WebSchemaHandler> webSchemaHandlers = new HashMap<String, WebSchemaHandler>();
  private WCMConfigurationService wcmConfigService;

  private Log log = ExoLogger.getLogger("wcm:WebSchemaConfigService");

  public WebSchemaConfigServiceImpl(WCMConfigurationService configurationService) { 
    this.wcmConfigService = configurationService;
  }     

  public void addWebSchemaHandler(ComponentPlugin plugin) throws Exception {
    if (plugin instanceof WebSchemaHandler) {
      String clazz = plugin.getClass().getName();
      webSchemaHandlers.put(clazz, (WebSchemaHandler)plugin);
    }
  }

  public Collection<WebSchemaHandler> getAllWebSchemaHandler() throws Exception {
    return webSchemaHandlers.values();    
  }

  public <T extends WebSchemaHandler> T getWebSchemaHandlerByType(Class<T> clazz){    
    WebSchemaHandler schemaHandler = webSchemaHandlers.get(clazz.getName());
    if (schemaHandler == null) return null;
    return clazz.cast(schemaHandler);    
  }

  public void createSchema(Node node) throws Exception {
    for (WebSchemaHandler handler: getAllWebSchemaHandler()) {
      if (handler.matchHandler(node)) {
        handler.process(node);
        return;
      }
    }    
  }

  private void createLiveSharePortalFolders() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    RepositoryService repositoryService = 
      (RepositoryService)container.getComponentInstanceOfType(RepositoryService.class);
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    for (NodeLocation locationEntry: wcmConfigService.getAllLivePortalsLocation()) {
      String repoName = locationEntry.getRepository();
      try {
        ManageableRepository repository = repositoryService.getRepository(repoName);      
        Session session = sessionProvider.getSession(locationEntry.getWorkspace(), repository);
        Node livePortalsStorage = (Node)session.getItem(locationEntry.getPath());
        String liveSharedPortalName = wcmConfigService.getSharedPortalName(repoName);
        livePortalsStorage.addNode(liveSharedPortalName, "exo:portalFolder");
        session.save();
      } catch (Exception e) {
        log.error("Error when try to create share portal folder for repository: "+ repoName, e);
      }            
    }
  }
  public void start() {
    createLiveSharePortalFolders();
  }

  public void stop() {
    // TODO Auto-generated method stub

  }   
}
