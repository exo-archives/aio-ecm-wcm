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
package org.exoplatform.services.wcm.publication.defaultlifecycle;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WCMConfigurationService;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Oct 6, 2008  
 */
public class PageEventListenerDelegate {
  
  private String lifecycleName;
//  private ExoContainer container;
  
  public PageEventListenerDelegate(String lifecycleName, ExoContainer container) {
    this.lifecycleName = lifecycleName;
//    this.container = container;
  }

  public void updateLifecyleOnCreatePage(Page page) throws Exception { 
    updateAddedApplication(page);
  }

  public void updateLifecyleOnChangePage(Page page) throws Exception {
    updateAddedApplication(page);
    updateRemovedApplication(page);
  }

  public void updateLifecycleOnRemovePage(Page page) throws Exception {
    List<String> listPageApplicationId = Util.getListApplicationIdByPage(page);
    for (String applicationId : listPageApplicationId) {
      Node content = Util.getNodeByApplicationId(applicationId);
      Util.saveRemovedItem(page, applicationId, content);
    }
  }
  
  private void updateAddedApplication(Page page) throws Exception {
    List<String> listPageApplicationId = Util.getListApplicationIdByPage(page);
    for (String applicationtId : listPageApplicationId) {
      Node content = Util.getNodeByApplicationId(applicationtId);
      if (content != null) Util.saveAddedItem(page, applicationtId, content, lifecycleName);
    }
  }
  
  private void updateRemovedApplication(Page page) throws Exception {
    RepositoryService repositoryService = Util.getServices(RepositoryService.class);
    WCMConfigurationService configurationService = Util.getServices(WCMConfigurationService.class);
    ManageableRepository repository = repositoryService.getCurrentRepository();
    NodeLocation nodeLocation = configurationService.getLivePortalsLocation(repository.getConfiguration().getName());
    
    List<Node> listNode = getListNodeByApplicationId(page, nodeLocation);
    List<String> listApplicationId = Util.getListApplicationIdByPage(page);
    for (Node content : listNode) {
      for (Value value : content.getProperty("publication:applicationIDs").getValues()) {
        String[] tmp = Util.parseMixedApplicationId(value.getString());
        String nodeApplicationId = tmp[1];
        if (tmp[0].equals(page.getPageId()) && !listApplicationId.contains(nodeApplicationId)) {
          Util.saveRemovedItem(page, nodeApplicationId, content);
        }
      }
    }
  }
  
  private List<Node> getListNodeByApplicationId(Page page, NodeLocation nodeLocation) throws Exception {
    String repositoryName = nodeLocation.getRepository();
    String workspaceName = nodeLocation.getWorkspace();
    String path = nodeLocation.getPath();
    SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
    RepositoryService repositoryService = Util.getServices(RepositoryService.class);
    Session session = sessionProvider.getSession(workspaceName, repositoryService.getRepository(repositoryName));
    
    List<Node> listNodeApplicationId = new ArrayList<Node>();
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    Query query = queryManager.createQuery("select * from publication:wcmPublication where publication:lifecycleName='" + lifecycleName + "' and publication:webPageIDs like '%" + page.getPageId() + "%' and jcr:path like '" + path + "/%' order by jcr:score", Query.SQL);
    QueryResult results = query.execute();
    for (NodeIterator nodeIterator = results.getNodes(); nodeIterator.hasNext();) {
      listNodeApplicationId.add(nodeIterator.nextNode());
    }
    return listNodeApplicationId;
  }
}
