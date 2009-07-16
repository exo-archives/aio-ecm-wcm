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
package org.exoplatform.services.wcm.publication.listener.page;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.wcm.publication.WCMPublicationService;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham
 * hoa.pham@exoplatform.com
 * Sep 24, 2008
 */
public class CreatePageEventListener extends Listener<UserPortalConfigService, Page>{  
  
  /** The log. */
  private static Log log = ExoLogger.getLogger(CreatePageEventListener.class);
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.listener.Listener#onEvent(org.exoplatform.services.listener.Event)
   */
  public void onEvent(Event<UserPortalConfigService, Page> event) throws Exception {
    System.out.println("\n\n\n\n\n ++++++++++++++++++++++++++++++++++++++++++= \n\n\n\n\n\n\n\n\n");
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    WCMPublicationService publicationService = 
      (WCMPublicationService)container.getComponentInstanceOfType(WCMPublicationService.class);
    try {
      publicationService.updateLifecyleOnCreatePage(event.getData(), ConversationState.getCurrent().getIdentity().getUserId());
    } catch (Exception e) {
      log.error("Exception when update publication lifecyle", e);
    }    
  }

}
