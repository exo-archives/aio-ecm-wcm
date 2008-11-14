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
import org.exoplatform.wcm.webui.scv.UISingleContentViewerPortlet;
import org.exoplatform.wcm.webui.scv.config.quickedition.UIQuickEditContainer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
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
public class UIPortletConfig extends UIContainer {

  private UIComponent uiBackComponent;
  private boolean isNewConfig;

  public UIPortletConfig() throws Exception {
  }

  public void init() throws Exception {
    isNewConfig = checkNewConfig();
    UISingleContentViewerPortlet uiPresentationPortlet = getAncestorOfType(UISingleContentViewerPortlet.class);
    if(!uiPresentationPortlet.canEditPortlet()) {     
      addChild(UINonEditable.class, null, null);
      return;
    }     
    try{
      Node node = uiPresentationPortlet.getReferencedContent();
      if(uiPresentationPortlet.canEditContent(node)) {
        addChild(UIQuickEditContainer.class, null, null);
        return;
      }      
    }catch(Exception e) {
      e.printStackTrace();
    }
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
    System.out.println("===============> isQuickEdit (UIPortletConfig): "+ isQuickEdit);
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
}
