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
package org.exoplatform.services.wcm.publication.lifecycle.stageversion;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.application.Preference;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.portletcontainer.pci.ExoWindowID;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.publication.WCMPublicationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong_phan@exoplatform.com
 * Mar 19, 2009  
 */
@ComponentConfig (
    lifecycle = UIFormLifecycle.class,
    template = "classpath:groovy/wcm/webui/publication/lifecycle/stageversion/UIPublishClvChooser.gtmpl",
    events = {
      @EventConfig(listeners = UIPublishClvChooser.ChooseActionListener.class),
      @EventConfig(listeners = UIPublishClvChooser.CloseActionListener.class)
    }
)
public class UIPublishClvChooser extends UIForm implements UIPopupComponent {
  
  private Page page;
  private NodeLocation nodeLocation;
  
  public Page getPage() {return page;}
  public void setPage(Page page) {this.page = page;}
  public Node getNode() {
	return NodeLocation.getNodeByLocation(nodeLocation);
  }
  public void setNode(Node node) {
	nodeLocation = NodeLocation.make(node);
  }
  
  public UIPublishClvChooser() {
  }
  
  public List<Application> getClvPortlets() throws Exception {
    WCMConfigurationService wcmConfigurationService = Util.getServices(WCMConfigurationService.class);
    DataStorage dataStorage = Util.getServices(DataStorage.class);
    List<String> clvPortletsId = Util.findAppInstancesByName(page, wcmConfigurationService.getRuntimeContextParam("CLVPortlet"));
    List<Application> applications = new ArrayList<Application>();
    for (String clvPortletId : clvPortletsId) {
      Application application = Util.findAppInstancesById(page, clvPortletId);
      PortletPreferences portletPreferences = dataStorage.getPortletPreferences(new ExoWindowID(clvPortletId));      
      if (portletPreferences != null) {
        for (Object object : portletPreferences.getPreferences()) {
          Preference preference = (Preference) object;
          if ("header".equals(preference.getName()) && preference.getValues().size() > 0) {
            application.setTitle(preference.getValues().get(0).toString());
          }
        }
      }
      applications.add(application);
    }
    return applications;
  }

  public static class ChooseActionListener extends EventListener<UIPublishClvChooser> {
    public void execute(Event<UIPublishClvChooser> event) throws Exception {
      UIPublishClvChooser clvChooser = event.getSource();
      String clvPortletId = event.getRequestContext().getRequestParameter(OBJECTID);
      clvPortletId = PortalConfig.PORTAL_TYPE + "#" + org.exoplatform.portal.webui.util.Util.getUIPortal().getOwner() + ":" + clvPortletId;
      DataStorage dataStorage = Util.getServices(DataStorage.class);
      PortletPreferences portletPreferences = dataStorage.getPortletPreferences(new ExoWindowID(clvPortletId));
      Node node = clvChooser.getNode();
      if (portletPreferences != null) {
        for (Object object : portletPreferences.getPreferences()) {
          Preference preference = (Preference) object;
          if ("contents".equals(preference.getName())) {
            String contentValues = preference.getValues().get(0).toString();
            if (contentValues.indexOf(node.getPath()) >= 0) {
              UIApplication application = clvChooser.getAncestorOfType(UIApplication.class);
              application.addMessage(new ApplicationMessage("UIPublishClvChooser.msg.duplicate", null, ApplicationMessage.WARNING));
              event.getRequestContext().addUIComponentToUpdateByAjax(application.getUIPopupMessages());
              return;
            }
          }
        }
      }
      WCMPublicationService presentationService = clvChooser.getApplicationComponent(WCMPublicationService.class);
      StageAndVersionBasedPublicationPlugin publicationPlugin = (StageAndVersionBasedPublicationPlugin) presentationService.getWebpagePublicationPlugins().get(Constant.LIFECYCLE_NAME);
      publicationPlugin.publishContentToCLV(node, clvChooser.page, clvPortletId, portletPreferences);
      UIPopupWindow popupWindow = clvChooser.getAncestorOfType(UIPopupWindow.class);
      popupWindow.setShow(false);
    }
  }
  
  public static class CloseActionListener extends EventListener<UIPublishClvChooser> {
    public void execute(Event<UIPublishClvChooser> event) throws Exception {
      UIPublishClvChooser clvChooser = event.getSource();
      UIPopupWindow popupWindow = clvChooser.getAncestorOfType(UIPopupWindow.class);
      popupWindow.setShow(false);
    }
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }
}
