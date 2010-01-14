///*
// * Copyright (C) 2003-2008 eXo Platform SAS.
// *
// * This program is free software; you can redistribute it and/or
// * modify it under the terms of the GNU Affero General Public License
// * as published by the Free Software Foundation; either version 3
// * of the License, or (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program; if not, see<http://www.gnu.org/licenses/>.
// */
//package org.exoplatform.wcm.webui.administration;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.Iterator;
//import java.util.List;
//import java.util.ResourceBundle;
//
//import javax.portlet.PortletMode;
//import javax.portlet.PortletRequest;
//import javax.servlet.http.HttpServletRequest;
//
//import org.exoplatform.commons.utils.PageList;
//import org.exoplatform.portal.application.PortalRequestContext;
//import org.exoplatform.portal.application.PortletPreferences;
//import org.exoplatform.portal.application.Preference;
//import org.exoplatform.portal.config.DataStorage;
//import org.exoplatform.portal.config.Query;
//import org.exoplatform.portal.config.UserACL;
//import org.exoplatform.portal.config.UserPortalConfigService;
//import org.exoplatform.portal.config.model.Page;
//import org.exoplatform.portal.config.model.PageNavigation;
//import org.exoplatform.portal.config.model.PageNode;
//import org.exoplatform.portal.config.model.PortalConfig;
//import org.exoplatform.portal.webui.UIWelcomeComponent;
//import org.exoplatform.portal.webui.application.UIPortlet;
//import org.exoplatform.portal.webui.container.UIContainer;
//import org.exoplatform.portal.webui.navigation.PageNavigationUtils;
//import org.exoplatform.portal.webui.page.UIPage;
//import org.exoplatform.portal.webui.page.UIPageBrowser;
//import org.exoplatform.portal.webui.page.UIPageCreationWizard;
//import org.exoplatform.portal.webui.page.UIPageEditWizard;
//import org.exoplatform.portal.webui.page.UIWizardPageCreationBar;
//import org.exoplatform.portal.webui.page.UIWizardPageSetInfo;
//import org.exoplatform.portal.webui.portal.PageNodeEvent;
//import org.exoplatform.portal.webui.portal.UIPortal;
//import org.exoplatform.portal.webui.portal.UIPortalForm;
//import org.exoplatform.portal.webui.portal.UISkinSelector;
//import org.exoplatform.portal.webui.util.PortalDataMapper;
//import org.exoplatform.portal.webui.util.Util;
//import org.exoplatform.portal.webui.workspace.UIControlWorkspace;
//import org.exoplatform.portal.webui.workspace.UIExoStart;
//import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
//import org.exoplatform.portal.webui.workspace.UIPortalApplication;
//import org.exoplatform.portal.webui.workspace.UIPortalToolPanel;
//import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
//import org.exoplatform.portal.webui.workspace.UIControlWorkspace.UIControlWSWorkingArea;
//import org.exoplatform.services.jcr.util.IdGenerator;
//import org.exoplatform.services.resources.LocaleConfig;
//import org.exoplatform.services.resources.LocaleConfigService;
//import org.exoplatform.services.security.Identity;
//import org.exoplatform.services.security.IdentityRegistry;
//import org.exoplatform.services.security.MembershipEntry;
//import org.exoplatform.services.wcm.core.WCMConfigurationService;
//import org.exoplatform.wcm.webui.Utils;
//import org.exoplatform.web.application.ApplicationMessage;
//import org.exoplatform.webui.application.WebuiApplication;
//import org.exoplatform.webui.application.WebuiRequestContext;
//import org.exoplatform.webui.application.portlet.PortletRequestContext;
//import org.exoplatform.webui.config.annotation.ComponentConfig;
//import org.exoplatform.webui.config.annotation.EventConfig;
//import org.exoplatform.webui.event.Event;
//import org.exoplatform.webui.event.EventListener;
//
///*
// * Created by The eXo Platform SAS
// * Author : Anh Do Ngoc
// *          anh.do@exoplatform.com
// * Oct 6, 2008  
// */
//@ComponentConfig(template = "app:/groovy/SiteAdministration/UISiteAdminToolBar.gtmpl", events = {
//    @EventConfig(listeners = UISiteAdminToolbar.AddPageActionListener.class),
//    @EventConfig(listeners = UISiteAdminToolbar.EditPageActionListener.class),
//    @EventConfig(listeners = UISiteAdminToolbar.EditPortalActionListener.class),
//    @EventConfig(listeners = UISiteAdminToolbar.CreatePortalActionListener.class),
//    @EventConfig(listeners = UISiteAdminToolbar.ChangePortalActionListener.class),
//    @EventConfig(listeners = UISiteAdminToolbar.SkinSettingsActionListener.class),
//    @EventConfig(listeners = UISiteAdminToolbar.LanguageSettingsActionListener.class),
//    @EventConfig(listeners = UISiteAdminToolbar.AccountSettingsActionListener.class),
//    @EventConfig(listeners = UISiteAdminToolbar.AddContentActionListener.class),
//    @EventConfig(listeners = UISiteAdminToolbar.BrowsePortalActionListener.class),
//    @EventConfig(listeners = UISiteAdminToolbar.BrowsePageActionListener.class),
//    @EventConfig(listeners = UISiteAdminToolbar.EditPageAndNavigationActionListener.class),
//    @EventConfig(listeners = UISiteAdminToolbar.ChangePageActionListener.class),
//    @EventConfig(listeners = UISiteAdminToolbar.TurnOnQuickEditActionListener.class),
//    @EventConfig(listeners = UISiteAdminToolbar.TurnOffQuickEditActionListener.class) })
//public class UISiteAdminToolbar extends UIContainer {
//
//  /** The Constant MESSAGE. */
//  public static final String MESSAGE            = "UISiteAdminToolbar.msg.not-permission";
//
//  /** The Constant TURN_ON_QUICK_EDIT. */
//  public static final String TURN_ON_QUICK_EDIT = "isTurnOn";
//
//  /** The Constant ADMIN. */
//  public static final int ADMIN              = 2;
//
//  /** The Constant EDITOR. */
//  public static final int EDITOR             = 1;
//
//  /** The Constant REDACTOR. */
//  public static final int REDACTOR           = 0;
//
//  /** The Constant VISITOR. */
//  public static final int VISITOR           = -1;
//
//  /** Does the current user have group navigations ?. */
//  private boolean hasGroupNavigations = false;
//
//  /** Group navigations nodes list. */
//  private List<PageNavigation> groupNavigations = null;
//
//  /** Current site navigation list. */
//  private List<PageNavigation> currentSiteNavigations = null;
//
//
//  /** The role of the current user. it can be VISITOR, REDACTOR, EDITOR or ADMINISTRATOR */
//  private int role = VISITOR;  
//
//  /**
//   * Instantiates a new uI site admin toolbar.
//   * 
//   * @throws Exception the exception
//   */
//  public UISiteAdminToolbar() throws Exception {
//    refresh();
//  }
//
//  /**
//   * Sets the role of the current user. Needs to be refreshed each time we change site
//   * 
//   * @throws Exception the exception
//   */
//  protected void setRole() throws Exception {
//    String userId = Util.getPortalRequestContext().getRemoteUser();
//    UserACL userACL = getApplicationComponent(UserACL.class);
//    IdentityRegistry identityRegistry = getApplicationComponent(IdentityRegistry.class);
//    WCMConfigurationService wcmConfigurationService = getApplicationComponent(WCMConfigurationService.class);
//    Identity identity = identityRegistry.getIdentity(userId);
//    String editorMembershipType = userACL.getMakableMT();
//    List<String> accessControlWorkspaceGroups = userACL.getAccessControlWorkspaceGroups();
//    String editSitePermission = Util.getUIPortal().getEditPermission();
//    String redactorMembershipType = wcmConfigurationService.getRuntimeContextParam(WCMConfigurationService.REDACTOR_MEMBERSHIP_TYPE);   
//    // admin
//    if (userACL.getSuperUser().equals(userId)) {
//      role = UISiteAdminToolbar.ADMIN;
//      return;
//    }
//    if (userACL.hasAccessControlWorkspacePermission(userId)
//        && userACL.hasCreatePortalPermission(userId)) {
//      role = UISiteAdminToolbar.ADMIN;
//      return;
//    }
//
//    // editor
//    MembershipEntry editorEntry = null;
//    for (String membership : accessControlWorkspaceGroups) {
//      editorEntry = MembershipEntry.parse(membership);
//      if (editorEntry.getMembershipType().equals(editorMembershipType)
//          || editorEntry.getMembershipType().equals(MembershipEntry.ANY_TYPE)) {
//        if (identity.isMemberOf(editorEntry)) {
//
//          MembershipEntry editEntry = MembershipEntry.parse(editSitePermission);
//          if (MembershipEntry.ANY_TYPE.equals(editEntry.getMembershipType())) {
//            editEntry = MembershipEntry.parse(editorMembershipType+":"+editEntry.getGroup());
//          }
//          if (identity.isMemberOf(editEntry)) {
//            role = UISiteAdminToolbar.EDITOR;
//            return;
//          }
//        }
//      }
//    }
//
//    // redactor
//    MembershipEntry redactorEntry = MembershipEntry.parse(editSitePermission);
//    if (redactorEntry.getMembershipType().equals(redactorMembershipType)
//        || redactorEntry.getMembershipType().equals(MembershipEntry.ANY_TYPE)) {
//      if (identity.isMemberOf(redactorEntry)) {
//        role = UISiteAdminToolbar.REDACTOR;
//        return;
//      }
//    }
//    role = UISiteAdminToolbar.VISITOR;
//  }
//
//  /**
//   * gets the current user role based on the current site context.
//   * 
//   * @return user role
//   * 
//   * @throws Exception the exception
//   */
//  public int getRole() throws Exception {    
//    return role;
//  }
//
//  /**
//   * Checks if we changed portal in order to refresh the user role and the navigation if needed.
//   * 
//   * @throws Exception the exception
//   */
//  public void refresh() throws Exception {	  
//    setRole();
//    buildNavigations();    
//  }
//  
//  /**
//   * Change navigations language.
//   * 
//   * @param language the language
//   */
//  public void changeNavigationsLanguage(String language) {
//    LocaleConfig localeConfig = getApplicationComponent(LocaleConfigService.class).getLocaleConfig(language) ;
//    for(PageNavigation nav : groupNavigations) {      
//      ResourceBundle res = localeConfig.getNavigationResourceBundle(nav.getOwnerType(), nav.getOwnerId()) ;
//      for(PageNode node : nav.getNodes()) {
//        resolveLabel(res, node) ;
//      }
//    }
//    for(PageNavigation nav: currentSiteNavigations) {
//      ResourceBundle res = localeConfig.getNavigationResourceBundle(nav.getOwnerType(), nav.getOwnerId()) ;
//      for(PageNode node : nav.getNodes()) {
//        resolveLabel(res, node) ;
//      }
//    }
//  }    
//  
//  /**
//   * Resolve label.
//   * 
//   * @param res the res
//   * @param node the node
//   */
//  private void resolveLabel(ResourceBundle res, PageNode node) {
//    node.setResolvedLabel(res) ;
//    if(node.getChildren() == null) return;
//    for(PageNode childNode : node.getChildren()) {
//      resolveLabel(res, childNode) ;
//    }
//  }
//  
//  /**
//   * Checks if is show workspace area.
//   * 
//   * @return true, if is show workspace area
//   * 
//   * @throws Exception the exception
//   */
//  public boolean isShowWorkspaceArea() throws Exception {
//    UserACL userACL = getApplicationComponent(UserACL.class);
//    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
//    String userId = context.getRemoteUser();
//    if (userACL.hasAccessControlWorkspacePermission(userId))
//      return true;
//    return false;
//  }
//
//  /**
//   * Gets the current portal uri.
//   * 
//   * @return the current portal uri
//   */
//  public String getCurrentPortalURI() {
//    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
//    String portalContextURI = portalRequestContext.getPortalURI();
//    HttpServletRequest servletRequest = portalRequestContext.getRequest();    
//    String baseURI = servletRequest.getScheme() + "://" + servletRequest.getServerName() + ":"
//    + servletRequest.getServerPort() + portalContextURI.substring(0, portalContextURI.length() - 1);    
//    return baseURI;
//  }
//
//  /**
//   * Gets the all portals.
//   * 
//   * @return the all portals
//   * 
//   * @throws Exception the exception
//   */
//  public List<String> getAllPortals() throws Exception {
//    List<String> portals = new ArrayList<String>();
//    DataStorage dataStorage = getApplicationComponent(DataStorage.class);
//    Query<PortalConfig> query = new Query<PortalConfig>(null, null, null, null, PortalConfig.class) ;
//    PageList pageList = dataStorage.find(query) ;
//    String userId = Util.getPortalRequestContext().getRemoteUser();
//    UserACL userACL = getApplicationComponent(UserACL.class) ;
//    Iterator<?> itr = pageList.getAll().iterator();    
//    while(itr.hasNext()) {
//      PortalConfig pConfig = (PortalConfig)itr.next() ;
//      if(userACL.hasPermission(pConfig, userId)) {
//        portals.add(pConfig.getName());                
//      }
//    }     
//    String currentPortal = Util.getUIPortal().getName();
//    portals.remove(currentPortal);
//    Collections.sort(portals, new Comparator<String>() {
//      public int compare(String o1, String o2) {
//        return o1.compareToIgnoreCase(o2);
//      }      
//    });        
//    portals.add(currentPortal);
//    return portals; 
//  }
//
//  /**
//   * Return true if the user has at least one group navigation node.
//   * 
//   * @return true, if checks for group navigations
//   */
//  public boolean hasGroupNavigations() {
//    return hasGroupNavigations;
//  }
//
//  /**
//   * Allows to set a list of the user group navigation.
//   * 
//   * @throws Exception the exception
//   */
//  
//  private void buildNavigations() throws Exception {
//    hasGroupNavigations = false;
//    String remoteUser = Util.getPortalRequestContext().getRemoteUser();
//    List<PageNavigation> allNavigations = Util.getUIPortal().getNavigations();
//    groupNavigations = new ArrayList<PageNavigation>();
//    currentSiteNavigations = new ArrayList<PageNavigation>();
//    for (PageNavigation navigation : allNavigations) {      
//      if (navigation.getOwnerType().equals(PortalConfig.GROUP_TYPE)) {
//        groupNavigations.add(PageNavigationUtils.filter(navigation, remoteUser));
//        hasGroupNavigations = true;
//      }
//      if (navigation.getOwnerType().equals(PortalConfig.PORTAL_TYPE)) {
//        currentSiteNavigations.add(PageNavigationUtils.filter(navigation, remoteUser));       
//      }
//    }
//  }
//
//  /**
//   * Gets the current site navigations.
//   * 
//   * @return the current site navigations
//   * 
//   * @throws Exception the exception
//   */
//  public List<PageNavigation> getCurrentSiteNavigations() throws Exception {   
//    return currentSiteNavigations;
//  }
//
//  /**
//   * Get the list of group navigation nodes.
//   * 
//   * @return A list of navigation nodes
//   * 
//   * @throws Exception the exception
//   */
//  public List<PageNavigation> getGroupNavigations() throws Exception {    
//    return groupNavigations;
//  }
//
//  /**
//   * The listener interface for receiving addPageAction events. The class that
//   * is interested in processing a addPageAction event implements this
//   * interface, and the object created with that class is registered with a
//   * component using the component's
//   * <code>addAddPageActionListener<code> method. When
//   * the addPageAction event occurs, that object's appropriate
//   * method is invoked.
//   * 
//   * @see AddPageActionEvent
//   */
//  public static class AddPageActionListener extends EventListener<UISiteAdminToolbar> {
//    
//    /* (non-Javadoc)
//     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
//     */
//    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
//      UIPortalApplication uiApp = Util.getUIPortalApplication();
//      PortalRequestContext portalContext = Util.getPortalRequestContext();
//      event.setRequestContext(Util.getPortalRequestContext());
//      UserACL userACL = uiApp.getApplicationComponent(UserACL.class);
//      String remoteUser = portalContext.getRemoteUser();
//      if (!userACL.hasAccessControlWorkspacePermission(remoteUser)) {
//        Utils.createPopupMessage(event.getSource(), UISiteAdminToolbar.MESSAGE, null, ApplicationMessage.WARNING);
//        return;
//      }
//      UIControlWorkspace uiControl = uiApp.getChild(UIControlWorkspace.class);
//      UIControlWSWorkingArea uiWorking = uiControl.getChildById(UIControlWorkspace.WORKING_AREA_ID);
//      uiWorking.setUIComponent(uiWorking.createUIComponent(UIWizardPageCreationBar.class,
//          null,
//          null));
//      uiApp.setEditting(true);
//      UIWorkingWorkspace uiWorkingWS = uiApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
//      uiWorkingWS.setRenderedChild(UIPortalToolPanel.class);
//      UIPortalToolPanel uiToolPanel = uiWorkingWS.getChild(UIPortalToolPanel.class);
//      uiToolPanel.setShowMaskLayer(false);
//      portalContext.addUIComponentToUpdateByAjax(uiWorkingWS);
//      uiToolPanel.setWorkingComponent(UIPageCreationWizard.class, null);
//      UIPageCreationWizard uiWizard = (UIPageCreationWizard) uiToolPanel.getUIComponent();
//      uiWizard.setDescriptionWizard(2);
//      uiWizard.viewStep(2);
//      UIWizardPageSetInfo uiPageSetInfo = uiWizard.getChild(UIWizardPageSetInfo.class);
//      uiPageSetInfo.setShowPublicationDate(false);
//      uiWorking.setUIComponent(uiWorking.createUIComponent(UIWelcomeComponent.class, null, null));
//    }
//  }
//
//  /**
//   * The listener interface for receiving editPageAction events. The class that
//   * is interested in processing a editPageAction event implements this
//   * interface, and the object created with that class is registered with a
//   * component using the component's
//   * <code>addEditPageActionListener<code> method. When
//   * the editPageAction event occurs, that object's appropriate
//   * method is invoked.
//   * 
//   * @see EditPageActionEvent
//   */
//  public static class EditPageActionListener extends EventListener<UISiteAdminToolbar> {
//    
//    /* (non-Javadoc)
//     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
//     */
//    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
//      PortalRequestContext portalContext = Util.getPortalRequestContext();
//      event.setRequestContext(Util.getPortalRequestContext());
//      UIPortalApplication uiApp = Util.getUIPortalApplication();
//      UserACL userACL = uiApp.getApplicationComponent(UserACL.class);
//      String remoteUser = portalContext.getRemoteUser();
//      UIPortal uiPortal = Util.getUIPortal();
//      String pageId = uiPortal.getSelectedNode().getPageReference();
//      UserPortalConfigService portalConfigService = uiApp.getApplicationComponent(UserPortalConfigService.class);
//      Page currentPage = portalConfigService.getPage(pageId, remoteUser);
//      if (currentPage == null)
//      	return;
//      if (!userACL.hasAccessControlWorkspacePermission(remoteUser) || !userACL.hasEditPermission(currentPage, remoteUser)) {
//      	Utils.createPopupMessage(event.getSource(), UISiteAdminToolbar.MESSAGE, null, ApplicationMessage.WARNING);
//        return;
//      }
//      uiApp.setEditting(true);
//      UIWorkingWorkspace uiWorkingWS = uiApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
//      uiWorkingWS.setRenderedChild(UIPortalToolPanel.class);
//      UIPortalToolPanel uiToolPanel = uiWorkingWS.getChild(UIPortalToolPanel.class);
//      uiToolPanel.setShowMaskLayer(false);
//      portalContext.addUIComponentToUpdateByAjax(uiWorkingWS);
//      uiToolPanel.setWorkingComponent(UIPageEditWizard.class, null);
//      UIPageEditWizard uiWizard = (UIPageEditWizard) uiToolPanel.getUIComponent();
//      uiWizard.setDescriptionWizard(2);
//      uiWizard.viewStep(3);
//      UIWizardPageSetInfo uiPageSetInfo = uiWizard.getChild(UIWizardPageSetInfo.class);
//      uiPageSetInfo.setEditMode();
//      uiPageSetInfo.createEvent("ChangeNode", Event.Phase.DECODE, event.getRequestContext())
//      .broadcast();
//    }
//  }
//
//  /**
//   * The listener interface for receiving createPortalAction events. The class
//   * that is interested in processing a createPortalAction event implements this
//   * interface, and the object created with that class is registered with a
//   * component using the component's
//   * <code>addCreatePortalActionListener<code> method. When
//   * the createPortalAction event occurs, that object's appropriate
//   * method is invoked.
//   * 
//   * @see CreatePortalActionEvent
//   */
//  public static class CreatePortalActionListener extends EventListener<UISiteAdminToolbar> {
//    
//    /* (non-Javadoc)
//     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
//     */
//    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
//      event.setRequestContext(Util.getPortalRequestContext());
//      PortalRequestContext portalContext = Util.getPortalRequestContext();
//      UIPortalApplication uiApp = Util.getUIPortalApplication();
//      UserACL userACL = uiApp.getApplicationComponent(UserACL.class);
//      if (!userACL.hasCreatePortalPermission(portalContext.getRemoteUser())) {
//      	Utils.createPopupMessage(event.getSource(), UISiteAdminToolbar.MESSAGE, null, ApplicationMessage.WARNING);
//        return;
//      }
//      UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
//      UIPortalForm uiNewPortal = uiMaskWS.createUIComponent(UIPortalForm.class,
//          "CreatePortal",
//      "UIPortalForm");
//      uiMaskWS.setUIComponent(uiNewPortal);
//      uiMaskWS.setShow(true);
//      portalContext.addUIComponentToUpdateByAjax(uiMaskWS);
//    }
//  }
//
//  /**
//   * The listener interface for receiving editPortalAction events. The class
//   * that is interested in processing a editPortalAction event implements this
//   * interface, and the object created with that class is registered with a
//   * component using the component's
//   * <code>addEditPortalActionListener<code> method. When
//   * the editPortalAction event occurs, that object's appropriate
//   * method is invoked.
//   * 
//   * @see EditPortalActionEvent
//   */
//  public static class EditPortalActionListener extends EventListener<UISiteAdminToolbar> {
//    
//    /* (non-Javadoc)
//     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
//     */
//    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
//      event.setRequestContext(Util.getPortalRequestContext());      
//      UIPortal uiPortal = Util.getUIPortal();
//      UIPortalApplication uiApp = Util.getUIPortalApplication();      
//      if (!uiPortal.isModifiable()) {
//      	Utils.createPopupMessage(event.getSource(), UISiteAdminToolbar.MESSAGE, null, ApplicationMessage.WARNING);
//        return;
//      }
//      UIControlWorkspace uiControlWorkspace = uiApp.getChild(UIControlWorkspace.class);
//      uiControlWorkspace.getChild(UIExoStart.class)
//      .createEvent("EditPortal", Event.Phase.PROCESS, event.getRequestContext())
//      .broadcast();
//    }
//  }
//
//  /**
//   * The listener interface for receiving browsePortalAction events. The class
//   * that is interested in processing a browsePortalAction event implements this
//   * interface, and the object created with that class is registered with a
//   * component using the component's
//   * <code>addBrowsePortalActionListener<code> method. When
//   * the browsePortalAction event occurs, that object's appropriate
//   * method is invoked.
//   * 
//   * @see BrowsePortalActionEvent
//   */
//  public static class BrowsePortalActionListener extends EventListener<UISiteAdminToolbar> {
//    
//    /* (non-Javadoc)
//     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
//     */
//    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
//      event.setRequestContext(Util.getPortalRequestContext());            
//      UIPortalApplication uiApp = Util.getUIPortalApplication();           
//      UIControlWorkspace uiControlWorkspace = uiApp.getChild(UIControlWorkspace.class);
//      UIExoStart uiExoStart = uiControlWorkspace.getChild(UIExoStart.class);
//      uiExoStart.createEvent("BrowsePortal", Event.Phase.PROCESS, event.getRequestContext())
//      .broadcast();
//    }
//  }
//
//  /**
//   * The listener interface for receiving browsePageAction events. The class
//   * that is interested in processing a browsePageAction event implements this
//   * interface, and the object created with that class is registered with a
//   * component using the component's
//   * <code>addBrowsePageActionListener<code> method. When
//   * the browsePageAction event occurs, that object's appropriate
//   * method is invoked.
//   * 
//   * @see BrowsePageActionEvent
//   */
//  public static class BrowsePageActionListener extends EventListener<UISiteAdminToolbar> {
//    
//    /* (non-Javadoc)
//     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
//     */
//    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
//      event.setRequestContext(Util.getPortalRequestContext());            
//      UIPortalApplication uiApp = Util.getUIPortalApplication();            
//      UIControlWorkspace uiControlWorkspace = uiApp.getChild(UIControlWorkspace.class);
//      UIExoStart uiExoStart = uiControlWorkspace.getChild(UIExoStart.class);      
//      WebuiApplication webuiApplication = (WebuiApplication)Util.getPortalRequestContext().getApplication();
//      // put UIBrowserPageForm component config into ConfigurationManager to avoid exception when add new page
//      //this is strange bug in WCM
//      webuiApplication.getConfigurationManager().getComponentConfig(UIPageBrowser.class,"UIBrowserPageForm");
//      uiExoStart.createEvent("BrowsePage", Event.Phase.PROCESS,Util.getPortalRequestContext()).broadcast();
//    }
//  }
//
//  /**
//   * The listener interface for receiving changePortalAction events. The class
//   * that is interested in processing a changePortalAction event implements this
//   * interface, and the object created with that class is registered with a
//   * component using the component's
//   * <code>addChangePortalActionListener<code> method. When
//   * the changePortalAction event occurs, that object's appropriate
//   * method is invoked.
//   * 
//   * @see ChangePortalActionEvent
//   */
//  public static class ChangePortalActionListener extends EventListener<UISiteAdminToolbar> {
//    
//    /* (non-Javadoc)
//     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
//     */
//    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
//      event.setRequestContext(Util.getPortalRequestContext());
//      UIPortalApplication uiApp = Util.getUIPortalApplication();
//      UIControlWorkspace uiControlWorkspace = uiApp.getChild(UIControlWorkspace.class);
//      UIExoStart uiExoStart = uiControlWorkspace.getChild(UIExoStart.class);
//      uiExoStart.createEvent("ChangePortal", Event.Phase.PROCESS, event.getRequestContext())
//      .broadcast();
//    }
//  }
//
//  /**
//   * The listener interface for receiving skinSettingsAction events. The class
//   * that is interested in processing a skinSettingsAction event implements this
//   * interface, and the object created with that class is registered with a
//   * component using the component's
//   * <code>addSkinSettingsActionListener<code> method. When
//   * the skinSettingsAction event occurs, that object's appropriate
//   * method is invoked.
//   * 
//   * @see SkinSettingsActionEvent
//   */
//  public static class SkinSettingsActionListener extends EventListener<UISiteAdminToolbar> {
//    
//    /* (non-Javadoc)
//     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
//     */
//    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
//      event.setRequestContext(Util.getPortalRequestContext());
//      UIPortal uiPortal = Util.getUIPortal();
//      UIPortalApplication uiApp = uiPortal.getAncestorOfType(UIPortalApplication.class);      
//      UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID) ; 
//      UISkinSelector uiChangeSkin = uiMaskWS.createUIComponent(UISkinSelector.class, null, null);
//      uiMaskWS.setUIComponent(uiChangeSkin);
//      uiMaskWS.setWindowSize(640, 400);
//      uiMaskWS.setShow(true);
//      event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWS);
//    }
//  }
//
//  /**
//   * The listener interface for receiving languageSettingsAction events. The
//   * class that is interested in processing a languageSettingsAction event
//   * implements this interface, and the object created with that class is
//   * registered with a component using the component's
//   * <code>addLanguageSettingsActionListener<code> method. When
//   * the languageSettingsAction event occurs, that object's appropriate
//   * method is invoked.
//   * 
//   * @see LanguageSettingsActionEvent
//   */
//  public static class LanguageSettingsActionListener extends EventListener<UISiteAdminToolbar> {
//    
//    /* (non-Javadoc)
//     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
//     */
//    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
//      event.setRequestContext(Util.getPortalRequestContext());
//      UIPortal uiPortal = Util.getUIPortal();
//      uiPortal.createEvent("ChangeLanguage", Event.Phase.PROCESS, event.getRequestContext()).broadcast();
//    }
//  }
//
//  /**
//   * The listener interface for receiving accountSettingsAction events. The
//   * class that is interested in processing a accountSettingsAction event
//   * implements this interface, and the object created with that class is
//   * registered with a component using the component's
//   * <code>addAccountSettingsActionListener<code> method. When
//   * the accountSettingsAction event occurs, that object's appropriate
//   * method is invoked.
//   * 
//   * @see AccountSettingsActionEvent
//   */
//  public static class AccountSettingsActionListener extends EventListener<UISiteAdminToolbar> {
//    
//    /* (non-Javadoc)
//     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
//     */
//    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
//      event.setRequestContext(Util.getPortalRequestContext());
//      UIPortal uiPortal = Util.getUIPortal();
//      uiPortal.createEvent("AccountSettings", Event.Phase.PROCESS, event.getRequestContext()).broadcast();
//    }
//  }
//
//  /**
//   * The listener interface for receiving editPageAndNavigationAction events.
//   * The class that is interested in processing a editPageAndNavigationAction
//   * event implements this interface, and the object created with that class is
//   * registered with a component using the component's
//   * <code>addEditPageAndNavigationActionListener<code> method. When
//   * the editPageAndNavigationAction event occurs, that object's appropriate
//   * method is invoked.
//   * 
//   * @see EditPageAndNavigationActionEvent
//   */
//  public static class EditPageAndNavigationActionListener extends EventListener<UISiteAdminToolbar> {
//    
//    /* (non-Javadoc)
//     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
//     */
//    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
//      event.setRequestContext(Util.getPortalRequestContext());
//      UIPortalApplication uiApp = Util.getUIPortalApplication();
//      UIControlWorkspace uiControlWorkspace = uiApp.getChild(UIControlWorkspace.class);
//      UIExoStart uiExoStart = uiControlWorkspace.getChild(UIExoStart.class);
//      uiExoStart.createEvent("EditPage", Event.Phase.PROCESS, event.getRequestContext())
//      .broadcast();
//    }
//  }
//
//  /**
//   * The listener interface for receiving changePageAction events.
//   * The class that is interested in processing a changePageAction
//   * event implements this interface, and the object created
//   * with that class is registered with a component using the
//   * component's <code>addChangePageActionListener<code> method. When
//   * the changePageAction event occurs, that object's appropriate
//   * method is invoked.
//   * 
//   * @see ChangePageActionEvent
//   */
//  public static class ChangePageActionListener extends EventListener<UISiteAdminToolbar> {
//    
//    /* (non-Javadoc)
//     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
//     */
//    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
//      String uri = event.getRequestContext().getRequestParameter(OBJECTID);
//      UIPortal uiPortal = Util.getUIPortal();
//      uiPortal.setMode(UIPortal.COMPONENT_VIEW_MODE);
//      PageNodeEvent<UIPortal> pnevent = new PageNodeEvent<UIPortal>(uiPortal,
//          PageNodeEvent.CHANGE_PAGE_NODE,
//          uri);
//      uiPortal.broadcast(pnevent, Event.Phase.PROCESS);
//    }
//  }
//
//  /**
//   * The listener interface for receiving addContentAction events. The class
//   * that is interested in processing a addContentAction event implements this
//   * interface, and the object created with that class is registered with a
//   * component using the component's
//   * <code>addAddContentActionListener<code> method. When
//   * the addContentAction event occurs, that object's appropriate
//   * method is invoked.
//   * 
//   * @see AddContentActionEvent
//   */
//  public static class AddContentActionListener extends EventListener<UISiteAdminToolbar> {
//    
//    /* (non-Javadoc)
//     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
//     */
//    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
//      UISiteAdminToolbar siteAdminToolbar = event.getSource();
//      UIPortal uiPortal = Util.getUIPortal();
//      UIPortlet uiPortlet = new UIPortlet();
//      uiPortlet.setShowInfoBar(false);
//
//      // Create portlet
//      WCMConfigurationService configurationService = siteAdminToolbar.getApplicationComponent(WCMConfigurationService.class);
//      StringBuilder windowId = new StringBuilder();
//      String random = IdGenerator.generate();
//      windowId.append(PortalConfig.PORTAL_TYPE)
//      .append("#")
//      .append(uiPortal.getOwner())
//      .append(":")
//      .append(configurationService.getRuntimeContextParam(WCMConfigurationService.SCV_PORTLET))
//      .append("/")
//      .append(random);
//      uiPortlet.setWindowId(windowId.toString());
//
//      // Add preferences to portlet
//      PortletPreferences portletPreferences = new PortletPreferences();
//      portletPreferences.setWindowId(windowId.toString());
//      portletPreferences.setOwnerType(PortalConfig.PORTAL_TYPE);
//      portletPreferences.setOwnerId(org.exoplatform.portal.webui.util.Util.getUIPortal().getOwner());
//      ArrayList<Preference> listPreference = new ArrayList<Preference>();
//
//      PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
//      PortletRequest request = portletRequestContext.getRequest();
//      javax.portlet.PortletPreferences preferences = request.getPreferences();
//      
//      Preference preferenceR = new Preference();
//      ArrayList<String> listValue = new ArrayList<String>();
//      listValue.add(preferences.getValue("repository", ""));
//      preferenceR.setName("repository");
//      preferenceR.setValues(listValue);
//      listPreference.add(preferenceR);
//
//      Preference preferenceW = new Preference();
//      listValue = new ArrayList<String>();
//      listValue.add(preferences.getValue("workspace", ""));
//      preferenceW.setName("workspace");
//      preferenceW.setValues(listValue);
//      listPreference.add(preferenceW);
//
//      Preference preferenceQ = new Preference();
//      listValue = new ArrayList<String>();
//      listValue.add("true");
//      preferenceQ.setName("ShowQuickEdit");
//      preferenceQ.setValues(listValue);
//      listPreference.add(preferenceQ);
//
//      Preference preferenceT = new Preference();
//      listValue = new ArrayList<String>();
//      listValue.add("true");
//      preferenceT.setName("ShowTitle");
//      preferenceT.setValues(listValue);
//      listPreference.add(preferenceT);
//   
//      Preference preferenceV = new Preference();
//      listValue = new ArrayList<String>();
//      listValue.add("false");
//      preferenceV.setName("ShowVote");
//      preferenceV.setValues(listValue);
//      listPreference.add(preferenceV);
//      
//      Preference preferenceC = new Preference();
//      listValue = new ArrayList<String>();
//      listValue.add("false");
//      preferenceC.setName("ShowComments");
//      preferenceC.setValues(listValue);
//      listPreference.add(preferenceC);
//      
//      Preference preferenceP = new Preference();
//      listValue = new ArrayList<String>();
//      listValue.add("true");
//      preferenceP.setName("ShowPrintAction");
//      preferenceP.setValues(listValue);
//      listPreference.add(preferenceP);
//
//      Preference preferenceQC = new Preference();
//      listValue = new ArrayList<String>();
//      listValue.add("true");
//      preferenceQC.setName("isQuickCreate");
//      preferenceQC.setValues(listValue);
//      listPreference.add(preferenceQC);
//      
//      portletPreferences.setPreferences(listPreference);
//
//      DataStorage dataStorage = siteAdminToolbar.getApplicationComponent(DataStorage.class);
//      dataStorage.save(portletPreferences);
//
//      // Add portlet to page
//      UserPortalConfigService userPortalConfigService = siteAdminToolbar.getApplicationComponent(UserPortalConfigService.class);
//      Page page = userPortalConfigService.getPage(uiPortal.getSelectedNode().getPageReference(),
//          Util.getPortalRequestContext().getRemoteUser());
//      if (page == null) return;
//      ArrayList<Object> listPortlet = page.getChildren();
//      listPortlet.add(PortalDataMapper.toPortletModel(uiPortlet));
//      page.setChildren(listPortlet);
//      userPortalConfigService.update(page);
//      UIPage uiPage = uiPortal.findFirstComponentOfType(UIPage.class);
//      uiPage.setChildren(null);
//      PortalDataMapper.toUIPage(uiPage, page);
//      ((UIPortlet) uiPage.findComponentById(random)).setCurrentPortletMode(PortletMode.EDIT);
//      Utils.updatePortal((PortletRequestContext) event.getRequestContext());
//    }
//  }
//
//  /**
//   * The listener interface for receiving turnOnQuickEditAction events.
//   * The class that is interested in processing a turnOnQuickEditAction
//   * event implements this interface, and the object created
//   * with that class is registered with a component using the
//   * component's <code>addTurnOnQuickEditActionListener<code> method. When
//   * the turnOnQuickEditAction event occurs, that object's appropriate
//   * method is invoked.
//   * 
//   * @see TurnOnQuickEditActionEvent
//   */
//  public static class TurnOnQuickEditActionListener extends EventListener<UISiteAdminToolbar> {
//    
//    /* (non-Javadoc)
//     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
//     */
//    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
//      UIPortalApplication uiApp = Util.getUIPortalApplication();
//      UserACL userACL = uiApp.getApplicationComponent(UserACL.class);
//      PortalConfig portalConfig = PortalDataMapper.toPortal(Util.getUIPortal());
//      if (!userACL.hasPermission(portalConfig.getEditPermission())) {
//      	Utils.createPopupMessage(event.getSource(), UISiteAdminToolbar.MESSAGE, null, ApplicationMessage.WARNING);
//        return;
//      }
//      PortalRequestContext context = Util.getPortalRequestContext();
//      context.getRequest().getSession().setAttribute(Utils.TURN_ON_QUICK_EDIT, true);
//      Utils.updatePortal((PortletRequestContext) event.getRequestContext());      
//    }
//  }
//
//  /**
//   * The listener interface for receiving turnOffQuickEditAction events.
//   * The class that is interested in processing a turnOffQuickEditAction
//   * event implements this interface, and the object created
//   * with that class is registered with a component using the
//   * component's <code>addTurnOffQuickEditActionListener<code> method. When
//   * the turnOffQuickEditAction event occurs, that object's appropriate
//   * method is invoked.
//   * 
//   * @see TurnOffQuickEditActionEvent
//   */
//  public static class TurnOffQuickEditActionListener extends EventListener<UISiteAdminToolbar> {
//    
//    /* (non-Javadoc)
//     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
//     */
//    public void execute(Event<UISiteAdminToolbar> event) throws Exception {
//      UIPortalApplication uiApp = Util.getUIPortalApplication();
//      UserACL userACL = uiApp.getApplicationComponent(UserACL.class);
//      PortalConfig portalConfig = PortalDataMapper.toPortal(Util.getUIPortal());
//      if (!userACL.hasPermission(portalConfig.getEditPermission())) {
//      	Utils.createPopupMessage(event.getSource(), UISiteAdminToolbar.MESSAGE, null, ApplicationMessage.WARNING);
//        return;
//      }
//      PortalRequestContext context = Util.getPortalRequestContext();
//      context.getRequest().getSession().setAttribute(Utils.TURN_ON_QUICK_EDIT, false);
//      Utils.updatePortal((PortletRequestContext) event.getRequestContext());
//    }
//  }
//}
