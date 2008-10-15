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
package org.exoplatform.wcm.presentation.acp.config.selector;

import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.portlet.PortletMode;
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
import org.exoplatform.wcm.presentation.acp.UIAdvancedPresentationPortlet;
import org.exoplatform.wcm.presentation.acp.config.UIPortletConfig;
import org.exoplatform.wcm.presentation.acp.config.UIWCMPublicationGrid;
import org.exoplatform.wcm.presentation.acp.config.UIWelcomeScreen;
import org.exoplatform.wcm.webui.selector.document.UIDocumentPathSelector;
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
 *          dzungdev@gmail.com
 * Sep 3, 2008  
 */
@ComponentConfig (
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl",
    events = {
      @EventConfig(listeners = UIDMSSelectorForm.SaveActionListener.class),
      @EventConfig(listeners = UIDMSSelectorForm.BackActionListener.class),
      @EventConfig(listeners = UIDMSSelectorForm.BrowseActionListener.class),
      @EventConfig(listeners = UIDMSSelectorForm.BrowsePublicationActionListener.class)
    }
)
public class UIDMSSelectorForm extends UIForm implements UISelectable{

  final static String PATH = "path".intern();
  final static String FIELD_PATH = "location".intern();
  final static String PUBLICATION = "publication".intern();
  final static String PUBLICATION_PATH = "PublicationPath".intern();

  private String repository;
  private String workspace;
  private String livePortalsPath;
  private String liveSharedPortalName;

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

  public void init() throws Exception {
    WCMPublicationService wcmService = getApplicationComponent(WCMPublicationService.class);
    Map<String,WebpagePublicationPlugin> publicationPluginMap = wcmService.getWebpagePublicationPlugins();
    Set<String> keySet = publicationPluginMap.keySet();
    if (keySet.size() > 1) {
      UIFormInputSetWithAction uiPublicationSelector = new UIFormInputSetWithAction(PUBLICATION);
      uiPublicationSelector.addUIFormInput(new UIFormStringInput(PUBLICATION_PATH, PUBLICATION_PATH, null).setEditable(false));
      uiPublicationSelector.setActionInfo(PUBLICATION_PATH, new String [] {"BrowsePublication"});
      addChild(uiPublicationSelector);
    } else {
      for (String str: keySet) {
        WebpagePublicationPlugin publicationPlugin = publicationPluginMap.get(str);
        String lifecycleName = publicationPlugin.getLifecycleName();
        addChild(new UIFormStringInput(PUBLICATION, PUBLICATION, lifecycleName).setEditable(false));
      }
    }
  }

  public String getLivePortalPath() { return livePortalsPath; }
  public String getLiveSharedPortalName() { return liveSharedPortalName; }
  public String getRepositoryName() { return repository; }
  public String getWorkspace() { return workspace; }

  public void doSelect(String selectField, Object value) throws Exception {
    getUIStringInput(selectField).setValue((String)value);
    showPopupComponent(null);
  }

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

  public static class BrowsePublicationActionListener extends EventListener<UIDMSSelectorForm> {
    public void execute(Event<UIDMSSelectorForm> event) throws Exception {
      UIDMSSelectorForm uiDSelectorForm = event.getSource();
      UIWCMPublicationGrid publicationGrid = uiDSelectorForm.createUIComponent(UIWCMPublicationGrid.class, null, null);
      publicationGrid.setSourceComponent(uiDSelectorForm);
      publicationGrid.updateGrid();
      uiDSelectorForm.showPopupComponent(publicationGrid);
    }
  }

  public static class SaveActionListener extends EventListener<UIDMSSelectorForm> {
    public void execute(Event<UIDMSSelectorForm> event) throws Exception {
      UIDMSSelectorForm uiDMSSelectorForm = event.getSource();
      String dmsPath = uiDMSSelectorForm.getUIStringInput(UIDMSSelectorForm.PATH).getValue();
      String lifecycleName =  uiDMSSelectorForm.getUIStringInput(UIWebContentSelectorForm.PUBLICATION_PATH).getValue();
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
      prefs.setValue(UIAdvancedPresentationPortlet.REPOSITORY, nodeIdentifier.getRepository());
      prefs.setValue(UIAdvancedPresentationPortlet.WORKSPACE, nodeIdentifier.getWorkspace());
      prefs.setValue(UIAdvancedPresentationPortlet.UUID, nodeIdentifier.getUUID());
      prefs.store();

      WCMPublicationService wcmPublicationService = uiDMSSelectorForm.getApplicationComponent(WCMPublicationService.class);
      UIWelcomeScreen welcomeScreen = uiDMSSelectorForm.getAncestorOfType(UIWelcomeScreen.class);
      UIPortletConfig portletConfig = welcomeScreen.getAncestorOfType(UIPortletConfig.class);
      if (portletConfig.isEditPortletInCreatePageWizard()) {
        wcmPublicationService.enrollNodeInLifecycle(webContent, lifecycleName);
      } else {
        String pageId = Util.getUIPortal().getSelectedNode().getPageReference();
        UserPortalConfigService upcService = uiDMSSelectorForm.getApplicationComponent(UserPortalConfigService.class);
        Page page = upcService.getPage(pageId);
        try {
          if (!wcmPublicationService.isEnrolledInWCMLifecycle(webContent)) {
            wcmPublicationService.enrollNodeInLifecycle(webContent, lifecycleName);
            wcmPublicationService.updateLifecyleOnChangePage(page);
          }
        }catch (NotInWCMPublicationException e){
          wcmPublicationService.unsubcribeLifecycle(webContent);
          wcmPublicationService.enrollNodeInLifecycle(webContent, lifecycleName);
          wcmPublicationService.updateLifecyleOnChangePage(page);
        }
      }


      UIPortletConfig uiPortletConfig = uiDMSSelectorForm.getAncestorOfType(UIPortletConfig.class);
      if(uiPortletConfig.isEditPortletInCreatePageWizard()) {
        uiPortletConfig.getChildren().clear();
        uiPortletConfig.addUIWelcomeScreen();
      } else {        
        pContext.setApplicationMode(PortletMode.VIEW);
      }
    }
  }

  public static class BackActionListener extends EventListener<UIDMSSelectorForm> {
    public void execute(Event<UIDMSSelectorForm> event) {
      UIDMSSelectorForm uiSelectorForm = event.getSource();
      UIPortletConfig uiPortletConfig = uiSelectorForm.getAncestorOfType(UIPortletConfig.class);
      uiPortletConfig.getChildren().clear();
      uiPortletConfig.addChild(uiPortletConfig.getBackComponent());
    }
  }

  public static class BrowseActionListener extends EventListener<UIDMSSelectorForm> {
    public void execute(Event<UIDMSSelectorForm> event) throws Exception {
      UIDMSSelectorForm uiDMSSelectorForm = event.getSource();
      UIDocumentPathSelector uiDMSPathSelector = uiDMSSelectorForm.createUIComponent(UIDocumentPathSelector.class, null, null);
      uiDMSPathSelector.setSourceComponent(uiDMSSelectorForm, new String[] {UIDMSSelectorForm.PATH});
      uiDMSPathSelector.init();
      uiDMSSelectorForm.showPopupComponent(uiDMSPathSelector);
    }
  }

}
