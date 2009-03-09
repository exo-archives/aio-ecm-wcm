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

import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.BaseWebSchemaHandler;
import org.exoplatform.services.wcm.publication.WCMPublicationService;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.phamvu@exoplatform.com
 * Mar 5, 2009  
 */
public class StateAndVersionPublicationHandler extends BaseWebSchemaHandler {
  private TemplateService templateService;  
  private WCMPublicationService wcmPublicationService;
  private PublicationService publicationService;
  public StateAndVersionPublicationHandler(TemplateService templateService, WCMPublicationService wcmpublicationService, PublicationService publicationService) {    
    this.templateService = templateService;
    this.wcmPublicationService = wcmpublicationService;
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
    String primaryNodeType = node.getPrimaryNodeType().getName();
    String repository = ((ManageableRepository)node.getSession().getRepository()).getConfiguration().getName();
    return templateService.isManagedNodeType(primaryNodeType,repository);    
  }

  public void onCreateNode(Node node, SessionProvider sessionProvider) throws Exception {
    Node checkNode = node;
    if(node.isNodeType("nt:file")) {      
      Node parentNode = node.getParent();
      if(parentNode.isNodeType("exo:webContent")) {
        checkNode = parentNode;        
      }                 
    }    
    if(wcmPublicationService.isEnrolledInWCMLifecycle(checkNode)) return;
    wcmPublicationService.enrollNodeInLifecycle(checkNode,Constant.LIFECYCLE_NAME);    
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
    String lifecycle = publicationService.getNodeLifecycleName(checkNode);
    if(!Constant.LIFECYCLE_NAME.equalsIgnoreCase(lifecycle))   
      return;
    String currentState = publicationService.getCurrentState(checkNode);
    if(!Constant.ENROLLED.equalsIgnoreCase(currentState))
      return;
    publicationService.changeState(checkNode,Constant.DRAFT,new HashMap<String,String>());
  }
}
