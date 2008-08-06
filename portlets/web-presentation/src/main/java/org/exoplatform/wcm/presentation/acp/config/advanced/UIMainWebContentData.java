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
import org.exoplatform.webui.core.UITabPane;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

@ComponentConfig(
    lifecycle=Lifecycle.class,
    template =  "app:/groovy/advancedPresentation/config/UITabPane_New.gtmpl"
)
public class UIMainWebContentData extends UITabPane {

  public UIMainWebContentData() throws Exception {
    UIWebContentForm uiHtmlContent = addChild(UIWebContentForm.class, null, null) ;
    addChild(UIInternalDocuments.class, null, null) ;
    addChild(UINoNameTwo.class, null, null) ;
    addChild(UICSSFileManager.class, null, null) ;
    addChild(UIJSFileManager.class, null, null) ;
    setSelectedTab(uiHtmlContent.getId()) ;
  }

}
