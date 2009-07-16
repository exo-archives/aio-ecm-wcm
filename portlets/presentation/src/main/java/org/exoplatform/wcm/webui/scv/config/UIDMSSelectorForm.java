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

import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.wcm.core.NodeIdentifier;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.publication.NotInWCMPublicationException;
import org.exoplatform.services.wcm.publication.WCMPublicationService;
import org.exoplatform.services.wcm.publication.WebpagePublicationPlugin;
import org.exoplatform.wcm.webui.scv.UISingleContentViewerPortlet;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.ext.UIFormInputSetWithAction;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 * dzungdev@gmail.com
 * Sep 3, 2008
 */
@ComponentConfig (
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl",
    events = {
      @EventConfig(listeners = UIDMSSelectorForm.SaveActionListener.class),
      @EventConfig(listeners = UIDMSSelectorForm.BackActionListener.class),
      @EventConfig(listeners = UIDMSSelectorForm.BrowseActionListener.class)
    }
)
public class UIDMSSelectorForm extends UIForm implements UISelectable{

  /** The Constant PATH. */
  final static String PATH = "path".intern();

  /** The Constant FIELD_PATH. */
  final static String FIELD_PATH = "location".intern();

  /** The Constant PUBLICATION. */
  final static String PUBLICATION = "publication".intern();

  /** The Constant PUBLICATION_PATH. */
  final static String PUBLICATION_PATH = "PublicationPath".intern();

  /** The repository. */
  private String repository;

  /** The workspace. */
  private String workspace;

  /** The live portals path. */
  private String livePortalsPath;

  /** The live shared portal name. */
  private String liveSharedPortalName;

  /**
   * Instantiates a new uIDMS selector form.
   * 
   * @throws Exception the exception
   */
  public UIDMSSelectorForm() throws Exception {
    RepositoryService repoService = getApplicationComponent(RepositoryService.class);
    String repoName = repoService.getCurrentRepository().getConfiguration().getName();
    WCMConfigurationService configurationService = getApplicationComponent(WCMConfigurationService.class);
    NodeLocation nodeLocation = configurationService.getLivePortalsLocation(repoName);
    repository = nodeLocation.getRepository();
    workspace = nodeLocation.getWorkspace();
    livePortalsPath = nodeLocation.getPath();
    liveSharedPortalName = configurationService.getSharedPortalName(repository);
    UIFormInputSetWithAction uiPathSelection = new UIFormInputSetWithAction(FIELD_PATH);
    uiPathSelection.addUIFormInput(new UIFormStringInput(PATH, PATH, null).setEditable(false));
    uiPathSelection.setActionInfo(PATH, new String[] {"Browse"});
    addChild(uiPathSelection);
    setActions(new String[] {"Save","Back"});
  }

  /**
   * Inits the.
   * 
   * @throws Exception the exception
   */
  public void init() throws Exception {
  }

  /**
   * Checks if is only one publication plugin.
   * 
   * @return true, if is only one publication plugin
   */
  private boolean isOnlyOnePublicationPlugin() {
    WCMPublicationService wcmService = getApplicationComponent(WCMPublicationService.class);
    Map<String,WebpagePublicationPlugin> publicationPluginMap = wcmService.getWebpagePublicationPlugins();
    Set<String> keySet = publicationPluginMap.keySet();
    if (keySet.size() > 1) return false;
    return true;
  }

  /**
   * Gets the live portal path.
   * 
   * @return the live portal path
   */
  public String getLivePortalPath() { return livePortalsPath; }

  /**
   * Gets the live shared portal name.
   * 
   * @return the live shared portal name
   */
  public String getLiveSharedPortalName() { return liveSharedPortalName; }

  /**
   * Gets the repository name.
   * 
   * @return the repository name
   */
  public String getRepositoryName() { return repository; }

  /**
   * Gets the workspace.
   * 
   * @return the workspace
   */
  public String getWorkspace() { return workspace; }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.selector.UISelectable#doSelect(java.lang.String, java.lang.Object)
   */
  public void doSelect(String selectField, Object value) throws Exception {
    getUIStringInput(selectField).setValue((String)value);
    UIPortletConfig uiPortletConfig = getAncestorOfType(UIPortletConfig.class);
    UIPopupWindow uiPopup = uiPortletConfig.getChildById(UIPortletConfig.POPUP_DMS_SELECTOR);
    uiPopup.setShow(false);
  }

  /**
   * Show popup component.
   * 
   * @param uiComponent the ui component
   * 
   * @throws Exception the exception
   */
  public void showPopupComponent(UIComponent uiComponent) throws Exception {
    UIContainer uiParent = getParent();
    if(uiComponent == null) {
      uiParent.removeChild(UIPopupWindow.class);
      return ;
    }
    UIPopupWindow uiPopup = uiParent.getChild(UIPopupWindow.class);
    if( uiPopup == null)  uiPopup = uiParent.addChild(UIPopupWindow.class, null, null);
    uiPopup.setUIComponent(uiComponent);
    uiPopup.setWindowSize(610, 300);
    uiPopup.setResizable(true);
    uiPopup.setShow(true);
  }

  /**
   * The listener interface for receiving saveAction events.
   * The class that is interested in processing a saveAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addSaveActionListener<code> method. When
   * the saveAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SaveActionEvent
   */
  public static class SaveActionListener extends EventListener<UIDMSSelectorForm> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIDMSSelectorForm> event) throws Exception {
      UIDMSSelectorForm uiDMSSelectorForm = event.getSource();
      String dmsPath = uiDMSSelectorForm.getUIStringInput(UIDMSSelectorForm.PATH).getValue();
      String lifecycleName =  "States and versions based publication";

      //String lifecycleName =  uiDMSSelectorForm.getUIStringInput(UIWebContentSelectorForm.PUBLICATION).getValue();
      if(dmsPath == null) {
        UIApplication uiApplication = uiDMSSelectorForm.getAncestorOfType(UIApplication.class);
        uiApplication.addMessage(new ApplicationMessage("UIDMSSelectorForm.msg.require-choose", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
        return;
      }
      RepositoryService repositoryService = uiDMSSelectorForm.getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getRepository(uiDMSSelectorForm.getRepositoryName());
      Session session = SessionProviderFactory.createSystemProvider().getSession(uiDMSSelectorForm.getWorkspace(), manageableRepository);
      Node webContent = (Node) session.getItem(dmsPath);
      NodeIdentifier nodeIdentifier = NodeIdentifier.make(webContent);
      PortletRequestContext pContext = (PortletRequestContext) event.getRequestContext();
      PortletPreferences prefs = pContext.getRequest().getPreferences();
      prefs.setValue(UISingleContentViewerPortlet.REPOSITORY, nodeIdentifier.getRepository());
      prefs.setValue(UISingleContentViewerPortlet.WORKSPACE, nodeIdentifier.getWorkspace());
      prefs.setValue(UISingleContentViewerPortlet.IDENTIFIER, nodeIdentifier.getUUID());
      prefs.store();

      WCMPublicationService wcmPublicationService = uiDMSSelectorForm.getApplicationComponent(WCMPublicationService.class);
      UIPortletConfig portletConfig = uiDMSSelectorForm.getAncestorOfType(UIPortletConfig.class);
      if (portletConfig.isEditPortletInCreatePageWizard()) {
        if (!wcmPublicationService.isEnrolledInWCMLifecycle(webContent)) {
          wcmPublicationService.enrollNodeInLifecycle(webContent, lifecycleName);          
        }
      } else {
        String pageId = Util.getUIPortal().getSelectedNode().getPageReference();
        UserPortalConfigService upcService = uiDMSSelectorForm.getApplicationComponent(UserPortalConfigService.class);
        Page page = upcService.getPage(pageId);
        try {
          if (!wcmPublicationService.isEnrolledInWCMLifecycle(webContent)) {
            wcmPublicationService.enrollNodeInLifecycle(webContent, lifecycleName);
            wcmPublicationService.updateLifecyleOnChangePage(page, event.getRequestContext().getRemoteUser());
          }
        }catch (NotInWCMPublicationException e){
          wcmPublicationService.unsubcribeLifecycle(webContent);
          wcmPublicationService.enrollNodeInLifecycle(webContent, lifecycleName);
          wcmPublicationService.updateLifecyleOnChangePage(page, event.getRequestContext().getRemoteUser());
        }
      }

      UIPortletConfig uiPortletConfig = uiDMSSelectorForm.getAncestorOfType(UIPortletConfig.class);
      uiPortletConfig.closePopupAndUpdateUI(event.getRequestContext(),true);
    }
  }

  /**
   * The listener interface for receiving backAction events.
   * The class that is interested in processing a backAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addBackActionListener<code> method. When
   * the backAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see BackActionEvent
   */
  public static class BackActionListener extends EventListener<UIDMSSelectorForm> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIDMSSelectorForm> event) throws Exception{
      UIDMSSelectorForm uiSelectorForm = event.getSource();
      UIPortletConfig uiPortletConfig = uiSelectorForm.getAncestorOfType(UIPortletConfig.class);
      uiPortletConfig.getChildren().clear();
      uiPortletConfig.addChild(uiPortletConfig.getBackComponent());
      uiPortletConfig.showPopup(event.getRequestContext());
    }
  }

  /**
   * The listener interface for receiving browseAction events.
   * The class that is interested in processing a browseAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addBrowseActionListener<code> method. When
   * the browseAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see BrowseActionEvent
   */
  public static class BrowseActionListener extends EventListener<UIDMSSelectorForm> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIDMSSelectorForm> event) throws Exception {
      UIDMSSelectorForm uiDMSSelectorForm = event.getSource();
      ((UIPortletConfig) uiDMSSelectorForm.getParent()).initPopupDMSSelector();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDMSSelectorForm.getParent());
    }
  }

}
