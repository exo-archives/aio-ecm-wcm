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
package org.exoplatform.services.wcm.publication.listener.post;

import javax.jcr.Node;

import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.util.Text;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.publication.WCMPublicationService;
import org.exoplatform.services.wcm.webcontent.WebContentSchemaHandler;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham
 * hoa.phamvu@exoplatform.com
 * Mar 5, 2009
 */
public class PostCreateContentEventListener extends Listener<CmsService, Node>{
  
  /** The publication service. */
  private WCMPublicationService publicationService;
  
  /** The publication service. */
  private WCMConfigurationService configurationService;
  
  /** The web content schema handler. */
  private WebContentSchemaHandler webContentSchemaHandler;
  
  /**
   * Instantiates a new post create content event listener.
   * 
   * @param publicationService the publication service
   * @param configurationService the configuration service
   * @param schemaConfigService the schema config service
   */
   public PostCreateContentEventListener(WCMPublicationService publicationService,WCMConfigurationService configurationService, WebSchemaConfigService schemaConfigService) {
    this.publicationService = publicationService;
    this.configurationService = configurationService;
    webContentSchemaHandler = schemaConfigService.getWebSchemaHandlerByType(WebContentSchemaHandler.class);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.listener.Listener#onEvent(org.exoplatform.services.listener.Event)
   */
  public void onEvent(Event<CmsService, Node> event) throws Exception {
    Node currentNode = event.getData();
    if(currentNode.canAddMixin("exo:rss-enable")) {
      currentNode.addMixin("exo:rss-enable");
      if(!currentNode.hasProperty("exo:title")) {
    	  currentNode.setProperty("exo:title",Text.unescapeIllegalJcrChars(currentNode.getName())); 
      }      
    }
    if(webContentSchemaHandler.isWebcontentChildNode(currentNode) || currentNode.isNodeType("exo:cssFile") || 
        currentNode.isNodeType("exo:jsFile") || currentNode.getParent().isNodeType("exo:actionStorage")){
      return;    
    }
    String repository = ((ManageableRepository)currentNode.getSession().getRepository()).getConfiguration().getName();
    String workspace = currentNode.getSession().getWorkspace().getName();
    NodeLocation nodeLocation = configurationService.getLivePortalsLocation(repository);
    if(!workspace.equalsIgnoreCase(nodeLocation.getWorkspace()))
      return;
    if(!currentNode.getPath().startsWith(nodeLocation.getPath()))
      return;

    String siteName = Util.getPortalRequestContext().getPortalOwner();
    String remoteUser = Util.getPortalRequestContext().getRemoteUser();    	
    if (remoteUser != null) publicationService.updateLifecyleOnChangeContent(currentNode, siteName, remoteUser);
  }
}
