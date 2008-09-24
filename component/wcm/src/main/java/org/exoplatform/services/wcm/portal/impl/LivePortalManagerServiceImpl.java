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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ExtendedNode;
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

/**
 * The Class LivePortalManagerServiceImpl.
 */
public class LivePortalManagerServiceImpl implements LivePortalManagerService {    
  private final String PORTAL_FOLDER = "exo:portalFolder".intern();
  private Map<String,String> livePortalPaths = new HashMap<String,String>();

  private RepositoryService repositoryService; 
  private WCMConfigurationService wcmConfigService;

  /**
   * Instantiates a new live portal manager service impl.
   * 
   * @param configService the config service
   * @param repositoryService the repository service
   */
  public LivePortalManagerServiceImpl(final WCMConfigurationService configService, final RepositoryService repositoryService) {
    this.wcmConfigService = configService;
    this.repositoryService = repositoryService;
  }  

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.portal.LivePortalManagerService#getLivePortal(java.lang.String, org.exoplatform.services.jcr.ext.common.SessionProvider)
   */
  public final Node getLivePortal(final String portalName, final SessionProvider sessionProvider) throws Exception {
    String currentRepository = repositoryService.getCurrentRepository().getConfiguration().getName();
    return getLivePortal(currentRepository, portalName, sessionProvider);    
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.portal.LivePortalManagerService#getLivePortals(org.exoplatform.services.jcr.ext.common.SessionProvider)
   */
  public final List<Node> getLivePortals(final SessionProvider sessionProvider) throws Exception {
    String currentRepository = repositoryService.getCurrentRepository().getConfiguration().getName();
    return getLivePortals(currentRepository,sessionProvider);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.portal.LivePortalManagerService#getLiveSharedPortal(org.exoplatform.services.jcr.ext.common.SessionProvider)
   */
  public final Node getLiveSharedPortal(final SessionProvider sessionProvider) throws Exception {
    String currentRepository = repositoryService.getCurrentRepository().getConfiguration().getName();
    return getLiveSharedPortal(currentRepository, sessionProvider);    
  }  

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.portal.LivePortalManagerService#getLivePortal(java.lang.String, java.lang.String, org.exoplatform.services.jcr.ext.common.SessionProvider)
   */
  public final Node getLivePortal(final String repository, final String portalName, final SessionProvider sessionProvider) throws Exception {
    Node portalsStorage = getLivePortalsStorage(sessionProvider, repository);
    return portalsStorage.getNode(portalName); 
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.portal.LivePortalManagerService#getLivePortals(java.lang.String, org.exoplatform.services.jcr.ext.common.SessionProvider)
   */
  public final List<Node> getLivePortals(final String repository, final SessionProvider sessionProvider) throws Exception {
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

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.portal.LivePortalManagerService#getLiveSharedPortal(java.lang.String, org.exoplatform.services.jcr.ext.common.SessionProvider)
   */
  public final Node getLiveSharedPortal(final String repository, final SessionProvider sessionProvider) throws Exception {
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

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.portal.LivePortalManagerService#addLivePortal(org.exoplatform.portal.config.model.PortalConfig, org.exoplatform.services.jcr.ext.common.SessionProvider)
   */
  public final void addLivePortal(final PortalConfig portalConfig, final SessionProvider sessionProvider)
  throws Exception {
    String currentRepository = repositoryService.getCurrentRepository().getConfiguration().getName();
    Node livePortalsStorage = getLivePortalsStorage(sessionProvider,currentRepository) ;
    String portalName = portalConfig.getName();
    if(livePortalsStorage.hasNode(portalName)) {
      throw new ItemExistsException("Live portal folder existed: " + portalName);
    }
    ExtendedNode newPortal = (ExtendedNode)livePortalsStorage.addNode(portalName,PORTAL_FOLDER);
    if (!newPortal.isNodeType("exo:owneable"))       
      newPortal.addMixin("exo:owneable");
    //Need set some other property for the portal node from portal config like access permission ..    
    newPortal.getSession().save();
    //put sharedPortal path to the map at the first time when run this method
    if(livePortalPaths.size() == 0) {
      String sharedPortalName = wcmConfigService.getSharedPortalName(currentRepository);
      NodeLocation nodeLocation = wcmConfigService.getLivePortalsLocation(currentRepository);
      livePortalPaths.put(sharedPortalName,nodeLocation.getPath() + "/"+ sharedPortalName);
    }
    livePortalPaths.put(portalName,newPortal.getPath());    
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.portal.LivePortalManagerService#removeLivePortal(org.exoplatform.portal.config.model.PortalConfig, org.exoplatform.services.jcr.ext.common.SessionProvider)
   */
  public void removeLivePortal(final PortalConfig portalConfig, final SessionProvider sessionProvider)
  throws Exception {    
    //we should not remove portal folder when a portal was removed. 
    //Should move the portal folder to other location to backup the content
    livePortalPaths.remove(portalConfig.getName());
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.portal.LivePortalManagerService#getLivePortalsPath()
   */
  public Collection<String> getLivePortalsPath() throws Exception {
    return livePortalPaths.values();    
  }

  public String getPortalNameByPath(String portalPath) throws Exception {
    Set<String> keys = livePortalPaths.keySet();
    for(String portalName: keys.toArray(new String[keys.size()])) {
      if(livePortalPaths.get(portalName).equalsIgnoreCase(portalPath)) {
        return portalName;
      }
    }    
    return null;
  }        
}
