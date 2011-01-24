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
package org.exoplatform.wcm.webui.administration;

import java.util.Locale;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/*
 * Created by The eXo Platform SAS 
 * Author : Anh Do Ngoc 
 * anh.do@exoplatform.com
 * Oct 6, 2008
 */
/**
 * The Class UISiteAdministrationPortlet.
 */
@ComponentConfig(lifecycle = UIApplicationLifecycle.class)
public class UISiteAdministrationPortlet extends UIPortletApplication {

  /** The last portal uri. */
  private String lastPortalURI = null;
  
  /** The last locale. */
  private Locale lastLocale = null;
  

  /** The Constant VISITOR. */
  public static final int VISITOR           = -1;
  /**
   * Instantiates a new uI site administration portlet.
   * 
   * @throws Exception the exception
   */
  public UISiteAdministrationPortlet() throws Exception {
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    String userId = portalRequestContext.getRemoteUser();
    if (userId != null) {      
      lastPortalURI = portalRequestContext.getPortalURI();
      lastLocale = portalRequestContext.getLocale();
      addChild(UISiteAdminToolbar.class, null, UIPortletApplication.VIEW_MODE);  
    }    
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIPortletApplication#processRender(org.exoplatform.webui.application.WebuiApplication, org.exoplatform.webui.application.WebuiRequestContext)
   */
  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {    
    UISiteAdminToolbar adminToolbar = getChild(UISiteAdminToolbar.class);
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    if(adminToolbar != null) {
      if(portalRequestContext.getFullRender()) {
        adminToolbar.refresh();
      }  
      String currentPortalURI = portalRequestContext.getPortalURI();
      if(!currentPortalURI.equalsIgnoreCase(lastPortalURI)) {
        adminToolbar.refresh();
        lastPortalURI = currentPortalURI;
      }
      Locale currentLocale = portalRequestContext.getLocale();
      if(!currentLocale.getLanguage().equalsIgnoreCase(lastLocale.getLanguage())) {
        adminToolbar.changeNavigationsLanguage(currentLocale.getLanguage());
        lastLocale = currentLocale;
      }
      if (adminToolbar.getRole() != VISITOR) super.processRender(app, context);
    }             
  }
}
