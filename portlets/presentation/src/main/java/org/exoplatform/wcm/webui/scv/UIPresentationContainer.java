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
import java.util.ArrayList;
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
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationConstant;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationState;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationConstant.SITE_MODE;
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
  private boolean isObsoletedContent = false;
  private boolean hasLiveRevision = false;


  /**
   * Instantiates a new uI presentation container.
   * 
   * @throws Exception the exception
   */
  public UIPresentationContainer() throws Exception{   	  
    addChild(UIPresentation.class,null,"UIPresentation");
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

  public boolean isObsoletedContent() {
    return isObsoletedContent;
  }


  public void setObsoletedContent(boolean isObsoletedContent) {
    this.isObsoletedContent = isObsoletedContent;
  }


  private boolean isDraftRevision() {
    return isDraftRevision;
  }


  private void setDraftRevision(boolean isDraftRevision) {
    this.isDraftRevision = isDraftRevision;
  }

  public boolean hasLiveRevision() {
	  return hasLiveRevision;
  }
  
  private void setHasLiveRevision(boolean hasLiveRevision) {
	  this.hasLiveRevision = hasLiveRevision;
  }
  
  public boolean isEditable() {
	  //	  boolean isEditable = false;
	  UISingleContentViewerPortlet uiportlet = getAncestorOfType(UISingleContentViewerPortlet.class);
	  Node originalNode = null;
	  try {
		  originalNode = uiportlet.getReferencedContent();
	  } catch(ItemNotFoundException ex) {
		  originalNode =  null;
	  } catch(RepositoryException rx) {
		  originalNode =  null;
	  } catch(Exception rx) {
		  originalNode =  null;
	  }

	  try {
		  ((ExtendedNode)originalNode).checkPermission(PermissionType.SET_PROPERTY);
		  //content exists, we can edit it
		  return true;
	  } catch(AccessControlException e) {
		  //content exists but no rights, we can't edit it
		  return false;
	  } catch(RepositoryException e) {
		  // no access on the repository, we can't edit it
		  return false;
	  } catch(NullPointerException e) {
		  // content is null, we can edit it to select a content or create one.
		  return true;
	  }
  }
  
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    UISingleContentViewerPortlet uiportlet = getAncestorOfType(UISingleContentViewerPortlet.class);
    Node originalNode = null;
    setHasLiveRevision(false);
    try {
      originalNode = uiportlet.getReferencedContent();
    } catch(ItemNotFoundException ex) {
      originalNode = null;
    } catch(RepositoryException rx) {
      originalNode =  null;
    }
    String currentState = StageAndVersionPublicationState.getRevisionState(originalNode);
    if(StageAndVersionPublicationConstant.OBSOLETE_STATE.equals(currentState)) {
      setObsoletedContent(true);
    }else {
      setObsoletedContent(false);
      UIPresentation livePresentation = getChild(UIPresentation.class);
      if(Utils.isLiveMode()) {
        Node liveRevision = getLiveRevision(originalNode);
        if (liveRevision!=null) setHasLiveRevision(true);
        livePresentation.setOriginalNode(originalNode);
        livePresentation.setViewNode(liveRevision);      
        setDraftRevision(false);       
      }else {        
        if(StageAndVersionPublicationConstant.DRAFT_STATE.equals(currentState)) {
          livePresentation.setViewNode(originalNode);          
          livePresentation.setOriginalNode(originalNode);
          setDraftRevision(true);          
        } else if(currentState == null || StageAndVersionPublicationConstant.PUBLISHED_STATE.equals(currentState)) {
          Node liveRevision = getLiveRevision(originalNode);
          if (liveRevision!=null) setHasLiveRevision(true);
          livePresentation.setOriginalNode(originalNode);
          livePresentation.setViewNode(liveRevision);
          setDraftRevision(false);             
        }
      }  
    }
    super.processRender(context);
  }

  private Node getLiveRevision(Node content) throws Exception {
    if (content == null) return null;
    HashMap<String,Object> context = new HashMap<String, Object>();    
    context.put(StageAndVersionPublicationConstant.RUNTIME_MODE, SITE_MODE.LIVE);    
    PublicationService pubService = getApplicationComponent(PublicationService.class);
    String lifecycleName = null;
    try {
    	lifecycleName = pubService.getNodeLifecycleName(content);
		} catch (NotInPublicationLifecycleException e) {}
		if (lifecycleName == null) return content;
    PublicationPlugin pubPlugin = pubService.getPublicationPlugins().get(lifecycleName);
    return pubPlugin.getNodeView(content, context);
  }

  /**
   * Gets the portlet id.
   * 
   * @return the portlet id
   */
  public String getPortletId() {
    PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();    
    return pContext.getWindowId();
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