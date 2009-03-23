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
import java.security.AccessControlException;
import java.util.HashMap;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.Constant;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.Constant.SITE_MODE;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/*
 * Created by The eXo Platform SAS 
 * Author : Anh Do Ngoc 
 * anh.do@exoplatform.com
 * Sep 24, 2008
 */

/**
 * The Class UIContentViewer.
 */
@ComponentConfig(lifecycle = Lifecycle.class)
public class UIContentViewer extends UIBaseNodePresentation {

  /** The content node. */
  private Node                contentNode;
  private Node  orginalNode;
  /** The resource resolver. */
  private JCRResourceResolver resourceResolver;

  /** The repository. */
  private String              repository;

  /** The workspace. */
  private String              workspace;

  /** The Constant CONTENT_NOT_FOUND_EXC. */
  public final static String  CONTENT_NOT_FOUND_EXC = "UIMessageBoard.msg.content-not-found";

  /** The Constant ACCESS_CONTROL_EXC. */
  public final static String  ACCESS_CONTROL_EXC    = "UIMessageBoard.msg.access-control-exc";

  /** The Constant CONTENT_UNSUPPORT_EXC. */
  public final static String  CONTENT_UNSUPPORT_EXC = "UIMessageBoard.msg.content-unsupport-exc";

  /** The Constant PARAMETER_REGX. */
  public final static String  PARAMETER_REGX        = "(.*)/(.*)";

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getNode()
   */
  @Override
  public Node getNode() throws Exception {
    return contentNode;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getOriginalNode()
   */
  @Override
  public Node getOriginalNode() throws Exception {
	  return orginalNode;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getRepositoryName()
   */
  @Override
  public String getRepositoryName() throws Exception {
    return repository;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getWorkspaceName()
   */
  public String getWorkspaceName() throws Exception {
    return workspace;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getTemplatePath()
   */
  @Override
  public String getTemplatePath() throws Exception {
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    return templateService.getTemplatePath(orginalNode, false);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.webui.portal.UIPortalComponent#getTemplate()
   */
  public String getTemplate() {
    try {
      return getTemplatePath();
    } catch (Exception e) {
      return null;
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIComponent#getTemplateResourceResolver(org.exoplatform.webui.application.WebuiRequestContext, java.lang.String)
   */
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    try {
      resourceResolver = new JCRResourceResolver(repository, workspace, "exo:templateFile");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return resourceResolver;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getNodeType()
   */
  public String getNodeType() throws Exception {
    return contentNode.getPrimaryNodeType().getName();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#isNodeTypeSupported()
   */
  public boolean isNodeTypeSupported() {
    return false;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#setNode(javax.jcr.Node)
   */
  public void setNode(Node node) {
    this.contentNode = node;
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
   * Sets the workspace.
   * 
   * @param workspace the new workspace
   */
  public void setWorkspace(String workspace) {
    this.workspace = workspace;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIComponent#processRender(org.exoplatform.webui.application.WebuiRequestContext)
   */
  public void processRender(WebuiRequestContext context) throws Exception {
    PortletRequestContext porletRequestContext = (PortletRequestContext) context;
    HttpServletRequestWrapper requestWrapper = (HttpServletRequestWrapper) porletRequestContext.getRequest();
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    UIPortal uiPortal = Util.getUIPortal();
    String portalURI = portalRequestContext.getPortalURI();
    String requestURI = requestWrapper.getRequestURI();
    String pageNodeSelected = uiPortal.getSelectedNode().getName();
    String parameters = null;
    Object object = requestWrapper.getAttribute("ParameterizedContentViewerPortlet.data.object");

    try {
      parameters = URLDecoder.decode(StringUtils.substringAfter(requestURI,portalURI.concat(pageNodeSelected + "/")), "UTF-8");      
    } catch (UnsupportedEncodingException e) { }
    if (!parameters.matches(PARAMETER_REGX)) {
      renderErrorMessage(context, CONTENT_NOT_FOUND_EXC);
      return;
    }
    String nodeIdentifier = null;
    String[] params = parameters.split("/");
    String repository = params[0];
    String workspace = params[1];
    Node currentNode = null;
    SessionProvider sessionProvider = null;
    Session session = null;
    String userId = Util.getPortalRequestContext().getRemoteUser();
    if (userId == null) {
      sessionProvider = SessionProviderFactory.createAnonimProvider();
    } else {
      sessionProvider = SessionProviderFactory.createSessionProvider();
    }
    if (object instanceof ItemNotFoundException || object instanceof AccessControlException
        || object instanceof ItemNotFoundException || object == null) {
      try {
        RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
        ManageableRepository manageableRepository = repositoryService.getRepository(repository);
        session = sessionProvider.getSession(workspace, manageableRepository);
      } catch (AccessControlException ace) {
        renderErrorMessage(context, ACCESS_CONTROL_EXC);
        return;
      } catch (Exception e) {
        renderErrorMessage(context, CONTENT_NOT_FOUND_EXC);
        return;
      }
      if (params.length > 2) {
        StringBuffer identifier = new StringBuffer();
        for (int i = 2; i < params.length; i++) {
          identifier.append("/").append(params[i]);
        }
        nodeIdentifier = identifier.toString();
        boolean isUUID = false;
        try {
          currentNode = (Node) session.getItem(nodeIdentifier);
        } catch (Exception e) {
          isUUID = true;
        }
        if (isUUID) {
          try {
            String uuid = params[params.length - 1];
            currentNode = session.getNodeByUUID(uuid);
          } catch (ItemNotFoundException exc) {
            renderErrorMessage(context, CONTENT_NOT_FOUND_EXC);
            return;
          }
        }
      } else if (params.length == 2) {
        currentNode = session.getRootNode();
      }
    } else {
      currentNode = (Node) object;
    }

    TemplateService templateService = getApplicationComponent(TemplateService.class);
    List<String> documentTypes = templateService.getDocumentTemplates(repository);
    Boolean isDocumentType = false;
    for (String docType : documentTypes) {
      if (currentNode.isNodeType(docType)) {
        isDocumentType = true;
        break;
      }
    }
    if (currentNode.isNodeType("exo:hiddenable")) {
      renderErrorMessage(context, ACCESS_CONTROL_EXC);
      return;
    } else if (isDocumentType) { // content is document
      if (hasChildren()) {
        removeChild(UIContentViewerContainer.class);
      }
      PublicationService publicationService = uiPortal.getApplicationComponent(PublicationService.class);
      HashMap<String, Object> hmContext = new HashMap<String, Object>();
      if(Utils.isLiveMode()) {
    	  hmContext.put(Constant.RUNTIME_MODE, SITE_MODE.LIVE);
      } else {
          hmContext.put(Constant.RUNTIME_MODE, SITE_MODE.EDITING);
      }   
      String lifeCycleName = publicationService.getNodeLifecycleName(currentNode);
      PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(lifeCycleName);
//      if(publicationPlugin == null) {
//    	  
//      } else {
//    	  
//      }
      Node nodeView = publicationPlugin.getNodeView(currentNode, hmContext);
      setRepository(repository);
	  setWorkspace(workspace);
	  this.orginalNode = currentNode;
      if(nodeView != null) {
    	  this.contentNode = nodeView;
      }else {
    	  this.contentNode = currentNode;
      }         
      super.processRender(context);
    } else { // content is folders
      renderErrorMessage(context, CONTENT_UNSUPPORT_EXC);
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
  private void renderErrorMessage(WebuiRequestContext context, String keyBundle) throws Exception {
    Writer writer = context.getWriter();
    String message = context.getApplicationResourceBundle().getString(keyBundle);
    writer.write("<div style=\"height: 55px; font-size: 13px; text-align: center; padding-top: 10px;\">");
    writer.write("<span>");
    writer.write(message);
    writer.write("</span>");
    writer.write("</div>");
    writer.close();
  }

}
