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
      @EventConfig(listeners=UIPublishedPages.SelectTreeNodeActionListener.class)
    }
)

public class UIPublishedPages extends UIContainer {
  private String selectedTreeNode;
  private List<String> listTreeNode;

  public String getSelectedTreeNode() {return selectedTreeNode;}
  public void setSelectedTreeNode(String selectedTreeNode) {this.selectedTreeNode = selectedTreeNode;}
  public List<String> getListTreeNode() {return listTreeNode;}
  public void setListTreeNode(List<String> listTreeNode) {this.listTreeNode = listTreeNode;}
  
  public UIPublishedPages() throws Exception {
    // TODO: Need get from JCR
    listTreeNode = new ArrayList<String>();
  }

  public static class SelectTreeNodeActionListener extends EventListener<UIPublishedPages> {
    public void execute(Event<UIPublishedPages> event) throws Exception {
      UIPublishedPages publishedPages = event.getSource();
      String selectedTreeNode = event.getRequestContext().getRequestParameter(OBJECTID);
      publishedPages.setSelectedTreeNode(selectedTreeNode);
    }
  }
}
