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
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.services.wcm.publication.WCMComposer;
import org.exoplatform.services.wcm.publication.WebpagePublicationPlugin;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationConstant.SITE_MODE;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.config.VersionData;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.config.VersionLog;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.ui.UIPublicationContainer;
import org.exoplatform.services.wcm.publication.listener.navigation.NavigationEventListenerDelegate;
import org.exoplatform.services.wcm.publication.listener.page.PageEventListenerDelegate;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.form.UIForm;

/**
 * The Class StageAndVersionPublicationPlugin.
 */
public class StageAndVersionPublicationPlugin extends WebpagePublicationPlugin{

  /** The page event listener delegate. */
  private PageEventListenerDelegate pageEventListenerDelegate;  
  
  /** The navigation event listener delegate. */
  private NavigationEventListenerDelegate navigationEventListenerDelegate;  

  /**
   * Instantiates a new stage and version publication plugin.
   */
  public StageAndVersionPublicationPlugin() {
    pageEventListenerDelegate = new PageEventListenerDelegate(StageAndVersionPublicationConstant.LIFECYCLE_NAME, ExoContainerContext.getCurrentContainer());
    navigationEventListenerDelegate = new NavigationEventListenerDelegate(StageAndVersionPublicationConstant.LIFECYCLE_NAME, ExoContainerContext.getCurrentContainer());
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.ecm.publication.PublicationPlugin#addMixin(javax.jcr.Node)
   */
  public void addMixin(Node node) throws Exception {
    node.addMixin(StageAndVersionPublicationConstant.PUBLICATION_LIFECYCLE_TYPE);
    if(!node.isNodeType(StageAndVersionPublicationConstant.MIX_VERSIONABLE)) {
      node.addMixin(StageAndVersionPublicationConstant.MIX_VERSIONABLE);
    }            
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.ecm.publication.PublicationPlugin#canAddMixin(javax.jcr.Node)
   */
  public boolean canAddMixin(Node node) throws Exception {
    return node.canAddMixin(StageAndVersionPublicationConstant.PUBLICATION_LIFECYCLE_TYPE);   
  }    

  /* (non-Javadoc)
   * @see org.exoplatform.services.ecm.publication.PublicationPlugin#changeState(javax.jcr.Node, java.lang.String, java.util.HashMap)
   */
  public void changeState(Node node, String newState, HashMap<String, String> context) throws IncorrectStateUpdateLifecycleException,Exception {
    String versionName = context.get(StageAndVersionPublicationConstant.CURRENT_REVISION_NAME);        
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
    if(StageAndVersionPublicationConstant.ENROLLED_STATE.equalsIgnoreCase(newState)) {
      versionLog = new VersionLog(logItemName,newState,node.getSession().getUserID(),GregorianCalendar.getInstance(),StageAndVersionPublicationConstant.ENROLLED_TO_LIFECYCLE);            
      node.setProperty(StageAndVersionPublicationConstant.CURRENT_STATE,newState);
      VersionData revisionData = new VersionData(node.getUUID(),newState,userId);
      revisionsMap.put(node.getUUID(),revisionData);
      addRevisionData(node,revisionsMap.values());      
      addLog(node,versionLog);
    } else if(StageAndVersionPublicationConstant.DRAFT_STATE.equalsIgnoreCase(newState)) {
      node.setProperty(StageAndVersionPublicationConstant.CURRENT_STATE,newState);
      versionLog = new VersionLog(logItemName,newState,node.getSession().getUserID(),GregorianCalendar.getInstance(),StageAndVersionPublicationConstant.CHANGE_TO_DRAFT);      
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
    } else if(StageAndVersionPublicationConstant.PUBLISHED_STATE.equals(newState)) {      
        if (!node.isCheckedOut()) {
            node.checkout();
          }
        Version liveVersion = node.checkin();
        node.checkout();
		  //Change current live revision to obsolete      
		  Node oldLiveRevision = getLiveRevision(node);
		  if(oldLiveRevision != null) {                
		    VersionData versionData = revisionsMap.get(oldLiveRevision.getUUID());
		    if(versionData != null) {
		      versionData.setAuthor(userId);
		      versionData.setState(StageAndVersionPublicationConstant.OBSOLETE_STATE);
		    }else {
		      versionData = new VersionData(oldLiveRevision.getUUID(),StageAndVersionPublicationConstant.OBSOLETE_STATE,userId);
		    }        
		    revisionsMap.put(oldLiveRevision.getUUID(),versionData);
		    versionLog = new VersionLog(oldLiveRevision.getName(),StageAndVersionPublicationConstant.OBSOLETE_STATE, userId, new GregorianCalendar(),StageAndVersionPublicationConstant.CHANGE_TO_OBSOLETE);
		    addLog(node,versionLog);
		  }
		  versionLog = new VersionLog(liveVersion.getName(),newState,userId,new GregorianCalendar(),StageAndVersionPublicationConstant.CHANGE_TO_LIVE);
		  addLog(node,versionLog);      
		  //change base version to published state
		  node.setProperty(StageAndVersionPublicationConstant.CURRENT_STATE,StageAndVersionPublicationConstant.PUBLISHED_STATE);
		  VersionData editableRevision = revisionsMap.get(node.getUUID());
		  if(editableRevision != null) {
		    editableRevision.setAuthor(userId);
		    editableRevision.setState(StageAndVersionPublicationConstant.ENROLLED_STATE);
		  }else {
		    editableRevision = new VersionData(node.getUUID(),StageAndVersionPublicationConstant.ENROLLED_STATE,userId);
		  }
		  revisionsMap.put(node.getUUID(),editableRevision);
		  versionLog = new VersionLog(node.getBaseVersion().getName(),StageAndVersionPublicationConstant.DRAFT_STATE,userId, new GregorianCalendar(),StageAndVersionPublicationConstant.ENROLLED_TO_LIFECYCLE);
		  //Change all awaiting, live revision to obsolete      
		  Value  liveVersionValue = valueFactory.createValue(liveVersion);
		  node.setProperty(StageAndVersionPublicationConstant.LIVE_REVISION_PROP,liveVersionValue);
		  node.setProperty(StageAndVersionPublicationConstant.LIVE_DATE_PROP,new GregorianCalendar());
		  VersionData liveRevisionData = new VersionData(liveVersion.getUUID(),StageAndVersionPublicationConstant.PUBLISHED_STATE,userId);
		  revisionsMap.put(liveVersion.getUUID(),liveRevisionData);
		  addRevisionData(node,revisionsMap.values());
    } else if(StageAndVersionPublicationConstant.OBSOLETE_STATE.equalsIgnoreCase(newState)) {      
      Value value = valueFactory.createValue(selectedRevision);
      Value liveRevision = getValue(node,StageAndVersionPublicationConstant.LIVE_REVISION_PROP);
      if(liveRevision != null && value.getString().equals(liveRevision.getString())) {        
        node.setProperty(StageAndVersionPublicationConstant.LIVE_REVISION_PROP,valueFactory.createValue(""));
      }                        
      versionLog = new VersionLog(selectedRevision.getName(),StageAndVersionPublicationConstant.OBSOLETE_STATE,userId,new GregorianCalendar(),StageAndVersionPublicationConstant.CHANGE_TO_OBSOLETE);
      VersionData versionData = revisionsMap.get(selectedRevision.getUUID());
      if(versionData != null) {
        versionData.setAuthor(userId);
        versionData.setState(StageAndVersionPublicationConstant.OBSOLETE_STATE);
      }else {
        versionData = new VersionData(selectedRevision.getUUID(),StageAndVersionPublicationConstant.OBSOLETE_STATE,userId);
      }      
      revisionsMap.put(selectedRevision.getUUID(),versionData);
      addLog(node,versionLog);
	  //change base version to published state
	  node.setProperty(StageAndVersionPublicationConstant.CURRENT_STATE,StageAndVersionPublicationConstant.OBSOLETE_STATE);
      addRevisionData(node,revisionsMap.values());
    }
    if(!node.isNew())
      node.save();
  }  

  /**
   * Gets the value.
   * 
   * @param node the node
   * @param prop the prop
   * 
   * @return the value
   */
  private Value getValue(Node node, String prop) {
    try {     
      return node.getProperty(prop).getValue();
    } catch (Exception e) {      
      return null;
    }
  }    
  
  /**
   * Gets the live revision.
   * 
   * @param node the node
   * 
   * @return the live revision
   */
  private Node getLiveRevision(Node node) {
    try {
      String nodeVersionUUID = node.getProperty(StageAndVersionPublicationConstant.LIVE_REVISION_PROP).getString(); 
      return node.getVersionHistory().getSession().getNodeByUUID(nodeVersionUUID);
    } catch (Exception e) {      
      return null;
    }
  }
  
  /**
   * Adds the log.
   * 
   * @param node the node
   * @param versionLog the version log
   * 
   * @throws Exception the exception
   */
  private void addLog(Node node, VersionLog versionLog) throws Exception{
    Value[] values = node.getProperty(StageAndVersionPublicationConstant.HISTORY).getValues();
    ValueFactory valueFactory = node.getSession().getValueFactory();
    List<Value> list = new ArrayList<Value>(Arrays.asList(values));
    list.add(valueFactory.createValue(versionLog.toString()));    
    node.setProperty(StageAndVersionPublicationConstant.HISTORY,list.toArray(new Value[]{})); 
  }

  /**
   * Adds the revision data.
   * 
   * @param node the node
   * @param list the list
   * 
   * @throws Exception the exception
   */
  private void addRevisionData(Node node, Collection<VersionData> list) throws Exception {
    List<Value> valueList = new ArrayList<Value>();
    ValueFactory factory = node.getSession().getValueFactory();
    for(VersionData versionData: list) {
      valueList.add(factory.createValue(versionData.toStringValue()));
    }
    node.setProperty(StageAndVersionPublicationConstant.REVISION_DATA_PROP,valueList.toArray(new Value[]{}));
  }

  /**
   * Gets the revision data.
   * 
   * @param node the node
   * 
   * @return the revision data
   * 
   * @throws Exception the exception
   */
  private Map<String, VersionData> getRevisionData(Node node) throws Exception{
    Map<String,VersionData> map = new HashMap<String,VersionData>();    
    try {
      for(Value v: node.getProperty(StageAndVersionPublicationConstant.REVISION_DATA_PROP).getValues()) {
        VersionData versionData = VersionData.toVersionData(v.getString());        
        map.put(versionData.getUUID(),versionData);;
      }
    } catch (Exception e) {
      return map;
    }
    return map;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.ecm.publication.PublicationPlugin#getLocalizedAndSubstituteMessage(java.util.Locale, java.lang.String, java.lang.String[])
   */
  public String getLocalizedAndSubstituteMessage(Locale locale, String key, String[] values) throws Exception {
    ClassLoader cl=this.getClass().getClassLoader();    
    ResourceBundle resourceBundle= ResourceBundle.getBundle(StageAndVersionPublicationConstant.LOCALIZATION, locale, cl);
    String result = resourceBundle.getString(key);
    if(values != null) {
      return String.format(result, values); 
    }        
    return result;

  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.ecm.publication.PublicationPlugin#getNodeView(javax.jcr.Node, java.util.Map)
   */
  public Node getNodeView(Node node, Map<String, Object> context) throws Exception {
    // if current mode is edit mode
    if (context.get(WCMComposer.FILTER_MODE).equals(WCMComposer.MODE_EDIT)) return node;
    // if current mode is live mode
    Node liveNode = getLiveRevision(node);
    if(liveNode != null) {
      return liveNode.getNode("jcr:frozenNode");
    }
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.ecm.publication.PublicationPlugin#getPossibleStates()
   */
  public String[] getPossibleStates() {    
    return new String[] { StageAndVersionPublicationConstant.ENROLLED_STATE, StageAndVersionPublicationConstant.DRAFT_STATE, StageAndVersionPublicationConstant.AWAITING, StageAndVersionPublicationConstant.PUBLISHED_STATE, StageAndVersionPublicationConstant.OBSOLETE_STATE};
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.ecm.publication.PublicationPlugin#getStateImage(javax.jcr.Node, java.util.Locale)
   */
  public byte[] getStateImage(Node arg0, Locale arg1) throws IOException,
  FileNotFoundException,
  Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.ecm.publication.PublicationPlugin#getStateUI(javax.jcr.Node, org.exoplatform.webui.core.UIComponent)
   */
  public UIForm getStateUI(Node node, UIComponent component) throws Exception {   
    UIPublicationContainer publicationContainer = component.createUIComponent(UIPublicationContainer.class, null, null);
    publicationContainer.initContainer(node);
    return publicationContainer;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.ecm.publication.PublicationPlugin#getUserInfo(javax.jcr.Node, java.util.Locale)
   */
  public String getUserInfo(Node arg0, Locale arg1) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.publication.WebpagePublicationPlugin#publishContentToSCV(javax.jcr.Node, org.exoplatform.portal.config.model.Page, java.lang.String)
   */
  public void publishContentToSCV(Node content, Page page, String portalOwnerName) throws Exception {
    // Create portlet
    Application portlet = new Application();
    portlet.setApplicationType(org.exoplatform.web.application.Application.EXO_PORTLET_TYPE);
    portlet.setShowInfoBar(false);
    
    //// generate new portlet's id
    WCMConfigurationService configurationService = StageAndVersionPublicationUtil.getServices(WCMConfigurationService.class);
    StringBuilder windowId = new StringBuilder();
    windowId.append(PortalConfig.PORTAL_TYPE)
            .append("#")
            .append(portalOwnerName)
            .append(":")
            .append(configurationService.getRuntimeContextParam(WCMConfigurationService.SCV_PORTLET))
            .append("/")
            .append(IdGenerator.generate());
    portlet.setInstanceId(windowId.toString());

    //// Add preferences to portlet
    ArrayList<Preference> preferences = new ArrayList<Preference>();
    preferences.add(addPreference("repository", ((ManageableRepository) content.getSession().getRepository()).getConfiguration().getName()));
    preferences.add(addPreference("workspace", content.getSession().getWorkspace().getName()));
    preferences.add(addPreference("nodeIdentifier", content.getUUID()));
    preferences.add(addPreference("ShowQuickEdit", "true"));
    preferences.add(addPreference("ShowTitle", "true"));
    preferences.add(addPreference("ShowVote", "true"));
    preferences.add(addPreference("ShowComments", "true"));
    preferences.add(addPreference("ShowPrintAction", "true"));
    preferences.add(addPreference("isQuickCreate", "false"));
    savePortletPreferences(windowId.toString(), preferences, portalOwnerName);
    
    // Add portlet to page
    ArrayList<Object> listPortlet = page.getChildren();
    listPortlet.add(portlet);
    page.setChildren(listPortlet);
    UserPortalConfigService userPortalConfigService = StageAndVersionPublicationUtil.getServices(UserPortalConfigService.class);
    userPortalConfigService.update(page);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.publication.WebpagePublicationPlugin#publishContentToCLV(javax.jcr.Node, org.exoplatform.portal.config.model.Page, java.lang.String, java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  public void publishContentToCLV(Node content, Page page, String clvPortletId, String portalOwnerName, String remoteUser) throws Exception {
    WCMConfigurationService wcmConfigurationService = StageAndVersionPublicationUtil.getServices(WCMConfigurationService.class);
    ArrayList<Preference> preferences = new ArrayList<Preference>();
    
    DataStorage dataStorage = StageAndVersionPublicationUtil.getServices(DataStorage.class);
    PortletPreferences portletPreferences = dataStorage.getPortletPreferences(new ExoWindowID(clvPortletId));
    
    if (portletPreferences == null) {
      preferences.add(addPreference("repository", ((ManageableRepository) content.getSession().getRepository()).getConfiguration().getName()));
      preferences.add(addPreference("workspace", content.getSession().getWorkspace().getName()));
      preferences.add(addPreference("folderPath", content.getPath() + ";"));
      preferences.add(addPreference("formViewTemplatePath", wcmConfigurationService.getRuntimeContextParam(WCMConfigurationService.FORM_VIEW_TEMPLATE_PATH)));
      preferences.add(addPreference("paginatorTemplatePath", wcmConfigurationService.getRuntimeContextParam(WCMConfigurationService.PAGINATOR_TEMPLAET_PATH)));
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
      
      savePortletPreferences(clvPortletId, preferences, portalOwnerName);
      updateOnAddNodeProperties(page, content, clvPortletId, remoteUser);
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
        
        savePortletPreferences(clvPortletId, preferences, portalOwnerName);
        updateOnAddNodeProperties(page, content, clvPortletId, remoteUser);
      }
    }
  }
  
  /**
   * Update on add node properties.
   * 
   * @param page the page
   * @param content the content
   * @param clvPortletId the clv portlet id
   * @param remoteUser the remote user
   * 
   * @throws Exception the exception
   */
  private void updateOnAddNodeProperties(Page page, Node content, String clvPortletId, String remoteUser) throws Exception {
    if (content.canAddMixin("publication:webpagesPublication")) content.addMixin("publication:webpagesPublication");
    List<String> listExistedNavigationNodeUri = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:navigationNodeURIs");
    List<String> listPageNavigationUri = getListPageNavigationUri(page, remoteUser);
    if (listPageNavigationUri.isEmpty()) return ;
    for (String uri : listPageNavigationUri) {
      if(!listExistedNavigationNodeUri.contains(uri)) {
        listExistedNavigationNodeUri.add(uri);                           
      }            
    }   
    
    List<String> nodeAppIds = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:applicationIDs");
    String mixedAppId = StageAndVersionPublicationUtil.setMixedApplicationId(page.getPageId(), clvPortletId);
    if(nodeAppIds.contains(mixedAppId)) return;
    nodeAppIds.add(mixedAppId);
    
    List<String> nodeWebPageIds = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:webPageIDs");
    nodeWebPageIds.add(page.getPageId());
    
    Session session = content.getSession();
    ValueFactory valueFactory = session.getValueFactory();    
    content.setProperty("publication:navigationNodeURIs", StageAndVersionPublicationUtil.toValues(valueFactory, listExistedNavigationNodeUri));
    content.setProperty("publication:applicationIDs", StageAndVersionPublicationUtil.toValues(valueFactory, nodeAppIds));
    content.setProperty("publication:webPageIDs", StageAndVersionPublicationUtil.toValues(valueFactory, nodeWebPageIds));
    session.save();
  }
  
  /**
   * Update on remove node properties.
   * 
   * @param page the page
   * @param content the content
   * @param clvPortletId the clv portlet id
   * @param remoteUser the remote user
   * 
   * @throws Exception the exception
   */
  private void updateOnRemoveNodeProperties(Page page, Node content, String clvPortletId, String remoteUser) throws Exception {
    List<String> listExistedApplicationId = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:applicationIDs");
    listExistedApplicationId.remove(StageAndVersionPublicationUtil.setMixedApplicationId(page.getPageId(), clvPortletId));
    
    List<String> listExistedPageId = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:webPageIDs");
    listExistedPageId.remove(0);
    
    List<String> listPageNavigationUri = getListPageNavigationUri(page, remoteUser);
    List<String> listExistedNavigationNodeUri = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:navigationNodeURIs");
    List<String> listExistedNavigationNodeUriTmp = new ArrayList<String>();
    listExistedNavigationNodeUriTmp.addAll(listExistedNavigationNodeUri);    
    for (String existedNavigationNodeUri : listExistedNavigationNodeUriTmp) {
      if (listPageNavigationUri.contains(existedNavigationNodeUri)) {
        listExistedNavigationNodeUri.remove(existedNavigationNodeUri);        
      }
    }
    
    Session session = content.getSession();
    ValueFactory valueFactory = session.getValueFactory();    
    content.setProperty("publication:applicationIDs", StageAndVersionPublicationUtil.toValues(valueFactory, listExistedApplicationId));
    content.setProperty("publication:webPageIDs", StageAndVersionPublicationUtil.toValues(valueFactory, listExistedPageId));
    content.setProperty("publication:navigationNodeURIs", StageAndVersionPublicationUtil.toValues(valueFactory, listExistedNavigationNodeUri));
    session.save();
  }
  
  /**
   * Adds the preference.
   * 
   * @param name the name
   * @param value the value
   * 
   * @return the preference
   */
  private Preference addPreference(String name, String value) {
    Preference preference = new Preference();
    ArrayList<String> listValue = new ArrayList<String>();
    listValue.add(value);
    preference.setName(name);
    preference.setValues(listValue);
    return preference;
  }
  
  /**
   * Save portlet preferences.
   * 
   * @param portletId the portlet id
   * @param listPreference the list preference
   * @param portalOwnerName the portal owner name
   * 
   * @throws Exception the exception
   */
  private void savePortletPreferences(String portletId, ArrayList<Preference> listPreference, String portalOwnerName) throws Exception {
    PortletPreferences portletPreferences = new PortletPreferences();
    portletPreferences.setWindowId(portletId);
    portletPreferences.setOwnerType(PortalConfig.PORTAL_TYPE);
    portletPreferences.setOwnerId(portalOwnerName);
    portletPreferences.setPreferences(listPreference);
    DataStorage dataStorage = StageAndVersionPublicationUtil.getServices(DataStorage.class);
    dataStorage.save(portletPreferences);
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.publication.WebpagePublicationPlugin#suspendPublishedContentFromPage(javax.jcr.Node, org.exoplatform.portal.config.model.Page, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  public void suspendPublishedContentFromPage(Node content, Page page, String remoteUser) throws Exception {
    // Remove content from CLV portlet
    DataStorage dataStorage = StageAndVersionPublicationUtil.getServices(DataStorage.class);
    WCMConfigurationService wcmConfigurationService = StageAndVersionPublicationUtil.getServices(WCMConfigurationService.class);
    List<String> clvPortletsId = StageAndVersionPublicationUtil.findAppInstancesByName(page, wcmConfigurationService.getRuntimeContextParam(WCMConfigurationService.CLV_PORTLET));
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
          updateOnRemoveNodeProperties(page, content, clvPortletId, remoteUser);
        }
      }
    }
    
    // Remove content from SCV portlet
    String pageId = page.getPageId();
    List<String> mixedApplicationIDs = StageAndVersionPublicationUtil.getValuesAsString(content, "publication:applicationIDs");
    ArrayList<String> removedApplicationIDs = new ArrayList<String>();
    for(String mixedID: mixedApplicationIDs) {
      if(mixedID.startsWith(pageId) && mixedID.contains(wcmConfigurationService.getRuntimeContextParam(WCMConfigurationService.SCV_PORTLET))) {
        String realAppID = StageAndVersionPublicationUtil.parseMixedApplicationId(mixedID)[1];
        removedApplicationIDs.add(realAppID);
      }
    }
    if(removedApplicationIDs.size() == 0) return;
    StageAndVersionPublicationUtil.removeApplicationFromPage(page, removedApplicationIDs);
    UserPortalConfigService userPortalConfigService = StageAndVersionPublicationUtil.getServices(UserPortalConfigService.class);
    userPortalConfigService.update(page);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.publication.WebpagePublicationPlugin#updateLifecycleOnChangeNavigation(org.exoplatform.portal.config.model.PageNavigation, java.lang.String)
   */
  public void updateLifecycleOnChangeNavigation(PageNavigation pageNavigation, String remoteUser) throws Exception {
    navigationEventListenerDelegate.updateLifecycleOnChangeNavigation(pageNavigation, remoteUser);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.publication.WebpagePublicationPlugin#updateLifecycleOnRemovePage(org.exoplatform.portal.config.model.Page, java.lang.String)
   */
  public void updateLifecycleOnRemovePage(Page page, String remoteUser) throws Exception {
    pageEventListenerDelegate.updateLifecycleOnRemovePage(page, remoteUser);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.publication.WebpagePublicationPlugin#updateLifecyleOnChangePage(org.exoplatform.portal.config.model.Page, java.lang.String)
   */
  public void updateLifecyleOnChangePage(Page page, String remoteUser) throws Exception {
    pageEventListenerDelegate.updateLifecyleOnChangePage(page, remoteUser);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.publication.WebpagePublicationPlugin#updateLifecyleOnCreateNavigation(org.exoplatform.portal.config.model.PageNavigation)
   */
  public void updateLifecyleOnCreateNavigation(PageNavigation pageNavigation) throws Exception {
    navigationEventListenerDelegate.updateLifecyleOnCreateNavigation(pageNavigation);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.publication.WebpagePublicationPlugin#updateLifecyleOnCreatePage(org.exoplatform.portal.config.model.Page, java.lang.String)
   */
  public void updateLifecyleOnCreatePage(Page page, String remoteUser) throws Exception {
    pageEventListenerDelegate.updateLifecyleOnCreatePage(page, remoteUser);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.publication.WebpagePublicationPlugin#updateLifecyleOnRemoveNavigation(org.exoplatform.portal.config.model.PageNavigation)
   */
  public void updateLifecyleOnRemoveNavigation(PageNavigation pageNavigation) throws Exception {
    navigationEventListenerDelegate.updateLifecyleOnRemoveNavigation(pageNavigation);
  }

  /**
   * Gets the running portals.
   * 
   * @param userId the user id
   * 
   * @return the running portals
   * 
   * @throws Exception the exception
   */
  private List<String> getRunningPortals(String userId) throws Exception {
    List<String> listPortalName = new ArrayList<String>();
    DataStorage service = StageAndVersionPublicationUtil.getServices(DataStorage.class);
    Query<PortalConfig> query = new Query<PortalConfig>(null, null, null, null, PortalConfig.class) ;
    PageList pageList = service.find(query) ;
    UserACL userACL = StageAndVersionPublicationUtil.getServices(UserACL.class);
    for(Object object:pageList.getAll()) {
      PortalConfig portalConfig = (PortalConfig)object;
      if(userACL.hasPermission(portalConfig, userId)) {
        listPortalName.add(portalConfig.getName());
      }
    }
    return listPortalName;
  }

  /**
   * Gets the list page navigation uri.
   * 
   * @param page the page
   * @param remoteUser the remote user
   * 
   * @return the list page navigation uri
   * 
   * @throws Exception the exception
   */
  public List<String> getListPageNavigationUri(Page page, String remoteUser) throws Exception {
    List<String> listPageNavigationUri = new ArrayList<String>();
    DataStorage dataStorage = StageAndVersionPublicationUtil.getServices(DataStorage.class);    
    for (String portalName : getRunningPortals(remoteUser)) {
      Query<PageNavigation> query = new Query<PageNavigation>(PortalConfig.PORTAL_TYPE,portalName,PageNavigation.class);
      PageList list = dataStorage.find(query);
      for(Object object: list.getAll()) {
        PageNavigation pageNavigation = PageNavigation.class.cast(object);
        List<PageNode> listPageNode = StageAndVersionPublicationUtil.findPageNodeByPageId(pageNavigation, page.getPageId());        
        for (PageNode pageNode : listPageNode) {
          listPageNavigationUri.add(StageAndVersionPublicationUtil.setMixedNavigationUri(portalName, pageNode.getUri()));
        }
      }
    }
    return listPageNavigationUri;
  }

  /**
   * In this publication process, we put the content in Draft state when editing it.
   */
  public void updateLifecyleOnChangeContent(Node node, String remoteUser)
  throws Exception {
	  updateLifecyleOnChangeContent(node, remoteUser, PublicationDefaultStates.DRAFT);
  }
/**
 * In this publication process, we put the content in Draft state when editing it.
 */
public void updateLifecyleOnChangeContent(Node node, String remoteUser,String newState)
		throws Exception {
	
    String state = node.getProperty(StageAndVersionPublicationConstant.CURRENT_STATE).getString();
    
    if(state.equals(newState))
      return;
    
    HashMap<String, String> context = new HashMap<String, String>();
//    if(node != null) {
//      context.put(StageAndVersionPublicationConstant.CURRENT_REVISION_NAME, node.getName());
//    }
    
    changeState(node, newState, context);
	
}
}