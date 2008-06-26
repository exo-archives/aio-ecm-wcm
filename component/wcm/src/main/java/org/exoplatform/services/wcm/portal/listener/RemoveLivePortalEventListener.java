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
package org.exoplatform.services.wcm.portal.listener;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.jcr.DataStorageImpl;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;

/*
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 23, 2008  
 */
public class RemoveLivePortalEventListener extends Listener<DataStorageImpl,PortalConfig> {
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.listener.Listener#onEvent(org.exoplatform.services.listener.Event)
   */
  public void onEvent(Event<DataStorageImpl, PortalConfig> event) throws Exception {  
    PortalConfig portalConfig = event.getData();
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    LivePortalManagerService livePortalManagerService = 
      (LivePortalManagerService)container.getComponentInstanceOfType(LivePortalManagerService.class);
    ThreadLocalSessionProviderService providerService =
      (ThreadLocalSessionProviderService)container.getComponentInstanceOfType(ThreadLocalSessionProviderService.class) ;
    SessionProvider sessionProvider = providerService.getSystemSessionProvider(null);
    livePortalManagerService.removeLivePortal(portalConfig,sessionProvider) ;
  }
  
}
