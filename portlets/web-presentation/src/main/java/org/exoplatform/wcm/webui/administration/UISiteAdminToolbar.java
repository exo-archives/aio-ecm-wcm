package org.exoplatform.wcm.webui.administration;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.webui.UIWelcomeComponent;
import org.exoplatform.portal.webui.UIManagement.ManagementMode;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.page.UIPageCreationWizard;
import org.exoplatform.portal.webui.page.UIPageEditWizard;
import org.exoplatform.portal.webui.page.UIWizardPageCreationBar;
import org.exoplatform.portal.webui.page.UIWizardPageSetInfo;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.portal.UIPortalForm;
import org.exoplatform.portal.webui.portal.UIPortalManagement;
import org.exoplatform.portal.webui.portal.UIPortalSelector;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIControlWorkspace;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIPortalToolPanel;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.portal.webui.workspace.UIControlWorkspace.UIControlWSWorkingArea;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponentDecorator;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

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

/*
 * Created by The eXo Platform SAS
 * Author : Anh Do Ngoc
 *          anh.do@exoplatform.com
 * Oct 6, 2008  
 */

@ComponentConfig(template = "app:/groovy/SiteAdministration/UISiteAdminToolBar.gtmpl", 
    events = {
      @EventConfig(listeners = UISiteAdminToolbar.AddPageActionListener.class),
      @EventConfig(listeners = UISiteAdminToolbar.EditPageActionListener.class),
      @EventConfig(listeners = UISiteAdminToolbar.EditPortalActionListener.class),
      @EventConfig(listeners = UISiteAdminToolbar.CreatePortalActionListener.class),
      @EventConfig(listeners = UISiteAdminToolbar.ChangePortalActionListener.class) 
    }
)
public class UISiteAdminToolbar extends UIContainer {

  public UISiteAdminToolbar() {    
  }
    

  public static class AddPageActionListener extends EventListener<UISiteAdminToolbar> {
    public void execute(Event<UISiteAdminToolbar> event) throws Exception {       
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      PortalRequestContext portalContext = Util.getPortalRequestContext();
      event.setRequestContext(Util.getPortalRequestContext());    
      UIControlWorkspace uiControl = uiApp.getChild(UIControlWorkspace.class);
      UIControlWSWorkingArea uiWorking = uiControl.getChildById(UIControlWorkspace.WORKING_AREA_ID);
      uiWorking.setUIComponent(uiWorking.createUIComponent(UIWizardPageCreationBar.class, null, null));      
      uiApp.setEditting(true) ;
      UIWorkingWorkspace uiWorkingWS = uiApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
      uiWorkingWS.setRenderedChild(UIPortalToolPanel.class) ;
      UIPortalToolPanel uiToolPanel = uiWorkingWS.getChild(UIPortalToolPanel.class) ;
      uiToolPanel.setShowMaskLayer(false);
      portalContext.addUIComponentToUpdateByAjax(uiWorkingWS);
      uiToolPanel.setWorkingComponent(UIPageCreationWizard.class, null) ;
      UIPageCreationWizard uiWizard = (UIPageCreationWizard) uiToolPanel.getUIComponent() ;
      UIWizardPageSetInfo uiPageSetInfo = uiWizard.getChild(UIWizardPageSetInfo.class);
      uiPageSetInfo.setShowPublicationDate(false) ;
      uiWorking.setUIComponent(uiWorking.createUIComponent(UIWelcomeComponent.class, null, null));      
    }
  }
  
  public static class EditPageActionListener extends EventListener<UISiteAdminToolbar> {
    public void execute(Event<UISiteAdminToolbar> event) throws Exception {        
      PortalRequestContext portalContext = Util.getPortalRequestContext();   
      event.setRequestContext(Util.getPortalRequestContext());
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      uiApp.setEditting(true);
      UIWorkingWorkspace uiWorkingWS = uiApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
      uiWorkingWS.setRenderedChild(UIPortalToolPanel.class) ;
      UIPortalToolPanel uiToolPanel = uiWorkingWS.getChild(UIPortalToolPanel.class) ;
      uiToolPanel.setShowMaskLayer(false);
      portalContext.addUIComponentToUpdateByAjax(uiWorkingWS);     
      uiToolPanel.setWorkingComponent(UIPageEditWizard.class, null);
      UIPageEditWizard uiWizard = (UIPageEditWizard)uiToolPanel.getUIComponent();
      uiWizard.setDescriptionWizard(1);
      UIWizardPageSetInfo uiPageSetInfo = uiWizard.getChild(UIWizardPageSetInfo.class);
      uiPageSetInfo.setEditMode();      
      uiPageSetInfo.createEvent("ChangeNode", Event.Phase.DECODE, event.getRequestContext()).broadcast();
    }
  }

  public static class CreatePortalActionListener extends EventListener<UISiteAdminToolbar> {
    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
      event.setRequestContext(Util.getPortalRequestContext());
      PortalRequestContext portalContext = Util.getPortalRequestContext();
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      UserACL userACL = uiApp.getApplicationComponent(UserACL.class);
      if (!userACL.hasCreatePortalPermission(portalContext.getRemoteUser())) {
        uiApp.addMessage(new ApplicationMessage("UIPortalBrowser.msg.Invalid-createPermission",
            null));
        portalContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
      UIPortalForm uiNewPortal = uiMaskWS.createUIComponent(UIPortalForm.class, "CreatePortal", "UIPortalForm");
      uiMaskWS.setUIComponent(uiNewPortal);
      uiMaskWS.setShow(true);
      portalContext.addUIComponentToUpdateByAjax(uiMaskWS);
    }
  }
  
  public static class EditPortalActionListener extends EventListener<UISiteAdminToolbar> {
    public void execute(Event<UISiteAdminToolbar> event) throws Exception {      
      event.setRequestContext(Util.getPortalRequestContext());
      UIPortal uiPortal = Util.getUIPortal();      
      UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
      if (!uiPortal.isModifiable()) {        
        uiPortalApp.addMessage(new ApplicationMessage("UIPortalManagement.msg.Invalid-editPermission", new String[]{uiPortal.getName()}));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortalApp.getUIPopupMessages());
        return ;
      }      
      UIControlWorkspace uiControlWorkspace = uiPortalApp.getChild(UIControlWorkspace.class);
      UIControlWSWorkingArea uiControlWSWorkingArea = uiControlWorkspace.getChildById(UIControlWorkspace.WORKING_AREA_ID);
      uiControlWSWorkingArea.setUIComponent(uiControlWSWorkingArea.createUIComponent(UIPortalManagement.class, null, null));      
      PortalRequestContext pcontext = Util.getPortalRequestContext();      
      ((UIPortalApplication)pcontext.getUIApplication()).setEditting(true);
      UIPortalManagement uiManagement = (UIPortalManagement)uiControlWSWorkingArea.getUIComponent();      
      uiManagement.setMode(ManagementMode.EDIT, event);
    }
  }

  public static class ChangePortalActionListener extends EventListener<UISiteAdminToolbar> {
    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
      event.setRequestContext(Util.getPortalRequestContext());
      PortalRequestContext portalContext = Util.getPortalRequestContext();
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
      UIPortalSelector uiPortalSelector = uiMaskWS.createUIComponent(UIPortalSelector.class, null,
          null);
      uiMaskWS.setUIComponent(uiPortalSelector);
      uiMaskWS.setShow(true);
      portalContext.addUIComponentToUpdateByAjax(uiMaskWS);      
    }
  }

}
