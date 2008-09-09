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
package org.exoplatform.services.wcm.plugins.publication;

import javax.jcr.Node;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Sep 9, 2008  
 */
@ComponentConfig (
    lifecycle = UIApplicationLifecycle.class,
    template = "classpath:groovy/wcm/webui/publication/UIPublishingManager.gtmpl"
)
public class UIPublishingPanel extends UIForm {
  
  private Node currentNode;
  
  public UIPublishingPanel() throws Exception {
    //Left panel
    addChild(UIPortalNavigationExplorer.class,null,null);
    //right panel
    addChild(UIPublishedPages.class,null,null);
  }
  
  public void initPanel(Node node) {
    currentNode = node;
  }
  
  public Node getNode(Node node) { return this.currentNode; }
  public void setNode(Node node) {this.currentNode = node; }  
}
