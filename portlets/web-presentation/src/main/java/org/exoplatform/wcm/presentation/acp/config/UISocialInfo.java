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

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UITabPane;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * May 28, 2008  
 */

@ComponentConfig(
    lifecycle=Lifecycle.class,
    template =  "app:/groovy/advancedPresentation/config/UITabPane_New.gtmpl"
)

public class UISocialInfo extends UITabPane {
  public UISocialInfo() throws Exception {
    UITagging uiTagging = addChild(UITagging.class, null, null) ;
    addChild(UICategorizing.class, null,null) ;
    addChild(UIMetadata.class, null, null) ;
    setSelectedTab(uiTagging.getId()) ;
  }
}
