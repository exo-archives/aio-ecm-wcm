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
package org.exoplatform.services.wcm.publication.lifecycle.stageversion;

import java.util.HashMap;

import javax.jcr.Node;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.ecm.publication.NotInPublicationLifecycleException;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.BaseWebSchemaHandler;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.webcontent.WebContentSchemaHandler;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.phamvu@exoplatform.com
 * Mar 5, 2009  
 */
public class StageAndVersionPublicationHandler extends BaseWebSchemaHandler {
  private TemplateService templateService;   
  private PublicationService publicationService;
  public StageAndVersionPublicationHandler(TemplateService templateService, PublicationService publicationService) {    
    this.templateService = templateService;   
    this.publicationService = publicationService;
  }

  protected String getHandlerNodeType() {
    return null;
  }

  @Override
  protected String getParentNodeType() {
    return null;
  }

  public boolean matchHandler(Node node, SessionProvider sessionProvider) throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    WebSchemaConfigService schemaConfigService = (WebSchemaConfigService)container.getComponentInstanceOfType(WebSchemaConfigService.class);
    WebContentSchemaHandler webContentSchemaHandler = schemaConfigService.getWebSchemaHandlerByType(WebContentSchemaHandler.class);
    if(webContentSchemaHandler.isWebcontentChildNode(node))
      return false;
    if(node.isNodeType("exo:cssFile") || node.isNodeType("exo:jsFile"))
      return false;    
    String primaryNodeType = node.getPrimaryNodeType().getName();
    String repository = ((ManageableRepository)node.getSession().getRepository()).getConfiguration().getName();
    return templateService.isManagedNodeType(primaryNodeType,repository);    
  }

  public void onCreateNode(Node node, SessionProvider sessionProvider) throws Exception {    
    Node checkNode = node;
    if(node.isNodeType("nt:file")) {
      if(node.canAddMixin("exo:rss-enable")) {
        node.addMixin("exo:rss-enable");
        if(!node.hasProperty("exo:title")) {
          node.setProperty("exo:title",node.getName());
        }        
      }
      Node parentNode = node.getParent();
      if(parentNode.isNodeType("exo:webContent")) {
        checkNode = parentNode;        
      }      
    }             
    if(publicationService.isNodeEnrolledInLifecycle(checkNode)) return;
    publicationService.enrollNodeInLifecycle(checkNode,StageAndVersionPublicationConstant.LIFECYCLE_NAME);
    publicationService.changeState(checkNode,StageAndVersionPublicationConstant.DRAFT_STATE,new HashMap<String,String>());
  }   
  public void onModifyNode(Node node, SessionProvider sessionProvider) throws Exception {
    if(node.isNew())
      return;   
    Node checkNode = node;
    if(node.isNodeType("nt:file")) {      
      Node parentNode = node.getParent();
      if(parentNode.isNodeType("exo:webContent")) {
        checkNode = parentNode;        
      }                 
    }
    String lifecycle = null;
    try {
      lifecycle = publicationService.getNodeLifecycleName(checkNode);
    } catch (NotInPublicationLifecycleException e) {
      return;
    }

    if(!StageAndVersionPublicationConstant.LIFECYCLE_NAME.equalsIgnoreCase(lifecycle))   
      return;
    String currentState = publicationService.getCurrentState(checkNode);
    if(!StageAndVersionPublicationConstant.ENROLLED_STATE.equalsIgnoreCase(currentState))
      return;
    publicationService.changeState(checkNode,StageAndVersionPublicationConstant.DRAFT_STATE,new HashMap<String,String>());
  }
}
