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
package org.exoplatform.wcm.webui.scv.config.quickedition;

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
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.webui.utils.DialogFormUtil;
import org.exoplatform.ecm.webui.utils.LockUtil;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.NodeIdentifier;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.wcm.webui.scv.UISingleContentViewerPortlet;
import org.exoplatform.wcm.webui.scv.config.UIContentDialogForm;
import org.exoplatform.wcm.webui.scv.config.UIPortletConfig;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
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
 * Sep 16, 2008  
 */

@ComponentConfig (
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = UIQuickEditWebContentForm.SaveActionListener.class),
      @EventConfig(listeners = UIQuickEditWebContentForm.CancelActionListener.class)
    }
)

public class UIQuickEditWebContentForm extends UIContentDialogForm{

  public UIQuickEditWebContentForm() throws Exception {
    setActions(ACTIONS);
  }

  public void init() throws Exception {
    PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences prefs = pContext.getRequest().getPreferences();
    String repositoryName = prefs.getValue(UISingleContentViewerPortlet.REPOSITORY, null);
    String workspaceName = prefs.getValue(UISingleContentViewerPortlet.WORKSPACE, null);
    String nodeIdentifier = prefs.getValue(UISingleContentViewerPortlet.IDENTIFIER, null);
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName);
    Session session = SessionProviderFactory.createSystemProvider().getSession(workspaceName, manageableRepository);
    Node webContentNode = null; 
    try {
      webContentNode = session.getNodeByUUID(nodeIdentifier);
    } catch (Exception e) {
      webContentNode = (Node) session.getItem(nodeIdentifier);
    }
      
    NodeLocation nodeLocation = new NodeLocation();
    nodeLocation.setRepository(repositoryName);
    nodeLocation.setWorkspace(workspaceName);
    nodeLocation.setPath(webContentNode.getParent().getPath());
    this.setStoredLocation(nodeLocation);
    this.setNodePath(webContentNode.getPath());
    this.setContentType(webContentNode.getPrimaryNodeType().getName());
    this.addNew(false);
    this.resetProperties();
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

  private boolean nodeIsLocked(Node node) throws Exception {
    if(!node.isLocked()) return false;        
    String lockToken = LockUtil.getLockToken(node);
    if(lockToken != null) {
      node.getSession().addLockToken(lockToken);
      return false;
    }                
    return true;
  }

  public static class SaveActionListener extends EventListener<UIQuickEditWebContentForm> {
    public void execute(Event<UIQuickEditWebContentForm> event) throws Exception {
      UIQuickEditWebContentForm uiQuickEditForm = event.getSource();
      PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
      PortletPreferences prefs = pContext.getRequest().getPreferences();
      String repositoryName = prefs.getValue(UISingleContentViewerPortlet.REPOSITORY, null);
      String workspaceName = prefs.getValue(UISingleContentViewerPortlet.WORKSPACE, null);
      String UUID = prefs.getValue(UISingleContentViewerPortlet.IDENTIFIER, null);
      RepositoryService repositoryService = uiQuickEditForm.getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName);
      Session session = SessionProviderFactory.createSystemProvider().getSession(workspaceName, manageableRepository);
      Node webContentNode = session.getNodeByUUID(UUID);
      UIApplication uiApplication = uiQuickEditForm.getAncestorOfType(UIApplication.class);

      if (uiQuickEditForm.nodeIsLocked(webContentNode)) {
        Object[] objs = { webContentNode.getPath() };
        uiApplication.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", objs));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
        return;
      }

      boolean isCheckedOut = true;
      if (!webContentNode.isCheckedOut()) {
        isCheckedOut = false;
        webContentNode.checkout();
      }

      List inputs = uiQuickEditForm.getChildren();
      Map inputProperties = DialogFormUtil.prepareMap(inputs, uiQuickEditForm.getInputProperties());
      Node newNode = null;
      String nodeType;
      Node homeNode;
      if (uiQuickEditForm.isAddNew()) {
        homeNode = uiQuickEditForm.getParentNode();
        nodeType = uiQuickEditForm.contentType;
      } else {
        homeNode = uiQuickEditForm.getNode().getParent();
        nodeType = uiQuickEditForm.getNode().getPrimaryNodeType().getName();
      }
      try{
        CmsService cmsService = uiQuickEditForm.getApplicationComponent(CmsService.class);
        String addedPath = cmsService.storeNode(nodeType, homeNode, inputProperties, uiQuickEditForm.isAddNew, uiQuickEditForm.repositoryName);
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
      uiQuickEditForm.savedNodeIdentifier = NodeIdentifier.make(newNode);
      uiQuickEditForm.setWebContent(newNode);

      if (!isCheckedOut) {
        newNode.checkin();
      }

      UIPortletConfig uiPortletConfig = uiQuickEditForm.getAncestorOfType(UIPortletConfig.class);
      if(uiPortletConfig.isEditPortletInCreatePageWizard()) {
        uiPortletConfig.getChildren().clear();
        uiPortletConfig.addUIWelcomeScreen();
      } else {        
        PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
        context.setApplicationMode(PortletMode.VIEW);
      }
    }
  }

  public static class CancelActionListener extends EventListener<UIQuickEditWebContentForm> {
    public void execute(Event<UIQuickEditWebContentForm> event) throws Exception {
      PortletRequestContext context = (PortletRequestContext)event.getRequestContext();
      UIPortletConfig uiPortletConfig = event.getSource().getAncestorOfType(UIPortletConfig.class);
      if(uiPortletConfig.isEditPortletInCreatePageWizard()) {
        uiPortletConfig.getChildren().clear();
        uiPortletConfig.addUIWelcomeScreen();
      } else {        
        context.setApplicationMode(PortletMode.VIEW);
      }
    }
  }
}
