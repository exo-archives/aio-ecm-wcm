/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.wcm.publication.lifecycle.stageversion;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.version.Version;

import org.exoplatform.commons.utils.PageList;
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
import org.exoplatform.services.ecm.publication.IncorrectStateUpdateLifecycleException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.portletcontainer.pci.ExoWindowID;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.publication.WebpagePublicationPlugin;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.Constant.SITE_MODE;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong_phan@exoplatform.com
 * Mar 2, 2009  
 */
public class StageAndVersionBasedPublicationPlugin extends WebpagePublicationPlugin{

  private PageEventListenerDelegate pageEventListenerDelegate;  
  private NavigationEventListenerDelegate navigationEventListenerDelegate;  

  public StageAndVersionBasedPublicationPlugin() {
    pageEventListenerDelegate = new PageEventListenerDelegate(Constant.LIFECYCLE_NAME, ExoContainerContext.getCurrentContainer());
    navigationEventListenerDelegate = new NavigationEventListenerDelegate(Constant.LIFECYCLE_NAME, ExoContainerContext.getCurrentContainer());
  }

  public void addMixin(Node node) throws Exception {
    node.addMixin(Constant.PUBLICATION_LIFECYCLE_TYPE);
    if(!node.isNodeType(Constant.MIX_VERSIONABLE)) {
      node.addMixin(Constant.MIX_VERSIONABLE);
    }            
  }

  public boolean canAddMixin(Node node) throws Exception {
    return node.canAddMixin(Constant.PUBLICATION_LIFECYCLE_TYPE);   
  }    

  public void changeState(Node node, String newState, HashMap<String, String> context) throws IncorrectStateUpdateLifecycleException,Exception {
    String versionName = context.get(Constant.CURRENT_REVISION_NAME);        
    String logItemName = versionName;
    String userId = node.getSession().getUserID();
    Node selectedRevision = null;
    if(node.getName().equals(versionName) || versionName == null) {
      selectedRevision = node;
      logItemName = node.getName();
    }else {
      selectedRevision = node.getVersionHistory().getVersion(versionName);
    }     
    Map<String, VersionData> revisionsMap = getRevisionData(node);
    VersionLog versionLog = null;
    ValueFactory valueFactory = node.getSession().getValueFactory();
    if(Constant.ENROLLED_STATE.equalsIgnoreCase(newState)) {
      versionLog = new VersionLog(logItemName,newState,node.getSession().getUserID(),GregorianCalendar.getInstance(),Constant.ENROLLED_TO_LIFECYCLE);            
      node.setProperty(Constant.CURRENT_STATE,newState);
      VersionData revisionData = new VersionData(node.getUUID(),newState,userId);
      revisionsMap.put(node.getUUID(),revisionData);
      addRevisionData(node,revisionsMap.values());      
      addLog(node,versionLog);
    } else if(Constant.DRAFT_STATE.equalsIgnoreCase(newState)) {
      node.setProperty(Constant.CURRENT_STATE,newState);
      versionLog = new VersionLog(logItemName,newState,node.getSession().getUserID(),GregorianCalendar.getInstance(),Constant.CHANGE_TO_DRAFT);      
      addLog(node,versionLog);      
      VersionData versionData = revisionsMap.get(node.getUUID());
      if(versionData != null) {
        versionData.setAuthor(userId);
        versionData.setState(newState);        
      }else {
        versionData = new VersionData(node.getUUID(),newState,userId);
      }
      revisionsMap.put(node.getUUID(),versionData);
      addRevisionData(node,revisionsMap.values());
    } else if(Constant.LIVE_STATE.equals(newState)) {      
      Version liveVersion = node.checkin();
      node.checkout();
      //Change current live revision to obsolete      
      Node oldLiveRevision = getLiveRevision(node);
      if(oldLiveRevision != null) {                
        VersionData versionData = revisionsMap.get(oldLiveRevision.getUUID());
        if(versionData != null) {
          versionData.setAuthor(userId);
          versionData.setState(Constant.OBSOLETE_STATE);
        }else {
          versionData = new VersionData(oldLiveRevision.getUUID(),Constant.OBSOLETE_STATE,userId);
        }        
        revisionsMap.put(oldLiveRevision.getUUID(),versionData);
        versionLog = new VersionLog(oldLiveRevision.getName(),Constant.OBSOLETE_STATE, userId, new GregorianCalendar(),Constant.CHANGE_TO_OBSOLETE);
        addLog(node,versionLog);
      }
      versionLog = new VersionLog(liveVersion.getName(),newState,userId,new GregorianCalendar(),Constant.CHANGE_TO_LIVE);
      addLog(node,versionLog);      
      //change base version to draft state
      node.setProperty(Constant.CURRENT_STATE,Constant.ENROLLED_STATE);
      VersionData editableRevision = revisionsMap.get(node.getUUID());
      if(editableRevision != null) {
        editableRevision.setAuthor(userId);
        editableRevision.setState(Constant.ENROLLED_STATE);
      }else {
        editableRevision = new VersionData(node.getUUID(),Constant.ENROLLED_STATE,userId);
      }
      revisionsMap.put(node.getUUID(),editableRevision);
      versionLog = new VersionLog(node.getBaseVersion().getName(),Constant.DRAFT_STATE,userId, new GregorianCalendar(),Constant.ENROLLED_TO_LIFECYCLE);
      //Change all awaiting, live revision to obsolete      
      Value  liveVersionValue = valueFactory.createValue(liveVersion);
      node.setProperty(Constant.LIVE_REVISION_PROP,liveVersionValue);
      node.setProperty(Constant.LIVE_DATE_PROP,new GregorianCalendar());
      VersionData liveRevisionData = new VersionData(liveVersion.getUUID(),Constant.LIVE_STATE,userId);
      revisionsMap.put(liveVersion.getUUID(),liveRevisionData);
      addRevisionData(node,revisionsMap.values());
    } else if(Constant.OBSOLETE_STATE.equalsIgnoreCase(newState)) {      
      Value value = valueFactory.createValue(selectedRevision);
      Value liveRevision = getValue(node,Constant.LIVE_REVISION_PROP);
      if(liveRevision != null && value.getString().equals(liveRevision.getString())) {        
        node.setProperty(Constant.LIVE_REVISION_PROP,valueFactory.createValue(""));
      }                        
      versionLog = new VersionLog(selectedRevision.getName(),Constant.OBSOLETE_STATE,userId,new GregorianCalendar(),Constant.CHANGE_TO_OBSOLETE);
      VersionData versionData = revisionsMap.get(selectedRevision.getUUID());
      if(versionData != null) {
        versionData.setAuthor(userId);
        versionData.setState(Constant.OBSOLETE_STATE);
      }else {
        versionData = new VersionData(selectedRevision.getUUID(),Constant.OBSOLETE_STATE,userId);
      }      
      revisionsMap.put(selectedRevision.getUUID(),versionData);
      addLog(node,versionLog);
      addRevisionData(node,revisionsMap.values());
    }
    if(!node.isNew())
      node.save();
  }  

  private Value getValue(Node node, String prop) {
    try {     
      return node.getProperty(prop).getValue();
    } catch (Exception e) {      
      return null;
    }
  }    
  
  private Node getLiveRevision(Node  node) {
    try {     
      return node.getProperty(Constant.LIVE_REVISION_PROP).getNode();
    } catch (Exception e) {      
      return null;
    }
  }
  private void addLog(Node node, VersionLog versionLog) throws Exception{
    Value[] values = node.getProperty(Constant.HISTORY).getValues();
    ValueFactory valueFactory = node.getSession().getValueFactory();
    List<Value> list = new ArrayList<Value>(Arrays.asList(values));
    list.add(valueFactory.createValue(versionLog.toString()));    
    node.setProperty(Constant.HISTORY,list.toArray(new Value[]{})); 
  }

  private void addRevisionData(Node node, Collection<VersionData> list) throws Exception {
    List<Value> valueList = new ArrayList<Value>();
    ValueFactory factory = node.getSession().getValueFactory();
    for(VersionData versionData: list) {
      valueList.add(factory.createValue(versionData.toStringValue()));
    }
    node.setProperty(Constant.REVISION_DATA_PROP,valueList.toArray(new Value[]{}));
  }

  private Map<String, VersionData> getRevisionData(Node node) throws Exception{
    Map<String,VersionData> map = new HashMap<String,VersionData>();    
    try {
      for(Value v: node.getProperty(Constant.REVISION_DATA_PROP).getValues()) {
        VersionData versionData = VersionData.toVersionData(v.getString());        
        map.put(versionData.getUUID(),versionData);;
      }
    } catch (Exception e) {
      return map;
    }
    return map;
  }

  public String getLocalizedAndSubstituteMessage(Locale locale, String key, String[] values) throws Exception {
    ClassLoader cl=this.getClass().getClassLoader();    
    ResourceBundle resourceBundle= ResourceBundle.getBundle(Constant.LOCALIZATION, locale, cl);
    String result = resourceBundle.getString(key);
    if(values != null) {
      return String.format(result, values); 
    }        
    return result;

  }

  public Node getNodeView(Node node, Map<String, Object> context) throws Exception {
    Object mode = context.get(Constant.RUNTIME_MODE);   
    SITE_MODE runtimeMode = null;
    if(mode == null) {
      runtimeMode = SITE_MODE.LIVE; 
    }else {
      runtimeMode = (SITE_MODE)mode;
    }
    Node viewNode = null;
    Node liveRevision = getLiveRevision(node);
    if(liveRevision != null) {
      viewNode = liveRevision.getNode("jcr:frozenNode");
    }    
    if(runtimeMode == SITE_MODE.LIVE) {
      return viewNode;
    }
    if(viewNode == null)
      viewNode = node;
    return viewNode;
  }

  public String[] getPossibleStates() {    
    return new String[] { Constant.ENROLLED_STATE, Constant.DRAFT_STATE, Constant.AWAITING, Constant.LIVE_STATE, Constant.OBSOLETE_STATE};
  }

  public byte[] getStateImage(Node arg0, Locale arg1) throws IOException,
  FileNotFoundException,
  Exception {
    return null;
  }

  public UIForm getStateUI(Node node, UIComponent component) throws Exception {   
    UIPublicationContainer publicationContainer = component.createUIComponent(UIPublicationContainer.class, null, null);
    publicationContainer.initContainer(node);
    return publicationContainer;
  }

  public String getUserInfo(Node arg0, Locale arg1) throws Exception {
    return null;
  }

  public void publishContentToPage(Node content, Page page) throws Exception {
    // Create portlet
    Application portlet = new Application();
    portlet.setApplicationType(org.exoplatform.web.application.Application.EXO_PORTLET_TYPE);
    portlet.setShowInfoBar(false);
    
    //// generate new portlet's id
    WCMConfigurationService configurationService = Util.getServices(WCMConfigurationService.class);
    StringBuilder windowId = new StringBuilder();
    windowId.append(PortalConfig.PORTAL_TYPE)
            .append("#")
            .append(org.exoplatform.portal.webui.util.Util.getUIPortal().getOwner())
            .append(":")
            .append(configurationService.getPublishingPortletName())
            .append("/")
            .append(IdGenerator.generate());
    portlet.setInstanceId(windowId.toString());

    //// Add preferences to portlet
    ArrayList<Preference> preferences = new ArrayList<Preference>();
    preferences.add(addPreference("repository", ((ManageableRepository) content.getSession().getRepository()).getConfiguration().getName()));
    preferences.add(addPreference("workspace", content.getSession().getWorkspace().getName()));
    preferences.add(addPreference("nodeIdentifier", content.getUUID()));
    savePortletPreferences(windowId.toString(), preferences);
    
    // Add portlet to page
    ArrayList<Object> listPortlet = page.getChildren();
    listPortlet.add(portlet);
    page.setChildren(listPortlet);
    UserPortalConfigService userPortalConfigService = Util.getServices(UserPortalConfigService.class);
    userPortalConfigService.update(page);
  }

  @SuppressWarnings("unchecked")
  public void publishContentToCLV(Node content, Page page, String clvPortletId, PortletPreferences portletPreferences) throws Exception {
    WCMConfigurationService wcmConfigurationService = Util.getServices(WCMConfigurationService.class);
    ArrayList<Preference> preferences = new ArrayList<Preference>();
    if (portletPreferences == null) {
      preferences.add(addPreference("repository", ((ManageableRepository) content.getSession().getRepository()).getConfiguration().getName()));
      preferences.add(addPreference("workspace", content.getSession().getWorkspace().getName()));
      preferences.add(addPreference("folderPath", content.getPath() + ";"));
      preferences.add(addPreference("formViewTemplatePath", wcmConfigurationService.getRuntimeContextParam("formViewTemplatePath")));
      preferences.add(addPreference("paginatorTemplatePath", wcmConfigurationService.getRuntimeContextParam("paginatorTemplatePath")));
      preferences.add(addPreference("itemsPerPage", "10"));
      preferences.add(addPreference("showQuickEditButton", "true"));
      preferences.add(addPreference("showRefreshButton", "false"));
      preferences.add(addPreference("showThumbnailsView", "true"));
      preferences.add(addPreference("showTitle", "true"));
      preferences.add(addPreference("showDateCreated", "true"));
      preferences.add(addPreference("showSummary", "true"));
      preferences.add(addPreference("showHeader", "true"));
      preferences.add(addPreference("source", "folder"));
      preferences.add(addPreference("mode", "ManualViewerMode"));
      preferences.add(addPreference("orderBy", "exo:title"));
      preferences.add(addPreference("orderType", "DESC"));

      Preference preference = new Preference();
      preference.setName("contents");
      ArrayList<String> contentValues = new ArrayList<String>();
      contentValues.add(content.getPath());
      preference.setValues(contentValues);
      preferences.add(preference);
      
      savePortletPreferences(clvPortletId, preferences);
      updateOnAddNodeProperties(page, content, clvPortletId);
    } else {
      String clvMode = "";
      Preference folderPreference = new Preference();
      Preference contentPreference = new Preference();
      int folderPrefIndex = 0;
      int contentPrefIndex = 0;
      List<?> listPrefs = portletPreferences.getPreferences();
      for (Object object: listPrefs) {
        Preference preference = (Preference) object;
        if ("mode".equals(preference.getName())) {
          clvMode = preference.getValues().get(0).toString();
        } else if ("contents".equals(preference.getName())) {
          contentPreference = preference;
          contentPrefIndex = listPrefs.indexOf(object);
        } else if ("folderPath".equals(preference.getName())) {
          folderPreference = preference;
          folderPrefIndex = listPrefs.indexOf(object);
        }
        preferences.add(preference);
      }
      if ("ManualViewerMode".equals(clvMode)) {
        ArrayList folderValues = new ArrayList(folderPreference.getValues());
        String folderValue = folderValues.get(0).toString();
        folderValues.set(0, content.getPath() + ";" + folderValue);
        folderPreference.setValues(folderValues);
        preferences.set(folderPrefIndex, folderPreference);
        
        ArrayList contentValues = new ArrayList(contentPreference.getValues());
        contentValues.add(0, content.getPath()); 
        contentPreference.setValues(contentValues);    
        preferences.set(contentPrefIndex, contentPreference);
        
        savePortletPreferences(clvPortletId, preferences);
        updateOnAddNodeProperties(page, content, clvPortletId);
      }
    }
  }
  
  private void updateOnAddNodeProperties(Page page, Node content, String clvPortletId) throws Exception {
    List<String> listExistedNavigationNodeUri = Util.getValuesAsString(content, "publication:navigationNodeURIs");
    List<String> listPageNavigationUri = getListPageNavigationUri(page);
    if (listPageNavigationUri.isEmpty()) return ;
    for (String uri : listPageNavigationUri) {
      if(!listExistedNavigationNodeUri.contains(uri)) {
        listExistedNavigationNodeUri.add(uri);                           
      }            
    }   
    
    List<String> nodeAppIds = Util.getValuesAsString(content, "publication:applicationIDs");
    String mixedAppId = Util.setMixedApplicationId(page.getPageId(), clvPortletId);
    if(nodeAppIds.contains(mixedAppId)) return;
    nodeAppIds.add(mixedAppId);
    
    List<String> nodeWebPageIds = Util.getValuesAsString(content, "publication:webPageIDs");
    nodeWebPageIds.add(page.getPageId());
    
    Session session = content.getSession();
    ValueFactory valueFactory = session.getValueFactory();    
    content.setProperty("publication:navigationNodeURIs", Util.toValues(valueFactory, listExistedNavigationNodeUri));
    content.setProperty("publication:applicationIDs", Util.toValues(valueFactory, nodeAppIds));
    content.setProperty("publication:webPageIDs", Util.toValues(valueFactory, nodeWebPageIds));
    session.save();
  }
  
  private void updateOnRemoveNodeProperties(Page page, Node content, String clvPortletId) throws Exception {
    List<String> listExistedApplicationId = Util.getValuesAsString(content, "publication:applicationIDs");
    listExistedApplicationId.remove(Util.setMixedApplicationId(page.getPageId(), clvPortletId));
    
    List<String> listExistedPageId = Util.getValuesAsString(content, "publication:webPageIDs");
    listExistedPageId.remove(0);
    
    List<String> listPageNavigationUri = getListPageNavigationUri(page);
    List<String> listExistedNavigationNodeUri = Util.getValuesAsString(content, "publication:navigationNodeURIs");
    List<String> listExistedNavigationNodeUriTmp = new ArrayList<String>();
    listExistedNavigationNodeUriTmp.addAll(listExistedNavigationNodeUri);    
    for (String existedNavigationNodeUri : listExistedNavigationNodeUriTmp) {
      if (listPageNavigationUri.contains(existedNavigationNodeUri)) {
        listExistedNavigationNodeUri.remove(existedNavigationNodeUri);        
      }
    }
    
    Session session = content.getSession();
    ValueFactory valueFactory = session.getValueFactory();    
    content.setProperty("publication:applicationIDs", Util.toValues(valueFactory, listExistedApplicationId));
    content.setProperty("publication:webPageIDs", Util.toValues(valueFactory, listExistedPageId));
    content.setProperty("publication:navigationNodeURIs", Util.toValues(valueFactory, listExistedNavigationNodeUri));
    session.save();
  }
  
  private Preference addPreference(String name, String value) {
    Preference preference = new Preference();
    ArrayList<String> listValue = new ArrayList<String>();
    listValue.add(value);
    preference.setName(name);
    preference.setValues(listValue);
    return preference;
  }
  
  private void savePortletPreferences(String portletId, ArrayList<Preference> listPreference) throws Exception {
    PortletPreferences portletPreferences = new PortletPreferences();
    portletPreferences.setWindowId(portletId);
    portletPreferences.setOwnerType(PortalConfig.PORTAL_TYPE);
    portletPreferences.setOwnerId(org.exoplatform.portal.webui.util.Util.getUIPortal().getOwner());
    portletPreferences.setPreferences(listPreference);
    DataStorage dataStorage = Util.getServices(DataStorage.class);
    dataStorage.save(portletPreferences);
  }
  
  @SuppressWarnings("unchecked")
  public void suspendPublishedContentFromPage(Node content, Page page) throws Exception {
    // Remove content from CLV portlet
    DataStorage dataStorage = Util.getServices(DataStorage.class);
    WCMConfigurationService wcmConfigurationService = Util.getServices(WCMConfigurationService.class);
    List<String> clvPortletsId = Util.findAppInstancesByName(page, wcmConfigurationService.getRuntimeContextParam("CLVPortlet"));
    if (content != null && !clvPortletsId.isEmpty()) {
      for (String clvPortletId : clvPortletsId) {
        PortletPreferences portletPreferences = dataStorage.getPortletPreferences(new ExoWindowID(clvPortletId));
        if (portletPreferences != null) {
          ArrayList<Preference> preferences = new ArrayList<Preference>();
          for (Object preferenceTmp : portletPreferences.getPreferences()) {
            Preference preference = (Preference) preferenceTmp;
            if ("folderPath".equals(preference.getName()) && preference.getValues().size() > 0) {
              ArrayList<String> values = new ArrayList<String>();
              values.add(preference.getValues().get(0).toString().replaceAll(content.getPath() + ";", ""));
              preference.setValues(values);
            } else if ("contents".equals(preference.getName()) && preference.getValues().size() > 0) {
              List<String> values = preference.getValues();
              values.remove(content.getPath());
              preference.setValues(new ArrayList<String>(values));
            }
            preferences.add(preference);
          }
          dataStorage.save(portletPreferences);
          updateOnRemoveNodeProperties(page, content, clvPortletId);
        }
      }
    }
    
    // Remove content from SCV portlet
    String pageId = page.getPageId();
    List<String> mixedApplicationIDs = Util.getValuesAsString(content, "publication:applicationIDs");
    ArrayList<String> removedApplicationIDs = new ArrayList<String>();
    for(String mixedID: mixedApplicationIDs) {
      if(mixedID.startsWith(pageId) && mixedID.contains(wcmConfigurationService.getRuntimeContextParam("SCVPortlet"))) {
        String realAppID = Util.parseMixedApplicationId(mixedID)[1];
        removedApplicationIDs.add(realAppID);
      }
    }
    if(removedApplicationIDs.size() == 0) return;
    Util.removeApplicationFromPage(page, removedApplicationIDs);
    UserPortalConfigService userPortalConfigService = Util.getServices(UserPortalConfigService.class);
    userPortalConfigService.update(page);
  }

  public void updateLifecycleOnChangeNavigation(PageNavigation pageNavigation) throws Exception {
    navigationEventListenerDelegate.updateLifecycleOnChangeNavigation(pageNavigation);
  }

  public void updateLifecycleOnRemovePage(Page page) throws Exception {
    pageEventListenerDelegate.updateLifecycleOnRemovePage(page);
  }

  public void updateLifecyleOnChangePage(Page page) throws Exception {
    pageEventListenerDelegate.updateLifecyleOnChangePage(page);
  }

  public void updateLifecyleOnCreateNavigation(PageNavigation pageNavigation) throws Exception {
    navigationEventListenerDelegate.updateLifecyleOnCreateNavigation(pageNavigation);
  }

  public void updateLifecyleOnCreatePage(Page page) throws Exception {
    pageEventListenerDelegate.updateLifecyleOnCreatePage(page);
  }

  public void updateLifecyleOnRemoveNavigation(PageNavigation pageNavigation) throws Exception {
    navigationEventListenerDelegate.updateLifecyleOnRemoveNavigation(pageNavigation);
  }

  private List<String> getRunningPortals(String userId) throws Exception {
    List<String> listPortalName = new ArrayList<String>();
    DataStorage service = Util.getServices(DataStorage.class);
    Query<PortalConfig> query = new Query<PortalConfig>(null, null, null, null, PortalConfig.class) ;
    PageList pageList = service.find(query) ;
    UserACL userACL = Util.getServices(UserACL.class);
    for(Object object:pageList.getAll()) {
      PortalConfig portalConfig = (PortalConfig)object;
      if(userACL.hasPermission(portalConfig, userId)) {
        listPortalName.add(portalConfig.getName());
      }
    }
    return listPortalName;
  }

  public List<String> getListPageNavigationUri(Page page) throws Exception {
    List<String> listPageNavigationUri = new ArrayList<String>();
    DataStorage dataStorage = Util.getServices(DataStorage.class);    
    RequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    for (String portalName : getRunningPortals(requestContext.getRemoteUser())) {
      Query<PageNavigation> query = new Query<PageNavigation>(PortalConfig.PORTAL_TYPE,portalName,PageNavigation.class);
      PageList list = dataStorage.find(query);
      for(Object object: list.getAll()) {
        PageNavigation pageNavigation = PageNavigation.class.cast(object);
        List<PageNode> listPageNode = Util.findPageNodeByPageId(pageNavigation, page.getPageId());        
        for (PageNode pageNode : listPageNode) {
          listPageNavigationUri.add(Util.setMixedNavigationUri(portalName, pageNode.getUri()));
        }
      }
    }
    return listPageNavigationUri;
  }
}