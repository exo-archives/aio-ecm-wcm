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
package org.exoplatform.wcm.webui.pcv;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.portlet.PortletMode;

import org.exoplatform.services.wcm.publication.lifecycle.stageversion.Constant;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.scv.UIPresentation;
import org.exoplatform.wcm.webui.scv.UIPresentationContainer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/*
 * Created by The eXo Platform SAS 
 * Author : Anh Do Ngoc 
 * anh.do@exoplatform.com
 * Sep 24, 2008
 */

/**
 * The Class UIParameterizedContentViewerPortlet.
 */
@ComponentConfig(lifecycle = UIApplicationLifecycle.class)
public class UIParameterizedContentViewerPortlet extends UIPortletApplication {

  /** The Constant QUICK_EDIT_ABLE. */
  public final static String QUICK_EDIT_ABLE = "quickEditable";

  /**
   * Instantiates a new uI parameterized content viewer portlet.
   * 
   * @throws Exception the exception
   */
  public UIParameterizedContentViewerPortlet() throws Exception {
    addChild(UIContentViewerContainer.class, null, null);
    
  }
  
  /**
   * Can edit portlet.
   * 
   * @return true, if successful
   * 
   * @throws Exception the exception
   */
  public boolean canEditPortlet() throws Exception {
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    String userId = context.getRemoteUser();
    return Utils.canEditCurrentPortal(userId);
  }
}
