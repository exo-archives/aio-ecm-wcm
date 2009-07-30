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
package org.exoplatform.services.wcm.publication.listener;

import javax.jcr.Node;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationInitializerService;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 * phan.le.thanh.chuong@gmail.com, chuong_phan@exoplatform.com
 * Mar 25, 2009
 */
public class PortalInitializationEventListener extends Listener<String, SessionProvider>{

  /* (non-Javadoc)
   * @see org.exoplatform.services.listener.Listener#onEvent(org.exoplatform.services.listener.Event)
   */
  public void onEvent(Event<String, SessionProvider> event) throws Exception {
    ExoContainer exoContainer = ExoContainerContext.getCurrentContainer();
    LivePortalManagerService livePortalManagerService = (LivePortalManagerService)exoContainer.getComponentInstanceOfType(LivePortalManagerService.class);
    String portalName = event.getSource();    
    SessionProvider sessionProvider = event.getData();
    Node portal = livePortalManagerService.getLivePortal(portalName, sessionProvider);
    StageAndVersionPublicationInitializerService publicationInitializerService = (StageAndVersionPublicationInitializerService)exoContainer.getComponentInstanceOfType(StageAndVersionPublicationInitializerService.class);
    publicationInitializerService.initializePublication(portal);    
  }
  
}