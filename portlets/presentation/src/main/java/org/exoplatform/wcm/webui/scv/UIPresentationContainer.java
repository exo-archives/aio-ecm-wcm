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
package org.exoplatform.wcm.webui.scv;

import java.security.AccessControlException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.portlet.PortletPreferences;

import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.ecm.publication.NotInPublicationLifecycleException;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.core.WCMService;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.services.wcm.publication.WCMComposer;
import org.exoplatform.services.wcm.publication.WCMPublicationService;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.WebUIPropertiesConfigService;
import org.exoplatform.wcm.webui.WebUIPropertiesConfigService.PopupWindowProperties;
import org.exoplatform.wcm.webui.scv.config.UIPortletConfig;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Author : Do Ngoc Anh *
 * Email: anh.do@exoplatform.com *
 * May 14, 2008
 */

@ComponentConfig(
    lifecycle=Lifecycle.class,
    template="app:/groovy/SingleContentViewer/UIPresentationContainer.gtmpl",
    events = {
      @EventConfig(listeners=UIPresentationContainer.QuickEditActionListener.class)
    }
)


public class UIPresentationContainer extends UIContainer{

  private boolean isDraftRevision = false;
  private boolean isShowDraft     = false;

  /** The date formatter. */
  private DateFormat               dateFormatter = null;

  public boolean isShowDraft() {
    return isShowDraft;
  }

  public void setShowDraft(boolean isShowDraft) {
    this.isShowDraft = isShowDraft;
  }

  /**
   * Instantiates a new uI presentation container.
   * 
   * @throws Exception the exception
   */
  public UIPresentationContainer() throws Exception{   	  
    addChild(UIPresentation.class, null, null);
    dateFormatter = new SimpleDateFormat();
    ((SimpleDateFormat) dateFormatter).applyPattern("dd.MM.yyyy '|' hh'h'mm");
  }

  /**
   * Checks if is quick editable.
   * 
   * @return true, if is quick editable
   * 
   * @throws Exception the exception
   */
  public boolean isQuickEditable() throws Exception {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();           
    return Utils.turnOnQuickEditable(portletRequestContext, true);
  }

  public boolean isDraftRevision() {
    return isDraftRevision;
  }


  public void setDraftRevision(boolean isDraftRevision) {
    this.isDraftRevision = isDraftRevision;
  }

  public boolean isEditable() throws Exception {
	  Node originalNode = null;
	  try {
		  originalNode = getReferenceNode();
	  } catch(ItemNotFoundException ex) {
		  originalNode =  null;
	  } catch(RepositoryException rx) {
		  originalNode =  null;
	  } catch(Exception rx) {
		  originalNode =  null;
	  }

	  try {
		  ((ExtendedNode)originalNode).checkPermission(PermissionType.SET_PROPERTY);
		  return true;
	  } catch(AccessControlException e) {
		  return false;
	  } catch(RepositoryException e) {
		  return false;
	  } catch(NullPointerException e) {
		  return true;
	  }
  }
  
  /**
   * Gets the title.
   * 
   * @param node the node
   * 
   * @return the title
   * 
   * @throws Exception the exception
   */
  public String getTitle(Node node) throws Exception {
	  String title = null;
	  if (node.hasNode("jcr:content")) {
		  Node content = node.getNode("jcr:content");
		  if (content.hasProperty("dc:title")) {
			  title = content.getProperty("dc:title").getValues()[0].getString();
		  }
	  } else if (node.hasProperty("exo:title")) {
		  title = node.getProperty("exo:title").getValue().getString();
	  }
	  if (title==null) title = node.getName();
	  
	  return title;
  }

  /**
   * Gets the created date.
   * 
   * @param node the node
   * 
   * @return the created date
   * 
   * @throws Exception the exception
   */
  public String getCreatedDate(Node node) throws Exception {
    if (node.hasProperty("exo:dateCreated")) {
      Calendar calendar = node.getProperty("exo:dateCreated").getValue().getDate();
      return dateFormatter.format(calendar.getTime());
    }
    return null;
  }

  
  private Node nodeReference;
  
  public Node getReferenceNode() throws Exception {
    // Get node by reference
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    PortletPreferences preferences = portletRequestContext.getRequest().getPreferences();
    String repository = preferences.getValue(UISingleContentViewerPortlet.REPOSITORY, null);    
    String workspace = preferences.getValue(UISingleContentViewerPortlet.WORKSPACE, null);
    String nodeIdentifier = preferences.getValue(UISingleContentViewerPortlet.IDENTIFIER, null) ;
    WCMService wcmService = getApplicationComponent(WCMService.class);
    try { 
      nodeReference = wcmService.getReferencedContent(repository, workspace, nodeIdentifier, Utils.getSessionProvider(this));
    } catch(ItemNotFoundException e) {
      return null;
    }
    PublicationService publicationService = getApplicationComponent(PublicationService.class);
    String lifecycleName = null;
    try {
      lifecycleName = publicationService.getNodeLifecycleName(nodeReference);
    } catch (NotInPublicationLifecycleException e) {}
    if (lifecycleName == null) return nodeReference;
    PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(lifecycleName);
    HashMap<String,Object> context = new HashMap<String, Object>();    
    context.put(WCMComposer.FILTER_MODE, Utils.getCurrentMode());
    
    // filter node by current mode
    return publicationPlugin.getNodeView(nodeReference, context);
  }
  
  public Node getNode() throws Exception {
    Node nodeView = getReferenceNode();
    
    // Set draft for template
    WCMPublicationService wcmPublicationService = getApplicationComponent(WCMPublicationService.class);
    String contentState = wcmPublicationService.getContentState(nodeView);
    if (PublicationDefaultStates.DRAFT.equals(contentState)) isDraftRevision = true;
    else isDraftRevision = false;
    
    // set view draft for template
    if (WCMComposer.MODE_EDIT.equals(Utils.getCurrentMode())) isShowDraft = true;
    else isShowDraft = false;
    
    // Set original node for UIBaseNodePresentation (in case nodeView is a version node)
    UIPresentation presentation = getChild(UIPresentation.class);
    if (nodeView != null && nodeView.isNodeType("nt:frozenNode")) {
      String nodeUUID = nodeView.getProperty("jcr:frozenUuid").getString();
      presentation.setOriginalNode(nodeReference.getSession().getNodeByUUID(nodeUUID));
      presentation.setNode(nodeView);
    } else {
      presentation.setOriginalNode(nodeView);
      presentation.setNode(nodeView);
    }
    
    return nodeView;
  } 
  
  /**
   * The listener interface for receiving quickEditAction events.
   * The class that is interested in processing a quickEditAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addQuickEditActionListener<code> method. When
   * the quickEditAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see QuickEditActionEvent
   */
  public static class QuickEditActionListener extends EventListener<UIPresentationContainer>{   
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIPresentationContainer> event) throws Exception {
      UIPresentationContainer uicomp = event.getSource();
      UISingleContentViewerPortlet uiportlet = uicomp.getAncestorOfType(UISingleContentViewerPortlet.class);
      PortletRequestContext context = (PortletRequestContext)event.getRequestContext();      
      org.exoplatform.webui.core.UIPopupContainer maskPopupContainer = uiportlet.getChild(org.exoplatform.webui.core.UIPopupContainer.class);     
      UIPortletConfig portletConfig = maskPopupContainer.createUIComponent(UIPortletConfig.class,null,null);
      uicomp.addChild(portletConfig);
      portletConfig.init();
      portletConfig.setRendered(true);
      WebUIPropertiesConfigService propertiesConfigService = uicomp.getApplicationComponent(WebUIPropertiesConfigService.class);
      PopupWindowProperties popupProperties = (PopupWindowProperties)propertiesConfigService.getProperties(WebUIPropertiesConfigService.SCV_POPUP_SIZE_QUICK_EDIT);
      maskPopupContainer.activate(portletConfig,popupProperties.getWidth(),popupProperties.getHeight());            
      context.addUIComponentToUpdateByAjax(maskPopupContainer);
    }
  }

  /**
   * Checks if the Portlet shows the Quick Print icon.
   * 
   * @return <code>true</code> if the Quick Print is shown. Otherwise, <code>false</code>
   */
  public boolean isQuickPrint() {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    PortletRequestContext porletRequestContext = (PortletRequestContext) context;
    PortletPreferences prefs = porletRequestContext.getRequest().getPreferences();
    UIPortal uiPortal = Util.getUIPortal();
    UIPage uiPage = uiPortal.findFirstComponentOfType(UIPage.class);

    if(uiPage == null) {
      return false;
    }
    WCMConfigurationService wcmConfigurationService = getApplicationComponent(WCMConfigurationService.class);
    List<String> ids = org.exoplatform.services.wcm.publication.lifecycle.stageversion
    .StageAndVersionPublicationUtil.getListApplicationIdByPage(PortalDataMapper.toPageModel(uiPage), wcmConfigurationService.getRuntimeContextParam(WCMConfigurationService.SCV_PORTLET));
    List<String> newIds = new ArrayList<String>();
    int length = ids.size();

    for(int i = 0; i < length; i++) {
      String temp = ids.get(i);
      String id = temp.substring(temp.lastIndexOf("/") + 1, temp.length());
      newIds.add(id);
    }

    if(newIds.contains(porletRequestContext.getWindowId()) 
        && "true".equals(prefs.getValue("ShowPrintAction", null))) {
      return true;
    }
    return false;
  }
}