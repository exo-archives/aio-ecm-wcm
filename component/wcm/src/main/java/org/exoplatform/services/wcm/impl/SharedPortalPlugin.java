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

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Xuan Hoa
 *          hoa.pham@exoplatform.com
 * Mar 10, 2008  
 */
public class SharedPortalPlugin extends BaseComponentPlugin {

  private PropertiesParam pluginProperties ;  

  public SharedPortalPlugin(InitParams initParams) throws Exception{
    pluginProperties = initParams.getPropertiesParam("sharedPortal.properties") ;       
  }

  public void createSharePortaFolder(SessionProvider sessionProvider,RepositoryService repositoryService) throws Exception {
    //TODO using NodeHierarchyCreatorService to get node/path by pathAlias
    //Path alias should be managed by the service with format: repository::workspace::path    
    String sharedPortalPathAlias = pluginProperties.getProperty("storedLocationAlias") ;    
    String temp[] = sharedPortalPathAlias.split("::");
    String repository = temp[0],workspace = temp[1], path= temp[2] ;
    String sharedPortalName = pluginProperties.getProperty("sharedPortalName") ;
    ManageableRepository manageableRepository = repositoryService.getRepository(repository) ;
    Session session = sessionProvider.getSession(workspace,manageableRepository) ;
    Node sharedPortalHome = (Node)session.getItem(path) ;
    if(sharedPortalHome.hasNode(sharedPortalName)) return;
    sharedPortalHome.addNode(sharedPortalName,"exo:portalFolder");    
    session.save();        
  }    
}
