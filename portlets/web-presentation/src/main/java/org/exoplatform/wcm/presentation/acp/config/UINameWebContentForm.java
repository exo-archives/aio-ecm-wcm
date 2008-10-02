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

import javax.jcr.Node;
import javax.jcr.Session;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.exoplatform.services.wcm.webcontent.WebContentSchemaHandler;
import org.exoplatform.wcm.presentation.acp.UIAdvancedPresentationPortlet;
import org.exoplatform.wcm.presentation.acp.config.quickcreation.UIQuickCreationWizard;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.IdentifierValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;

/**
 * Created by The eXo Platform SAS Author : DANG TAN DUNG dzungdev@gmail.com Sep
 * 8, 2008
 */

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIForm.gtmpl", events = {
    @EventConfig(listeners = UINameWebContentForm.SaveActionListener.class),
    @EventConfig(listeners = UINameWebContentForm.AbortActionListener.class, phase = Phase.DECODE) })
public class UINameWebContentForm extends UIForm {

  public static final String NAME_WEBCONTENT    = "name".intern();

  public static final String SUMMARY_WEBCONTENT = "summary".intern();

  public UINameWebContentForm() throws Exception {
    addUIFormInput(new UIFormStringInput(NAME_WEBCONTENT, NAME_WEBCONTENT, null).addValidator(
        MandatoryValidator.class).addValidator(IdentifierValidator.class));
    addUIFormInput(new UIFormWYSIWYGInput(SUMMARY_WEBCONTENT, SUMMARY_WEBCONTENT, null));
  }

  public void init() throws Exception {
    if (!isNewConfig()) {
      Node currentNode = getNode();
      String summary = "";
      if (currentNode.hasProperty("exo:summary")) {
        summary = currentNode.getProperty("exo:summary").getValue().getString();
      }
      UIFormWYSIWYGInput uiFormWYSIWYGInput = getChild(UIFormWYSIWYGInput.class);
      uiFormWYSIWYGInput.setValue(summary);
      UIFormStringInput uiFormStringInput = getChild(UIFormStringInput.class);
      uiFormStringInput.setValue(currentNode.getName());
      uiFormStringInput.setEditable(false);
    }
  }

  public Node getNode() throws Exception {
    PortletRequestContext context = WebuiRequestContext.getCurrentInstance();
    PortletPreferences prefs = context.getRequest().getPreferences();
    String repositoryName = prefs.getValue(UIAdvancedPresentationPortlet.REPOSITORY, null);
    String workspace = prefs.getValue(UIAdvancedPresentationPortlet.WORKSPACE, null);
    String UUID = prefs.getValue(UIAdvancedPresentationPortlet.UUID, null);
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName);
    Session session = SessionProviderFactory.createSystemProvider().getSession(workspace,
        manageableRepository);
    return session.getNodeByUUID(UUID);
  }

  public boolean isNewConfig() {
    UIPortletConfig uiPortletConfig = getAncestorOfType(UIPortletConfig.class);
    return uiPortletConfig.isNewConfig();
  }

  public static class SaveActionListener extends EventListener<UINameWebContentForm> {
    public void execute(Event<UINameWebContentForm> event) throws Exception {
      UINameWebContentForm uiNameWebContentForm = event.getSource();
      String portalName = Util.getUIPortal().getName();
      LivePortalManagerService livePortalManagerService = uiNameWebContentForm
          .getApplicationComponent(LivePortalManagerService.class);
      SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
      Node portalNode = livePortalManagerService.getLivePortal(portalName, sessionProvider);
      WebSchemaConfigService webSchemaConfigService = uiNameWebContentForm
          .getApplicationComponent(WebSchemaConfigService.class);
      PortalFolderSchemaHandler handler = webSchemaConfigService
          .getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
      Node webContentStorage = handler.getWebContentStorage(portalNode);
      String webContentName = ((UIFormStringInput) uiNameWebContentForm
          .getChildById(NAME_WEBCONTENT)).getValue();
      String summaryContent = uiNameWebContentForm.getChild(UIFormWYSIWYGInput.class).getValue();
      Node webContentNode = null;
      UIQuickCreationWizard uiQuickCreationWizard = uiNameWebContentForm
      .getAncestorOfType(UIQuickCreationWizard.class);
      UIContentDialogForm uiCDForm = uiQuickCreationWizard.getChild(UIContentDialogForm.class);
      if (uiNameWebContentForm.isNewConfig()) {
        webContentNode = webContentStorage.addNode(webContentName, "exo:webContent");
        WebContentSchemaHandler webContentSchemaHandler = webSchemaConfigService
            .getWebSchemaHandlerByType(WebContentSchemaHandler.class);
        webContentSchemaHandler.createDefaultSchema(webContentNode);
      } else {
        webContentNode = uiNameWebContentForm.getNode();
      }
      if (!webContentNode.isCheckedOut()) {
        webContentNode.checkout();
        uiCDForm.setCheckInOpened(true);
        webContentNode.getSession().save();
      }
      if (webContentNode.canAddMixin("mix:votable"))
        webContentNode.addMixin("mix:votable");
      if (webContentNode.canAddMixin("mix:commentable"))
        webContentNode.addMixin("mix:commentable");
      webContentNode.setProperty("exo:summary", summaryContent);
      webContentStorage.getSession().save();
      String repositoryName = ((ManageableRepository) webContentNode.getSession().getRepository())
          .getConfiguration().getName();
      String workspaceName = webContentNode.getSession().getWorkspace().getName();
      NodeLocation nodeLocation = new NodeLocation();
      nodeLocation.setRepository(repositoryName);
      nodeLocation.setWorkspace(workspaceName);
      nodeLocation.setPath(webContentNode.getParent().getPath());
      uiCDForm.setStoredLocation(nodeLocation);
      uiCDForm.setNodePath(webContentNode.getPath());
      uiCDForm.setContentType("exo:webContent");
      uiCDForm.addNew(false);
      uiCDForm.resetProperties();
      uiQuickCreationWizard.viewStep(2);
    }
  }

  public static class AbortActionListener extends EventListener<UINameWebContentForm> {
    public void execute(Event<UINameWebContentForm> event) throws Exception {
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      context.setApplicationMode(PortletMode.VIEW);
    }
  }
}
