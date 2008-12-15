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
package org.exoplatform.wcm.webui;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.webui.application.portlet.PortletRequestContext;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.phamvu@exoplatform.com
 * Oct 23, 2008  
 */
public class Utils {
  
  public static boolean isEditPortletInCreatePageWizard() {
    UIPortal uiPortal = Util.getUIPortal();
    UIPortalApplication uiApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
    UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
    // show maskworkpace is being in Portal page edit mode    
    if(uiMaskWS.getWindowWidth() > 0 && uiMaskWS.getWindowHeight() < 0) return true;
    return false;
  }
  
  public static void refreshBrowser(PortletRequestContext context) {
    context.getJavascriptManager().addJavascript("location.reload();");
  }
  
  public static boolean canEditCurrentPortal(String remoteUser) throws Exception {
    if(remoteUser == null) return false;
    IdentityRegistry identityRegistry = Util.getUIPortalApplication().getApplicationComponent(IdentityRegistry.class);        
    Identity identity = identityRegistry.getIdentity(remoteUser);
    if(identity == null) return false;
    UIPortal uiPortal = Util.getUIPortal();
    String editPermission = uiPortal.getEditPermission();
    MembershipEntry membershipEntry = MembershipEntry.parse(editPermission);    
    return identity.isMemberOf(membershipEntry);
  }
}
