/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.wcm.publication.lifecycle.stageversion.ui;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 * chuong_phan@exoplatform.com
 * Mar 5, 2009
 */
@ComponentConfig(
  lifecycle = Lifecycle.class    
)
public class UIVersionViewer extends UIBaseNodePresentation {

  /** The original node. */
  private NodeLocation originalNodeLocation;
  
  /** The node. */
  private NodeLocation nodeLocation;
  
  /** The resource resolver. */
  private JCRResourceResolver resourceResolver ;
  
  /** The Constant log. */
  public static final Log log = ExoLogger.getLogger("wcm:StageAndVersionPubliciation");
  
  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getNode()
   */
  public Node getNode() throws Exception {
    return NodeLocation.getNodeByLocation(nodeLocation);
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#setNode(javax.jcr.Node)
   */
  public void setNode(Node node) {
    nodeLocation = NodeLocation.make(node);
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getOriginalNode()
   */
  public Node getOriginalNode() throws Exception {
    return NodeLocation.getNodeByLocation(originalNodeLocation);
  }
  
  /**
   * Sets the original node.
   * 
   * @param originalNode the new original node
   */
  public void setOriginalNode(Node originalNode) {
    originalNodeLocation = NodeLocation.make(originalNode);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getRepositoryName()
   */
  public String getRepositoryName() throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.webui.portal.UIPortalComponent#getTemplate()
   */
  public String getTemplate() {
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    try {
      String nodeType = getOriginalNode().getPrimaryNodeType().getName();
      String repositoryName = getRepository();
      if(templateService.isManagedNodeType(nodeType, repositoryName)) 
        return templateService.getTemplatePathByUser(false, nodeType, userName, repositoryName) ;
    } catch (Exception e) {}
    return null ;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getTemplatePath()
   */
  public String getTemplatePath() throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIComponent#getTemplateResourceResolver(org.exoplatform.webui.application.WebuiRequestContext, java.lang.String)
   */
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    try{
        String repository = getRepository();
        DMSConfiguration dmsConfiguration = getApplicationComponent(DMSConfiguration.class);
        String workspace = dmsConfiguration.getConfig(repository).getSystemWorkspace();
        resourceResolver = new JCRResourceResolver(repository, workspace, "exo:templateFile");
    }catch (Exception e) {}    
    return resourceResolver ;   
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getNodeType()
   */
  public String getNodeType() throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#isNodeTypeSupported()
   */
  public boolean isNodeTypeSupported() {
    return false;
  }

  public UIComponent getCommentComponent() {
  	return null;
  }
}