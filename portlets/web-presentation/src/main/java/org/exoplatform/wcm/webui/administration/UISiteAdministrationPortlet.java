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

  /**
   * Instantiates a new uI site administration portlet.
   * 
   * @throws Exception the exception
   */
  public UISiteAdministrationPortlet() throws Exception {
    String userId = Util.getPortalRequestContext().getRemoteUser();
    if (userId != null) {
      addChild(UISiteAdminToolbar.class, null, UIPortletApplication.VIEW_MODE);  
    }    
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIPortletApplication#processRender(org.exoplatform.webui.application.WebuiApplication, org.exoplatform.webui.application.WebuiRequestContext)
   */
  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    super.processRender(app, context);
  }

}
