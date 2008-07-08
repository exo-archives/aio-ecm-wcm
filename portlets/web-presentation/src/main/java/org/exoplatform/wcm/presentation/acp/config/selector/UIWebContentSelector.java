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

import javax.jcr.Node;
import javax.jcr.Session;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.dms.webui.component.UINodesExplorer;
import org.exoplatform.dms.webui.component.UISelectable;
import org.exoplatform.dms.webui.form.UIFormInputSetWithAction;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.NodeIdentifier;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.wcm.presentation.acp.UIAdvancedPresentationPortlet;
import org.exoplatform.wcm.presentation.acp.config.UIPortletConfig;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * May 27, 2008  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl",
    events = {
      @EventConfig(listeners = UIWebContentSelector.SaveActionListener.class),
      @EventConfig(listeners = UIWebContentSelector.BackActionListener.class),
      @EventConfig(listeners = UIWebContentSelector.BrowseActionListener.class)
    }
)

public class UIWebContentSelector extends UIForm implements UISelectable{

  final static String PATH = "path".intern();
  final static String FIELD_PATH = "location".intern();
  private String repository;
  private String workspace;
  private String livePortalsPath;
  private String liveSharedPortalName;

  public UIWebContentSelector() throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    String repoName = repositoryService.getCurrentRepository().getConfiguration().getName();
    WCMConfigurationService configurationService = getApplicationComponent(WCMConfigurationService.class);
    NodeLocation nodeLocation = configurationService.getLivePortalsLocation(repoName);
    repository = nodeLocation.getRepository();
    workspace = nodeLocation.getWorkspace();
    livePortalsPath = nodeLocation.getPath();
    liveSharedPortalName = configurationService.getSharedPortalName(repository);
    addChild(new UIFormTextAreaInput("Describe", "Describe", ""));
    UIFormInputSetWithAction uiPathSelection = new UIFormInputSetWithAction(FIELD_PATH);
    uiPathSelection.addUIFormInput(new UIFormStringInput(PATH, PATH, null).setEditable(false));
    uiPathSelection.setActionInfo(PATH, new String [] {"Browse"});
    addChild(uiPathSelection);
    setActions(new String[] {"Save", "Back"});
  }

  public void doSelect(String selectField, String value) throws Exception {
    getUIStringInput(selectField).setValue(value);
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
    uiPopup.setShow(true);
  }

  public String getLivePortalPath() { return livePortalsPath; }
  public String getLiveSharedPortalName() { return liveSharedPortalName; }
  public String getRepositoryName() { return repository; }
  public String getWorkspace() { return workspace; }

  public static class BrowseActionListener extends EventListener<UIWebContentSelector> {
    public void execute(Event<UIWebContentSelector> event) throws Exception {
      UIWebContentSelector uiWebContentSelector = event.getSource();
      UINodesExplorer uiExplorer = uiWebContentSelector.createUIComponent(UINodesExplorer.class, null, null);
      uiExplorer.setRepository(uiWebContentSelector.getRepositoryName());
      uiExplorer.setIsDisable(uiWebContentSelector.getWorkspace(), true);
      String sharedPortal = uiWebContentSelector.getLiveSharedPortalName();
      uiExplorer.setAllowedNodes(new String [] {Util.getUIPortal().getName(), sharedPortal});
      uiExplorer.setRootPath(uiWebContentSelector.getLivePortalPath());
      uiExplorer.setFilterNodeType(new String[] {"exo:webFolder", "exo:portalFolder"});
      if(event.getRequestContext().getRemoteUser() == null) {
        uiExplorer.setSessionProvider(SessionProviderFactory.createAnonimProvider());
      }
      String [] filterType = new String[] { "exo:webContent" };
      uiExplorer.setFilterType(filterType);
      uiExplorer.setComponent(uiWebContentSelector, new String [] {UIWebContentSelector.PATH}); 
      uiWebContentSelector.showPopupComponent(uiExplorer); 
    }
  }

  public static class SaveActionListener extends EventListener<UIWebContentSelector> {
    public void execute(Event<UIWebContentSelector> event) throws Exception {
      UIWebContentSelector uiWebContentSelector = event.getSource();
      RepositoryService repositoryService = uiWebContentSelector.getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getRepository(uiWebContentSelector.getRepositoryName());
      Session session = SessionProvider.createSystemProvider().getSession(uiWebContentSelector.getWorkspace(), manageableRepository);
      Node node = (Node) session.getItem(uiWebContentSelector.getUIStringInput(UIWebContentSelector.PATH).getValue());
      NodeIdentifier identifier = NodeIdentifier.make(node);
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      PortletPreferences prefs = context.getRequest().getPreferences();
      prefs.setValue(UIAdvancedPresentationPortlet.REPOSITORY, identifier.getRepository());
      prefs.setValue(UIAdvancedPresentationPortlet.WORKSPACE, identifier.getWorkspace());
      prefs.setValue(UIAdvancedPresentationPortlet.UUID, identifier.getUUID());
      prefs.store();
      context.setApplicationMode(PortletMode.VIEW ) ;
    }
  }

  public static class BackActionListener extends EventListener<UIWebContentSelector> {
    public void execute(Event<UIWebContentSelector> event) throws Exception {
      UIWebContentSelector uiWeSelector = event.getSource();
      UIPortletConfig uiPConfig = uiWeSelector.getAncestorOfType(UIPortletConfig.class);
      uiPConfig.getChildren().clear();
      uiPConfig.addChild(uiPConfig.getBackComponent());
    }
  }
}
