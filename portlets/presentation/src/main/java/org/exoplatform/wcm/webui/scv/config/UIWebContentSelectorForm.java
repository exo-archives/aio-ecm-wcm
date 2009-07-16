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
import javax.jcr.Session;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.NodeIdentifier;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.publication.NotInWCMPublicationException;
import org.exoplatform.services.wcm.publication.WCMPublicationService;
import org.exoplatform.wcm.webui.scv.UISingleContentViewerPortlet;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
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
 * May 27, 2008
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl",
    events = {
      @EventConfig(listeners = UIWebContentSelectorForm.SaveActionListener.class),
      @EventConfig(listeners = UIWebContentSelectorForm.BackActionListener.class),
      @EventConfig(listeners = UIWebContentSelectorForm.BrowseActionListener.class)
    }
)

public class UIWebContentSelectorForm extends UIForm implements UISelectable{

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
   * Instantiates a new uI web content selector form.
   * 
   * @throws Exception the exception
   */
  public UIWebContentSelectorForm() throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    String repoName = repositoryService.getCurrentRepository().getConfiguration().getName();
    WCMConfigurationService configurationService = getApplicationComponent(WCMConfigurationService.class);
    NodeLocation nodeLocation = configurationService.getLivePortalsLocation(repoName);
    repository = nodeLocation.getRepository();
    workspace = nodeLocation.getWorkspace();
    livePortalsPath = nodeLocation.getPath();
    liveSharedPortalName = configurationService.getSharedPortalName(repository);
    UIFormInputSetWithAction uiPathSelection = new UIFormInputSetWithAction(FIELD_PATH);
    uiPathSelection.addUIFormInput(new UIFormStringInput(PATH, PATH, null).setEditable(false));
    uiPathSelection.setActionInfo(PATH, new String [] {"Browse"});
    addChild(uiPathSelection);
    setActions(new String[] {"Save", "Back"});
  }

  /**
   * Inits the.
   * 
   * @throws Exception the exception
   */
  public void init() throws Exception {
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.selector.UISelectable#doSelect(java.lang.String, java.lang.Object)
   */
  public void doSelect(String selectField, Object value) throws Exception {
    getUIStringInput(selectField).setValue((String)value);
    UIPortletConfig uiPortletConfig = this.getAncestorOfType(UIPortletConfig.class);
    UIPopupWindow uiPopup = uiPortletConfig.getChildById(UIPortletConfig.POPUP_WEBCONTENT_SELECTOR);
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
    UIPopupWindow uiPopup = getChild(UIPopupWindow.class);
    if(uiPopup == null) {
      uiPopup = addChild(UIPopupWindow.class, null, null);
      uiPopup.setWindowSize(560, 300);
    } else {
      uiPopup.setRendered(true) ;
    }
    uiPopup.setUIComponent(uiComponent);
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;        
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
  public static class BrowseActionListener extends EventListener<UIWebContentSelectorForm> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIWebContentSelectorForm> event) throws Exception {
      UIWebContentSelectorForm uiWebContentSelector = event.getSource();
      ((UIPortletConfig) uiWebContentSelector.getParent()).initPopupWebContentSelector();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWebContentSelector.getParent());
    }
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
  public static class SaveActionListener extends EventListener<UIWebContentSelectorForm> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIWebContentSelectorForm> event) throws Exception {
      UIWebContentSelectorForm uiWebContentSelector = event.getSource();
      String webContentPath = uiWebContentSelector.getUIStringInput(UIWebContentSelectorForm.PATH).getValue();
      String lifecycleName =  "States and versions based publication";
      //uiWebContentSelector.getUIStringInput(UIWebContentSelectorForm.PUBLICATION).getValue();
      if(webContentPath == null) {
        UIApplication uiApplication = uiWebContentSelector.getAncestorOfType(UIApplication.class);
        uiApplication.addMessage(new ApplicationMessage("UIWebContentSelector.msg.require-choose", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
        return;
      }
      RepositoryService repositoryService = uiWebContentSelector.getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getRepository(uiWebContentSelector.getRepositoryName());
      Session session = SessionProvider.createSystemProvider().getSession(uiWebContentSelector.getWorkspace(), manageableRepository);
      Node webContent = (Node) session.getItem(webContentPath);
      NodeIdentifier identifier = NodeIdentifier.make(webContent);
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      PortletPreferences prefs = context.getRequest().getPreferences();
      prefs.setValue(UISingleContentViewerPortlet.REPOSITORY, identifier.getRepository());
      prefs.setValue(UISingleContentViewerPortlet.WORKSPACE, identifier.getWorkspace());
      prefs.setValue(UISingleContentViewerPortlet.IDENTIFIER, identifier.getUUID());
      prefs.store();
      WCMPublicationService wcmPublicationService = uiWebContentSelector.getApplicationComponent(WCMPublicationService.class);
      UIPortletConfig portletConfig = uiWebContentSelector.getAncestorOfType(UIPortletConfig.class);
      if (portletConfig.isEditPortletInCreatePageWizard()) {
        if (!wcmPublicationService.isEnrolledInWCMLifecycle(webContent)) {
          wcmPublicationService.enrollNodeInLifecycle(webContent, lifecycleName);          
        }
      } else {
        String pageId = Util.getUIPortal().getSelectedNode().getPageReference();
        UserPortalConfigService upcService = uiWebContentSelector.getApplicationComponent(UserPortalConfigService.class);
        Page page = upcService.getPage(pageId);
        try {
          if (!wcmPublicationService.isEnrolledInWCMLifecycle(webContent)) {
            wcmPublicationService.enrollNodeInLifecycle(webContent, lifecycleName);
            wcmPublicationService.updateLifecyleOnChangePage(page, WebuiRequestContext.getCurrentInstance().getRemoteUser());
          }
        }catch (NotInWCMPublicationException e){
          wcmPublicationService.unsubcribeLifecycle(webContent);
          wcmPublicationService.enrollNodeInLifecycle(webContent, lifecycleName);
          wcmPublicationService.updateLifecyleOnChangePage(page, WebuiRequestContext.getCurrentInstance().getRemoteUser());
        }
      }
      UIPortletConfig uiPortletConfig = uiWebContentSelector.getAncestorOfType(UIPortletConfig.class);
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
  public static class BackActionListener extends EventListener<UIWebContentSelectorForm> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIWebContentSelectorForm> event) throws Exception {
      UIWebContentSelectorForm uiWeSelector = event.getSource();
      UIPortletConfig uiPConfig = uiWeSelector.getAncestorOfType(UIPortletConfig.class);
      uiPConfig.getChildren().clear();
      uiPConfig.addChild(uiPConfig.getBackComponent());
      uiPConfig.showPopup(event.getRequestContext());
    }
  }

}
