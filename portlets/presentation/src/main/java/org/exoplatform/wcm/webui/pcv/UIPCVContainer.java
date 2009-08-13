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

import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.ecm.publication.NotInPublicationLifecycleException;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.services.wcm.publication.WCMComposer;
import org.exoplatform.services.wcm.publication.WCMPublicationService;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

// TODO: Auto-generated Javadoc
/**
 * Created by The eXo Platform SAS Author : Phan Le Thanh Chuong
 * chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com Nov 4, 2008
 */

@ComponentConfig(
  lifecycle = Lifecycle.class, 
  template = "app:/groovy/ParameterizedContentViewer/UIPCVContainer.gtmpl", 
  events = { 
    @EventConfig(listeners = UIPCVContainer.QuickEditActionListener.class) 
  }
)
public class UIPCVContainer extends UIContainer {

  /** Flag indicating the draft revision. */
  private boolean isDraftRevision = false;

  /** Flag indicating the obsolete revision. */
  private boolean isObsoletedContent = false;

  /** Content child of this content. */
  private UIPCVPresentation uiContentViewer;
  
  /** The repository. */
  private String repository;
  
  /** A flag used to display Print/Close buttons and hide Back one if its' value is <code>true</code>. In <code>false</code> case, the Back button will be shown only */
  private boolean isPrint;

  /**
   * Instantiates a new uI content viewer container.
   * 
   * @throws Exception the exception
   */
  public UIPCVContainer() throws Exception {
    
    addChild(UIPCVPresentation.class, null, null);
    uiContentViewer = getChild(UIPCVPresentation.class);
  }
  
  /**
   * Gets the repository.
   * 
   * @return the repository
   * 
   * @throws RepositoryException the repository exception
   */
  public String getRepository() throws RepositoryException {
    PortletRequestContext porletRequestContext = WebuiRequestContext.getCurrentInstance();
    repository = porletRequestContext.getRequest().getPreferences().getValue("repository", "");
    return repository;
  }

  /**
   * Sets the repository.
   * 
   * @param repository the new repository
   */
  public void setRepository(String repository) {
    this.repository = repository;
  }

  /**
   * Gets the node.
   * 
   * @return the node
   * 
   * @throws Exception the exception
   */
  public Node getNode() throws Exception {
    String parameters = getRequestParameters();
    Node node = getNodebyPath(parameters);
    if (node == null) node = getNodeByCategory(parameters);
    if (node == null) return null;

    // check node is a document node
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    List<String> documentTypes = templateService.getDocumentTemplates(this.getRepository());
    boolean isDocumentType = false;
    for (String documentType : documentTypes) {
      if (node.isNodeType(documentType)) {
        isDocumentType = true;
        break;
      }
    }
    if (!isDocumentType) return null;
    if (hasChildren()) removeChild(UIPCVContainer.class);
    PublicationService publicationService = getApplicationComponent(PublicationService.class);
    String lifecycleName = null;
    try {
      lifecycleName = publicationService.getNodeLifecycleName(node);
    } catch (NotInPublicationLifecycleException e) {}
    Node nodeView = null;
    // if content doesn't join to any lifecycle (deploy from xml)
    if (lifecycleName == null) {
      nodeView = node;
    } else {
      PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(lifecycleName);
      HashMap<String, Object> context = new HashMap<String, Object>();
      context.put(WCMComposer.FILTER_MODE, Utils.getCurrentMode());
      nodeView = publicationPlugin.getNodeView(node, context);
    }
    
    // set node view for UIPCVPresentation
    if (nodeView != null && nodeView.isNodeType("nt:frozenNode")) {
      String nodeUUID = nodeView.getProperty("jcr:frozenUuid").getString();
      uiContentViewer.setOrginalNode(node.getSession().getNodeByUUID(nodeUUID));
      uiContentViewer.setNode(nodeView);
    } else if (nodeView == null) {
      return null;
    } else {
      uiContentViewer.setOrginalNode(nodeView);
      uiContentViewer.setNode(nodeView);
    }
    uiContentViewer.setRepository(this.getRepository());
    uiContentViewer.setWorkspace(nodeView.getSession().getWorkspace().getName());
    
    // Set draft for template
    WCMPublicationService wcmPublicationService = getApplicationComponent(WCMPublicationService.class);
    String contentState = wcmPublicationService.getContentState(nodeView);
    if (PublicationDefaultStates.DRAFT.equals(contentState)) isDraftRevision = true;
    else isDraftRevision = false;
    
    PortletRequestContext porletRequestContext = WebuiRequestContext.getCurrentInstance();
    HttpServletRequest request = (HttpServletRequest) porletRequestContext.getRequest();
    isPrint = "isPrint=true".equals(request.getQueryString()) ? true :false;  
  
    return nodeView;
  }
  
  /**
   * Gets the request parameters.
   * 
   * @return the request parameters
   */
  private String getRequestParameters() {
    PortletRequestContext porletRequestContext = WebuiRequestContext.getCurrentInstance();
    HttpServletRequestWrapper requestWrapper = (HttpServletRequestWrapper) porletRequestContext.getRequest();
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    UIPortal uiPortal = Util.getUIPortal();
    String portalURI = portalRequestContext.getPortalURI();
    String requestURI = requestWrapper.getRequestURI();
    String pageNodeSelected = uiPortal.getSelectedNode().getUri();
    String parameters = null;
    try {
      parameters = URLDecoder.decode(StringUtils.substringAfter(requestURI, portalURI.concat(pageNodeSelected + "/")), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return null;
    }
    if (!parameters.matches(UIPCVPresentation.PARAMETER_REGX)) {
      return null;
    }
    return parameters;
  }
  
  /**
   * Gets the nodeby path.
   * 
   * @param parameters the parameters
   * 
   * @return the nodeby path
   * 
   * @throws Exception the exception
   */
  private Node getNodebyPath(String parameters) throws Exception {
    ManageableRepository manageableRepository = null;
    String[] params = parameters.split("/");
    String repository = params[0];
    String workspace = params[1];
    try {
      RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
      manageableRepository = repositoryService.getRepository(repository);
    } catch(Exception e) {
      return null;
    }
    String nodeIdentifier = null;
    Node currentNode = null;
    Session session = Utils.getSessionProvider(this).getSession(workspace, manageableRepository);
    if (params.length > 2) {
      StringBuffer identifier = new StringBuffer();
      for (int i = 2; i < params.length; i++) {
        identifier.append("/").append(params[i]);
      }
      nodeIdentifier = identifier.toString();
      boolean isUUID = false;
      // try to get node by path
      try {
        currentNode = (Node) session.getItem(nodeIdentifier);
      } catch (Exception e) {
        isUUID = true;
      }
      if (isUUID) {
        // try to get node by uuid
        try {
          String uuid = params[params.length - 1];
          currentNode = session.getNodeByUUID(uuid);
        } catch (ItemNotFoundException exc) {
          return null;
        }
      }
    } else if (params.length == 2) {
      currentNode = session.getRootNode();
    }
    
    return currentNode;  
  }
  
  /**
   * Gets the node by category.
   * 
   * @param parameters the parameters
   * 
   * @return the node by category
   * 
   * @throws Exception the exception
   */
  private Node getNodeByCategory(String parameters) throws Exception {
    
    TaxonomyService taxonomyService = getApplicationComponent(TaxonomyService.class);
    String[] params = parameters.split("/");
    Node taxonomyTree = taxonomyService.getTaxonomyTree(this.getRepository(), params[0]);
    
    String symLinkPath = parameters.substring(parameters.indexOf("/") + 1);
    try {
      Node symLink = taxonomyTree.getNode(symLinkPath);
      return taxonomyTree.getSession().getNodeByUUID(symLink.getProperty("exo:uuid").getString());
    } catch (PathNotFoundException e) {
      return null;
    }
  }
  
  /**
   * Checks if is quick edit able.
   * 
   * @return true, if is quick edit able
   * 
   * @throws Exception the exception
   */
  public boolean isQuickEditAble() throws Exception {
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext
        .getCurrentInstance();
    return Utils.turnOnQuickEditable(context, true);
  }

  /**
   * The listener interface for receiving quickEditAction events. The class
   * that is interested in processing a quickEditAction event implements this
   * interface, and the object created with that class is registered with a
   * component using the component's
   * <code>addQuickEditActionListener<code> method. When
   * the quickEditAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see QuickEditActionEvent
   */
  public static class QuickEditActionListener extends
      EventListener<UIPCVContainer> {
    /*
     * (non-Javadoc)
     * 
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIPCVContainer> event) throws Exception {
      UIPCVContainer uiContentViewerContainer = event.getSource();
      UIPCVPresentation uiContentViewer = uiContentViewerContainer.getChild(UIPCVPresentation.class);
      Node orginialNode = uiContentViewer.getOriginalNode();
      ManageableRepository manageableRepository = (ManageableRepository) orginialNode.getSession().getRepository();
      String repository = manageableRepository.getConfiguration().getName();
      String workspace = orginialNode.getSession().getWorkspace().getName();
      UIPCVContentDialog uiDocumentForm = uiContentViewerContainer.createUIComponent(UIPCVContentDialog.class, null, null);
      uiDocumentForm.setRepositoryName(repository);
      uiDocumentForm.setWorkspace(workspace);
      uiDocumentForm.setContentType(orginialNode.getPrimaryNodeType().getName());
      uiDocumentForm.setNodePath(orginialNode.getPath());
      uiDocumentForm.setStoredPath(orginialNode.getParent().getPath());
      uiDocumentForm.addNew(false);
      uiContentViewerContainer.addChild(uiDocumentForm);
      Utils.createPopupWindow(uiContentViewerContainer, uiDocumentForm, "UIDocumentFormPopupWindow", 800, 600);
    }
  }

  /**
   * Render error message.
   * 
   * @param context the context
   * @param keyBundle the key bundle
   * 
   * @throws Exception the exception
   */
  public void renderErrorMessage(WebuiRequestContext context, String keyBundle)
      throws Exception {
    Writer writer = context.getWriter();
    String message = context.getApplicationResourceBundle().getString(
        keyBundle);
    writer
        .write("<div style=\"height: 55px; font-size: 13px; text-align: center; padding-top: 10px;\">");
    writer.write("<span>");
    writer.write(message);
    writer.write("</span>");
    writer.write("</div>");
    writer.close();
  }

  /**
   * Gets <code>isPrint</code> value that is used to display Print/Close
   * buttons and hide Back one if its' value is <code>True</code>. In
   * <code>False</code> case, the Back button will be shown only.
   * 
   * @return <code>isPrint</code>
   */
  public boolean getIsPrint() {
    return isPrint;
  }

  /**
   * Sets <code>isPrint</code> value that is used to display Print/Close
   * buttons and hide Back one if its' value is <code>True</code>. In
   * <code>False</code> case, the Back button will be shown only.
   * 
   * @param isPrint the is print
   */
  public void setIsPrint(boolean isPrint) {
    this.isPrint = isPrint;
  }

  /**
   * Gets the draft revision value. If the revision is draft, an icon and one
   * text is shown. Otherwise, false.
   * 
   * @return <code>isDraftRevision</code>
   */
  public boolean isDraftRevision() {
    return isDraftRevision;
  }

  /**
   * Sets the draft revision value. If the revision is draft, an icon and one
   * text is shown. Otherwise, false.
   * 
   * @param isDraftRevision the is draft revision
   */
  public void setDraftRevision(boolean isDraftRevision) {
    this.isDraftRevision = isDraftRevision;
  }

  /**
   * Gets the draft obsolete value. If the revision is draft, the message is
   * shown to inform users of this state. Otherwise, false.
   * 
   * @return <code>isDraftRevision</code>
   */
  public boolean isObsoletedContent() {
    return isObsoletedContent;
  }

  /**
   * Sets the draft obsolete value. If the revision is draft, the message is
   * shown to inform users of this state. Otherwise, false.
   * 
   * @param isObsoletedContent the is obsoleted content
   */
  public void setObsoletedContent(boolean isObsoletedContent) {
    this.isObsoletedContent = isObsoletedContent;
  }
}
