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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.application.Preference;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.portletcontainer.pci.ExoWindowID;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.publication.WCMPublicationService;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Sep 24, 2008  
 */
public class UpdatePageEventListener extends Listener<UserPortalConfigService, Page> {
  
  private static Log log = ExoLogger.getLogger(UpdatePageEventListener.class);
  private ExoContainer exoContainer = ExoContainerContext.getCurrentContainer();
  private DataStorage dataStorage = (DataStorage) exoContainer.getComponentInstanceOfType(DataStorage.class);
    
  public void onEvent(Event<UserPortalConfigService, Page> event) throws Exception {    
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    WCMPublicationService publicationService = 
      (WCMPublicationService)container.getComponentInstanceOfType(WCMPublicationService.class);
    try {
      publicationService.updateLifecyleOnChangePage(event.getData());
    } catch (Exception e) {
      log.error("Exception when update publication lifecyle", e);
    }       
  }

  private Application getPublishingApplication(Page page) {
    WCMConfigurationService configurationService = (WCMConfigurationService) exoContainer.getComponentInstanceOfType(WCMConfigurationService.class);
    ArrayList<Object> listChildren = page.getChildren();
    for (Object child : listChildren) {
      if (child instanceof Application) {
        Application application = (Application) child;
        if (application.getInstanceId().contains(configurationService.getPublishingPortletName())) {
          return application;
        }
      }
    }
    return null;
  }

  private Map<String, String> getPortletPreferenceData(String portletId) throws Exception {
    PortletPreferences portletPreferences = dataStorage.getPortletPreferences(new ExoWindowID(portletId));
    String repositoryName = null; 
    String workspaceName = null;
    String nodeUUID = null;
    for (Object obj : portletPreferences.getPreferences()) {
      Preference preference = (Preference) obj;
      if ("repository".equals(preference.getName())) {
        repositoryName = (String) preference.getValues().get(0);
      } else if ("workspace".equals(preference.getName())) {
        workspaceName = (String) preference.getValues().get(0);
      } else if ("nodeUUID".equals(preference.getName())) {
        nodeUUID = (String) preference.getValues().get(0);
      }
    }

    if (repositoryName == null || workspaceName == null || nodeUUID == null) {
      return null;
    }

    Map<String,String> data = new HashMap<String,String>();
    data.put("workspace", workspaceName);
    data.put("repository", repositoryName);
    data.put("nodeUUID", nodeUUID);
    return data;
  }
}