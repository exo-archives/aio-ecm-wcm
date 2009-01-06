package org.exoplatform.wcm.webui.administration;

import java.util.ArrayList;

import javax.faces.component.UIComponent;
import javax.portlet.PortletMode;

import org.exoplatform.portal.account.UIAccountSetting;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.application.Preference;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.UIWelcomeComponent;
import org.exoplatform.portal.webui.UIManagement.ManagementMode;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.page.UIPageCreationWizard;
import org.exoplatform.portal.webui.page.UIPageEditWizard;
import org.exoplatform.portal.webui.page.UIWizardPageCreationBar;
import org.exoplatform.portal.webui.page.UIWizardPageSetInfo;
import org.exoplatform.portal.webui.portal.UILanguageSelector;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.portal.UIPortalForm;
import org.exoplatform.portal.webui.portal.UIPortalManagement;
import org.exoplatform.portal.webui.portal.UIPortalSelector;
import org.exoplatform.portal.webui.portal.UISkinSelector;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIControlWorkspace;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIPortalToolPanel;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.portal.webui.workspace.UIControlWorkspace.UIControlWSWorkingArea;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.portletcontainer.pci.ExoWindowID;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.w3c.tidy.Out;

// TODO: Auto-generated Javadoc
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

/**
 * The Class UISiteAdminToolbar.
 */
@ComponentConfig(template = "app:/groovy/SiteAdministration/UISiteAdminToolBar.gtmpl", events = {
    @EventConfig(listeners = UISiteAdminToolbar.AddPageActionListener.class),
    @EventConfig(listeners = UISiteAdminToolbar.EditPageActionListener.class),
    @EventConfig(listeners = UISiteAdminToolbar.EditPortalActionListener.class),
    @EventConfig(listeners = UISiteAdminToolbar.CreatePortalActionListener.class),
    @EventConfig(listeners = UISiteAdminToolbar.ChangePortalActionListener.class),
    @EventConfig(listeners = UISiteAdminToolbar.SkinSettingsActionListener.class),
    @EventConfig(listeners = UISiteAdminToolbar.LanguageSettingsActionListener.class),
    @EventConfig(listeners = UISiteAdminToolbar.AccountSettingsActionListener.class),
    @EventConfig(listeners = UISiteAdminToolbar.AddContentActionListener.class)
})
public class UISiteAdminToolbar extends UIContainer {

  /** The Constant MESSAGE. */
  public static final String MESSAGE = "UISiteAdminToolbar.msg.not-permission";

  /**
   * Instantiates a new uI site admin toolbar.
   */
  public UISiteAdminToolbar() {
  }

  /**
   * Checks if is show workspace area.
   * 
   * @return true, if is show workspace area
   * @throws Exception the exception
   */
  public boolean isShowWorkspaceArea() throws Exception {
    UserACL userACL = getApplicationComponent(UserACL.class);
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    String userId = context.getRemoteUser();
    if (userACL.hasAccessControlWorkspacePermission(userId))
      return true;
    return false;
  }

  /**
   * The listener interface for receiving addPageAction events. The class that
   * is interested in processing a addPageAction event implements this
   * interface, and the object created with that class is registered with a
   * component using the component's
   * <code>addAddPageActionListener<code> method. When
   * the addPageAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see AddPageActionEvent
   */
  public static class AddPageActionListener extends EventListener<UISiteAdminToolbar> {

    /*
     * (non-Javadoc)
     * @see
     * org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui
     * .event.Event)
     */
    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      PortalRequestContext portalContext = Util.getPortalRequestContext();
      event.setRequestContext(Util.getPortalRequestContext());
      UserACL userACL = uiApp.getApplicationComponent(UserACL.class);
      String remoteUser = portalContext.getRemoteUser();
      if (!userACL.hasAccessControlWorkspacePermission(remoteUser)) {
        uiApp.addMessage(new ApplicationMessage(UISiteAdminToolbar.MESSAGE, null));
        portalContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      UIControlWorkspace uiControl = uiApp.getChild(UIControlWorkspace.class);
      UIControlWSWorkingArea uiWorking = uiControl.getChildById(UIControlWorkspace.WORKING_AREA_ID);
      uiWorking.setUIComponent(uiWorking.createUIComponent(UIWizardPageCreationBar.class,
                                                           null,
                                                           null));
      uiApp.setEditting(true);
      UIWorkingWorkspace uiWorkingWS = uiApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
      uiWorkingWS.setRenderedChild(UIPortalToolPanel.class);
      UIPortalToolPanel uiToolPanel = uiWorkingWS.getChild(UIPortalToolPanel.class);
      uiToolPanel.setShowMaskLayer(false);
      portalContext.addUIComponentToUpdateByAjax(uiWorkingWS);
      uiToolPanel.setWorkingComponent(UIPageCreationWizard.class, null);
      UIPageCreationWizard uiWizard = (UIPageCreationWizard) uiToolPanel.getUIComponent();
      UIWizardPageSetInfo uiPageSetInfo = uiWizard.getChild(UIWizardPageSetInfo.class);
      uiPageSetInfo.setShowPublicationDate(false);
      uiWorking.setUIComponent(uiWorking.createUIComponent(UIWelcomeComponent.class, null, null));
    }
  }

  /**
   * The listener interface for receiving editPageAction events. The class that
   * is interested in processing a editPageAction event implements this
   * interface, and the object created with that class is registered with a
   * component using the component's
   * <code>addEditPageActionListener<code> method. When
   * the editPageAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see EditPageActionEvent
   */
  public static class EditPageActionListener extends EventListener<UISiteAdminToolbar> {

    /*
     * (non-Javadoc)
     * @see
     * org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui
     * .event.Event)
     */
    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
      PortalRequestContext portalContext = Util.getPortalRequestContext();
      event.setRequestContext(Util.getPortalRequestContext());
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      UserACL userACL = uiApp.getApplicationComponent(UserACL.class);
      String remoteUser = portalContext.getRemoteUser();
      UIPortal uiPortal = Util.getUIPortal();
      String pageId = uiPortal.getSelectedNode().getPageReference();
      UserPortalConfigService portalConfigService = uiApp.getApplicationComponent(UserPortalConfigService.class);
      Page currentPage = portalConfigService.getPage(pageId, remoteUser);
      if (!userACL.hasAccessControlWorkspacePermission(remoteUser)
          || !userACL.hasEditPermission(currentPage, remoteUser)) {
        uiApp.addMessage(new ApplicationMessage(UISiteAdminToolbar.MESSAGE, null));
        portalContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      uiApp.setEditting(true);
      UIWorkingWorkspace uiWorkingWS = uiApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
      uiWorkingWS.setRenderedChild(UIPortalToolPanel.class);
      UIPortalToolPanel uiToolPanel = uiWorkingWS.getChild(UIPortalToolPanel.class);
      uiToolPanel.setShowMaskLayer(false);
      portalContext.addUIComponentToUpdateByAjax(uiWorkingWS);
      uiToolPanel.setWorkingComponent(UIPageEditWizard.class, null);
      UIPageEditWizard uiWizard = (UIPageEditWizard) uiToolPanel.getUIComponent();
      uiWizard.setDescriptionWizard(1);
      UIWizardPageSetInfo uiPageSetInfo = uiWizard.getChild(UIWizardPageSetInfo.class);
      uiPageSetInfo.setEditMode();
      uiPageSetInfo.createEvent("ChangeNode", Event.Phase.DECODE, event.getRequestContext())
                   .broadcast();
    }
  }

  /**
   * The listener interface for receiving createPortalAction events. The class
   * that is interested in processing a createPortalAction event implements this
   * interface, and the object created with that class is registered with a
   * component using the component's
   * <code>addCreatePortalActionListener<code> method. When
   * the createPortalAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see CreatePortalActionEvent
   */
  public static class CreatePortalActionListener extends EventListener<UISiteAdminToolbar> {

    /*
     * (non-Javadoc)
     * @see
     * org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui
     * .event.Event)
     */
    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
      event.setRequestContext(Util.getPortalRequestContext());
      PortalRequestContext portalContext = Util.getPortalRequestContext();
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      UserACL userACL = uiApp.getApplicationComponent(UserACL.class);
      if (!userACL.hasCreatePortalPermission(portalContext.getRemoteUser())) {
        uiApp.addMessage(new ApplicationMessage(UISiteAdminToolbar.MESSAGE, null));
        portalContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
      UIPortalForm uiNewPortal = uiMaskWS.createUIComponent(UIPortalForm.class,
                                                            "CreatePortal",
                                                            "UIPortalForm");
      uiMaskWS.setUIComponent(uiNewPortal);
      uiMaskWS.setShow(true);
      portalContext.addUIComponentToUpdateByAjax(uiMaskWS);
    }
  }

  /**
   * The listener interface for receiving editPortalAction events. The class
   * that is interested in processing a editPortalAction event implements this
   * interface, and the object created with that class is registered with a
   * component using the component's
   * <code>addEditPortalActionListener<code> method. When
   * the editPortalAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see EditPortalActionEvent
   */
  public static class EditPortalActionListener extends EventListener<UISiteAdminToolbar> {

    /*
     * (non-Javadoc)
     * @see
     * org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui
     * .event.Event)
     */
    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
      event.setRequestContext(Util.getPortalRequestContext());
      PortalRequestContext portalContext = Util.getPortalRequestContext();
      UIPortal uiPortal = Util.getUIPortal();
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      UserACL userACL = uiApp.getApplicationComponent(UserACL.class);
      String remoteUser = portalContext.getRemoteUser();
      if (!uiPortal.isModifiable() || !userACL.hasCreatePortalPermission(remoteUser)
          || !Utils.canEditCurrentPortal(remoteUser)) {
        uiApp.addMessage(new ApplicationMessage(UISiteAdminToolbar.MESSAGE,
                                                new String[] { uiPortal.getName() }));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      UIControlWorkspace uiControlWorkspace = uiApp.getChild(UIControlWorkspace.class);
      UIControlWSWorkingArea uiControlWSWorkingArea = uiControlWorkspace.getChildById(UIControlWorkspace.WORKING_AREA_ID);
      uiControlWSWorkingArea.setUIComponent(uiControlWSWorkingArea.createUIComponent(UIPortalManagement.class,
                                                                                     null,
                                                                                     null));
      PortalRequestContext pcontext = Util.getPortalRequestContext();
      ((UIPortalApplication) pcontext.getUIApplication()).setEditting(true);
      UIPortalManagement uiManagement = (UIPortalManagement) uiControlWSWorkingArea.getUIComponent();
      uiManagement.setMode(ManagementMode.EDIT, event);
    }
  }

  /**
   * The listener interface for receiving changePortalAction events. The class
   * that is interested in processing a changePortalAction event implements this
   * interface, and the object created with that class is registered with a
   * component using the component's
   * <code>addChangePortalActionListener<code> method. When
   * the changePortalAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see ChangePortalActionEvent
   */
  public static class ChangePortalActionListener extends EventListener<UISiteAdminToolbar> {

    /*
     * (non-Javadoc)
     * @see
     * org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui
     * .event.Event)
     */
    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
      event.setRequestContext(Util.getPortalRequestContext());
      PortalRequestContext portalContext = Util.getPortalRequestContext();
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
      UIPortalSelector uiPortalSelector = uiMaskWS.createUIComponent(UIPortalSelector.class,
                                                                     null,
                                                                     null);
      uiMaskWS.setUIComponent(uiPortalSelector);
      uiMaskWS.setShow(true);
      portalContext.addUIComponentToUpdateByAjax(uiMaskWS);
    }
  }

  public static class SkinSettingsActionListener extends EventListener<UISiteAdminToolbar> {
    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
      event.setRequestContext(Util.getPortalRequestContext());
      PortalRequestContext portalContext = Util.getPortalRequestContext();
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
      UISkinSelector uiSkinSelector = uiMaskWS.createUIComponent(UISkinSelector.class, null, null);
      uiMaskWS.setUIComponent(uiSkinSelector);
      uiMaskWS.setShow(true);
      portalContext.addUIComponentToUpdateByAjax(uiMaskWS);
    }
  }

  public static class LanguageSettingsActionListener extends EventListener<UISiteAdminToolbar> {
    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
      event.setRequestContext(Util.getPortalRequestContext());
      PortalRequestContext portalContext = Util.getPortalRequestContext();
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
      UILanguageSelector uiLanguageSelector = uiMaskWS.createUIComponent(UILanguageSelector.class, null, null);
      uiMaskWS.setUIComponent(uiLanguageSelector);
      uiMaskWS.setShow(true);
      portalContext.addUIComponentToUpdateByAjax(uiMaskWS);
    }
  }
  
  public static class AccountSettingsActionListener extends EventListener<UISiteAdminToolbar> {
    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
      event.setRequestContext(Util.getPortalRequestContext());
      PortalRequestContext portalContext = Util.getPortalRequestContext();
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
      UIAccountSetting uiAccountSetting = uiMaskWS.createUIComponent(UIAccountSetting.class, null, null);
      uiMaskWS.setUIComponent(uiAccountSetting);
      uiMaskWS.setShow(true);
      portalContext.addUIComponentToUpdateByAjax(uiMaskWS);
    }
  }
  
  public static class AddContentActionListener extends EventListener<UISiteAdminToolbar> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
      UISiteAdminToolbar siteAdminToolbar = event.getSource();
      UIPortal uiPortal = Util.getUIPortal();
//      SessionProvider currentSessionProvider = SessionProviderFactory.createSessionProvider();
      UIPortlet uiPortlet = new UIPortlet();
      uiPortlet.setShowInfoBar(false);

      // Create portlet
      WCMConfigurationService configurationService = siteAdminToolbar.getApplicationComponent(WCMConfigurationService.class);
      StringBuilder windowId = new StringBuilder();
      String random = IdGenerator.generate();
      windowId.append(PortalConfig.PORTAL_TYPE)
              .append("#")
              .append(uiPortal.getOwner())
              .append(":")
              .append(configurationService.getPublishingPortletName())
              .append("/")
              .append(random);
      uiPortlet.setWindowId(windowId.toString());

      // Add preferences to portlet
      PortletPreferences portletPreferences = new PortletPreferences();
      portletPreferences.setWindowId(windowId.toString());
      portletPreferences.setOwnerType(PortalConfig.PORTAL_TYPE);
      portletPreferences.setOwnerId(org.exoplatform.portal.webui.util.Util.getUIPortal().getOwner());
      ArrayList<Preference> listPreference = new ArrayList<Preference>();

      Preference preferenceR = new Preference();
      ArrayList<String> listValue = new ArrayList<String>();
      listValue.add("repository");
      preferenceR.setName("repository");
      preferenceR.setValues(listValue);
      listPreference.add(preferenceR);

      Preference preferenceW = new Preference();
      listValue = new ArrayList<String>();
      listValue.add("collaboration");
      preferenceW.setName("workspace");
      preferenceW.setValues(listValue);
      listPreference.add(preferenceW);

      Preference preferenceQ = new Preference();
      listValue = new ArrayList<String>();
      listValue.add("true");
      preferenceQ.setName("ShowQuickEdit");
      preferenceQ.setValues(listValue);
      listPreference.add(preferenceQ);
      
      portletPreferences.setPreferences(listPreference);

      DataStorage dataStorage = siteAdminToolbar.getApplicationComponent(DataStorage.class);
      dataStorage.save(portletPreferences);

      // Add portlet to page
      UserPortalConfigService userPortalConfigService = siteAdminToolbar.getApplicationComponent(UserPortalConfigService.class);
      Page page = userPortalConfigService.getPage(uiPortal.getSelectedNode().getPageReference(), Util.getPortalRequestContext().getRemoteUser());
      ArrayList<Object> listPortlet = page.getChildren();
      listPortlet.add(PortalDataMapper.toPortletModel(uiPortlet));
      page.setChildren(listPortlet);
      userPortalConfigService.update(page);
      UIPage uiPage = uiPortal.findFirstComponentOfType(UIPage.class);
      uiPage.setChildren(null);
      PortalDataMapper.toUIPage(uiPage, page);
      ((UIPortlet)uiPage.findComponentById(random)).setCurrentPortletMode(PortletMode.EDIT);
      Utils.refreshBrowser((PortletRequestContext) event.getRequestContext());
    }
  }
}
