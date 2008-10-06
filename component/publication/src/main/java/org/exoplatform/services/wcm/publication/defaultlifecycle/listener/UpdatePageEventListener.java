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
package org.exoplatform.services.wcm.publication.defaultlifecycle.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.application.Preference;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.portletcontainer.pci.ExoWindowID;
import org.exoplatform.services.wcm.core.WCMConfigurationService;

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
    log.info("=======Upadate Page==="+event.getData().getPageId());    
    
    Application application = getPublishingApplication(event.getData());
    Map<String, String> data = getPortletPreferenceData(application.getInstanceId());

    RepositoryService repositoryService = (RepositoryService) exoContainer.getComponentInstanceOfType(RepositoryService.class);
    SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
    Session session = sessionProvider.getSession(data.get("workspace"), repositoryService.getRepository(data.get("repository")));
    Node node = session.getNodeByUUID(data.get("nodeUUID"));
    if (node.canAddMixin("publication:wcmPublication")) node.addMixin("publication:wcmPublication");
    ValueFactory valueFactory = session.getValueFactory();
    Value[] values;
    if (node.hasProperty("publication:publishedPageIds")) {
      values = new Value[node.getProperty("publication:publishedPageIds").getValues().length + 1];
    } else {
      values = new Value[1];
    }
    values[values.length - 1] = valueFactory.createValue(event.getData().getPageId());
    node.setProperty("publication:publishedPageIds", values);
    session.save();
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