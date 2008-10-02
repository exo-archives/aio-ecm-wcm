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
package org.exoplatform.services.wcm.publication.defaultlifecycle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.application.Preference;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.publication.defaultlifecycle.UIPublicationTree.TreeNode;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Sep 25, 2008  
 */

@ComponentConfig (
    lifecycle = UIFormLifecycle.class,
    template = "classpath:groovy/wcm/webui/publication/UIPublicationAction.gtmpl",
    events = {
      @EventConfig(listeners = UIPublicationAction.AddActionListener.class),
      @EventConfig(listeners = UIPublicationAction.RemoveActionListener.class)
    }
)
public class UIPublicationAction extends UIForm {

  public static class AddActionListener extends EventListener<UIPublicationAction> {
    public void execute(Event<UIPublicationAction> event) throws Exception {
      // Get service and component
      UIPublicationAction publicationAction = event.getSource();
      UIPublishingPanel publishingPanel = publicationAction.getAncestorOfType(UIPublishingPanel.class);
      UIPortalNavigationExplorer portalNavigationExplorer = publishingPanel.getChild(UIPortalNavigationExplorer.class);
      UIPublishedPages publishedPages = publishingPanel.getChild(UIPublishedPages.class);
      UIApplication application = publicationAction.getAncestorOfType(UIApplication.class);

      // Get page
      TreeNode selectedNode = portalNavigationExplorer.getSelectedNode();
      PageNode pageNode = selectedNode.getPageNode();
      if (pageNode == null) {
        application.addMessage(new ApplicationMessage("UIPublicationAction.msg.wrongNode", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(application.getUIPopupMessages());
        return;
      }
      UserPortalConfigService userPortalConfigService = publicationAction.getApplicationComponent(UserPortalConfigService.class);
      Page page = userPortalConfigService.getPage(pageNode.getPageReference(), event.getRequestContext().getRemoteUser());
      UIPage uiPage = publishingPanel.createUIComponent(UIPage.class, null, null);
      PortalDataMapper.toUIPage(uiPage, page);
      
      // Create portlet
      UIPortlet uiPortlet = uiPage.createUIComponent(UIPortlet.class, null, null);
      uiPortlet.setShowInfoBar(false);
      WCMConfigurationService configurationService = publicationAction.getApplicationComponent(WCMConfigurationService.class);
      StringBuilder windowId = new StringBuilder();
      windowId.append(PortalConfig.PORTAL_TYPE)
      .append("#")
      .append(Util.getUIPortal().getOwner())
      .append(":")
      .append(configurationService.getPublishingPortletName())
      .append("/")
      .append(IdGenerator.generate());
      uiPortlet.setWindowId(windowId.toString());

      // Add preferences to portlet
      PortletPreferences portletPreferences = new PortletPreferences();
      portletPreferences.setWindowId(windowId.toString());
      portletPreferences.setOwnerType(PortalConfig.PORTAL_TYPE);
      portletPreferences.setOwnerId(Util.getUIPortal().getOwner());
      ArrayList<Preference> listPreference = new ArrayList<Preference>();
      
      Preference preference = new Preference();
      ArrayList<String> listValue = new ArrayList<String>();
      listValue.add(((ManageableRepository)publishingPanel.getNode().getSession().getRepository()).getConfiguration().getName());
      preference.setName("repository");
      preference.setValues(listValue);
      listPreference.add(preference);
      
      preference = new Preference();
      listValue = new ArrayList<String>();
      listValue.add(publishingPanel.getNode().getSession().getWorkspace().getName());
      preference.setName("workspace");
      preference.setValues(listValue);
      listPreference.add(preference);
      
      preference = new Preference();
      listValue = new ArrayList<String>();
      listValue.add(publishingPanel.getNode().getUUID());
      preference.setName("nodeUUID");
      preference.setValues(listValue);
      
      listPreference.add(preference);
      portletPreferences.setPreferences(listPreference);
      
      DataStorage dataStorage = publicationAction.getApplicationComponent(DataStorage.class);
      dataStorage.save(portletPreferences);
      
      // Add portlet to page
      uiPage.addChild(uiPortlet);
      page = PortalDataMapper.toPageModel(uiPage);
      userPortalConfigService.update(page);

      // Add properties to node
      Node node = publishingPanel.getNode();
      Session session = node.getSession();
      ValueFactory valueFactory = session.getValueFactory();
      ArrayList<Value> listTmp;
      
      if (node.hasProperty("publication:navigationNodeURIs")) {
        listTmp = new ArrayList<Value>(Arrays.asList(node.getProperty("publication:navigationNodeURIs").getValues()));
      } else {
        listTmp = new ArrayList<Value>();
      }
      listTmp.add(valueFactory.createValue(selectedNode.getUri()));
      node.setProperty("publication:navigationNodeURIs", listTmp.toArray(new Value[0]));
      
      if (node.hasProperty("publication:webPageIDs")) {
        listTmp = new ArrayList<Value>(Arrays.asList(node.getProperty("publication:webPageIDs").getValues()));
      } else {
        listTmp = new ArrayList<Value>();
      }
      listTmp.add(valueFactory.createValue(page.getId()));
      node.setProperty("publication:webPageIDs", listTmp.toArray(new Value[0]));
      
      if (node.hasProperty("publication:applicationIDs")) {
        listTmp = new ArrayList<Value>(Arrays.asList(node.getProperty("publication:applicationIDs").getValues()));
      } else {
        listTmp = new ArrayList<Value>();
      }
      listTmp.add(valueFactory.createValue(uiPortlet.getWindowId()));
      node.setProperty("publication:applicationIDs",  listTmp.toArray(new Value[0]));
      
      session.save();

      // Display in PublishedPages
      String selectedNodeName = selectedNode.getUri();
      List<String> listPublishedPage = publishedPages.getListNavigationNodeURI();
      if (listPublishedPage.indexOf(selectedNodeName) < 0) { 
        listPublishedPage.add(selectedNodeName);
        publishedPages.setListNavigationNodeURI(listPublishedPage);
      } else {
        application.addMessage(new ApplicationMessage("UIPublicationAction.msg.duplicate", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(application.getUIPopupMessages());
        return;
      }
    }
  }

  public static class RemoveActionListener extends EventListener<UIPublicationAction> {
    public void execute(Event<UIPublicationAction> event) throws Exception {
      UIPublicationAction publicationAction = event.getSource();
      UIPublishingPanel publishingPanel = publicationAction.getAncestorOfType(UIPublishingPanel.class);
      UIPublishedPages publishedPages = publishingPanel.getChild(UIPublishedPages.class);

      Node node = publishingPanel.getNode();
      Session session = node.getSession();
      ValueFactory valueFactory = session.getValueFactory();
      Value[] values;
      if (node.hasProperty("publication:publishedPageIds")) {
        values = new Value[node.getProperty("publication:publishedPageIds").getValues().length + 1];
      } else {
        values = new Value[1];
      }
//      values[values.length - 1] = valueFactory.createValue(page.getPageId());
      node.setProperty("publication:publishedPageIds", values);
      session.save();      
      String selectedNode = publishedPages.getSelectedNavigationNodeURI();
      
    }
  }
}
