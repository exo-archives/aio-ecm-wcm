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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.selectmany.UICategoriesSelector;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * May 28, 2008  
 */

@ComponentConfig(
    lifecycle = UIContainerLifecycle.class
)

public class UICategorizing extends UIContainer implements UISelectable {
  
  final static String PATH_CATEGORY = "path".intern(); 
  
  public UICategorizing() throws Exception {
   UICategoriesSelector uiCategoriesSelector = addChild(UICategoriesSelector.class, null, null);
//   uiCategoriesSelector.setExistedCategories(getExistedCategory());
//   uiCategoriesSelector.setRootCategoryPath("");
   uiCategoriesSelector.init();
  }
  
  private List<String> getExistedCategory() {
    // if node is in edit 
    //get existed category
    return new ArrayList<String>();
  }
  
  
  public void doSelect(String name, Object value) throws Exception {
    
  }
}
