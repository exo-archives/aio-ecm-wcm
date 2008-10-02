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

import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Sep 9, 2008  
 */

@ComponentConfig(
    lifecycle = Lifecycle.class,
    template = "classpath:groovy/wcm/webui/publication/UIPublishedPages.gtmpl",
    events = {
      @EventConfig(listeners=UIPublishedPages.SelectNavigationNodeURIActionListener.class)
    }
)

public class UIPublishedPages extends UIContainer {
  
  private String selectedNavigationNodeURI;
  private List<String> listNavigationNodeURI;
  
  public List<String> getListNavigationNodeURI() {return listNavigationNodeURI;}
  public void setListNavigationNodeURI(List<String> listNavigationNodeURI) {this.listNavigationNodeURI = listNavigationNodeURI;}
  public String getSelectedNavigationNodeURI() {return selectedNavigationNodeURI;}
  public void setSelectedNavigationNodeURI(String selectedNavigationNodeURI) {this.selectedNavigationNodeURI = selectedNavigationNodeURI;}
  
  public void init() throws Exception {
    UIPublishingPanel publishingPanel = getAncestorOfType(UIPublishingPanel.class);
    Node contentNode = publishingPanel.getNode();
    if (contentNode.hasProperty("publication:navigationNodeURIs")) {
      listNavigationNodeURI = new ArrayList<String>();
      Value[] values = contentNode.getProperty("publication:navigationNodeURIs").getValues();
      for (Value value : values) {
        listNavigationNodeURI.add(value.getString()); 
      }
    } else {
      listNavigationNodeURI = new ArrayList<String>();
    }
  }
  
  public static class SelectNavigationNodeURIActionListener extends EventListener<UIPublishedPages> {
    public void execute(Event<UIPublishedPages> event) throws Exception {
      UIPublishedPages publishedPages = event.getSource();
      String selectedTreeNode = event.getRequestContext().getRequestParameter(OBJECTID);
      publishedPages.setSelectedNavigationNodeURI(selectedTreeNode);
    }
  }
  
}