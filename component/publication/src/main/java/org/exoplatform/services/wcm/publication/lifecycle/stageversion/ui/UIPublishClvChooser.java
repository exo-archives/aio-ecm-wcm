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
package org.exoplatform.services.wcm.publication.lifecycle.stageversion.ui;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.application.Preference;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.portletcontainer.pci.ExoWindowID;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.publication.WCMPublicationService;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationConstant;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationPlugin;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationUtil;
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
 * chuong_phan@exoplatform.com
 * Mar 19, 2009
 */
@ComponentConfig (
    lifecycle = UIFormLifecycle.class,
    template = "classpath:groovy/wcm/webui/publication/lifecycle/stageversion/ui/UIPublishClvChooser.gtmpl",
    events = {
      @EventConfig(listeners = UIPublishClvChooser.ChooseActionListener.class),
      @EventConfig(listeners = UIPublishClvChooser.CloseActionListener.class)
    }
)
public class UIPublishClvChooser extends UIForm implements UIPopupComponent {
  
  /** The page. */
  private Page page;
  
  /** The node. */
  private Node node;
  
  /**
   * Gets the page.
   * 
   * @return the page
   */
  public Page getPage() {return page;}
  
  /**
   * Sets the page.
   * 
   * @param page the new page
   */
  public void setPage(Page page) {this.page = page;}
  
  /**
   * Gets the node.
   * 
   * @return the node
   */
  public Node getNode() {return node;}
  
  /**
   * Sets the node.
   * 
   * @param node the new node
   */
  public void setNode(Node node) {this.node = node;}
  
  /**
   * Instantiates a new uI publish clv chooser.
   */
  public UIPublishClvChooser() {
  }
  
  /**
   * Gets the clv portlets.
   * 
   * @return the clv portlets
   * 
   * @throws Exception the exception
   */
  public List<Application> getClvPortlets() throws Exception {
    WCMConfigurationService wcmConfigurationService = StageAndVersionPublicationUtil.getServices(WCMConfigurationService.class);
    DataStorage dataStorage = StageAndVersionPublicationUtil.getServices(DataStorage.class);
    List<String> clvPortletsId = StageAndVersionPublicationUtil.findAppInstancesByName(page, wcmConfigurationService.getRuntimeContextParam(WCMConfigurationService.CLV_PORTLET));
    List<Application> applications = new ArrayList<Application>();
    for (String clvPortletId : clvPortletsId) {
      Application application = StageAndVersionPublicationUtil.findAppInstancesById(page, clvPortletId);
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

  /**
   * The listener interface for receiving chooseAction events.
   * The class that is interested in processing a chooseAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addChooseActionListener<code> method. When
   * the chooseAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see ChooseActionEvent
   */
  public static class ChooseActionListener extends EventListener<UIPublishClvChooser> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIPublishClvChooser> event) throws Exception {
      UIPublishClvChooser clvChooser = event.getSource();
      String clvPortletId = event.getRequestContext().getRequestParameter(OBJECTID);
      clvPortletId = PortalConfig.PORTAL_TYPE + "#" + org.exoplatform.portal.webui.util.Util.getUIPortal().getOwner() + ":" + clvPortletId;
      DataStorage dataStorage = StageAndVersionPublicationUtil.getServices(DataStorage.class);
      PortletPreferences portletPreferences = dataStorage.getPortletPreferences(new ExoWindowID(clvPortletId));
      if (portletPreferences != null) {
        for (Object object : portletPreferences.getPreferences()) {
          Preference preference = (Preference) object;
          if ("contents".equals(preference.getName())) {
            String contentValues = preference.getValues().get(0).toString();
            if (contentValues.indexOf(clvChooser.node.getPath()) >= 0) {
              UIApplication application = clvChooser.getAncestorOfType(UIApplication.class);
              application.addMessage(new ApplicationMessage("UIPublishClvChooser.msg.duplicate", null, ApplicationMessage.WARNING));
              event.getRequestContext().addUIComponentToUpdateByAjax(application.getUIPopupMessages());
              return;
            }
          }
        }
      }
      WCMPublicationService presentationService = clvChooser.getApplicationComponent(WCMPublicationService.class);
      StageAndVersionPublicationPlugin publicationPlugin = (StageAndVersionPublicationPlugin) presentationService.getWebpagePublicationPlugins().get(StageAndVersionPublicationConstant.LIFECYCLE_NAME);
      publicationPlugin.publishContentToCLV(clvChooser.node, clvChooser.page, clvPortletId, Util.getUIPortal().getOwner(), event.getRequestContext().getRemoteUser());
      UIPopupWindow popupWindow = clvChooser.getAncestorOfType(UIPopupWindow.class);
      popupWindow.setShow(false);
    }
  }
  
  /**
   * The listener interface for receiving closeAction events.
   * The class that is interested in processing a closeAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addCloseActionListener<code> method. When
   * the closeAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see CloseActionEvent
   */
  public static class CloseActionListener extends EventListener<UIPublishClvChooser> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIPublishClvChooser> event) throws Exception {
      UIPublishClvChooser clvChooser = event.getSource();
      UIPopupWindow popupWindow = clvChooser.getAncestorOfType(UIPopupWindow.class);
      popupWindow.setShow(false);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIPopupComponent#activate()
   */
  public void activate() throws Exception {
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIPopupComponent#deActivate()
   */
  public void deActivate() throws Exception {
  }
}
