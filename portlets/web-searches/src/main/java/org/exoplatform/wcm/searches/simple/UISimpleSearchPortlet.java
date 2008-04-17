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

import org.exoplatform.wcm.searches.simple.UISimpleSearchForm;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : Nguyen Ba Phu
 *          phu.nguyen@exoplatform.com
 * Apr 11, 2008  
 */

@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template = "app:/groovy/simple-search/webui/component/UISimpleSearchPortlet.gtmpl"      
)

public class UISimpleSearchPortlet extends UIPortletApplication {

  public UISimpleSearchPortlet() throws Exception {
    addChild(UISimpleSearchForm.class, null, null).setRendered(true);
    addChild(UISearchResultForm.class, null, null).setRendered(true);
  }
}
