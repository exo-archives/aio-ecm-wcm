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

import java.util.HashMap;

import javax.jcr.Node;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.ecm.publication.NotInPublicationLifecycleException;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.services.wcm.publication.WCMComposer;
import org.exoplatform.services.wcm.publication.WCMPublicationService;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
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

  /** The Constant TURN_ON_QUICK_EDIT. */
  public static final String TURN_ON_QUICK_EDIT = "turnOnQuickEdit";

  /**
   * Checks if is edits the portlet in create page wizard.
   * 
   * @return true, if is edits the portlet in create page wizard
   */
  public static boolean isEditPortletInCreatePageWizard() {
    UIPortalApplication portalApplication = Util.getUIPortalApplication();
    UIMaskWorkspace uiMaskWS = portalApplication.getChildById(UIPortalApplication.UI_MASK_WS_ID);
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
   * Get node view by publication lifecycle and current mode
   * 
   * @param originalNode the original node
   * 
   * @return the node is got by publication lifecycle and current mode. Return original node if node doesn't join to any lifecycle.  
   */
  public static Node getNodeView(Node originalNode) {
  	try {
    	UIPortalApplication portalApplication = Util.getUIPortalApplication();
    	PublicationService publicationService = portalApplication.getApplicationComponent(PublicationService.class);
    	String lifecycleName = publicationService.getNodeLifecycleName(originalNode);
      PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(lifecycleName);
      HashMap<String,Object> context = new HashMap<String, Object>();    
      context.put(WCMComposer.FILTER_MODE, getCurrentMode());
      return publicationPlugin.getNodeView(originalNode, context);
  	} catch (NotInPublicationLifecycleException e) {
  		return originalNode;
  	} catch (Exception e) {
  		return null;
  	}
  }
  
  /**
   * Can edit current portal.
   * 
   * @param remoteUser the remote user
   * 
   * @return true, if successful
   * 
   * @throws Exception the exception
   */
  public static boolean canEditCurrentPortal(String remoteUser) throws Exception {
    if (remoteUser == null) return false;
    IdentityRegistry identityRegistry = Util.getUIPortalApplication().getApplicationComponent(IdentityRegistry.class);
    Identity identity = identityRegistry.getIdentity(remoteUser);
    if (identity == null) return false;
    UIPortal uiPortal = Util.getUIPortal();
    // this code only work for single edit permission
    String editPermission = uiPortal.getEditPermission();
    MembershipEntry membershipEntry = MembershipEntry.parse(editPermission);
    return identity.isMemberOf(membershipEntry);
  }

  /**
   * Gets the current mode.
   * 
   * @return the current mode
   */
  public static String getCurrentMode() {
    Object isQuickEditable = Util.getPortalRequestContext().getRequest().getSession().getAttribute(Utils.TURN_ON_QUICK_EDIT);
    if(isQuickEditable == null) return WCMComposer.MODE_LIVE;
    boolean turnOnQuickEdit = Boolean.parseBoolean(isQuickEditable.toString()); 
    return turnOnQuickEdit ? WCMComposer.MODE_EDIT : WCMComposer.MODE_LIVE;
  }

  /**
   * Check if the content is draft and in edit mode
   * 
   * @return true, if successful
   */
  public static boolean isShowDraft(Node content) {
  	try {
  		WCMPublicationService wcmPublicationService = WCMCoreUtils.getService(WCMPublicationService.class);
  		String contentState = wcmPublicationService.getContentState(content);
  		boolean isDraftContent = false;
  		if (PublicationDefaultStates.DRAFT.equals(contentState)) isDraftContent = true;
  		boolean isShowDraft = false;
  		if (WCMComposer.MODE_EDIT.equals(getCurrentMode())) isShowDraft = true;
  		return isDraftContent && isShowDraft;
		} catch (Exception e) {
			return false;
		}
  }
  
  /**
   * Check if the portlet is in edit mode
   * 
   * @return true, if successful
   */
  public static boolean isShowQuickEdit() {
  	try {
  		boolean isEditMode = false;
  		if (WCMComposer.MODE_EDIT.equals(getCurrentMode())) isEditMode = true;
  		return isEditMode;
		} catch (Exception e) {
			return false;
		}
  }
  
  /**
   * Check if the content is editable and in edit mode
   * 
   * @return true, if successful
   */
  public static boolean isShowQuickEdit(Node content) {
  	try {
  		boolean isEditMode = false;
  		if (WCMComposer.MODE_EDIT.equals(getCurrentMode())) isEditMode = true;
  		((ExtendedNode) content).checkPermission(PermissionType.SET_PROPERTY);
  		return isEditMode;
		} catch (Exception e) {
			return false;
		}
  }
  
  /**
   * Clean string.
   * 
   * @param str the str
   * 
   * @return the string
   */
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
        if(i > 0 && !(Character.isLetterOrDigit(c) || c == '_')) {
          cleanedStr.deleteCharAt(i--);
          continue;
        }
        if(i > 0 && c == '_' && cleanedStr.charAt(i-1) == '_')
          cleanedStr.deleteCharAt(i--);
      }
      return cleanedStr.toString().toLowerCase();
  }

  /**
   * Creates the popup window.
   * 
   * @param container the container
   * @param component the component
   * @param popupWindowId the popup window id
   * @param width the width
   * @param height the height
   * 
   * @throws Exception the exception
   */
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
  
  /**
   * Close popup window.
   * 
   * @param container the container
   * @param popupWindowId the popup window id
   */
  public static void closePopupWindow(UIContainer container, String popupWindowId) {
    UIPopupContainer popupContainer = getPopupContainer(container);
    popupContainer.removeChildById(popupWindowId);
  }
  
  /**
   * Checks if is quick editmode.
   * 
   * @param container the container
   * @param popupWindowId the popup window id
   * 
   * @return true, if is quick editmode
   */
  public static boolean isQuickEditMode(UIContainer container, String popupWindowId) {
    UIPopupContainer popupContainer = getPopupContainer(container);
    if (popupContainer == null) return false;
    UIPopupWindow popupWindow = popupContainer.getChildById(popupWindowId);
    if (popupWindow == null) return false;
    return true;
  }
  
  /**
   * Gets the popup container.
   * 
   * @param container the container
   * 
   * @return the popup container
   */
  public static UIPopupContainer getPopupContainer(UIContainer container) {
  	if (container instanceof UIPortletApplication) return container.getChild(UIPopupContainer.class);
    UIPortletApplication portletApplication = container.getAncestorOfType(UIPortletApplication.class);
    return portletApplication.getChild(UIPopupContainer.class);
  }
  
  /**
   * Creates the popup message.
   * 
   * @param container the container
   * @param message the message
   * @param args the args
   * @param type the type
   */
  public static void createPopupMessage(UIContainer container, String message, Object[] args, int type) {
    UIApplication application = container.getAncestorOfType(UIApplication.class);
    application.addMessage(new ApplicationMessage(message, args, type)) ;
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    requestContext.addUIComponentToUpdateByAjax(application.getUIPopupMessages()) ;
  }

  /**
   * Gets the service.
   * 
   * @param component the component
   * @param clazz the clazz
   * 
   * @return the service
   */
  public static <T> T getService(Class<T> clazz) {
  	UIPortalApplication portalApplication = Util.getUIPortalApplication();
  	return clazz.cast(portalApplication.getApplicationComponent(clazz));
  }
  
  /**
   * Gets the session provider.
   * 
   * @param component the component
   * 
   * @return the session provider
   */
  public static SessionProvider getSessionProvider(UIComponent component) {
  	return SessionProviderFactory.createSessionProvider();
  }
  
  /**
   * Gets the system provider.
   * 
   * @param component the component
   * 
   * @return the system provider
   */
  public static SessionProvider getSystemProvider(UIComponent component) {
    return SessionProviderFactory.createSystemProvider();
  }
}
