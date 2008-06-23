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
package org.exoplatform.services.wcm.portal.impl;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;

/*
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 19, 2008  
 */
public class LivePortalManagerServiceImpl implements LivePortalManagerService {
  
  private final String PORTAL_FOLDER = "exo:portalFolder".intern();
  
  private RepositoryService repositoryService;
  private WCMConfigurationService wcmConfigService;
  
  public LivePortalManagerServiceImpl(WCMConfigurationService configService, RepositoryService repositoryService) {
    this.wcmConfigService = configService;
    this.repositoryService = repositoryService;
  }  

  public Node getLivePortal(final String repository, final String portalName, final SessionProvider sessionProvider) throws Exception {
    Node portalsStorage = getLivePortalsStorage(sessionProvider, repository);
    return portalsStorage.getNode(portalName); 
  }

  public List<Node> getLivePortals(final String repository, final SessionProvider sessionProvider) throws Exception {
    List<Node> list = new ArrayList<Node>();    
    Node portalsStorage = getLivePortalsStorage(sessionProvider, repository);
    for (NodeIterator iterator = portalsStorage.getNodes(); iterator.hasNext(); ) {
      Node node = iterator.nextNode();
      if (PORTAL_FOLDER.equals(node.getPrimaryNodeType().getName())) {
        list.add(node);
      }
    }
    return list;
  }

  public Node getLiveSharedPortal(final String repository, final SessionProvider sessionProvider) throws Exception {
    Node portalsStorage = getLivePortalsStorage(sessionProvider, repository);
    String sharePortalName = wcmConfigService.getSharedPortalName(repository);
    return portalsStorage.getNode(sharePortalName);
  }

  private Node getLivePortalsStorage(final SessionProvider sessionProvider, final String repository) throws Exception {
    NodeLocation locationEntry = wcmConfigService.getLivePortalsLocation(repository);
    String workspace = locationEntry.getWorkspace();
    String portalsStoragePath = locationEntry.getPath();    
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Session session = sessionProvider.getSession(workspace,manageableRepository);
    return (Node)session.getItem(portalsStoragePath);
  }  
  
}
