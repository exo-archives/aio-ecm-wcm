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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.webui.form.validator.ECMNameValidator;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.exoplatform.services.wcm.publication.WCMPublicationService;
import org.exoplatform.services.wcm.webcontent.WebContentSchemaHandler;
import org.exoplatform.wcm.webui.scv.UISingleContentViewerPortlet;
import org.exoplatform.wcm.webui.scv.config.publication.UIWCMPublicationGrid;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SAS Author : DANG TAN DUNG dzungdev@gmail.com Sep
 * 8, 2008
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/groovy/SingleContentViewer/config/UINameWebContentForm.gtmpl", 
    events = {
      @EventConfig(listeners = UINameWebContentForm.SaveActionListener.class),
      @EventConfig(listeners = UINameWebContentForm.AbortActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UINameWebContentForm.ChangeTemplateTypeActionListener.class, phase = Phase.DECODE)
    }
)
public class UINameWebContentForm extends UIForm {

  public static final String NAME_WEBCONTENT    = "name".intern();
  public static final String SUMMARY_WEBCONTENT = "summary".intern();
  public static final String FIELD_SELECT = "selectTemplate".intern();
  private String pictureCSS;

  public UINameWebContentForm() throws Exception {
    addUIFormInput(new UIFormStringInput(NAME_WEBCONTENT, NAME_WEBCONTENT, null).addValidator(
        MandatoryValidator.class).addValidator(ECMNameValidator.class));
    setActions(new String[] {"Save", "Abort"});
  }

  public void init() throws Exception {
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    String repositoryName = repositoryService.getCurrentRepository().getConfiguration().getName();
    NodeTypeManager nodeTypeManager = repositoryService.getRepository(repositoryName).getNodeTypeManager();
    List<String> documentTemplates = templateService.getDocumentTemplates(repositoryName);
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    for (String documentTemplate: documentTemplates) {
      NodeType nodeType = nodeTypeManager.getNodeType(documentTemplate);
      if (nodeType.isNodeType("exo:webContent")) {
        String contentType = nodeType.getName();
        String templateLabel = templateService.getTemplateLabel(contentType, repositoryName);
        options.add(new SelectItemOption<String>(templateLabel, contentType));
      }
    }
    UIFormSelectBox templateSelect = new UIFormSelectBox(FIELD_SELECT, FIELD_SELECT, options) ;
    templateSelect.setOnChange("ChangeTemplateType");
    templateSelect.setDefaultValue("exo:webContent");
    setPictureCSS("exo_webContent");
    addUIFormInput(templateSelect) ;
    if (!isNewConfig()) {
      Node currentNode = getNode();
      UIFormStringInput uiFormStringInput = getChild(UIFormStringInput.class);
      uiFormStringInput.setValue(currentNode.getName());
      uiFormStringInput.setEditable(false);
    }
  }

  public Node getNode() throws Exception {
    PortletRequestContext context = WebuiRequestContext.getCurrentInstance();
    PortletPreferences prefs = context.getRequest().getPreferences();
    String repositoryName = prefs.getValue(UISingleContentViewerPortlet.REPOSITORY, null);
    String workspace = prefs.getValue(UISingleContentViewerPortlet.WORKSPACE, null);
    String UUID = prefs.getValue(UISingleContentViewerPortlet.IDENTIFIER, null);
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

  public String getPictureCSS() {
    return pictureCSS;
  }

  public void setPictureCSS(String pictureCSS) {
    this.pictureCSS = pictureCSS;
  }

  public static class SaveActionListener extends EventListener<UINameWebContentForm> {
    public void execute(Event<UINameWebContentForm> event) throws Exception {
      UINameWebContentForm uiNameWebContentForm = event.getSource();
      UIWebConentNameTabForm webConentNameTabForm = uiNameWebContentForm.getAncestorOfType(UIWebConentNameTabForm.class);
      UIWCMPublicationGrid wcPublicationGrid = webConentNameTabForm.getChild(UIWCMPublicationGrid.class);
      String lifecycleNameSelected = wcPublicationGrid.getLifecycleNameSelected();
      UIApplication uiApplication = uiNameWebContentForm.getAncestorOfType(UIApplication.class);
      if (lifecycleNameSelected == null) {
        uiApplication.addMessage(new ApplicationMessage("UINameWebContentForm.msg.non-lifecyclename", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
        return;
      }
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
      Node webContentNode = null;
      UIQuickCreationWizard uiQuickCreationWizard = uiNameWebContentForm
      .getAncestorOfType(UIQuickCreationWizard.class);
      UIContentDialogForm uiCDForm = uiQuickCreationWizard.getChild(UIContentDialogForm.class);
      String contentType = uiNameWebContentForm.getUIFormSelectBox(FIELD_SELECT).getValue();
      if (uiNameWebContentForm.isNewConfig()) {
        webContentNode = webContentStorage.addNode(webContentName, contentType);
        WebContentSchemaHandler webContentSchemaHandler = webSchemaConfigService
        .getWebSchemaHandlerByType(WebContentSchemaHandler.class);
        webContentSchemaHandler.createDefaultSchema(webContentNode);
        String htmlContent = "";
        String cssContent = "";
        if (contentType.equals("exo:pictureOnHeadWebcontent")) {
          htmlContent = "<div class=\"WebContentPageOnTop\"><img width=\"100px\" height=\"100px\"/></div><div class=\"WebContentHint\"><h3>Type the title at here</h3><span class=\"DropCap\">T</span>ype the text here</div><div style=\"clear: left\"><span></span></div>";
          cssContent = ".WebContentPageOnTop {\n\tfloat: left; \n\tmargin-right: 2px;\n}\n\n" + 
          ".DropCap {\n\tfont-size: 28px; \n\tmargin-right: 2px;\n} \n.WebContentHint {\n\tfloat: left}";
          webContentNode.getNode("default.html/jcr:content").setProperty("jcr:data", htmlContent);
          webContentNode.getNode("css/default.css/jcr:content").setProperty("jcr:data", cssContent);
        } else if (contentType.equals("exo:twoColumnsWebcontent")) {
          htmlContent = "<table class=\"WebContentTwoColumns\" cellspacing=\"10\">" +
          "<tr>" +
          "<td>&nbsp;" +
          "</td>" + 
          "<td>&nbsp;" +
          "</td>" +                                 
          "</tr>" +
          "</table>";
          cssContent = ".WebContentTwoColumns {\n\twidth: 100%; \n\theight: 100%;\n}\n\n";
          webContentNode.getNode("default.html/jcr:content").setProperty("jcr:data", htmlContent);
          webContentNode.getNode("css/default.css/jcr:content").setProperty("jcr:data", cssContent);
        }

      } else {
        webContentNode = uiNameWebContentForm.getNode();
      }
      if (webContentNode.canAddMixin("mix:votable"))
        webContentNode.addMixin("mix:votable");
      if (webContentNode.canAddMixin("mix:commentable"))
        webContentNode.addMixin("mix:commentable");
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
      uiCDForm.setContentType(contentType);
      uiCDForm.addNew(false);
      uiCDForm.resetProperties();
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      PortletPreferences prefs = context.getRequest().getPreferences();
      prefs.setValue(UISingleContentViewerPortlet.REPOSITORY, repositoryName);
      prefs.setValue(UISingleContentViewerPortlet.WORKSPACE, workspaceName);
      prefs.setValue(UISingleContentViewerPortlet.IDENTIFIER, webContentNode.getUUID());
      prefs.store();
      WCMPublicationService wcmPublicationService = uiNameWebContentForm.getApplicationComponent(WCMPublicationService.class);
      wcmPublicationService.enrollNodeInLifecycle(webContentNode, lifecycleNameSelected);
      UIPortletConfig portletConfig = uiQuickCreationWizard.getAncestorOfType(UIPortletConfig.class);
      if (!portletConfig.isEditPortletInCreatePageWizard()) {
        String pageId = Util.getUIPortal().getSelectedNode().getPageReference();
        UserPortalConfigService upcService = uiNameWebContentForm.getApplicationComponent(UserPortalConfigService.class);
        wcmPublicationService.updateLifecyleOnChangePage(upcService.getPage(pageId));
      }
      uiQuickCreationWizard.viewStep(2);
    }
  }

  public static class AbortActionListener extends EventListener<UINameWebContentForm> {
    public void execute(Event<UINameWebContentForm> event) throws Exception {
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      UIPortletConfig uiPortletConfig = event.getSource().getAncestorOfType(UIPortletConfig.class);
      if(uiPortletConfig.isEditPortletInCreatePageWizard()) {
        uiPortletConfig.getChildren().clear();
        uiPortletConfig.addUIWelcomeScreen();
      } else {        
        context.setApplicationMode(PortletMode.VIEW);
      }
    }
  }

  public static class ChangeTemplateTypeActionListener extends EventListener<UINameWebContentForm> {
    public void execute(Event<UINameWebContentForm> event) throws Exception {
      UINameWebContentForm uiNameWebContentForm = event.getSource();
      String contentType = uiNameWebContentForm.getUIFormSelectBox(FIELD_SELECT).getValue();
      if (contentType.equals("exo:webContent")) {
        uiNameWebContentForm.setPictureCSS("exo_webContent");
      } else if (contentType.equals("exo:twoColumnsWebcontent")) {
        uiNameWebContentForm.setPictureCSS("exo_twoColumnsWebcontent");
      } else {
        uiNameWebContentForm.setPictureCSS("exo_pictureOnHeadWebcontent");
      }
    }
  }

}
