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

import javax.jcr.Node;

import org.exoplatform.portal.config.DataStorageImpl;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.cms.drives.ManageDriveService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.portal.artifacts.RemovePortalArtifactsService;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;

/**
 * Created by The eXo Platform SAS
 * 
 * @author : Hoa.Pham hoa.pham@exoplatform.com Jun 23, 2008
 */
public class RemoveLivePortalEventListener extends Listener<DataStorageImpl, PortalConfig> {
  private Log log = ExoLogger.getLogger(RemoveLivePortalEventListener.class);

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.listener.Listener#onEvent(org.exoplatform.services.listener.Event)
   */
  public void onEvent(Event<DataStorageImpl, PortalConfig> event) throws Exception {
  	PortalConfig portalConfig = event.getData();
  	String portalName = portalConfig.getName();
  	LivePortalManagerService livePortalManagerService = WCMCoreUtils.getService(LivePortalManagerService.class);
  	SessionProvider sessionProvider = WCMCoreUtils.getSessionProvider();    
  	Node portal = livePortalManagerService.getLivePortal(sessionProvider, portalName);
  	
  	// Remove drive for the site content storage
  	ManageDriveService manageDriveService = WCMCoreUtils.getService(ManageDriveService.class);    
  	String repository = NodeLocation.make(portal).getRepository();
  	try {
  		manageDriveService.removeDrive(portalName, repository);
  		log.info("Removed drive for portal: " + portalName);
		} catch (Exception e) {
			log.error("Error when remove drive for portal: " + portalName, e.fillInStackTrace());
		}
  	
  	// Remove initial artifacts for this portal
  	RemovePortalArtifactsService removePortalArtifactsService = WCMCoreUtils.getService(RemovePortalArtifactsService.class);
  	removePortalArtifactsService.invalidateArtifactsFromPortal(sessionProvider, portalName);
		
  	// Remove site content storage for the portal
  	try {
  		livePortalManagerService.removeLivePortal(sessionProvider, portalConfig);
  		log.info("Removed resource storage for portal: " + portalName);
		} catch (Exception e) {
			log.error("Error when remove resource storage: " + portalName, e.fillInStackTrace());
		}
  	
    sessionProvider.close();
  }

}
