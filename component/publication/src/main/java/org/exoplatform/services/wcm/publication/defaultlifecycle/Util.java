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
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.application.Preference;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.ecm.publication.NotInPublicationLifecycleException;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.portletcontainer.pci.ExoWindowID;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.publication.WCMPublicationService;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Oct 2, 2008  
 */
public class Util {
  
  private static final String APPLICATION_SEPARATOR = "@";
  private static final String HISTORY_SEPARATOR = "; ";
  private static final String URI_SEPARATOR = "/";
  
  public static List<PageNode> findPageNodeByPageId(PageNavigation nav, String pageId) throws Exception {
    List<PageNode> list = new ArrayList<PageNode>();
    for (PageNode node : nav.getNodes()) {
      findPageNodeByPageId(node, pageId, list);
    }
    return list;
  }

  public static void findPageNodeByPageId(PageNode node, String pageId, List<PageNode> allPageNode)
      throws Exception {
    if (pageId.equals(node.getPageReference())) {
      allPageNode.add(node.clone());
    }
    List<PageNode> children = node.getChildren();
    if (children == null)
      return;
    for (PageNode child : children) {
      findPageNodeByPageId(child, pageId, allPageNode);
    }
  }
   
  public static List<String> findAppInstancesByName(Page page, String applicationName) {
    List<String> results = new ArrayList<String>();
    findAppInstancesByContainerAndName(page, applicationName, results);
    return results;
  }
  
  private static void findAppInstancesByContainerAndName(Container container, String applicationName, List<String> results) {
    ArrayList<Object> chidren = container.getChildren();
    if(chidren == null) return ;
    for(Object object: chidren) {
      if(object instanceof Application) {
        Application application = Application.class.cast(object);
        if(application.getInstanceId().contains(applicationName)) {
          results.add(application.getInstanceId());
        }
      } else if(object instanceof Container) {
        Container child = Container.class.cast(object);
        findAppInstancesByContainerAndName(child, applicationName, results);
      }
    }
  }
  
  public static List<String> getValuesAsString(Node node, String propName) throws Exception {
    if(!node.hasProperty(propName)) return new ArrayList<String>();
    List<String> results = new ArrayList<String>();
    for(Value value: node.getProperty(propName).getValues()) {
      results.add(value.getString());
    }
    return results;
  }
  
  public static Value[] toValues(ValueFactory factory, List<String> values) {
    List<Value> list = new ArrayList<Value>();
    for(String value: values) {
      list.add(factory.createValue(value));
    }
    return list.toArray(new Value[list.size()]);
  }
  
  
  public static Node getNodeByApplicationId(String applicationId) throws Exception {
    SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
    DataStorage dataStorage = getServices(DataStorage.class);
    RepositoryService repositoryService = getServices(RepositoryService.class);
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
  
  public static List<String> getListApplicationIdByPage(Page page) {
    WCMConfigurationService configurationService = getServices(WCMConfigurationService.class);
    return Util.findAppInstancesByName(page, configurationService.getPublishingPortletName());
  }
  
  public static void saveAddedItem(Page page, String applicationId, Node content, String lifecycleName) throws Exception {    
    PublicationService publicationService = getServices(PublicationService.class);                 
    String nodeLifecycleName = null;
    try {
      nodeLifecycleName = publicationService.getNodeLifecycleName(content);
    } catch (NotInPublicationLifecycleException e) { return; }
    if (!lifecycleName.equals(nodeLifecycleName)) return;
    
    WCMPublicationService presentationService = getServices(WCMPublicationService.class);
    WCMPublicationPlugin publicationPlugin = (WCMPublicationPlugin) presentationService.getWebpagePublicationPlugins().get(WCMPublicationPlugin.LIFECYCLE_NAME);
    Session session = content.getSession();
    ValueFactory valueFactory = session.getValueFactory();
    
    //Update navigationNodeURI
    List<String> listExistedNavigationNodeUri = getValuesAsString(content, "publication:navigationNodeURIs");    
    String nodeURILogs = "";
    for (String uri : publicationPlugin.getListPageNavigationUri(page)) {
      if(!listExistedNavigationNodeUri.contains(uri)) {
        listExistedNavigationNodeUri.add(uri);
      }            
      nodeURILogs += uri + HISTORY_SEPARATOR;
    }                   
    content.setProperty("publication:navigationNodeURIs", toValues(valueFactory, listExistedNavigationNodeUri));
    
    //Update applicationIDs
    List<String> appIdList = getValuesAsString(content, "publication:applicationIDs");
    String mixedAppId = setMixedApplicationId(page.getPageId(), applicationId);
    if(!appIdList.contains(mixedAppId)) {
      appIdList.add(mixedAppId);
      content.setProperty("publication:applicationIDs", toValues(valueFactory, appIdList));
    }
    //Update webpageIds
    List<String> pageIdList = getValuesAsString(content, "publication:webPageIDs");
    pageIdList.add(page.getPageId());    
    content.setProperty("publication:webPageIDs", toValues(valueFactory, pageIdList));
    
    publicationPlugin.changeState(content, "published", null);
    
    String[] logs = new String[] {new Date().toString(), WCMPublicationPlugin.PUBLISHED, session.getUserID(), "PublicationService.WCMPublicationPlugin.nodePublished", nodeURILogs};
    publicationService.addLog(content, logs);    
    session.save();
  } 
  
  public static void saveRemovedItem(Page page, String applicationId, Node content) throws Exception {
    WCMPublicationService presentationService = getServices(WCMPublicationService.class);
    PublicationService publicationService = getServices(PublicationService.class);
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
        if (!listPageNavigationUri.contains(value.getString())) {
          list.remove(value);
        }
      }
      content.setProperty("publication:navigationNodeURIs", list.toArray(new Value[0]));
    } else {
      content.setProperty("publication:navigationNodeURIs", new ArrayList<Value>().toArray(new Value[0]));
      publicationPlugin.changeState(content, "unpublished", null);
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
  
  public static String setMixedNavigationUri(String portalName, String pageNodeUri) {
    return URI_SEPARATOR + portalName + URI_SEPARATOR + pageNodeUri;
  }
  
  public static String[] parseMixedNavigationUri(String mixedNavigationUri) {
    return mixedNavigationUri.split(URI_SEPARATOR);
  }
  
  public static String setMixedApplicationId(String pageId, String applicationId) {
    return pageId + APPLICATION_SEPARATOR + applicationId;
  }
  
  public static String[] parseMixedApplicationId(String mixedApplicationId) {
    return mixedApplicationId.split(APPLICATION_SEPARATOR);
  }
  
  public static <T> T getServices(Class<T> clazz) {
    ExoContainer exoContainer = ExoContainerContext.getCurrentContainer();
    return clazz.cast(exoContainer.getComponentInstanceOfType(clazz));
  }
} 
