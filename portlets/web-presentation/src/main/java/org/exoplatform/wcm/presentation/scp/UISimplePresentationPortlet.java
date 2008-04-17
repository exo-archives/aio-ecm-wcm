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
package org.exoplatform.wcm.presentation.scp;

import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Mar 18, 2008  
 */

@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class
)

public class UISimplePresentationPortlet extends UIPortletApplication {

  private int mode_  = PortletRequestContext.VIEW_MODE ;
  static String REPOSITORY = "repository" ;
  static String WORKSPACE = "workspace" ;
  static String UUID = "nodeUUID" ;
  
  public UISimplePresentationPortlet() throws Exception {
    activateMode(mode_) ;
  }
  
  public void activateMode(int mode) throws Exception {
    getChildren().clear() ;
    if(PortletRequestContext.VIEW_MODE == mode) {
      addChild(UIPresentation.class, null, UIPortletApplication.VIEW_MODE) ;
    } else if (PortletRequestContext.EDIT_MODE == mode) {      
      addChild(UIPortletConfig.class, null, UIPortletApplication.EDIT_MODE) ;
    }
  }
  
  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    PortletRequestContext pContext = (PortletRequestContext) context ;
    int newMode = pContext.getApplicationMode() ;
    if(mode_ != newMode) {
      activateMode(newMode) ;
      mode_ = newMode ;
    }
    super.processRender(app, context) ;
  }

}
