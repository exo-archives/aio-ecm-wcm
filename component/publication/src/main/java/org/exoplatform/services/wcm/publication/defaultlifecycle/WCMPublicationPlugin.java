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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.lock.LockException;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.application.Preference;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.ecm.publication.IncorrectStateUpdateLifecycleException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.publication.WebpagePublicationPlugin;
import org.exoplatform.services.wcm.publication.defaultlifecycle.UIPublicationTree.TreeNode;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Sep 30, 2008  
 */
public class WCMPublicationPlugin extends WebpagePublicationPlugin {

  public static final String ENROLLED = "enrolled".intern();
  public static final String UNPUBLISHED = "unpublished".intern();
  public static final String PUBLISHED = "published".intern();
  public static final String DEFAULT_STATE = UNPUBLISHED;

  public static final String PUBLICATION = "publication:publication".intern();
  public static final String LIFECYCLE_PROP = "publication:lifecycleName".intern();
  public static final String CURRENT_STATE = "publication:currentState".intern();
  public static final String HISTORY = "publication:history".intern();
  public static final String WCM_PUBLICATION_MIXIN = "publication:wcmPublication".intern();
  public static final String LIFECYCLE_NAME = "Web Content Publishing".intern();


  public void addMixin(Node node) throws Exception {
    node.addMixin(WCM_PUBLICATION_MIXIN);
    node.setProperty(LIFECYCLE_PROP, LIFECYCLE_NAME);
    node.getSession().save();
  }

  public boolean canAddMixin(Node node) throws Exception {
    List<String> runningPortals = getRunningPortals(node.getSession().getUserID());
    if(runningPortals.size() == 0) {
      throw new AccessControlException("Current user doesn't have access permission to any portal");      
    }
    String portalName = getPortalForContent(node);
    if(portalName == null) {
      throw new PortalNotFoundException("This content doen't belong to any portal");
    }

    if(!isSharedPortal(portalName) && !runningPortals.contains(portalName)) {
      throw new PortalNotFoundException("The portal can be dead.");
    }
    
    //TODO: Need compare LockToken in session of current user with LockToken of LockedOwner
    if (!node.isCheckedOut() || node.isLocked()) {
      throw new LockException("This node is locked or checked-in");
    }
    
    return node.canAddMixin(WCM_PUBLICATION_MIXIN);   
  }

  public void changeState(Node node, String newState,
      HashMap<String, String> context) throws IncorrectStateUpdateLifecycleException, Exception {
    
    node.getSession().save();
  }

  public String[] getPossibleStates() { return new String[] { ENROLLED,UNPUBLISHED,PUBLISHED }; }

  public byte[] getStateImage(Node node, Locale locale) throws IOException,
  FileNotFoundException, Exception {  
    return null;
  }

  public UIForm getStateUI(Node node, UIComponent component) throws Exception {
    UIPublishingPanel form = component.createUIComponent(UIPublishingPanel.class,null,null);
    List<String> runningPortals = getRunningPortals(node.getSession().getUserID());
    String portalName = getPortalForContent(node);
    form.initPanel(node,portalName, runningPortals);
    return form;
  }

  public String getUserInfo(Node node, Locale locale) throws Exception {

    return null;
  }

  public List<String> getRunningPortals(String userId) throws Exception {
    List<String> listPortalName = new ArrayList<String>();
    DataStorage service = getServices(DataStorage.class);
    Query<PortalConfig> query = new Query<PortalConfig>(null, null, null, PortalConfig.class) ;
    PageList pageList = service.find(query) ;
    UserACL userACL = getServices(UserACL.class);
    for(Object object:pageList.getAll()) {
      PortalConfig portalConfig = (PortalConfig)object;
      if(userACL.hasPermission(portalConfig, userId)) {
        listPortalName.add(portalConfig.getName());
      }
    }
    return listPortalName;
  }

  public void publishContentToPage(Node content, Page page) throws Exception {
    UserPortalConfigService userPortalConfigService = getServices(UserPortalConfigService.class);
    Application portlet = new Application();
    portlet.setApplicationType(org.exoplatform.web.application.Application.EXO_PORTLET_TYPE);
    portlet.setShowInfoBar(false);
    
    // Create portlet
    WCMConfigurationService configurationService = getServices(WCMConfigurationService.class);
    StringBuilder windowId = new StringBuilder();
    windowId.append(PortalConfig.PORTAL_TYPE)
            .append("#")
            .append(Util.getUIPortal().getOwner())
            .append(":")
            .append(configurationService.getPublishingPortletName())
            .append("/")
            .append(IdGenerator.generate());
    portlet.setInstanceId(windowId.toString());

    // Add preferences to portlet
    PortletPreferences portletPreferences = new PortletPreferences();
    portletPreferences.setWindowId(windowId.toString());
    portletPreferences.setOwnerType(PortalConfig.PORTAL_TYPE);
    portletPreferences.setOwnerId(Util.getUIPortal().getOwner());
    ArrayList<Preference> listPreference = new ArrayList<Preference>();
    
    Preference preference = new Preference();
    ArrayList<String> listValue = new ArrayList<String>();
    listValue.add(((ManageableRepository) content.getSession().getRepository()).getConfiguration().getName());
    preference.setName("repository");
    preference.setValues(listValue);
    listPreference.add(preference);
    
    preference = new Preference();
    listValue = new ArrayList<String>();
    listValue.add(content.getSession().getWorkspace().getName());
    preference.setName("workspace");
    preference.setValues(listValue);
    listPreference.add(preference);
    
    preference = new Preference();
    listValue = new ArrayList<String>();
    listValue.add(content.getUUID());
    preference.setName("nodeUUID");
    preference.setValues(listValue);
    
    listPreference.add(preference);
    portletPreferences.setPreferences(listPreference);
    
    DataStorage dataStorage = getServices(DataStorage.class);
    dataStorage.save(portletPreferences);
    
    // Add portlet to page
    ArrayList<Object> listPortlet = page.getChildren();
    listPortlet.add(portlet);
    page.setChildren(listPortlet);
    userPortalConfigService.update(page);
    
    // Add properties to node
    Session session = content.getSession();
    ValueFactory valueFactory = session.getValueFactory();
    ArrayList<Value> listTmp;
    
    if (content.hasProperty("publication:navigationNodeURIs")) {
      listTmp = new ArrayList<Value>(Arrays.asList(content.getProperty("publication:navigationNodeURIs").getValues()));
    } else {
      listTmp = new ArrayList<Value>();
    }
    listTmp.add(valueFactory.createValue(getNavigationNodeUriByPage(page)));
    content.setProperty("publication:navigationNodeURIs", listTmp.toArray(new Value[0]));
    
    if (content.hasProperty("publication:webPageIDs")) {
      listTmp = new ArrayList<Value>(Arrays.asList(content.getProperty("publication:webPageIDs").getValues()));
    } else {
      listTmp = new ArrayList<Value>();
    }
    listTmp.add(valueFactory.createValue(page.getPageId()));
    content.setProperty("publication:webPageIDs", listTmp.toArray(new Value[0]));
    
    if (content.hasProperty("publication:applicationIDs")) {
      listTmp = new ArrayList<Value>(Arrays.asList(content.getProperty("publication:applicationIDs").getValues()));
    } else {
      listTmp = new ArrayList<Value>();
    }
    listTmp.add(valueFactory.createValue(portlet.getInstanceId()));
    content.setProperty("publication:applicationIDs",  listTmp.toArray(new Value[0]));
    
    session.save();
  }

  public void suspendPublishedContentFromPage(Node content, Page page) throws Exception {
    Session session = content.getSession();
    ValueFactory valueFactory = session.getValueFactory();
    ArrayList<Object> children = new ArrayList<Object>(page.getChildren());
    ArrayList<Value> listApplicationIDs = new ArrayList<Value>(Arrays.asList(content.getProperty("publication:applicationIDs").getValues()));
    ArrayList<Value> listWebPageIDs = new ArrayList<Value>(Arrays.asList(content.getProperty("publication:webPageIDs").getValues()));
    ArrayList<Value> listNavigationNodeURIs = new ArrayList<Value>(Arrays.asList(content.getProperty("publication:navigationNodeURIs").getValues()));
    for (Object object : page.getChildren()) {
      if (object instanceof Application) {
        Application application = (Application) object;
        for (Value value : content.getProperty("publication:applicationIDs").getValues()) {
          if (value.getString().equals(application.getInstanceId())) {
            children.remove(object);
            listApplicationIDs.remove(value);
            listWebPageIDs.remove(valueFactory.createValue(page.getPageId()));
            listNavigationNodeURIs.remove(valueFactory.createValue(getNavigationNodeUriByPage(page)));
          }
        }
      }
    }
    page.setChildren(children);
    UserPortalConfigService userPortalConfigService = getServices(UserPortalConfigService.class);
    userPortalConfigService.update(page);
    content.setProperty("publication:applicationIDs", listApplicationIDs.toArray(new Value[0]));
    content.setProperty("publication:webPageIDs", listWebPageIDs.toArray(new Value[0]));
    content.setProperty("publication:navigationNodeURIs", listNavigationNodeURIs.toArray(new Value[0]));
    session.save();
  }
  
  private String getNavigationNodeUriByPage(Page page) throws Exception {
    DataStorage dataStorage = getServices(DataStorage.class);
    RequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    for (String portalName : getRunningPortals(requestContext.getRemoteUser())) {
      Query<PageNavigation> query = new Query<PageNavigation>(PortalConfig.PORTAL_TYPE,portalName,PageNavigation.class);
      PageList list = dataStorage.find(query);
      for(Object object: list.getAll()) {
        PageNavigation pageNavigation = PageNavigation.class.cast(object);
        List<PageNode> listPageNode = org.exoplatform.services.wcm.publication.defaultlifecycle.Util.findPageNodeByPageReference(pageNavigation, page.getPageId());
        for (PageNode pageNode : listPageNode) {
          TreeNode treeNode = new TreeNode(portalName, pageNavigation, true);
          treeNode.setPageNode(pageNode);
          return treeNode.getUri();
        }
      }
    }
    return null;
  }
  
  private <T> T getServices(Class<T> clazz) {
    ExoContainer exoContainer = ExoContainerContext.getCurrentContainer();
    return clazz.cast(exoContainer.getComponentInstanceOfType(clazz));
  }

  private String getPortalForContent(Node contentNode) throws Exception {
    LivePortalManagerService livePortalManagerService = getServices(LivePortalManagerService.class);
    for(String portalPath:livePortalManagerService.getLivePortalsPath()) {
      if(contentNode.getPath().startsWith(portalPath)) {
        return livePortalManagerService.getPortalNameByPath(portalPath);
      }
    }

    return null;
  }

  private boolean isSharedPortal(String portalName) throws Exception{
    LivePortalManagerService livePortalManagerService = getServices(LivePortalManagerService.class);
    Node sharedPortal = livePortalManagerService.getLiveSharedPortal(SessionProviderFactory.createSessionProvider());
    return sharedPortal.getName().equals(portalName);    
  }
  
  @SuppressWarnings("unused")
  public Node getNodeView(Node node, Map<String, Object> context) throws Exception {
    return node;
  }
}
