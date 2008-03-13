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

package org.exoplatform.services.wcm.impl;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.wcm.WcmService;
import org.exoplatform.services.wcm.WebContentHandler;
import org.exoplatform.services.wcm.WebContentHandlerNotPound;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Xuan Hoa
 *          hoa.pham@exoplatform.com
 * Mar 6, 2008  
 */
public class WcmServiceImpl implements WcmService,Startable {

  private List<SharedPortalPlugin> sharedPortalPlugins = new ArrayList<SharedPortalPlugin>() ;

  private List<WebContentHandler> webContentHanlders = new ArrayList<WebContentHandler>() ;

  private NodeHierarchyCreator hierarchyCreator_ ;
  private RepositoryService repositoryService_ ;  

  public WcmServiceImpl(RepositoryService repositoryService, NodeHierarchyCreator hierarchyCreator) {    
    this.hierarchyCreator_ = hierarchyCreator ;
    this.repositoryService_ = repositoryService ;
  }

  public void processWebContent(Node webContent) throws Exception {    
    for(WebContentHandler handler:webContentHanlders) {
      if(handler.matchHandler(webContent)) {
        handler.handle(webContent) ;
        return;
      }
    }
    throw new WebContentHandlerNotPound() ;
  }    

  public void addPlugin(ComponentPlugin plugin) {
    if(plugin instanceof SharedPortalPlugin) {
      sharedPortalPlugins.add(SharedPortalPlugin.class.cast(plugin)) ;
    }
  }

  public void addWebContentHandler(ComponentPlugin plugin) {
    if(plugin instanceof WebContentHandler) {
      webContentHanlders.add(WebContentHandler.class.cast(plugin)) ;
    }
  } 

  public void start() {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();               
    for(SharedPortalPlugin sharedPortalPlugin:sharedPortalPlugins) {
      try{
        sharedPortalPlugin.createSharePortaFolder(sessionProvider,repositoryService_) ;
      }catch (Exception e) {
        e.printStackTrace();
      }
    }
    sessionProvider.close();
  }

  public void stop() {
    
  }  
}
