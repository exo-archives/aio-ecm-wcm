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
 *          dzungdev@gmail.com
 * May 26, 2008  
 */
@ComponentConfig (
    lifecycle = UIContainerLifecycle.class
)
public class UIPortletConfig extends UIContainer implements UIPopupComponent{

  private UIComponent uiBackComponent;
  private boolean isNewConfig;

  final static String POPUP_WEBCONTENT_SELECTOR = "PopupWebContentSelector".intern();
  final static String POPUP_DMS_SELECTOR = "PopupDMSSelector".intern();

  public UIPortletConfig() throws Exception {
  }

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
    }catch(Exception e) {}
    addUIWelcomeScreen();
  }

  public void addUIWelcomeScreen() throws Exception {
    UIWelcomeScreen uiWellcomeScreen = addChild(UIWelcomeScreen.class, null, null);
    uiWellcomeScreen.setCreateMode(checkNewConfig());
    uiBackComponent = uiWellcomeScreen ;
  }


  public boolean isQuickEditable() throws Exception {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    PortletPreferences prefs = portletRequestContext.getRequest().getPreferences();
    boolean isQuickEdit = Boolean.parseBoolean(prefs.getValue("ShowQuickEdit", null));        
    UISingleContentViewerPortlet uiPresentationPortlet = getAncestorOfType(UISingleContentViewerPortlet.class);
    if (isQuickEdit) return uiPresentationPortlet.canEditPortlet();
    return false;
  }

  private boolean checkNewConfig(){
    UISingleContentViewerPortlet uiportlet = getAncestorOfType(UISingleContentViewerPortlet.class);
    try {
      uiportlet.getReferencedContent();
      return false;
    } catch (Exception e) {
    }
    return true;
  }

  public UIComponent getBackComponent() {
    uiBackComponent.setRendered(true);
    return uiBackComponent; 
  }

  public void setBackComponent(UIComponent uicomponent) {
    this.uiBackComponent = uicomponent;
  }

  public void setNewConfig(boolean newConfig) { isNewConfig = newConfig; }  
  public boolean isNewConfig() { return isNewConfig; }

  public boolean isEditPortletInCreatePageWizard() {
    UIPortal uiPortal = Util.getUIPortal();
    UIPortalApplication uiApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
    UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
    // show maskworkpace is being in Portal page edit mode    
    if(uiMaskWS.getWindowWidth() > 0 && uiMaskWS.getWindowHeight() < 0) return true;
    return false;
  }

  public void closePopupAndUpdateUI(WebuiRequestContext requestContext, boolean isUpdate) throws Exception {    
    UISingleContentViewerPortlet uiPresentationPortlet = getAncestorOfType(UISingleContentViewerPortlet.class);    
    UIPopupContainer popupAction = uiPresentationPortlet.getChild(UIPopupContainer.class) ;
    popupAction.deActivate() ;                
    requestContext.addUIComponentToUpdateByAjax(popupAction) ;
    if(isUpdate && !isEditPortletInCreatePageWizard()) {
      Utils.refreshBrowser((PortletRequestContext)requestContext);
    }
  }

  public void activate() throws Exception {    
  }

  public void deActivate() throws Exception {    
  }

  public void initPopupWebContentSelector() throws Exception {
    UIPopupWindow uiPopup = getChildById(POPUP_WEBCONTENT_SELECTOR);
    if(uiPopup == null) {
      uiPopup = addChild(UIPopupWindow.class, null, POPUP_WEBCONTENT_SELECTOR);
    }
    uiPopup.setWindowSize(800, 600);
    UIWebContentPathSelector webContentPathSelector = this.createUIComponent(UIWebContentPathSelector.class, null, null);
    UIWebContentSelectorForm uiWebContentSelector = this.getChild(UIWebContentSelectorForm.class);
    webContentPathSelector.setSourceComponent(uiWebContentSelector, new String[] {UIWebContentSelectorForm.PATH});
    webContentPathSelector.init();
    uiPopup.setUIComponent(webContentPathSelector);
    uiPopup.setShow(true);
  }

  public void initPopupDMSSelector() throws Exception {
    UIPopupWindow uiPopup = getChildById(POPUP_DMS_SELECTOR);
    if(uiPopup == null) {
      uiPopup = addChild(UIPopupWindow.class, null, POPUP_DMS_SELECTOR);
    }
    UIDocumentPathSelector dmsSelector = this.createUIComponent(UIDocumentPathSelector.class, null, null);
    UIDMSSelectorForm dmsSelectorForm = this.getChild(UIDMSSelectorForm.class);
    dmsSelector.setSourceComponent(dmsSelectorForm, new String[] {UIDMSSelectorForm.PATH});
    dmsSelector.init();
    uiPopup.setWindowSize(800, 600);
    uiPopup.setUIComponent(dmsSelector);
    uiPopup.setShow(true);
  }
}
