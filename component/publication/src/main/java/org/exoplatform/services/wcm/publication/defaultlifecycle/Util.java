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
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.portletcontainer.pci.ExoWindowID;
import org.exoplatform.services.wcm.core.WCMConfigurationService;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Oct 2, 2008  
 */
public class Util {
 
  public static final String HISTORY_SEPARATOR = "; ";
  public static final String APPLICATION_SEPARATOR = "@";
  public static final String URI_SEPARATOR = "/";
  
  public static List<PageNode> findPageNodeByPageId(PageNavigation nav, String pageId) throws Exception {
    List<PageNode> list = new ArrayList<PageNode>();
    if (nav.getOwnerType().equals(PortalConfig.PORTAL_TYPE)) {
      for (PageNode node : nav.getNodes()) {
        findPageNodeByPageId(node, pageId, list);
      }
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
  
  private static void removedAppInstancesInContainerByNames(Container container, List<String> removingApplicationIds) {
    ArrayList<Object> chidren = container.getChildren();    
    ArrayList<Object> chidrenTmp = new ArrayList<Object>();
    if(chidren == null) return ;
    for(Object object: chidren) {
      if(object instanceof Application) {
        Application application = Application.class.cast(object);
        if(!removingApplicationIds.contains(application.getInstanceId())) {
          chidrenTmp.add(object);
        }        
      } else if(object instanceof Container) {
        Container child = Container.class.cast(object);
        removedAppInstancesInContainerByNames(child,removingApplicationIds);
      }
    }
    container.setChildren(chidrenTmp);
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
    if (portletPreferences == null) return null;
    String repositoryName = null;
    String workspaceName = null;
    String nodeIdentifier = null;
    for (Object object : portletPreferences.getPreferences()) {
      Preference preference = (Preference) object;
      if (preference.getName().equals("repository")) {
        repositoryName = preference.getValues().get(0).toString();
      } else if (preference.getName().equals("workspace")) {
        workspaceName = preference.getValues().get(0).toString();
      } else if (preference.getName().equals("nodeIdentifier")) {
        nodeIdentifier = preference.getValues().get(0).toString();
      }
      if (repositoryName != null && workspaceName != null && nodeIdentifier != null) {
        Session session = sessionProvider.getSession(workspaceName, repositoryService.getRepository(repositoryName));
        Node content = null;
        try {
          content = session.getNodeByUUID(nodeIdentifier);; 
        } catch (Exception e) {
          content = (Node) session.getItem(nodeIdentifier);
        }
        return content;
      }
    }
    return null;
  }
  
  public static void removeApplicationFromPage(Page page, List<String> removedApplicationIds) {
    removedAppInstancesInContainerByNames(page, removedApplicationIds);
  }
  
  public static List<String> getListApplicationIdByPage(Page page) {
    WCMConfigurationService configurationService = getServices(WCMConfigurationService.class);
    return Util.findAppInstancesByName(page, configurationService.getPublishingPortletName());
  }
  
  public static String setMixedNavigationUri(String portalName, String pageNodeUri) {
    return URI_SEPARATOR + portalName + URI_SEPARATOR + pageNodeUri;
  }

  public static String[] parseMixedNavigationUri(String mixedNavigationUri) {
    String[] mixedNavigationUris = new String[2];
    int first = 1;
    int second = mixedNavigationUri.indexOf(URI_SEPARATOR, first);
    mixedNavigationUris[0] = mixedNavigationUri.substring(first, second);
    mixedNavigationUris[1] = mixedNavigationUri.substring(second + URI_SEPARATOR.length(), mixedNavigationUri.length()); 
    return mixedNavigationUris;
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
