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
package org.exoplatform.wcm.webui.scv.config;

import javax.jcr.Node;
import javax.portlet.PortletPreferences;

import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.WebUIPropertiesConfigService;
import org.exoplatform.wcm.webui.WebUIPropertiesConfigService.PopupWindowProperties;
import org.exoplatform.wcm.webui.scv.UISingleContentViewerPortlet;
import org.exoplatform.wcm.webui.scv.config.quickedition.UIQuickEditContainer;
import org.exoplatform.wcm.webui.selector.document.UIDocumentPathSelector;
import org.exoplatform.wcm.webui.selector.webcontent.UIWebContentPathSelector;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 * dzungdev@gmail.com
 * May 26, 2008
 */
@ComponentConfig (
    lifecycle = UIContainerLifecycle.class
)
public class UIPortletConfig extends UIContainer implements UIPopupComponent{

  /** The ui back component. */
  private UIComponent uiBackComponent;

  /** The is new config. */
  private boolean isNewConfig;

  /** The Constant POPUP_WEBCONTENT_SELECTOR. */
  final static String POPUP_WEBCONTENT_SELECTOR = "PopupWebContentSelector".intern();

  /** The Constant POPUP_DMS_SELECTOR. */
  final static String POPUP_DMS_SELECTOR = "PopupDMSSelector".intern();

  /**
   * Instantiates a new uI portlet config.
   * 
   * @throws Exception the exception
   */
  public UIPortletConfig() throws Exception {
  }

  /**
   * Inits the.
   * 
   * @throws Exception the exception
   */
  public void init() throws Exception {
    isNewConfig = checkNewConfig();
    UISingleContentViewerPortlet uiPresentationPortlet = getAncestorOfType(UISingleContentViewerPortlet.class);
    if(!uiPresentationPortlet.canEditPortlet()) {     
      addChild(UINonEditable.class, null, null);
      return;
    }    
    if(Utils.isEditPortletInCreatePageWizard()) {
      addUIWelcomeScreen();
      return;
    }    
    try{
      Node node = uiPresentationPortlet.getReferencedContent();
      if(uiPresentationPortlet.canEditContent(node)) {
        addChild(UIQuickEditContainer.class, null, null);
        return;
      }      
    }catch(Exception e) {
      if(UISingleContentViewerPortlet.scvLog.isDebugEnabled()) {
        UISingleContentViewerPortlet.scvLog.debug(e);
      }
    }
    addUIWelcomeScreen();
  }

  /**
   * Adds the ui welcome screen.
   * 
   * @throws Exception the exception
   */
  public void addUIWelcomeScreen() throws Exception {
    UIWelcomeScreen uiWellcomeScreen = addChild(UIWelcomeScreen.class, null, null);
    uiWellcomeScreen.setCreateMode(checkNewConfig());
    uiBackComponent = uiWellcomeScreen ;
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
    PortletPreferences prefs = portletRequestContext.getRequest().getPreferences();
    boolean isQuickEdit = Boolean.parseBoolean(prefs.getValue("ShowQuickEdit", null));        
    UISingleContentViewerPortlet uiPresentationPortlet = getAncestorOfType(UISingleContentViewerPortlet.class);
    if (isQuickEdit) return uiPresentationPortlet.canEditPortlet();
    return false;
  }

  /**
   * Check new config.
   * 
   * @return true, if successful
   */
  private boolean checkNewConfig(){
    UISingleContentViewerPortlet uiportlet = getAncestorOfType(UISingleContentViewerPortlet.class);
    try {
      uiportlet.getReferencedContent();
      return false;
    } catch (Exception e) {
      if(UISingleContentViewerPortlet.scvLog.isDebugEnabled()) {
        UISingleContentViewerPortlet.scvLog.debug(e);
      }
    }
    return true;
  }

  /**
   * Gets the back component.
   * 
   * @return the back component
   */
  public UIComponent getBackComponent() {
    uiBackComponent.setRendered(true);
    return uiBackComponent; 
  }

  /**
   * Sets the back component.
   * 
   * @param uicomponent the new back component
   */
  public void setBackComponent(UIComponent uicomponent) {
    this.uiBackComponent = uicomponent;
  }

  /**
   * Sets the new config.
   * 
   * @param newConfig the new new config
   */
  public void setNewConfig(boolean newConfig) { isNewConfig = newConfig; }  

  /**
   * Checks if is new config.
   * 
   * @return true, if is new config
   */
  public boolean isNewConfig() { return isNewConfig; }

  /**
   * Checks if is edits the portlet in create page wizard.
   * 
   * @return true, if is edits the portlet in create page wizard
   */
  public boolean isEditPortletInCreatePageWizard() {
    UIPortal uiPortal = Util.getUIPortal();
    UIPortalApplication uiApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
    UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
    // show maskworkpace is being in Portal page edit mode    
    if(uiMaskWS.getWindowWidth() > 0 && uiMaskWS.getWindowHeight() < 0) return true;
    return false;
  }

  /**
   * Close popup and update ui.
   * 
   * @param requestContext the request context
   * @param isUpdate the is update
   * 
   * @throws Exception the exception
   */
  public void closePopupAndUpdateUI(WebuiRequestContext requestContext, boolean isUpdate) throws Exception {    
    UISingleContentViewerPortlet uiPresentationPortlet = getAncestorOfType(UISingleContentViewerPortlet.class);    
    UIPopupContainer popupAction = uiPresentationPortlet.getChild(UIPopupContainer.class) ;
    popupAction.deActivate() ;                
    requestContext.addUIComponentToUpdateByAjax(popupAction) ;
    if(isUpdate && !isEditPortletInCreatePageWizard()) {
      Utils.refreshBrowser((PortletRequestContext)requestContext);
    }
  }
  
  public void showPopup(WebuiRequestContext requestContext) throws Exception{
    UISingleContentViewerPortlet viewerPortlet = getAncestorOfType(UISingleContentViewerPortlet.class);
    UIPopupContainer popupContainer = viewerPortlet.getChild(UIPopupContainer.class);
    WebUIPropertiesConfigService propertiesConfigService = getApplicationComponent(WebUIPropertiesConfigService.class);
    PopupWindowProperties properties = null;
    if(Utils.isEditPortletInCreatePageWizard()) {
      properties = (PopupWindowProperties)propertiesConfigService.getProperties(WebUIPropertiesConfigService.SCV_POPUP_SIZE_EDIT_PORTLET_MODE);        
    }else {
      properties = (PopupWindowProperties) propertiesConfigService.getProperties(WebUIPropertiesConfigService.SCV_POPUP_SIZE_QUICK_EDIT);
    }
    popupContainer.activate(this,properties.getWidth(),properties.getHeight());
    requestContext.addUIComponentToUpdateByAjax(popupContainer);
  }
  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIPopupComponent#activate()
   */
  public void activate() throws Exception {    
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIPopupComponent#deActivate()
   */
  public void deActivate() throws Exception {    
  }

  /**
   * Inits the popup web content selector.
   * 
   * @throws Exception the exception
   */
  public void initPopupWebContentSelector() throws Exception {
    UIPopupWindow uiPopup = getChildById(POPUP_WEBCONTENT_SELECTOR);
    if(uiPopup == null) {
      uiPopup = addChild(UIPopupWindow.class, null, POPUP_WEBCONTENT_SELECTOR);
    }
    UIWebContentPathSelector webContentPathSelector = this.createUIComponent(UIWebContentPathSelector.class, null, null);
    UIWebContentSelectorForm uiWebContentSelector = this.getChild(UIWebContentSelectorForm.class);
    webContentPathSelector.setSourceComponent(uiWebContentSelector, new String[] {UIWebContentSelectorForm.PATH});
    uiPopup.setUIComponent(webContentPathSelector);
    webContentPathSelector.init();
    uiPopup.setWindowSize(600,400);
    uiPopup.setShow(true);
  }

  /**
   * Inits the popup dms selector.
   * 
   * @throws Exception the exception
   */
  public void initPopupDMSSelector() throws Exception {
    UIPopupWindow uiPopup = getChildById(POPUP_DMS_SELECTOR);
    if(uiPopup == null) {
      uiPopup = addChild(UIPopupWindow.class, null, POPUP_DMS_SELECTOR);
    }
    UIDocumentPathSelector dmsSelector = this.createUIComponent(UIDocumentPathSelector.class, null, null);
    UIDMSSelectorForm dmsSelectorForm = this.getChild(UIDMSSelectorForm.class);
    dmsSelector.setSourceComponent(dmsSelectorForm, new String[] {UIDMSSelectorForm.PATH});
    uiPopup.setUIComponent(dmsSelector);
    dmsSelector.init();
    uiPopup.setWindowSize(600,400);
    uiPopup.setShow(true);
  }
}
