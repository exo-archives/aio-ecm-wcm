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
package org.exoplatform.wcm.presentation.acp.config;

import org.exoplatform.wcm.presentation.acp.config.advanced.UIJSFileManager;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * May 30, 2008  
 */

@ComponentConfig(
    template = "classpath:groovy/ecm/webui/UIGridWithButton.gtmpl",
    events = {
        @EventConfig(listeners = UIJSGrid.AddActionListener.class)
    }
)

public class UIJSGrid extends UIGrid {

  public final static String[] BEAN_FIELD = {"Name", "Date", "LastModified", "Creator"} ;
  public final static String[] ACTIONS = {"Edit", "Delete"} ;

  public UIJSGrid() throws Exception {
    getUIPageIterator().setId("JSGridIterator") ;
    configure("name", BEAN_FIELD, ACTIONS) ;
  }

  public String[] getActions() {return new String[] {"Add"} ;}

  static public class AddActionListener extends EventListener<UIJSGrid> {
    public void execute(Event<UIJSGrid> event) throws Exception {
      UIJSGrid uiJSGrid = event.getSource();
      UIJSFileManager uiJSFileManager = uiJSGrid.getParent() ;
      uiJSFileManager.initJSPopup() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiJSFileManager) ;
    }
  }
}
