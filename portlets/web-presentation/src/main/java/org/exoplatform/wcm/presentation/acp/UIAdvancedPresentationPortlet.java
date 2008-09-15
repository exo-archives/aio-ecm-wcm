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
package org.exoplatform.wcm.presentation.acp;

import java.util.List;

import javax.jcr.Node;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.wcm.presentation.acp.config.UIPortletConfig;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Mar 18, 2008  
 */

@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    events = {
      @EventConfig(listeners = UIAdvancedPresentationPortlet.QuickEditActionListener.class)
    }
)

public class UIAdvancedPresentationPortlet extends UIPortletApplication {

  public static String REPOSITORY = "repository" ;
  public static String WORKSPACE = "workspace" ;
  public static String UUID = "nodeUUID" ;

  private PortletMode mode_ = PortletMode.VIEW ;

  public UIAdvancedPresentationPortlet() throws Exception {
    activateMode(mode_) ;
  }

  public void activateMode(PortletMode mode) throws Exception {
    getChildren().clear() ;
    if(PortletMode.VIEW.equals(mode)) {
      addChild(UIPresentationContainer.class, null, UIPortletApplication.VIEW_MODE) ;
    } else if (PortletMode.EDIT.equals(mode)) {      
      addChild(UIPortletConfig.class, null, UIPortletApplication.EDIT_MODE) ;
    }
  }

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    PortletRequestContext pContext = (PortletRequestContext) context ;
    PortletMode newMode = pContext.getApplicationMode() ;
    if(!mode_.equals(newMode)) {
      activateMode(newMode) ;
      mode_ = newMode ;
    }
    super.processRender(app, context) ;
  }

  public static class QuickEditActionListener extends EventListener<UIAdvancedPresentationPortlet> {
    public void execute(Event<UIAdvancedPresentationPortlet> event) throws Exception {
      PortletRequestContext context = (PortletRequestContext)event.getRequestContext();
      context.setApplicationMode(PortletMode.EDIT);
    }
  }
}
