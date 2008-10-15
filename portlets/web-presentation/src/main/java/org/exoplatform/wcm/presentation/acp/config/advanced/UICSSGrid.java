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
package org.exoplatform.wcm.presentation.acp.config.advanced;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * Jun 17, 2008  
 */

@ComponentConfig(
    template = "classpath:groovy/ecm/webui/UIGridWithButton.gtmpl",
    events = {
        @EventConfig(listeners = UICSSGrid.AddActionListener.class)
    }
)

public class UICSSGrid extends UIGrid {

  public static String[] BEAN_FIELD = {"Name", "Date", "LastModified", "Creator"}; 
  public static String[] ACTIONS = {"Edit", "Delete"};

  public UICSSGrid() throws Exception {
    configure("CSSGrid", BEAN_FIELD, ACTIONS);
    getUIPageIterator().setId("UICSSGridIterator");
  }

  public String[] getActions() { return new String[] {"Add"}; }

  static public class AddActionListener extends EventListener<UICSSGrid> {
    public void execute(Event<UICSSGrid> event) throws Exception {
      UICSSGrid uiCssGrid = event.getSource();
      UICSSFileManager uiCFileManager = uiCssGrid.getParent();
      uiCFileManager.initCssPoupup();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCFileManager);
    }
  }
}
