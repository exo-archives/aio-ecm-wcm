/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.dms.webui.component;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 9, 2006
 * 10:10:03 AM 
 */
@ComponentConfig(
    template =  "system:/groovy/dms/webui/component/UITreeList.gtmpl",
    events = {
        @EventConfig(listeners = UITreeNodeList.SelectActionListener.class)
    }
)
public class UITreeNodeList extends UIBaseNodeList {

  //Reference to ecm.DefaultListItem
  public UITreeNodeList() throws Exception {}

  static public class SelectActionListener extends EventListener<UITreeNodeList> {
    public void execute(Event<UITreeNodeList> event) throws Exception {
      UITreeNodeList uiDefault = event.getSource() ;
      String value = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UINodesExplorer uiJCRBrowser = uiDefault.getParent() ;
      String returnField = uiJCRBrowser.getReturnField() ;
      if(!uiJCRBrowser.isDisable()) value = uiJCRBrowser.getWorkspace() + ":" + value ;
      ((UISelectable)uiJCRBrowser.getReturnComponent()).doSelect(returnField, value) ;
    }
  }
}
