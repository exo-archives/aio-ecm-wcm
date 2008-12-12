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
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.ecm.publication.NotInPublicationLifecycleException;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.metadata.PageMetadataService;
import org.exoplatform.services.wcm.publication.WCMPublicationService;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Oct 6, 2008  
 */
public class PageEventListenerDelegate {

  private String lifecycleName;

  public PageEventListenerDelegate(String lifecycleName, ExoContainer container) {
    this.lifecycleName = lifecycleName;
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
      if (content != null) {
        saveRemovedApplication(page, applicationId, content);
      }
    }
  }

  private void updateAddedApplication(Page page) throws Exception {
    List<String> listPageApplicationId = Util.getListApplicationIdByPage(page);
    for (String applicationtId : listPageApplicationId) {
      Node content = Util.getNodeByApplicationId(applicationtId);
      if (content != null) saveAddedApplication(page, applicationtId, content, lifecycleName);
    }
  }

  private void updateRemovedApplication(Page page) throws Exception {
    List<Node> listNode = getListNodeByApplicationId(page);
    List<String> listApplicationId = Util.getListApplicationIdByPage(page);
    for (Node content : listNode) {
      for (Value value : content.getProperty("publication:applicationIDs").getValues()) {
        String[] tmp = Util.parseMixedApplicationId(value.getString());
        String nodeApplicationId = tmp[1];
        if (tmp[0].equals(page.getPageId()) && !listApplicationId.contains(nodeApplicationId)) {
          saveRemovedApplication(page, nodeApplicationId, content);
        }
      }
    }
  }

  private List<Node> getListNodeByApplicationId(Page page) throws Exception {
    RepositoryService repositoryService = Util.getServices(RepositoryService.class);
    WCMConfigurationService configurationService = Util.getServices(WCMConfigurationService.class);
    ManageableRepository repository = repositoryService.getCurrentRepository();
    NodeLocation nodeLocation = configurationService.getLivePortalsLocation(repository.getConfiguration().getName());

    String repositoryName = nodeLocation.getRepository();
    String workspaceName = nodeLocation.getWorkspace();
    String path = nodeLocation.getPath();
    SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
    Session session = sessionProvider.getSession(workspaceName, repositoryService.getRepository(repositoryName));

    List<Node> listPublishedNode = new ArrayList<Node>();
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    Query query = queryManager.createQuery("select * from publication:wcmPublication where publication:lifecycleName='" + lifecycleName + "' and publication:webPageIDs like '%" + page.getPageId() + "%' and jcr:path like '" + path + "/%' order by jcr:score", Query.SQL);
    QueryResult results = query.execute();
    for (NodeIterator nodeIterator = results.getNodes(); nodeIterator.hasNext();) {
      listPublishedNode.add(nodeIterator.nextNode());
    }
    return listPublishedNode;
  }

  private void saveAddedApplication(Page page, String applicationId, Node content, String lifecycleName) throws Exception {    
    PublicationService publicationService = Util.getServices(PublicationService.class);                 
    String nodeLifecycleName = null;
    try {
      nodeLifecycleName = publicationService.getNodeLifecycleName(content);
    } catch (NotInPublicationLifecycleException e) { return; }
    if (!lifecycleName.equals(nodeLifecycleName)) return;

    WCMPublicationService presentationService = Util.getServices(WCMPublicationService.class);
    WCMPublicationPlugin publicationPlugin = (WCMPublicationPlugin) presentationService.getWebpagePublicationPlugins().get(WCMPublicationPlugin.LIFECYCLE_NAME);
    Session session = content.getSession();
    ValueFactory valueFactory = session.getValueFactory();

    List<String> nodeAppIds = Util.getValuesAsString(content, "publication:applicationIDs");
    String mixedAppId = Util.setMixedApplicationId(page.getPageId(), applicationId);
    if(nodeAppIds.contains(mixedAppId))
      return;

    List<String> listExistedNavigationNodeUri = Util.getValuesAsString(content, "publication:navigationNodeURIs");    
    String nodeURILogs = "";
    List<String> listPageNavigationUri = publicationPlugin.getListPageNavigationUri(page);
    if (listPageNavigationUri.isEmpty())  {
      return ;
    }            
    for (String uri : listPageNavigationUri) {
      if(!listExistedNavigationNodeUri.contains(uri)) {
        listExistedNavigationNodeUri.add(uri);                           
      }            
      nodeURILogs += uri + Util.HISTORY_SEPARATOR;
    }                   
    content.setProperty("publication:navigationNodeURIs", Util.toValues(valueFactory, listExistedNavigationNodeUri));

    List<String> nodeWebPageIds = Util.getValuesAsString(content, "publication:webPageIDs");
    nodeWebPageIds.add(page.getPageId());
    nodeAppIds.add(mixedAppId);
    content.setProperty("publication:applicationIDs", Util.toValues(valueFactory, nodeAppIds));
    content.setProperty("publication:webPageIDs", Util.toValues(valueFactory, nodeWebPageIds));

    publicationPlugin.changeState(content, "published", null);

    String[] logs = new String[] {new Date().toString(), WCMPublicationPlugin.PUBLISHED, session.getUserID(), "PublicationService.WCMPublicationPlugin.nodePublished", nodeURILogs};
    publicationService.addLog(content, logs);    
    session.save();
  } 

  private void saveRemovedApplication(Page page, String applicationId, Node content) throws Exception {
    WCMPublicationService presentationService = Util.getServices(WCMPublicationService.class);
    PublicationService publicationService = Util.getServices(PublicationService.class);
    WCMPublicationPlugin publicationPlugin = (WCMPublicationPlugin) presentationService.getWebpagePublicationPlugins().get(WCMPublicationPlugin.LIFECYCLE_NAME);

    Session session = content.getSession();
    ValueFactory valueFactory = session.getValueFactory();

    List<String> listExistedApplicationId = Util.getValuesAsString(content, "publication:applicationIDs");
    listExistedApplicationId.remove(Util.setMixedApplicationId(page.getPageId(), applicationId));
    content.setProperty("publication:applicationIDs", Util.toValues(valueFactory, listExistedApplicationId));

    List<String> listExistedPageId = Util.getValuesAsString(content, "publication:webPageIDs");
    listExistedPageId.remove(0);
    content.setProperty("publication:webPageIDs", Util.toValues(valueFactory, listExistedPageId));

    List<String> listPageNavigationUri = publicationPlugin.getListPageNavigationUri(page);
    List<String> listExistedNavigationNodeUri = Util.getValuesAsString(content, "publication:navigationNodeURIs");
    List<String> listExistedNavigationNodeUriTmp = new ArrayList<String>();
    listExistedNavigationNodeUriTmp.addAll(listExistedNavigationNodeUri);    
    for (String existedNavigationNodeUri : listExistedNavigationNodeUriTmp) {
      if (listPageNavigationUri.contains(existedNavigationNodeUri)) {
        listExistedNavigationNodeUri.remove(existedNavigationNodeUri);        
      }
    }
    content.setProperty("publication:navigationNodeURIs", Util.toValues(valueFactory, listExistedNavigationNodeUri));

    String uris = "";
    for (String uri : listPageNavigationUri) {
      uris += uri + Util.HISTORY_SEPARATOR;
    }

    String[] logs = new String[] {new Date().toString(), WCMPublicationPlugin.PUBLISHED, session.getUserID(), "PublicationService.WCMPublicationPlugin.nodeRemoved", uris};
    publicationService.addLog(content, logs);

    if (listExistedPageId.isEmpty()) { 
      publicationPlugin.changeState(content, "unpublished", null);
    }

    session.save();
  }
}
