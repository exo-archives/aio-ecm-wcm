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
package org.exoplatform.wcm.web.banner;

import javax.portlet.PortletMode;

import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Author : Do Ngoc Anh *      
 * Email: anh.do@exoplatform.com *
 * May 9, 2008  
 */

@ComponentConfig(lifecycle = UIApplicationLifecycle.class)
public class UIBannerPortlet extends UIPortletApplication {

  private PortletMode currenMode_ = PortletMode.VIEW;

  public UIBannerPortlet() throws Exception {
    activateMode(currenMode_);
  }

  public void activateMode(PortletMode mode) throws Exception {
    getChildren().clear();
    if (PortletMode.VIEW.equals(mode)) {
      addChild(UIBannerViewMode.class, null, UIPortletApplication.VIEW_MODE);
    } else if (PortletMode.EDIT.equals(mode)) {
      addChild(UIBannerEditModeForm.class, null, UIPortletApplication.EDIT_MODE);      
      if(isQuickEditable()) getChild(UIBannerEditModeForm.class).setQuickEditChecked(true);
    }
  }

  public boolean isQuickEditable() throws Exception {
    PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    String quickEdit = pContext.getRequest().getPreferences().getValue("quickEdit", "");
    return (Boolean.parseBoolean(quickEdit));
  }

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    PortletRequestContext pContext = (PortletRequestContext) context;
    PortletMode newMode = pContext.getApplicationMode();
    if (!currenMode_.equals(newMode)) {
      activateMode(newMode);
      currenMode_ = newMode;
    }

    super.processRender(app, context);
  }

}
