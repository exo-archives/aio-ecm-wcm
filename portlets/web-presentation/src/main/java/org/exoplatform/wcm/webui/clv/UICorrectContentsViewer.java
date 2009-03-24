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
package org.exoplatform.wcm.webui.clv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.portlet.PortletPreferences;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : anh.do
 *          anh.do@exoplatform.com, anhdn86@gmail.com		
 * Feb 23, 2009  
 */

@ComponentConfig(      
  lifecycle = Lifecycle.class,                 
   template = "app:/groovy/ContentListViewer/UIContentListViewer.gtmpl",
   events = { 
     @EventConfig(listeners = UICorrectContentsViewer.QuickEditActionListener.class) 
   }
)
public class UICorrectContentsViewer extends UIListViewerBase {

  public void init() throws Exception {                       
    PortletPreferences portletPreferences = getPortletPreference();
    setViewAbleContent(true);
    String repository = portletPreferences.getValue(UIContentListViewerPortlet.REPOSITORY, null);
    String worksapce = portletPreferences.getValue(UIContentListViewerPortlet.WORKSPACE, null);
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    SessionProvider sessionProvider = null;
    String userId = Util.getPortalRequestContext().getRemoteUser();
    if (userId == null) {
      sessionProvider = SessionProviderFactory.createAnonimProvider();
    } else {
      sessionProvider = SessionProviderFactory.createSessionProvider();
    }
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Session session = sessionProvider.getSession(worksapce, manageableRepository);
    Node root = session.getRootNode();
    List<String> contents = Arrays.asList(portletPreferences.getValues(UIContentListViewerPortlet.CONTENT_LIST, null));
    int itemsPerPage = Integer.parseInt(portletPreferences.getValue(UIContentListViewerPortlet.ITEMS_PER_PAGE, null));
    List<Node> nodes = new ArrayList<Node>();
    List<String> tempContents = new ArrayList<String>(contents);
    if (contents != null && contents.size() != 0) {
      for (int i = 0; i < contents.size(); i++) {
        Node node = null;
        String path = contents.get(i);
        try {
          node = root.getNode(path.substring(1, path.length()));
        } catch (Exception e) {
          tempContents.remove(i);
        }
        if (node != null) {          
          nodes.add(node);       
        }          
      }
    }
    if (nodes.size() == 0) {
      messageKey = "UIMessageBoard.msg.contents-not-found";
      setViewAbleContent(false);
      return;
    }    
    if (tempContents.size() != contents.size()) {
      portletPreferences.setValues(UIContentListViewerPortlet.CONTENT_LIST, tempContents.toArray(new String[0]));      
      portletPreferences.store();
    }        
    ObjectPageList pageList = new ObjectPageList(nodes, itemsPerPage);
    UIContentListPresentation contentListPresentation = addChild(UIContentListPresentation.class, null, null);
    String templatePath = getFormViewTemplatePath();
    ResourceResolver resourceResolver = getTemplateResourceResolver();       
    contentListPresentation.init(templatePath, resourceResolver, pageList);    
    contentListPresentation.setContentColumn(portletPreferences.getValue(UIContentListViewerPortlet.HEADER, null));
    contentListPresentation.setShowHeader(Boolean.parseBoolean(portletPreferences.getValue(UIContentListViewerPortlet.SHOW_HEADER, null)));
    contentListPresentation.setHeader(portletPreferences.getValue(UIContentListViewerPortlet.HEADER, null));    
  }  
    
}
