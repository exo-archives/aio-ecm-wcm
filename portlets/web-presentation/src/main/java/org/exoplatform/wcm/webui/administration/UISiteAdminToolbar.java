package org.exoplatform.wcm.webui.administration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.portlet.PortletMode;
import javax.servlet.http.HttpServletRequest;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.application.Preference;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.UIWelcomeComponent;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.navigation.PageNavigationUtils;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.page.UIPageCreationWizard;
import org.exoplatform.portal.webui.page.UIPageEditWizard;
import org.exoplatform.portal.webui.page.UIWizardPageCreationBar;
import org.exoplatform.portal.webui.page.UIWizardPageSetInfo;
import org.exoplatform.portal.webui.portal.PageNodeEvent;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.portal.UIPortalForm;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIControlWorkspace;
import org.exoplatform.portal.webui.workspace.UIExoStart;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIPortalToolPanel;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.portal.webui.workspace.UIControlWorkspace.UIControlWSWorkingArea;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
    @EventConfig(listeners = UISiteAdminToolbar.AddContentActionListener.class),
    @EventConfig(listeners = UISiteAdminToolbar.BrowsePortalActionListener.class),
    @EventConfig(listeners = UISiteAdminToolbar.BrowsePageActionListener.class),
    @EventConfig(listeners = UISiteAdminToolbar.EditPageAndNavigationActionListener.class),
    @EventConfig(listeners = UISiteAdminToolbar.ChangePageActionListener.class),
    @EventConfig(listeners = UISiteAdminToolbar.TurnOnQuickEditActionListener.class),
    @EventConfig(listeners = UISiteAdminToolbar.TurnOffQuickEditActionListener.class) })
public class UISiteAdminToolbar extends UIContainer {

  /** The Constant MESSAGE. */
  public static final String MESSAGE            = "UISiteAdminToolbar.msg.not-permission";

  public static final String TURN_ON_QUICK_EDIT = "isTurnOn";

  public static final int ADMIN              = 2;

  public static final int EDITOR             = 1;

  public static final int REDACTOR           = 0;

  /**
   * Instantiates a new uI site admin toolbar.
   */
  public UISiteAdminToolbar() {
  }

  public int getRole() throws Exception {
    String userId = Util.getPortalRequestContext().getRemoteUser();
    UserACL userACL = getApplicationComponent(UserACL.class);
    IdentityRegistry identityRegistry = getApplicationComponent(IdentityRegistry.class);
    WCMConfigurationService wcmConfigurationService = getApplicationComponent(WCMConfigurationService.class);
    Identity identity = identityRegistry.getIdentity(userId);
    String editorMembershiptType = userACL.getMakableMT();
    List<String> accessControlWorkspaceGroups = userACL.getAccessControlWorkspaceGroups();
    String editSitePermission = Util.getUIPortal().getEditPermission();
    String redactorMembershipType = wcmConfigurationService.getRedactorMembershipType();   
    // admin
    if (userACL.getSuperUser().equals(userId)) {
      return UISiteAdminToolbar.ADMIN;
    }
    if (userACL.hasAccessControlWorkspacePermission(userId)
        && userACL.hasCreatePortalPermission(userId)) {
      return UISiteAdminToolbar.ADMIN;
    }
    // editor
    MembershipEntry editorEnry = null;
    for (String membership : accessControlWorkspaceGroups) {
      editorEnry = MembershipEntry.parse(membership);
      if (editorEnry.getMembershipType().equals(editorMembershiptType)
          || editorEnry.getMembershipType().equals(MembershipEntry.ANY_TYPE)) {
        if (identity.isMemberOf(editorEnry))
          return UISiteAdminToolbar.EDITOR;
      }
    }
    // redactor
    MembershipEntry redactorEnry = MembershipEntry.parse(editSitePermission);
    if (redactorEnry.getMembershipType().equals(redactorMembershipType)
        || redactorEnry.getMembershipType().equals(MembershipEntry.ANY_TYPE)) {
      if (identity.isMemberOf(redactorEnry))
        return UISiteAdminToolbar.REDACTOR;
    }

    return -1;
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

  public String getBaseURI(String portalName) {
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();    
    HttpServletRequest servletRequest = portalRequestContext.getRequest();
    String baseURI = servletRequest.getScheme() + "://" + servletRequest.getServerName() + ":"
        + servletRequest.getServerPort() + servletRequest.getContextPath() + "/private/"
        + portalName;
    return baseURI;
  }

  public List<String> getAllPortals() throws Exception {
    List<String> portals = new ArrayList<String>();
    DataStorage dataStorage = getApplicationComponent(DataStorage.class);
    Query<PortalConfig> query = new Query<PortalConfig>(null, null, null, null, PortalConfig.class) ;
    PageList pageList = dataStorage.find(query) ;
    String userId = Util.getPortalRequestContext().getRemoteUser();
    UserACL userACL = getApplicationComponent(UserACL.class) ;
    Iterator<?> itr = pageList.getAll().iterator();
    while(itr.hasNext()) {
      PortalConfig pConfig = (PortalConfig)itr.next() ;
      if(userACL.hasPermission(pConfig, userId)) {
        portals.add(pConfig.getName());
      }
    }     
    return portals;
  }

  public List<PageNavigation> getGroupNavigations() throws Exception {
    String removeUser = Util.getPortalRequestContext().getRemoteUser();
    List<PageNavigation> allNavigations = Util.getUIPortal().getNavigations();
    List<PageNavigation> groupNavigations = new ArrayList<PageNavigation>();
    for (PageNavigation navigation : allNavigations) {      
      if (navigation.getOwnerType().equals(PortalConfig.GROUP_TYPE)) {
        groupNavigations.add(PageNavigationUtils.filter(navigation, removeUser));
      }
    }
    return groupNavigations;
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
      uiWizard.setDescriptionWizard(2);
      uiWizard.viewStep(2);
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
      uiWizard.setDescriptionWizard(2);
      uiWizard.viewStep(3);
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
      uiControlWorkspace.getChild(UIExoStart.class)
                        .createEvent("EditPortal", Event.Phase.PROCESS, event.getRequestContext())
                        .broadcast();
    }
  }

  /**
   * The listener interface for receiving browsePortalAction events. The class
   * that is interested in processing a browsePortalAction event implements this
   * interface, and the object created with that class is registered with a
   * component using the component's
   * <code>addBrowsePortalActionListener<code> method. When
   * the browsePortalAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see BrowsePortalActionEvent
   */
  public static class BrowsePortalActionListener extends EventListener<UISiteAdminToolbar> {
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
      UIExoStart uiExoStart = uiControlWorkspace.getChild(UIExoStart.class);
      uiExoStart.createEvent("BrowsePortal", Event.Phase.PROCESS, event.getRequestContext())
                .broadcast();
    }
  }

  /**
   * The listener interface for receiving browsePageAction events. The class
   * that is interested in processing a browsePageAction event implements this
   * interface, and the object created with that class is registered with a
   * component using the component's
   * <code>addBrowsePageActionListener<code> method. When
   * the browsePageAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see BrowsePageActionEvent
   */
  public static class BrowsePageActionListener extends EventListener<UISiteAdminToolbar> {
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
      UIExoStart uiExoStart = uiControlWorkspace.getChild(UIExoStart.class);
      uiExoStart.createEvent("BrowsePage", Event.Phase.PROCESS, event.getRequestContext())
                .broadcast();
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
    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
      event.setRequestContext(Util.getPortalRequestContext());
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      UIControlWorkspace uiControlWorkspace = uiApp.getChild(UIControlWorkspace.class);
      UIExoStart uiExoStart = uiControlWorkspace.getChild(UIExoStart.class);
      uiExoStart.createEvent("ChangePortal", Event.Phase.PROCESS, event.getRequestContext())
                .broadcast();
    }
  }

  /**
   * The listener interface for receiving skinSettingsAction events. The class
   * that is interested in processing a skinSettingsAction event implements this
   * interface, and the object created with that class is registered with a
   * component using the component's
   * <code>addSkinSettingsActionListener<code> method. When
   * the skinSettingsAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SkinSettingsActionEvent
   */
  public static class SkinSettingsActionListener extends EventListener<UISiteAdminToolbar> {
    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
      event.setRequestContext(Util.getPortalRequestContext());
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      UIControlWorkspace uiControlWorkspace = uiApp.getChild(UIControlWorkspace.class);
      UIExoStart uiExoStart = uiControlWorkspace.getChild(UIExoStart.class);
      uiExoStart.createEvent("SkinSettings", Event.Phase.PROCESS, event.getRequestContext())
                .broadcast();
    }
  }

  /**
   * The listener interface for receiving languageSettingsAction events. The
   * class that is interested in processing a languageSettingsAction event
   * implements this interface, and the object created with that class is
   * registered with a component using the component's
   * <code>addLanguageSettingsActionListener<code> method. When
   * the languageSettingsAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see LanguageSettingsActionEvent
   */
  public static class LanguageSettingsActionListener extends EventListener<UISiteAdminToolbar> {
    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
      event.setRequestContext(Util.getPortalRequestContext());
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      UIControlWorkspace uiControlWorkspace = uiApp.getChild(UIControlWorkspace.class);
      UIExoStart uiExoStart = uiControlWorkspace.getChild(UIExoStart.class);
      uiExoStart.createEvent("LanguageSettings", Event.Phase.PROCESS, event.getRequestContext())
                .broadcast();
    }
  }

  /**
   * The listener interface for receiving accountSettingsAction events. The
   * class that is interested in processing a accountSettingsAction event
   * implements this interface, and the object created with that class is
   * registered with a component using the component's
   * <code>addAccountSettingsActionListener<code> method. When
   * the accountSettingsAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see AccountSettingsActionEvent
   */
  public static class AccountSettingsActionListener extends EventListener<UISiteAdminToolbar> {
    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
      event.setRequestContext(Util.getPortalRequestContext());
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      UIControlWorkspace uiControlWorkspace = uiApp.getChild(UIControlWorkspace.class);
      UIExoStart uiExoStart = uiControlWorkspace.getChild(UIExoStart.class);
      uiExoStart.createEvent("AccountSettings", Event.Phase.PROCESS, event.getRequestContext())
                .broadcast();
    }
  }

  /**
   * The listener interface for receiving editPageAndNavigationAction events.
   * The class that is interested in processing a editPageAndNavigationAction
   * event implements this interface, and the object created with that class is
   * registered with a component using the component's
   * <code>addEditPageAndNavigationActionListener<code> method. When
   * the editPageAndNavigationAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see EditPageAndNavigationActionEvent
   */
  public static class EditPageAndNavigationActionListener extends EventListener<UISiteAdminToolbar> {
    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
      event.setRequestContext(Util.getPortalRequestContext());
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      UIControlWorkspace uiControlWorkspace = uiApp.getChild(UIControlWorkspace.class);
      UIExoStart uiExoStart = uiControlWorkspace.getChild(UIExoStart.class);
      uiExoStart.createEvent("EditPage", Event.Phase.PROCESS, event.getRequestContext())
                .broadcast();
    }
  }

  public static class ChangePageActionListener extends EventListener<UISiteAdminToolbar> {
    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
      String uri = event.getRequestContext().getRequestParameter(OBJECTID);
      System.out.println("\n\nuri: " + uri);
      UIPortal uiPortal = Util.getUIPortal();
      uiPortal.setMode(UIPortal.COMPONENT_VIEW_MODE);
      PageNodeEvent<UIPortal> pnevent = new PageNodeEvent<UIPortal>(uiPortal,
                                                                    PageNodeEvent.CHANGE_PAGE_NODE,
                                                                    uri);
      uiPortal.broadcast(pnevent, Event.Phase.PROCESS);
    }
  }

  /**
   * The listener interface for receiving addContentAction events. The class
   * that is interested in processing a addContentAction event implements this
   * interface, and the object created with that class is registered with a
   * component using the component's
   * <code>addAddContentActionListener<code> method. When
   * the addContentAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see AddContentActionEvent
   */
  public static class AddContentActionListener extends EventListener<UISiteAdminToolbar> {
    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
      UISiteAdminToolbar siteAdminToolbar = event.getSource();
      UIPortal uiPortal = Util.getUIPortal();
      // SessionProvider currentSessionProvider =
      // SessionProviderFactory.createSessionProvider();
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

      Preference preferenceP = new Preference();
      listValue = new ArrayList<String>();
      listValue.add("true");
      preferenceP.setName("ShowPrintAction");
      preferenceP.setValues(listValue);
      listPreference.add(preferenceP);

      portletPreferences.setPreferences(listPreference);

      DataStorage dataStorage = siteAdminToolbar.getApplicationComponent(DataStorage.class);
      dataStorage.save(portletPreferences);

      // Add portlet to page
      UserPortalConfigService userPortalConfigService = siteAdminToolbar.getApplicationComponent(UserPortalConfigService.class);
      Page page = userPortalConfigService.getPage(uiPortal.getSelectedNode().getPageReference(),
                                                  Util.getPortalRequestContext().getRemoteUser());
      ArrayList<Object> listPortlet = page.getChildren();
      listPortlet.add(PortalDataMapper.toPortletModel(uiPortlet));
      page.setChildren(listPortlet);
      userPortalConfigService.update(page);
      UIPage uiPage = uiPortal.findFirstComponentOfType(UIPage.class);
      uiPage.setChildren(null);
      PortalDataMapper.toUIPage(uiPage, page);
      ((UIPortlet) uiPage.findComponentById(random)).setCurrentPortletMode(PortletMode.EDIT);
      Utils.refreshBrowser((PortletRequestContext) event.getRequestContext());
    }
  }

  public static class TurnOnQuickEditActionListener extends EventListener<UISiteAdminToolbar> {
    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
      PortalRequestContext context = Util.getPortalRequestContext();
      context.getRequest().getSession().setAttribute(Utils.TURN_ON_QUICK_EDIT, true);
      Utils.refreshBrowser((PortletRequestContext) event.getRequestContext());
    }
  }

  public static class TurnOffQuickEditActionListener extends EventListener<UISiteAdminToolbar> {
    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
      PortalRequestContext context = Util.getPortalRequestContext();
      context.getRequest().getSession().setAttribute(Utils.TURN_ON_QUICK_EDIT, false);
      Utils.refreshBrowser((PortletRequestContext) event.getRequestContext());
    }
  }

}
