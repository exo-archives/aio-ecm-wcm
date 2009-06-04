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

import java.util.HashMap;

import javax.jcr.Node;

import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationConstant;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.phamvu@exoplatform.com
 * Mar 6, 2009  
 */
public class PostEditContentEventListener extends Listener<CmsService,Node> {

  private PublicationService pservice;
  public PostEditContentEventListener(PublicationService pservice) {
    this.pservice = pservice;
  }

  public void onEvent(Event<CmsService, Node> event) throws Exception {
    Node currentNode = event.getData();
    String lifecycle = null;
    try {
      lifecycle = pservice.getNodeLifecycleName(currentNode); 
    } catch (Exception e) {
      return;
    }    
    if(!StageAndVersionPublicationConstant.LIFECYCLE_NAME.equalsIgnoreCase(lifecycle))
      return;    
    String state = pservice.getCurrentState(currentNode);
    if(!StageAndVersionPublicationConstant.ENROLLED_STATE.equalsIgnoreCase(state))
      return;
    pservice.changeState(currentNode,StageAndVersionPublicationConstant.DRAFT_STATE,new HashMap<String,String>());
  }

}
