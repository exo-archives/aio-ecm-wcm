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
import java.util.Arrays;
import java.util.Date;
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
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.application.Preference;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.ecm.publication.NotInPublicationLifecycleException;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.portletcontainer.pci.ExoWindowID;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.publication.WCMPublicationService;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Oct 6, 2008  
 */
public class PageEventListenerDelegate {
  
  private String lifecycleName;
  private ExoContainer container;
  private static final String APPLICATION_SEPARATOR = "@";
  private static final String HISTORY_SEPARATOR = "; ";
  
  public PageEventListenerDelegate(String lifecycleName, ExoContainer container) {
    this.lifecycleName = lifecycleName;
    this.container = container;
  }

  public void updateLifecyleOnCreatePage(Page page) throws Exception { }

  public void updateLifecyleOnChangePage(Page page) throws Exception {
     updateAddedApplication(page);
     updateRemovedApplication(page);
  }

  public void updateLifecycleOnRemovePage(Page page) throws Exception { }
  
  
  private void updateAddedApplication(Page page) throws Exception {
    List<String> listApplicationId = getListApplicationIdByPage(page);
    for (String applicationtId : listApplicationId) {
      Node content = getNodeByApplicationId(applicationtId);
      if (content != null) saveAddedNode(page, applicationtId, content);
    }
  }
  
  private void updateRemovedApplication(Page page) throws Exception {
    RepositoryService repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    WCMConfigurationService configurationService = (WCMConfigurationService) container.getComponentInstanceOfType(WCMConfigurationService.class);
    ManageableRepository repository = repositoryService.getCurrentRepository();
    NodeLocation nodeLocation = configurationService.getLivePortalsLocation(repository.getConfiguration().getName());
    
    List<Node> listNode = getListNodeByApplicationId(page, nodeLocation);
    List<String> listApplicationId = getListApplicationIdByPage(page);
    for (Node content : listNode) {
      for (Value value : content.getProperty("publication:applicationIDs").getValues()) {
        String[] tmp = parseMixedApplicationId(value.getString());
        String nodeApplicationId = tmp[1];
        if (tmp[0].equals(page.getPageId()) && !listApplicationId.contains(nodeApplicationId)) {
          saveRemovedNode(page, nodeApplicationId, content);
        }
      }
    }
  }
  
  private List<String> getListApplicationIdByPage(Page page) {
    List<String> listApplicationId = new ArrayList<String>();
    WCMConfigurationService configurationService = (WCMConfigurationService) container.getComponentInstanceOfType(WCMConfigurationService.class);    
    for (Object object : page.getChildren()) {
      if (object instanceof Application) {
        Application application = (Application) object;
        if (application.getInstanceId().contains(configurationService.getPublishingPortletName())) {
          listApplicationId.add(application.getInstanceId());
        }
      }
    }
    return listApplicationId;
  }
  
  private Node getNodeByApplicationId(String applicationId) throws Exception {
    SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
    DataStorage dataStorage = (DataStorage) container.getComponentInstanceOfType(DataStorage.class);
    RepositoryService repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    PortletPreferences portletPreferences = dataStorage.getPortletPreferences(new ExoWindowID(applicationId));
    String repositoryName = null;
    String workspaceName = null;
    String nodeUUID = null;
    for (Object object : portletPreferences.getPreferences()) {
      Preference preference = (Preference) object;
      if (preference.getName().equals("repository")) {
        repositoryName = preference.getValues().get(0).toString();
      } else if (preference.getName().equals("workspace")) {
        workspaceName = preference.getValues().get(0).toString();
      } else if (preference.getName().equals("nodeUUID")) {
        nodeUUID = preference.getValues().get(0).toString();
      }
      if (repositoryName != null && workspaceName != null && nodeUUID != null) {
        Session session = sessionProvider.getSession(workspaceName, repositoryService.getRepository(repositoryName));
        Node content = session.getNodeByUUID(nodeUUID);
        return content;
      }
    }
    return null;
  }
  
  private List<Node> getListNodeByApplicationId(Page page, NodeLocation nodeLocation) throws Exception {
    String repositoryName = nodeLocation.getRepository();
    String workspaceName = nodeLocation.getWorkspace();
    String path = nodeLocation.getPath();
    SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
    RepositoryService repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    Session session = sessionProvider.getSession(workspaceName, repositoryService.getRepository(repositoryName));
    
    List<Node> listNodeApplicationId = new ArrayList<Node>();
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    Query query = queryManager.createQuery("select * from publication:wcmPublication where publication:lifecycleName='" + lifecycleName + "' and publication:webPageIDs like '%" + page.getPageId() + "%' and jcr:path like '" + path + "/%' order by jcr:score", Query.SQL);
    QueryResult results = query.execute();
    NodeIterator nodeIterator = results.getNodes();
    for (; nodeIterator.hasNext();) {
      listNodeApplicationId.add(nodeIterator.nextNode());
    }
    return listNodeApplicationId;
  }
  
  private void saveAddedNode(Page page, String applicationId, Node content) throws Exception {
    WCMPublicationService presentationService = (WCMPublicationService) container.getComponentInstanceOfType(WCMPublicationService.class);
    PublicationService publicationService = (PublicationService) container.getComponentInstanceOfType(PublicationService.class);
    WCMPublicationPlugin publicationPlugin = (WCMPublicationPlugin) presentationService.getWebpagePublicationPlugins().get(WCMPublicationPlugin.LIFECYCLE_NAME);
    
    Session session = content.getSession();
    ValueFactory valueFactory = session.getValueFactory();
    Value value = null;
    String nodeLifecycleName = null;
    try {
      nodeLifecycleName = publicationService.getNodeLifecycleName(content);
    } catch (NotInPublicationLifecycleException e) {
      // Do nothing
    }
    if (nodeLifecycleName == null || !nodeLifecycleName.equals(lifecycleName)) { 
      return;
    }
    
    ArrayList<Value> listTmp = new ArrayList<Value>();
    if (content.hasProperty("publication:navigationNodeURIs")) {
      listTmp = new ArrayList<Value>(Arrays.asList(content.getProperty("publication:navigationNodeURIs").getValues()));
    }
    String uris = "";
    for (String uri : publicationPlugin.getListPageNavigationUri(page)) {
      value = valueFactory.createValue(uri);
      if (listTmp.indexOf(value) < 0) {
        listTmp.add(value);
      }
      uris += uri + HISTORY_SEPARATOR;
    }
    content.setProperty("publication:navigationNodeURIs", listTmp.toArray(new Value[0]));

    listTmp = new ArrayList<Value>();
    if (content.hasProperty("publication:webPageIDs")) {
      listTmp = new ArrayList<Value>(Arrays.asList(content.getProperty("publication:webPageIDs").getValues()));
    }
    value = valueFactory.createValue(page.getPageId());
    listTmp.add(value);
    content.setProperty("publication:webPageIDs", listTmp.toArray(new Value[0]));

    listTmp = new ArrayList<Value>();
    if (content.hasProperty("publication:applicationIDs")) {
      listTmp = new ArrayList<Value>(Arrays.asList(content.getProperty("publication:applicationIDs").getValues()));
    }
    value = valueFactory.createValue(setMixedApplicationId(page.getPageId(), applicationId));
    if (listTmp.indexOf(value) < 0) { 
      listTmp.add(value);
    }
    content.setProperty("publication:applicationIDs", listTmp.toArray(new Value[0]));
    
    String[] logs = new String[] {new Date().toString(), WCMPublicationPlugin.PUBLISHED, session.getUserID(), "PublicationService.WCMPublicationPlugin.nodePublished", uris};
    publicationService.addLog(content, logs);
    
    session.save();
  } 
  
  private void saveRemovedNode(Page page, String applicationId, Node content) throws Exception {
    WCMPublicationService presentationService = (WCMPublicationService) container.getComponentInstanceOfType(WCMPublicationService.class);
    PublicationService publicationService = (PublicationService) container.getComponentInstanceOfType(PublicationService.class);
    WCMPublicationPlugin publicationPlugin = (WCMPublicationPlugin) presentationService.getWebpagePublicationPlugins().get(WCMPublicationPlugin.LIFECYCLE_NAME);
    
    Session session = content.getSession();
    ValueFactory valueFactory = session.getValueFactory();
    List<Value> listTmp;
    
    listTmp = new ArrayList<Value>(Arrays.asList(content.getProperty("publication:applicationIDs").getValues()));
    listTmp.remove(valueFactory.createValue(setMixedApplicationId(page.getPageId(), applicationId)));
    content.setProperty("publication:applicationIDs", listTmp.toArray(new Value[0]));
    
    listTmp = new ArrayList<Value>(Arrays.asList(content.getProperty("publication:webPageIDs").getValues()));
    listTmp.remove(0);
    content.setProperty("publication:webPageIDs", listTmp.toArray(new Value[0]));
    
    List<String> listPageNavigationUri = publicationPlugin.getListPageNavigationUri(page);
    if (listTmp.size() > 0) {
      listTmp = new ArrayList<Value>(Arrays.asList(content.getProperty("publication:navigationNodeURIs").getValues()));
      List<Value> list = new ArrayList<Value>(Arrays.asList(content.getProperty("publication:navigationNodeURIs").getValues()));
      for (Value value : listTmp) {
        if (listPageNavigationUri.indexOf(value.getString()) < 0) {
          list.remove(value);
        }
      }
      content.setProperty("publication:navigationNodeURIs", list.toArray(new Value[0]));
    } else {
      content.setProperty("publication:navigationNodeURIs", new ArrayList<Value>().toArray(new Value[0]));
    }
    
    String uris = "";
    for (String uri : listPageNavigationUri) {
      uris += uri + HISTORY_SEPARATOR;
    }
    content.setProperty("publication:navigationNodeURIs", listTmp.toArray(new Value[0]));
    String[] logs = new String[] {new Date().toString(), WCMPublicationPlugin.PUBLISHED, session.getUserID(), "PublicationService.WCMPublicationPlugin.nodeRemoved", uris};
    publicationService.addLog(content, logs);
    
    session.save();
  }
  
  private String setMixedApplicationId(String pageId, String applicationId) {
    return pageId + APPLICATION_SEPARATOR + applicationId;
  }
  
  private String[] parseMixedApplicationId(String mixedApplicationId) {
    return mixedApplicationId.split(APPLICATION_SEPARATOR);
  }
}
