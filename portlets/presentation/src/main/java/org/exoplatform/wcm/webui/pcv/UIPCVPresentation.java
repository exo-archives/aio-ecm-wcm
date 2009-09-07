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

import javax.jcr.Node;

import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
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
public class UIPCVPresentation extends UIBaseNodePresentation {

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
  
  /** Content can't be printed.*/
  public final static String  CONTENT_NOT_PRINTED = "UIMessageBoard.msg.content-invisible";
  
  /** Content is obsolete.*/
  public final static String OBSOLETE_CONTENT = "UIMessageBoard.msg.content-obsolete";

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
  
  public void setOrginalNode(Node orginalNode) {
	this.orginalNode = orginalNode;
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
        DMSConfiguration dmsConfiguration = getApplicationComponent(DMSConfiguration.class);
        String workspace = dmsConfiguration.getConfig(repository).getSystemWorkspace();
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
    return orginalNode.getPrimaryNodeType().getName();
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
	  super.processRender(context);
  }

public UIComponent getCommentComponent() {
	// TODO Auto-generated method stub
	return null;
}
}
