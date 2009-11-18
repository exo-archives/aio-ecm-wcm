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
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.portal.config.jcr.DataStorageImpl;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.cms.drives.DriveData;
import org.exoplatform.services.cms.drives.ManageDriveService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.portal.artifacts.CreatePortalArtifactsService;
import org.exoplatform.services.wcm.skin.XSkinService;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;

/**
 * Created by The eXo Platform SAS
 * 
 * @author : Hoa.Pham hoa.pham@exoplatform.com Jun 23, 2008
 */
public class CreateLivePortalEventListener extends Listener<DataStorageImpl, PortalConfig> {
  private static Log log = ExoLogger.getLogger(CreateLivePortalEventListener.class);

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.listener.Listener#onEvent(org.exoplatform.services.listener.Event)
   */
  public final void onEvent(final Event<DataStorageImpl, PortalConfig> event) throws Exception {
  	// Create site content storage for the portal
    PortalConfig portalConfig = event.getData();
    String portalName = portalConfig.getName();
    LivePortalManagerService livePortalManagerService = WCMCoreUtils.getService(LivePortalManagerService.class);
    SessionProvider sessionProvider = WCMCoreUtils.getSessionProvider();
    try {
      livePortalManagerService.addLivePortal(sessionProvider, portalConfig);
      log.info("Create new resource storage for portal: " + portalName);
    } catch (Exception e) {
      log.error("Error when create new resource storage: " + portalName, e.fillInStackTrace());
    }
    
    //Deploy initial artifacts for this portal 
    CreatePortalArtifactsService artifactsInitializerService = WCMCoreUtils.getService(CreatePortalArtifactsService.class);
    try {      
      artifactsInitializerService.deployArtifactsToPortal(sessionProvider, portalName);
    } catch (Exception e) {
      log.error("Error when create drive for portal: " + portalName, e.fillInStackTrace());
    }
    
    // create drive for the site content storage
    try {
    	Node portal = livePortalManagerService.getLivePortal(sessionProvider, portalName);
    	createPortalDrive(portal,portalConfig);
    	log.info("Create new drive for portal: " + portalName);
    } catch (Exception e) {
    	log.error("Error when create drive for portal: " + portalName, e.fillInStackTrace());
    }
    
    // Update skin for new portal
    XSkinService xSkinService = WCMCoreUtils.getService(XSkinService.class);
    xSkinService.start();
    
    sessionProvider.close();
  }

  private void createPortalDrive(Node portal, PortalConfig portalConfig) throws Exception {
    Session session = portal.getSession();      
    String repository = ((ManageableRepository) session.getRepository()).getConfiguration().getName();
    String workspace = session.getWorkspace().getName();
    WCMConfigurationService configurationService = WCMCoreUtils.getService(WCMConfigurationService.class);
    DriveData mainDriveData = configurationService.getSiteDriveConfig();
    String permission = portalConfig.getEditPermission();
    String portalName = portal.getName();
    String portalPath = portal.getPath();
    
    String homePath = mainDriveData.getHomePath(); 
    homePath = homePath.replaceAll(WCMConfigurationService.SITE_NAME_EXP, portalName);
    homePath = homePath.replaceAll(WCMConfigurationService.SITE_PATH_EXP, portalPath);
    
    String views = mainDriveData.getViews();
    String icon = mainDriveData.getIcon();
    boolean viewReferences = mainDriveData.getViewPreferences();
    boolean viewNonDocument = mainDriveData.getViewNonDocument();
    boolean viewSideBar = mainDriveData.getViewSideBar();
    boolean showHiddenNode = mainDriveData.getShowHiddenNode();
    String allowCreateFolder = mainDriveData.getAllowCreateFolder();
    ManageDriveService manageDriveService = WCMCoreUtils.getService(ManageDriveService.class);
    manageDriveService.addDrive(portalName, workspace, permission, homePath, views, icon, viewReferences, viewNonDocument, viewSideBar, showHiddenNode, repository, allowCreateFolder);
  }
}
