/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.wcm.webui.newsletter.manager;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterManageUserHandler;
import org.exoplatform.services.wcm.publication.PublicationUtil;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : Tran Nguyen Ngoc
 * ngoc.tran@exoplatform.com
 * May 22, 2009
 */
@ComponentConfig (
		lifecycle = UIApplicationLifecycle.class
)
public class UINewsletterManagerPortlet extends UIPortletApplication {
  private boolean isAdmin = false;
  
  public boolean isAdmin() {
    return isAdmin;
  }

  public void setAdmin(boolean isAdmin) {
    this.isAdmin = isAdmin;
  }
  
	/** The is render ui categories. */
	private boolean isRenderUICategories = true;
	
	/** The is render ui subscription. */
	@SuppressWarnings("unused")
  private boolean isRenderUISubscription = false;
	
	/** The is render ui news letters. */
	private boolean isRenderUINewsLetters = false;
	
	/**
	 * Sets the render u icategories.
	 */
	public void setRenderUIcategories(){
	  isRenderUICategories = true;
	  isRenderUISubscription = false;
	  isRenderUINewsLetters = false;
	}
	
	/**
	 * Sets the render u isubscriptions.
	 */
	public void setRenderUIsubscriptions(){
	  isRenderUICategories = false;
	  isRenderUISubscription = true;
	  isRenderUINewsLetters = false;
	}
	
	/**
	 * Sets the render u inews letter.
	 */
	public void setRenderUInewsLetter(){
	  isRenderUICategories = false;
	  isRenderUISubscription = false;
	  isRenderUINewsLetters = true;
	}
	
	private List<String> getAllAdministrators() throws Exception{
    List<String> editPermission = new ArrayList<String>();
    NewsletterManagerService newsletterManagerService = getApplicationComponent(NewsletterManagerService.class);
    NewsletterManageUserHandler managerUserHandler = newsletterManagerService.getManageUserHandler();
    editPermission.addAll(managerUserHandler.getAllAdministrator(Utils.getSessionProvider(), NewsLetterUtil.getPortalName()));
    String supperUser = PublicationUtil.getServices(UserACL.class).getSuperUser(); 
    if(!editPermission.contains(supperUser) && supperUser.equals(NewsLetterUtil.getCurrentUser())){
      editPermission.add(supperUser);
      SessionProvider sessionProvider = Utils.getSessionProvider();
      managerUserHandler.addAdministrator(sessionProvider, NewsLetterUtil.getPortalName(), supperUser);
      sessionProvider.close();
    }
    return editPermission;
	}
	
	/**
	 * Instantiates a new uI newsletter manager portlet.
	 * 
	 * @throws Exception the exception
	 */
	public UINewsletterManagerPortlet() throws Exception {
	  try{
	    List<String> currentUsers = NewsLetterUtil.getAllGroupAndMembershipOfCurrentUser();
	    List<String> editPermission = getAllAdministrators();
      for(String str : currentUsers){
        if(editPermission.contains(str)){
          this.isAdmin = true;
          break;
        }
      }
	  }catch(Exception ex){
	    this.isAdmin = false;
	  }
	  UICategories categories = addChild(UICategories.class, null, null).setRendered(isRenderUICategories);
	  categories.setAdmin(isAdmin);
	  UISubscriptions subscriptions = addChild(UISubscriptions.class, null, null).setRendered(isRenderUICategories);
	  subscriptions.setAdmin(this.isAdmin);
		addChild(UINewsletterEntryManager.class, null, null).setRendered(isRenderUINewsLetters);
		addChild(UIPopupContainer.class, null, null);
	}
}
