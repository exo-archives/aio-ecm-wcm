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

import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.publication.WCMPublicationService;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.phamvu@exoplatform.com
 * Mar 5, 2009  
 */
public class PostCreateContentEventListener extends Listener<CmsService, Node>{
  private PublicationService publicationService;
  private WCMConfigurationService configurationService;
  public PostCreateContentEventListener(PublicationService publicationService,WCMConfigurationService configurationService) {
    this.publicationService = publicationService;
    this.configurationService = configurationService;
  }

  public void onEvent(Event<CmsService, Node> event) throws Exception {
    Node currentNode = event.getData();
    String repository = ((ManageableRepository)currentNode.getSession().getRepository()).getConfiguration().getName();
    String workspace = currentNode.getSession().getWorkspace().getName();
    NodeLocation nodeLocation = configurationService.getLivePortalsLocation(repository);
    if(!workspace.equalsIgnoreCase(nodeLocation.getWorkspace()))
      return;
    if(!currentNode.getPath().startsWith(nodeLocation.getPath()))
      return;
    if(publicationService.isNodeEnrolledInLifecycle(currentNode))
      return;
    publicationService.enrollNodeInLifecycle(currentNode,Constant.LIFECYCLE_NAME);
    publicationService.changeState(currentNode,Constant.DRAFT_STATE,new HashMap<String,String>());
  }

}
