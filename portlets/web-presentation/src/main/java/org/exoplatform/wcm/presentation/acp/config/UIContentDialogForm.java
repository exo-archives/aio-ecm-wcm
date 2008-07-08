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

import org.exoplatform.dms.application.JCRResourceResolver;
import org.exoplatform.dms.webui.form.UIBaseDialogForm;
import org.exoplatform.dms.webui.utils.Utils;
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
import org.exoplatform.wcm.presentation.acp.config.quickcreation.UIQuickCreationWizard;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

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

public class UIContentDialogForm extends UIBaseDialogForm {

  private String contentType;
  private boolean isAddNew = false;
  private JCRResourceResolver resourceResolver;
  private NodeLocation storedLocation;
  private NodeIdentifier savedNodeIdentifier;

  public UIContentDialogForm() throws Exception {
    setActions(new String[] {"Save", "Cancel"});
  }

  public void setStoredLocation(NodeLocation location) {
    this.storedLocation = location;
    setRepositoryName(location.getRepository());
    setWorkspace(location.getWorkspace());
    setStoredPath(location.getPath());
  }

  public NodeIdentifier getSavedNodeIdentifier() {
    return this.savedNodeIdentifier;
  }

  public void addNew(boolean addNew) {
    isAddNew = addNew;
  }

  public boolean isAddNew() {
    return isAddNew;
  }

  public String getTemplate() {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String userName = Util.getPortalRequestContext().getRemoteUser();
    try{
      resetScriptInterceptor();
      return templateService.getTemplatePathByUser(true, contentType, userName, repositoryName_);
    } catch(Exception e) {
      UIApplication uiApp = getAncestorOfType(UIApplication.class);
      Object[] arg = {contentType};
      uiApp.addMessage(new ApplicationMessage("UIDocumentForm.msg.not-support", arg, ApplicationMessage.ERROR));
      return null;
    }
  }

  public void setContentType(String type){
    this.contentType = type;
  }

  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    try{
      if (resourceResolver == null) {
        RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
        ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName_);
        String workspace = manageableRepository.getConfiguration().getSystemWorkspaceName();
        resourceResolver = new JCRResourceResolver(repositoryName_, workspace, "exo:templateFile");
      }
    }catch(Exception e) {}
    return resourceResolver;
  }

  public void onchange(Event event) throws Exception {

  }

  @Override
  public Node storeValue(Event event) throws Exception {
    List inputs = getChildren() ;
    Map inputProperties = Utils.prepareMap(inputs, getInputProperties());
    Node newNode = null;
    String nodeType;
    Node homeNode;
    UIApplication uiApplication = getAncestorOfType(UIApplication.class);
    if (isAddNew()) {
      homeNode = getParentNode();
      nodeType = contentType;
    } else {
      homeNode = getNode().getParent();
      nodeType = getNode().getPrimaryNodeType().getName();
    }
    try{
      CmsService cmsService = getApplicationComponent(CmsService.class);
      String addedPath = cmsService.storeNode(nodeType, homeNode, inputProperties, isAddNew, repositoryName_);
      try{
        homeNode.save();
        newNode = (Node) homeNode.getSession().getItem(addedPath);
      }catch(Exception e) {return null;} 
    }catch(AccessControlException ace) {
      throw new AccessDeniedException(ace.getMessage());
    }catch(VersionException ve) {
      uiApplication.addMessage(new ApplicationMessage("UIDocumentForm.msg.in-versioning", null, ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
      return null;
    }catch(ItemNotFoundException item) {
      uiApplication.addMessage(new ApplicationMessage("UIDocumentForm.msg.item-not-found", null, ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
      return null;
    }catch(RepositoryException repo) {
      repo.printStackTrace();
      String key = "UIDocumentForm.msg.repository-exception";
      if (ItemExistsException.class.isInstance(repo)) key = "UIDocumentForm.msg.not-allowed-same-name-sibling";
      uiApplication.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
      return null;
    }catch(NumberFormatException nfe) {
      uiApplication.addMessage(new ApplicationMessage("UIDocumentForm.msg.numberformat-exception", null, ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
      return null;
    }catch(Exception e) {
      e.printStackTrace() ;
      uiApplication.addMessage(new ApplicationMessage("UIDocumentForm.msg.cannot-save", null, ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
      return null;
    }
    savedNodeIdentifier = NodeIdentifier.make(newNode);
    return newNode;
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

  public boolean isEditing() { return !isAddNew; }

  static public class CancelActionListener extends EventListener<UIContentDialogForm> {
    public void execute(Event<UIContentDialogForm> event) throws Exception {

    }
  }

  static  public class SaveActionListener extends EventListener<UIBaseDialogForm> {
    public void execute(Event<UIBaseDialogForm> event) throws Exception {
      UIBaseDialogForm dialogForm = event.getSource();
      dialogForm.save(event);
      UIQuickCreationWizard uiQuickWizard = event.getSource().getParent();
      uiQuickWizard.viewStep(uiQuickWizard.getCurrentStep()+1);
    }
  }

}
