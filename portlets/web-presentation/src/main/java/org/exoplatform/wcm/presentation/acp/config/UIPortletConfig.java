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
package org.exoplatform.wcm.presentation.acp.config;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.portlet.PortletPreferences;

import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.wcm.presentation.acp.UIAdvancedPresentationPortlet;
import org.exoplatform.wcm.presentation.acp.UINonEditable;
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
    UIAdvancedPresentationPortlet uiPresentationPortlet = getAncestorOfType(UIAdvancedPresentationPortlet.class);
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
    }
    addUIWelcomeScreen();
  }

  public void addUIWelcomeScreen() throws Exception {
    System.out.println("=========================> isNewConfig: "+ checkNewConfig());
    UIWelcomeScreen uiWellcomeScreen = addChild(UIWelcomeScreen.class, null, null);
    uiWellcomeScreen.setCreateMode(checkNewConfig());
    uiBackComponent = uiWellcomeScreen ;
  }


  public boolean isQuickEditable() throws Exception {
    UIAdvancedPresentationPortlet uiportlet = getAncestorOfType(UIAdvancedPresentationPortlet.class);
    if(!uiportlet.canEditPortlet()) return false;
    try {
      Node content = uiportlet.getReferencedContent();
      return uiportlet.canEditContent(content);
    } catch (ItemNotFoundException e) {
      //Content not found but user can create new content for the portlet
      return true;
    }        
  }

  private boolean checkNewConfig(){
    UIAdvancedPresentationPortlet uiportlet = getAncestorOfType(UIAdvancedPresentationPortlet.class);
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

  public boolean isEditPortletInCreatePageWinzard() {
    UIPortal uiPortal = Util.getUIPortal();
    UIPortalApplication uiApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
    UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
    // show maskworkpace is being in Portal page edit mode
    if(uiMaskWS.getWindowWidth() > 0) return true;
    return false;
  }
}
