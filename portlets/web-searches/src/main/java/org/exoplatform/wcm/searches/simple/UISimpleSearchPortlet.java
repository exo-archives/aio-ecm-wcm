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
package org.exoplatform.wcm.searches.simple;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

// TODO: Auto-generated Javadoc
/**
 * Created by The eXo Platform SAS
 * Author : Anh Do Ngoc
 * anh.do@exoplatform.com
 * May 23, 2007
 */

@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class, 
    template = "app:/groovy/simple-search/webui/component/UISimpleSearchPortlet.gtmpl"
)

public class UISimpleSearchPortlet extends UIPortletApplication {
  
  /**
   * Instantiates a new uI simple search portlet.
   * 
   * @throws Exception the exception
   */
  public UISimpleSearchPortlet() throws Exception {
    addChild(UISimpleSearchForm.class, null, null).setRendered(true);
    addChild(UISearchResultForm.class, null, null).setRendered(true);
  }
  
}
