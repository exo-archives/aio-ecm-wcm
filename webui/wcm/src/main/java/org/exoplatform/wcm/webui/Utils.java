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

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;

import com.ibm.icu.text.Transliterator;

/**
 * Created by The eXo Platform SAS Author : Hoa Pham hoa.phamvu@exoplatform.com
 * Oct 23, 2008
 */
public class Utils {

  public static final String TURN_ON_QUICK_EDIT = "turnOnQuickEdit";

  /**
   * Checks if is edits the portlet in create page wizard.
   * 
   * @return true, if is edits the portlet in create page wizard
   */
  public static boolean isEditPortletInCreatePageWizard() {
    UIPortal uiPortal = Util.getUIPortal();
    UIPortalApplication uiApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
    UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
    // show maskworkpace is being in Portal page edit mode
    if (uiMaskWS.getWindowWidth() > 0 && uiMaskWS.getWindowHeight() < 0)
      return true;
    return false;
  }
  
  /**
   * Refresh browser.
   * 
   * @param context the context
   */
  public static void updatePortal(PortletRequestContext context) {
    UIPortalApplication portalApplication = Util.getUIPortalApplication();   
    PortalRequestContext portalRequestContext = (PortalRequestContext)context.getParentAppRequestContext();
    UIWorkingWorkspace uiWorkingWS = portalApplication.getChildById(UIPortalApplication.UI_WORKING_WS_ID);    
    portalRequestContext.addUIComponentToUpdateByAjax(uiWorkingWS) ;    
    portalRequestContext.setFullRender(true);
  }

  /**
   * Can edit current portal.
   * 
   * @param remoteUser the remote user
   * @return true, if successful
   * @throws Exception the exception
   */
  public static boolean canEditCurrentPortal(String remoteUser) throws Exception {
    if (remoteUser == null)
      return false;
    IdentityRegistry identityRegistry = Util.getUIPortalApplication()
    .getApplicationComponent(IdentityRegistry.class);
    Identity identity = identityRegistry.getIdentity(remoteUser);
    if (identity == null)
      return false;
    UIPortal uiPortal = Util.getUIPortal();
    //TODO this code only work for single edit permission
    String editPermission = uiPortal.getEditPermission();
    MembershipEntry membershipEntry = MembershipEntry.parse(editPermission);
    return identity.isMemberOf(membershipEntry);
  }

  public static boolean turnOnQuickEditable(PortletRequestContext context, boolean showAblePref) throws Exception {
    Object obj = Util.getPortalRequestContext().getRequest().getSession().getAttribute(Utils.TURN_ON_QUICK_EDIT);    
    boolean turnOnFlag = false;
    if (obj != null) {      
      turnOnFlag = Boolean.parseBoolean(obj.toString()); 
    }    
    if (showAblePref && turnOnFlag) {
      return true;
    } 
    return false;
  }

  public static boolean isLiveMode() {
    Object obj = Util.getPortalRequestContext().getRequest().getSession().getAttribute(Utils.TURN_ON_QUICK_EDIT);
    if(obj == null)
      return true;          
    return !Boolean.parseBoolean(obj.toString());     
  }
  
  public static String cleanString(String str) {
      Transliterator accentsconverter = Transliterator.getInstance("Latin; NFD; [:Nonspacing Mark:] Remove; NFC;");
      str = accentsconverter.transliterate(str); 
      //the character ? seems to not be changed to d by the transliterate function 
      StringBuffer cleanedStr = new StringBuffer(str.trim());
      // delete special character
      for(int i = 0; i < cleanedStr.length(); i++) {
        char c = cleanedStr.charAt(i);
        if(c == ' ') {
          if (i > 0 && cleanedStr.charAt(i - 1) == '_') {
            cleanedStr.deleteCharAt(i--);
          } else {
            c = '_';
            cleanedStr.setCharAt(i, c);
          }
          continue;
        }
        if(!(Character.isLetterOrDigit(c) || c == '_')) {
          cleanedStr.deleteCharAt(i--);
          continue;
        }
        if(i > 0 && c == '_' && cleanedStr.charAt(i-1) == '_')
          cleanedStr.deleteCharAt(i--);
      }
      return cleanedStr.toString().toLowerCase();
  }

  public static void createPopupWindow(UIContainer container, UIComponent component, String popupWindowId, int width, int height) throws Exception {
    UIPopupContainer popupContainer = getPopupContainer(container);
    popupContainer.removeChildById(popupWindowId);
    UIPopupWindow popupWindow = popupContainer.addChild(UIPopupWindow.class, null, popupWindowId);
    popupWindow.setShowMask(true);
    popupWindow.setUIComponent(component);
    popupWindow.setWindowSize(width, height);
    popupWindow.setShow(true);
    popupWindow.setResizable(true);
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    requestContext.addUIComponentToUpdateByAjax(popupContainer);
  }
  
  public static void closePopupWindow(UIContainer container, String popupWindowId) {
    UIPopupContainer popupContainer = getPopupContainer(container);
    popupContainer.removeChildById(popupWindowId);
  }
  
  public static boolean isQuickEditmode(UIContainer container, String popupWindowId) {
    UIPopupContainer popupContainer = getPopupContainer(container);
    if (popupContainer == null) return false;
    UIPopupWindow popupWindow = popupContainer.getChildById(popupWindowId);
    if (popupWindow == null) return false;
    return true;
  }
  
  public static UIPopupContainer getPopupContainer(UIContainer container) {
  	if (container instanceof UIPortletApplication) return container.getChild(UIPopupContainer.class);
    UIPortletApplication portletApplication = container.getAncestorOfType(UIPortletApplication.class);
    return portletApplication.getChild(UIPopupContainer.class);
  }
  
  public static void createPopupMessage(UIContainer container, String message, Object[] args, int type) {
    UIApplication application = container.getAncestorOfType(UIApplication.class);
    application.addMessage(new ApplicationMessage(message, args, type)) ;
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    requestContext.addUIComponentToUpdateByAjax(application.getUIPopupMessages()) ;
  }

  public static <T> T getService(UIContainer container, Class<T> clazz) {
  	return clazz.cast(container.getApplicationComponent(clazz));
  }
  
  public static SessionProvider getSessionProvider(UIContainer container) {
  	ThreadLocalSessionProviderService threadLocalSessionProviderService = getService(container, ThreadLocalSessionProviderService.class);
  	return threadLocalSessionProviderService.getSessionProvider(null);
  }
  
}
