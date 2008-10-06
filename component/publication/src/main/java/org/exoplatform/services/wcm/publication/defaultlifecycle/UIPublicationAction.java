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
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Value;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.wcm.publication.WCMPublicationPresentationService;
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
  
  private void updateUI() throws Exception {
    UIPublishingPanel publishingPanel = getAncestorOfType(UIPublishingPanel.class);
    UIPublishedPages publishedPages = publishingPanel.getChild(UIPublishedPages.class);
    
    Node node = publishingPanel.getNode();
    List<String> listPublishedPage = new ArrayList<String>();
    Value[] navigationNodeURIs = node.getProperty("publication:navigationNodeURIs").getValues();
    for (Value navigationNodeURI : navigationNodeURIs) {
      listPublishedPage.add(navigationNodeURI.getString());
    }
    publishedPages.setListNavigationNodeURI(listPublishedPage);
  }
  
  public static class AddActionListener extends EventListener<UIPublicationAction> {
    public void execute(Event<UIPublicationAction> event) throws Exception {
      UIPublicationAction publicationAction = event.getSource();
      UIPublishingPanel publishingPanel = publicationAction.getAncestorOfType(UIPublishingPanel.class);
      UIApplication application = publicationAction.getAncestorOfType(UIApplication.class);
      
      UIPortalNavigationExplorer portalNavigationExplorer = publishingPanel.getChild(UIPortalNavigationExplorer.class);
      TreeNode selectedNode = portalNavigationExplorer.getSelectedNode();
      
      if (selectedNode == null) {
        application.addMessage(new ApplicationMessage("UIPublicationAction.msg.none", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(application.getUIPopupMessages());
        return;
      }
      
      String selectedNavigationNodeURI = selectedNode.getUri();
      Node node = publishingPanel.getNode();
      if (node.hasProperty("publication:navigationNodeURIs")) {
        Value[] navigationNodeURIs = node.getProperty("publication:navigationNodeURIs").getValues();
        for (Value navigationNodeURI : navigationNodeURIs) {
          if (navigationNodeURI.getString().equals(selectedNavigationNodeURI)) {
            application.addMessage(new ApplicationMessage("UIPublicationAction.msg.duplicate", null, ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(application.getUIPopupMessages());
            return;
          }
        }
      }
      
      PageNode pageNode = selectedNode.getPageNode();
      if (pageNode == null) {
        application.addMessage(new ApplicationMessage("UIPublicationAction.msg.wrongNode", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(application.getUIPopupMessages());
        return;
      }
      
      UserPortalConfigService userPortalConfigService = publicationAction.getApplicationComponent(UserPortalConfigService.class);
      Page page = userPortalConfigService.getPage(pageNode.getPageReference(), event.getRequestContext().getRemoteUser());
      WCMPublicationPresentationService presentationService = publicationAction.getApplicationComponent(WCMPublicationPresentationService.class);
      WCMPublicationPlugin publicationPlugin = (WCMPublicationPlugin) presentationService.getWebpagePublicationPlugins().get(WCMPublicationPlugin.LIFECYCLE_NAME);
      publicationPlugin.publishContentToPage(node, page);
      
      publicationAction.updateUI();
    }
  }

  public static class RemoveActionListener extends EventListener<UIPublicationAction> {
    public void execute(Event<UIPublicationAction> event) throws Exception {
      UIPublicationAction publicationAction = event.getSource();
      UIPublishingPanel publishingPanel = publicationAction.getAncestorOfType(UIPublishingPanel.class);
      UserPortalConfigService userPortalConfigService = publicationAction.getApplicationComponent(UserPortalConfigService.class);
      
      UIPublishedPages publishedPages = publishingPanel.getChild(UIPublishedPages.class);
      DataStorage dataStorage = publicationAction.getApplicationComponent(DataStorage.class);
      String selectedNavigationNodeURI = publishedPages.getSelectedNavigationNodeURI();
      
      if (selectedNavigationNodeURI == null) {
        UIApplication application = publicationAction.getAncestorOfType(UIApplication.class);
        application.addMessage(new ApplicationMessage("UIPublicationAction.msg.none", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(application.getUIPopupMessages());
        return;
      }
      
      String portalName = selectedNavigationNodeURI.substring(1, selectedNavigationNodeURI.indexOf("/", 1));
      String pageNodeUri = selectedNavigationNodeURI.replaceFirst("/\\w+/", "");
      PageNavigation pageNavigation = null;
      Page page = null;
      Query<PageNavigation> query = new Query<PageNavigation>(PortalConfig.PORTAL_TYPE, portalName, PageNavigation.class);
      PageList list = dataStorage.find(query);
      for(Object object: list.getAll()) {
        pageNavigation = PageNavigation.class.cast(object);
      }
      if (pageNavigation != null) {
        Node contentNode = publishingPanel.getNode();
        if (contentNode.hasProperty("publication:applicationIDs")) {
          PageNode pageNode = pageNavigation.getNode(pageNodeUri);
          page = userPortalConfigService.getPage(pageNode.getPageReference(), event.getRequestContext().getRemoteUser());
        }
      }
      
      WCMPublicationPresentationService presentationService = publicationAction.getApplicationComponent(WCMPublicationPresentationService.class);
      WCMPublicationPlugin publicationPlugin = (WCMPublicationPlugin) presentationService.getWebpagePublicationPlugins().get(WCMPublicationPlugin.LIFECYCLE_NAME);
      publicationPlugin.suspendPublishedContentFromPage(publishingPanel.getNode(), page);
      
      publicationAction.updateUI();
    }
  }
}