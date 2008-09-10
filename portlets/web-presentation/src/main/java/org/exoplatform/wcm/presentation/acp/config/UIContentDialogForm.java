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

import java.security.AccessControlException;
import java.util.List;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.VersionException;

import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.form.UIDialogForm;
import org.exoplatform.ecm.webui.utils.DialogFormUtil;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.NodeIdentifier;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.exoplatform.wcm.presentation.acp.config.quickcreation.UIQuickCreationWizard;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * Jun 9, 2008  
 */

@ComponentConfig (
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = UIContentDialogForm.SaveActionListener.class),
      @EventConfig(listeners = UIContentDialogForm.CancelActionListener.class)
    }
)

public class UIContentDialogForm extends UIDialogForm {

  private JCRResourceResolver resourceResolver;
  private NodeLocation storedLocation;
  protected NodeIdentifier savedNodeIdentifier;
  protected Node webContent;

  public UIContentDialogForm() throws Exception {
    setActions(ACTIONS);
  }

  protected Node getWebContent () {
    return webContent;
  }

  protected void setWebContent(Node node) {
    webContent = node;
  }

  public void setStoredLocation(NodeLocation location) {
    storedLocation = location;
    setRepositoryName(location.getRepository());
    setWorkspace(location.getWorkspace());
    setStoredPath(location.getPath());
  }

  public NodeIdentifier getSavedNodeIdentifier() {
    return savedNodeIdentifier;
  }  

  public String getTemplate() {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String userName = Util.getPortalRequestContext().getRemoteUser();
    try{      
      return templateService.getTemplatePathByUser(true, contentType, userName, this.repositoryName);
    } catch(Exception e) {
      UIApplication uiApp = getAncestorOfType(UIApplication.class);
      Object[] arg = {contentType};
      uiApp.addMessage(new ApplicationMessage("UIDocumentForm.msg.not-support", arg, ApplicationMessage.ERROR));
      return null;
    }
  }

  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    try{
      if (resourceResolver == null) {
        RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
        ManageableRepository manageableRepository = repositoryService.getRepository(this.repositoryName);
        String workspace = manageableRepository.getConfiguration().getSystemWorkspaceName();
        resourceResolver = new JCRResourceResolver(this.repositoryName, workspace, TemplateService.EXO_TEMPLATE_FILE_PROP);
      }
    }catch(Exception e) {}
    return resourceResolver;
  }

  public void onchange(Event event) throws Exception {

  }

  private Node getParentNode() throws Exception {
    String repository = storedLocation.getRepository();
    String path = storedLocation.getPath();
    String workspace = storedLocation.getWorkspace();
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    SessionProvider provider = SessionProviderFactory.createSessionProvider();
    Session session = provider.getSession(workspace, manageableRepository);
    Node parentNode = (Node) session.getItem(path);
    return parentNode;
  }

  static public class CancelActionListener extends EventListener<UIContentDialogForm> {
    public void execute(Event<UIContentDialogForm> event) throws Exception {
      UIContentDialogForm uiContentDialogForm = event.getSource();
      UIQuickCreationWizard uiQuickCreationWizard = uiContentDialogForm.getAncestorOfType(UIQuickCreationWizard.class);
      UIPortletConfig uiPortletConfig = uiContentDialogForm.getAncestorOfType(UIPortletConfig.class);
      UINameWebContentForm uiNameWebContentForm = uiQuickCreationWizard.getChild(UINameWebContentForm.class);
      if(uiPortletConfig.isNewConfig()) {
        String portalName = Util.getUIPortal().getName();
        LivePortalManagerService livePortalManagerService = uiNameWebContentForm.getApplicationComponent(LivePortalManagerService.class);
        SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
        Node portalNode = livePortalManagerService.getLivePortal(portalName, sessionProvider);
        WebSchemaConfigService webSchemaConfigService = uiNameWebContentForm.getApplicationComponent(WebSchemaConfigService.class);
        PortalFolderSchemaHandler handler = webSchemaConfigService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
        Node webContentStorage = handler.getWebContentStorage(portalNode);
        String webContentName = ((UIFormStringInput)uiNameWebContentForm.getChildById(UINameWebContentForm.NAME_WEBCONTENT)).getValue();
        Node webContentNode = webContentStorage.getNode(webContentName);
        Session session = webContentNode.getSession();
        webContentNode.remove();
        session.save();
        uiNameWebContentForm.reset();
      } else {
        uiNameWebContentForm.init();
      }
      uiContentDialogForm.reset();
      uiQuickCreationWizard.viewStep(1);
    }
  }

  static  public class SaveActionListener extends EventListener<UIContentDialogForm> {
    public void execute(Event<UIContentDialogForm> event) throws Exception {
      UIContentDialogForm dialogForm = event.getSource();
      List inputs = dialogForm.getChildren();
      Map inputProperties = DialogFormUtil.prepareMap(inputs, dialogForm.getInputProperties());
      Node newNode = null;
      String nodeType;
      Node homeNode;
      UIApplication uiApplication = dialogForm.getAncestorOfType(UIApplication.class);
      if (dialogForm.isAddNew()) {
        homeNode = dialogForm.getParentNode();
        nodeType = dialogForm.contentType;
      } else {
        homeNode = dialogForm.getNode().getParent();
        nodeType = dialogForm.getNode().getPrimaryNodeType().getName();
      }
      try{
        CmsService cmsService = dialogForm.getApplicationComponent(CmsService.class);
        String addedPath = cmsService.storeNode(nodeType, homeNode, inputProperties, dialogForm.isAddNew, dialogForm.repositoryName);
        try{
          homeNode.save();
          newNode = (Node) homeNode.getSession().getItem(addedPath);
          event.getRequestContext().setAttribute("nodePath",newNode.getPath());
        }catch(Exception e) {} 
      }catch(AccessControlException ace) {
        throw new AccessDeniedException(ace.getMessage());
      }catch(VersionException ve) {
        uiApplication.addMessage(new ApplicationMessage("UIDocumentForm.msg.in-versioning", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
      }catch(ItemNotFoundException item) {
        uiApplication.addMessage(new ApplicationMessage("UIDocumentForm.msg.item-not-found", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
      }catch(RepositoryException repo) {
        repo.printStackTrace();
        String key = "UIDocumentForm.msg.repository-exception";
        if (ItemExistsException.class.isInstance(repo)) key = "UIDocumentForm.msg.not-allowed-same-name-sibling";
        uiApplication.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
      }catch(NumberFormatException nfe) {
        uiApplication.addMessage(new ApplicationMessage("UIDocumentForm.msg.numberformat-exception", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
      }catch(Exception e) {
        e.printStackTrace() ;
        uiApplication.addMessage(new ApplicationMessage("UIDocumentForm.msg.cannot-save", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
      }
      dialogForm.savedNodeIdentifier = NodeIdentifier.make(newNode);
      dialogForm.setWebContent(newNode);
      UIQuickCreationWizard uiQuickWizard = dialogForm.getAncestorOfType(UIQuickCreationWizard.class);
      UIPermissionManager uiPermissionManager = uiQuickWizard.getChild(UIPermissionManager.class);
      ((UIPermissionInfo) uiPermissionManager.getChild(UIPermissionInfo.class)).updateGrid();
      UISocialInfo uiSocialInfo = uiQuickWizard.getChild(UISocialInfo.class);
      uiSocialInfo.initUICategorizing();
      uiQuickWizard.viewStep(3);
    }
  }
}
