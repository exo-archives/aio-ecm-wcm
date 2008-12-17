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
package org.exoplatform.wcm.webui.scv.config;

import org.exoplatform.wcm.webui.scv.config.publication.UIWCMPublicationGrid;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 * dzungdev@gmail.com
 * Oct 9, 2008
 */
@ComponentConfig (
    template = "app:/groovy/SingleContentViewer/config/UITabPane.gtmpl"
)
public class UIWebConentNameTabForm extends UIContainer {

  /**
   * Instantiates a new uI web conent name tab form.
   * 
   * @throws Exception the exception
   */
  public UIWebConentNameTabForm() throws Exception {
    addChild(UINameWebContentForm.class, null, null);
    addChild(UIWCMPublicationGrid.class, null, null).setRendered(false);
  }

  /**
   * Inits the.
   * 
   * @throws Exception the exception
   */
  public void init() throws Exception {
    getChild(UINameWebContentForm.class).init();
    getChild(UIWCMPublicationGrid.class).updateGrid();
  }
}
