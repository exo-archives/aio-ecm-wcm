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
package org.exoplatform.wcm.webui.pcv;

import java.security.AccessControlException;
import java.util.HashMap;
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
import org.exoplatform.ecm.webui.utils.LockUtil;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationConstant;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Anh Do Ngoc
 * anh.do@exoplatform.com,
 * anhdn86@gmail.com Nov 13, 2008
 */

@ComponentConfig(
  lifecycle = UIFormLifecycle.class, events = {
    @EventConfig(listeners = UIPCVContentDialog.SaveDraftActionListener.class),
    @EventConfig(listeners = UIPCVContentDialog.FastPublishActionListener.class),
    @EventConfig(listeners = UIPCVContentDialog.CancelActionListener.class)  
  }  
)
public class UIPCVContentDialog extends UIDialogForm {

  /** The document node. */
  private Node documentNode;

  /**
   * Sets the document node.
   * 
   * @param node the new document node
   */
  public void setDocumentNode(Node node) {
    documentNode = node;
  }

  /**
   * Gets the document node.
   * 
   * @return the document node
   */
  public Node getDocumentNode() {
    return documentNode;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.form.UIDialogForm#getTemplate()
   */
  public String getTemplate() {
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    String userName = Util.getPortalRequestContext().getRemoteUser();
    try {
      return templateService.getTemplatePathByUser(true, contentType, userName, repositoryName);
    } catch (Exception e) {
      Utils.createPopupMessage(this, "UIDocumentForm.msg.not-support", new Object[] {contentType}, ApplicationMessage.ERROR);
      return null;
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIComponent#getTemplateResourceResolver(org.exoplatform.webui.application.WebuiRequestContext, java.lang.String)
   */
  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    try {
      if (resourceResolver == null) {
        RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
          DMSConfiguration dmsConfiguration = getApplicationComponent(DMSConfiguration.class);
          String workspace = dmsConfiguration.getConfig(this.repositoryName).getSystemWorkspace();
        resourceResolver = new JCRResourceResolver(this.repositoryName,
                                                   workspace,
                                                   TemplateService.EXO_TEMPLATE_FILE_PROP);
      }
    } catch (Exception e) { }
    return resourceResolver;
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
  public static class SaveDraftActionListener extends EventListener<UIPCVContentDialog> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIPCVContentDialog> event) throws Exception {
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      UIPCVContentDialog uiDocumentDialogForm = event.getSource();
      Node documentNode = uiDocumentDialogForm.getNode();
      Session session = documentNode.getSession();
      ManageableRepository manageableRepository = (ManageableRepository) session.getRepository();
      String repository = manageableRepository.getConfiguration().getName();
      String workspace = session.getWorkspace().getName();
      List<UIComponent> inputs = uiDocumentDialogForm.getChildren();
      Map<String, JcrInputProperty> inputProperties = DialogFormUtil.prepareMap(inputs,
                                                      uiDocumentDialogForm.getInputProperties());
      String nodeTypeName = documentNode.getPrimaryNodeType().getName();
      Node homeNode = documentNode.getParent();
      Node newNode = null;
      if (documentNode.isLocked())
        session.addLockToken(LockUtil.getLockToken(documentNode));
      try {
        CmsService cmsService = uiDocumentDialogForm.getApplicationComponent(CmsService.class);
        String addedPath = cmsService.storeNode(nodeTypeName,
                                                homeNode,
                                                inputProperties,
                                                uiDocumentDialogForm.isAddNew,
                                                uiDocumentDialogForm.repositoryName);
        try {
          homeNode.save();
          newNode = (Node) homeNode.getSession().getItem(addedPath);
          event.getRequestContext().setAttribute("nodePath", newNode.getPath());
        } catch (Exception e) {
          e.printStackTrace();
        }
      } catch (AccessControlException ace) {
        throw new AccessDeniedException(ace.getMessage());
      } catch (VersionException ve) {
      	Utils.createPopupMessage(uiDocumentDialogForm, "UIDocumentForm.msg.in-versioning", null, ApplicationMessage.WARNING);
        return;
      } catch (ItemNotFoundException item) {
      	Utils.createPopupMessage(uiDocumentDialogForm, "UIDocumentForm.msg.item-not-found", null, ApplicationMessage.WARNING);
        return;
      } catch (RepositoryException repo) {
        String key = "UIDocumentForm.msg.repository-exception";
        if (ItemExistsException.class.isInstance(repo)) key = "UIDocumentForm.msg.not-allowed-same-name-sibling";
        Utils.createPopupMessage(uiDocumentDialogForm, key, null, ApplicationMessage.WARNING);
        return;
      } catch (NumberFormatException nume) {
      	Utils.createPopupMessage(uiDocumentDialogForm, "UIDocumentForm.msg.numberformat-exception", null, ApplicationMessage.WARNING);
        return;
      } catch (Exception e) {
      	Utils.createPopupMessage(uiDocumentDialogForm, "UIDocumentForm.msg.cannot-save", null, ApplicationMessage.WARNING);
        return;
      }
      UIPCVPortlet uiportlet = uiDocumentDialogForm.getAncestorOfType(UIPCVPortlet.class);
      UIPCVContainer uiContentViewerContainer = uiportlet.getChild(UIPCVContainer.class);
      UIPCVPresentation uiContentViewer = uiContentViewerContainer.getChild(UIPCVPresentation.class);
      uiContentViewer.setNode(newNode);
      uiContentViewer.setRepository(repository);
      uiContentViewer.setWorkspace(workspace);
      Utils.closePopupWindow(uiDocumentDialogForm, "UIDocumentFormPopupWindow");
      Utils.updatePortal(context);
    }

  }

  /**
   * The listener interface for receiving cancelAction events.
   * The class that is interested in processing a cancelAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addCancelActionListener<code> method. When
   * the cancelAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see CancelActionEvent
   */
  public static class CancelActionListener extends EventListener<UIPCVContentDialog> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIPCVContentDialog> event) throws Exception {
      UIPCVContentDialog uiDocumentDialogForm = event.getSource();
      Node documentNode = uiDocumentDialogForm.getNode();
      Session session = documentNode.getSession();
      ManageableRepository manageableRepository = (ManageableRepository) session.getRepository();
      String repository = manageableRepository.getConfiguration().getName();
      String workspace = session.getWorkspace().getName();
      UIPCVPortlet uiportlet = uiDocumentDialogForm.getAncestorOfType(UIPCVPortlet.class);
      UIPCVContainer uiContentViewerContainer = uiportlet.getChild(UIPCVContainer.class);
      UIPCVPresentation uiContentViewer = uiContentViewerContainer.getChild(UIPCVPresentation.class);
      uiContentViewer.setNode(documentNode);
      uiContentViewer.setRepository(repository);
      uiContentViewer.setWorkspace(workspace);
      Utils.closePopupWindow(uiDocumentDialogForm, "UIDocumentFormPopupWindow");
    }
  }
  
  /**
  * The listener interface for receiving fastPublishAction events.
  * The class that is interested in processing a fastPublishAction
  * event implements this interface, and the object created
  * with that class is registered with a component using the
  * component's <code>addFastPublishActionListener<code> method. When
  * the cancelAction event occurs, that object's appropriate
  * method is invoked.
  * 
  * @see FastPublishActionEvent
  */
  public static class FastPublishActionListener extends EventListener<UIPCVContentDialog> {
    public void execute(Event<UIPCVContentDialog> event) throws Exception {
      UIPCVContentDialog uiDocumentDialogForm = event.getSource();
      Node documentNode = uiDocumentDialogForm.getNode();
      Session session = documentNode.getSession();
      ManageableRepository manageableRepository = (ManageableRepository) session.getRepository();
      String repository = manageableRepository.getConfiguration().getName();
      String workspace = session.getWorkspace().getName();
      List<UIComponent> inputs = uiDocumentDialogForm.getChildren();
      Map<String, JcrInputProperty> inputProperties = DialogFormUtil.prepareMap(inputs,
                                                      uiDocumentDialogForm.getInputProperties());
      String nodeTypeName = documentNode.getPrimaryNodeType().getName();
      Node homeNode = documentNode.getParent();
      Node newNode = null;
      if (documentNode.isLocked())
        session.addLockToken(LockUtil.getLockToken(documentNode));
      try {
        CmsService cmsService = uiDocumentDialogForm.getApplicationComponent(CmsService.class);
        String addedPath = cmsService.storeNode(nodeTypeName,
                                                homeNode,
                                                inputProperties,
                                                uiDocumentDialogForm.isAddNew,
                                                uiDocumentDialogForm.repositoryName);
        try {
          homeNode.save();
          newNode = (Node) homeNode.getSession().getItem(addedPath);
          event.getRequestContext().setAttribute("nodePath", newNode.getPath());
        } catch (Exception e) {
          e.printStackTrace();
        }
      } catch (AccessControlException ace) {
        throw new AccessDeniedException(ace.getMessage());
      } catch (VersionException ve) {
      	Utils.createPopupMessage(uiDocumentDialogForm, "UIDocumentForm.msg.in-versioning", null, ApplicationMessage.WARNING);
        return;
      } catch (ItemNotFoundException item) {
      	Utils.createPopupMessage(uiDocumentDialogForm, "UIDocumentForm.msg.item-not-found", null, ApplicationMessage.WARNING);
        return;
      } catch (RepositoryException repo) {
        String key = "UIDocumentForm.msg.repository-exception";
        if (ItemExistsException.class.isInstance(repo))
          key = "UIDocumentForm.msg.not-allowed-same-name-sibling";
        Utils.createPopupMessage(uiDocumentDialogForm, key, null, ApplicationMessage.WARNING);
        return;
      } catch (NumberFormatException nume) {
      	Utils.createPopupMessage(uiDocumentDialogForm, "UIDocumentForm.msg.numberformat-exception", null, ApplicationMessage.WARNING);
        return;
      } catch (Exception e) {
      	Utils.createPopupMessage(uiDocumentDialogForm, "UIDocumentForm.msg.cannot-save", null, ApplicationMessage.WARNING);
        return;
      }
      UIPCVPortlet uiportlet = uiDocumentDialogForm.getAncestorOfType(UIPCVPortlet.class);
      UIPCVContainer uiContentViewerContainer = uiportlet.getChild(UIPCVContainer.class);
      UIPCVPresentation uiContentViewer = uiContentViewerContainer.getChild(UIPCVPresentation.class);
      PublicationService publicationService = uiDocumentDialogForm.getApplicationComponent(PublicationService.class);
      PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(StageAndVersionPublicationConstant.LIFECYCLE_NAME);
      HashMap<String, String> context = new HashMap<String, String>();
      if(newNode != null) {
    	  context.put(StageAndVersionPublicationConstant.CURRENT_REVISION_NAME, newNode.getName());
      }
      publicationPlugin.changeState(newNode, StageAndVersionPublicationConstant.PUBLISHED_STATE, context);
      uiContentViewer.setNode(newNode);
      uiContentViewer.setRepository(repository);
      uiContentViewer.setWorkspace(workspace);
      Utils.closePopupWindow(uiDocumentDialogForm, "UIDocumentFormPopupWindow");
      
      PortletRequestContext pContext = (PortletRequestContext) event.getRequestContext();
      Utils.updatePortal(pContext);
    }
  }

}