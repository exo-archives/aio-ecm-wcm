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
package org.exoplatform.wcm.webui.clv;

import java.util.HashMap;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.NodeIterator;
import javax.portlet.PortletPreferences;

import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.wcm.publication.WCMComposer;
import org.exoplatform.services.wcm.utils.PaginatedNodeIterator;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

// TODO: Auto-generated Javadoc
/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 15, 2008
 */

@ComponentConfig(      
  lifecycle = Lifecycle.class,                 
   template = "app:/groovy/ContentListViewer/UICLVManualMode.gtmpl",
   events = { 
     @EventConfig(listeners = UICLVFolderMode.QuickEditActionListener.class) 
   }
)
public class UICLVFolderMode extends UICLVContainer {

  /* (non-Javadoc)
   * @see org.exoplatform.wcm.webui.clv.UICLVContainer#init()
   */
  public void init() throws Exception {
    PortletPreferences portletPreferences = getPortletPreference();    
    NodeIterator nodeIterator = null;
    setViewAbleContent(true);
    messageKey = null;
    try {
      nodeIterator = getRenderedContentNodes();
    } catch (ItemNotFoundException e) {
      messageKey = "UIMessageBoard.msg.folder-not-found";
      setViewAbleContent(false);
      return;
    } catch (AccessDeniedException e) {
      messageKey = "UIMessageBoard.msg.no-permission";
      setViewAbleContent(false);
      return;
    } catch (Exception e) {
      messageKey = "UIMessageBoard.msg.error-nodetype";
      setViewAbleContent(false);
      return;
    }
    if (nodeIterator.getSize() == 0) {
      messageKey = "UIMessageBoard.msg.folder-empty";
      setViewAbleContent(false);
      return;
    }
    int itemsPerPage = Integer.parseInt(portletPreferences.getValue(UICLVPortlet.ITEMS_PER_PAGE, null));
    PaginatedNodeIterator paginatedNodeIterator = new PaginatedNodeIterator(nodeIterator, itemsPerPage);
    getChildren().clear();
    UICLVPresentation contentListPresentation = addChild(UICLVPresentation.class, null, null);    
    String templatePath = getFormViewTemplatePath();
    ResourceResolver resourceResolver = getTemplateResourceResolver();    
    contentListPresentation.init(templatePath, resourceResolver, paginatedNodeIterator);    
    contentListPresentation.setContentColumn(portletPreferences.getValue(UICLVPortlet.HEADER, null));
    contentListPresentation.setShowLink(Boolean.parseBoolean(portletPreferences.getValue(UICLVPortlet.SHOW_LINK, null)));
    contentListPresentation.setShowHeader(Boolean.parseBoolean(portletPreferences.getValue(UICLVPortlet.SHOW_HEADER, null)));
    contentListPresentation.setShowReadmore(Boolean.parseBoolean(portletPreferences.getValue(UICLVPortlet.SHOW_READMORE, null)));
    contentListPresentation.setHeader(portletPreferences.getValue(UICLVPortlet.HEADER, null));
  }
  
  /**
   * Gets the rendered content nodes.
   * 
   * @return the rendered content nodes
   * 
   * @throws Exception the exception
   */
  public NodeIterator getRenderedContentNodes() throws Exception {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    PortletPreferences preferences = portletRequestContext.getRequest().getPreferences();
    String repository = preferences.getValue(UICLVPortlet.REPOSITORY, null);
    String workspace = preferences.getValue(UICLVPortlet.WORKSPACE, null);
    String folderPath = preferences.getValue(UICLVPortlet.FOLDER_PATH, null);
    if (repository == null || workspace == null || folderPath == null)
      throw new ItemNotFoundException();

    WCMComposer wcmComposer = getApplicationComponent(WCMComposer.class);
    HashMap<String, String> filters = new HashMap<String, String>();
    filters.put(WCMComposer.FILTER_MODE, Utils.isLiveMode()?WCMComposer.MODE_LIVE:WCMComposer.MODE_EDIT);
    String orderBy = preferences.getValue(UICLVPortlet.ORDER_BY, null);
    String orderType = preferences.getValue(UICLVPortlet.ORDER_TYPE, null);
    if (orderType == null) orderType = "DESC";
    if (orderBy == null) orderBy = "exo:title";
    filters.put(WCMComposer.FILTER_ORDER_BY, orderBy);
    filters.put(WCMComposer.FILTER_ORDER_TYPE, orderType);
    
    return wcmComposer.getContents(repository, workspace, folderPath, filters, Utils.getSessionProvider(this));
  }
}
