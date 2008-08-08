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

import org.exoplatform.ecm.webui.selector.UISelectable;
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
import org.exoplatform.webui.form.ext.UIFormInputSetWithAction;

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
      @EventConfig(listeners = UIWebContentSelectorForm.SaveActionListener.class),
      @EventConfig(listeners = UIWebContentSelectorForm.BackActionListener.class),
      @EventConfig(listeners = UIWebContentSelectorForm.BrowseActionListener.class)
    }
)

public class UIWebContentSelectorForm extends UIForm implements UISelectable{

  final static String PATH = "path".intern();
  final static String FIELD_PATH = "location".intern();
  private String repository;
  private String workspace;
  private String livePortalsPath;
  private String liveSharedPortalName;

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

  public String getLivePortalPath() { return livePortalsPath; }
  public String getLiveSharedPortalName() { return liveSharedPortalName; }
  public String getRepositoryName() { return repository; }
  public String getWorkspace() { return workspace; }

  public static class BrowseActionListener extends EventListener<UIWebContentSelectorForm> {
    public void execute(Event<UIWebContentSelectorForm> event) throws Exception {
      UIWebContentSelectorForm uiWebContentSelector = event.getSource();
      UIWebContentPathSelector webContentPathSelector = uiWebContentSelector.createUIComponent(UIWebContentPathSelector.class, null, null);
      webContentPathSelector.setSourceComponent(uiWebContentSelector, new String[] {UIWebContentSelectorForm.PATH});
      webContentPathSelector.init();      
      uiWebContentSelector.showPopupComponent(webContentPathSelector);      
    }
  }

  public static class SaveActionListener extends EventListener<UIWebContentSelectorForm> {
    public void execute(Event<UIWebContentSelectorForm> event) throws Exception {
      UIWebContentSelectorForm uiWebContentSelector = event.getSource();
      RepositoryService repositoryService = uiWebContentSelector.getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getRepository(uiWebContentSelector.getRepositoryName());
      Session session = SessionProvider.createSystemProvider().getSession(uiWebContentSelector.getWorkspace(), manageableRepository);
      Node node = (Node) session.getItem(uiWebContentSelector.getUIStringInput(UIWebContentSelectorForm.PATH).getValue());
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

  public static class BackActionListener extends EventListener<UIWebContentSelectorForm> {
    public void execute(Event<UIWebContentSelectorForm> event) throws Exception {
      UIWebContentSelectorForm uiWeSelector = event.getSource();
      UIPortletConfig uiPConfig = uiWeSelector.getAncestorOfType(UIPortletConfig.class);
      uiPConfig.getChildren().clear();
      uiPConfig.addChild(uiPConfig.getBackComponent());
    }
  }
  
}
