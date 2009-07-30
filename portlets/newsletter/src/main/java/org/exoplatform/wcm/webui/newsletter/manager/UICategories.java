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

import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.NewsletterSubscriptionConfig;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterCategoryHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterManageUserHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterSubscriptionHandler;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.newsletter.UINewsletterConstant;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(
		template = "app:/groovy/webui/newsletter/NewsletterManager/UICategories.gtmpl",
		events = {
		    @EventConfig(listeners = UICategories.AddEntryActionListener.class),
				@EventConfig(listeners = UICategories.AddCategoryActionListener.class),
				@EventConfig(listeners = UICategories.OpenCategoryActionListener.class),
        @EventConfig(listeners = UICategories.AddSubcriptionActionListener.class),
        @EventConfig(listeners = UICategories.ManagerUsersActionListener.class),
        @EventConfig(listeners = UICategories.SelectSubscriptionActionListener.class)
		}
)
public class UICategories extends UIContainer {
	NewsletterCategoryHandler categoryHandler = null;
	NewsletterSubscriptionHandler subscriptionHandler = null;
	NewsletterManageUserHandler userHandler = null;
	String portalName;
	
	public UICategories()throws Exception{
		NewsletterManagerService newsletterManagerService = getApplicationComponent(NewsletterManagerService.class);
		categoryHandler = newsletterManagerService.getCategoryHandler();
		subscriptionHandler = newsletterManagerService.getSubscriptionHandler();
		userHandler = newsletterManagerService.getManageUserHandler();
		portalName = NewsLetterUtil.getPortalName();
	}
	
	@SuppressWarnings("unused")
  private long getNumberOfWaitingNewsletter(String categoryName, String subscriptionName){
	  try{
	    return subscriptionHandler.getNumberOfNewslettersWaiting(portalName, categoryName, subscriptionName, Utils.getSessionProvider(this));
	  }catch(Exception ex){
	    ex.printStackTrace();
	    return 0;
	  }
	}
	
	@SuppressWarnings("unused")
  private int getNumberOfUser(String categoryName, String subscriptionName){
	  return userHandler.getQuantityUserBySubscription(portalName, categoryName, subscriptionName);
	}
	
	@SuppressWarnings("unused")
	private List<NewsletterCategoryConfig> getListCategories(){
	  List<NewsletterCategoryConfig> listCategories = new ArrayList<NewsletterCategoryConfig>();
		try{
			listCategories = categoryHandler.getListCategories(NewsLetterUtil.getPortalName(), Utils.getSessionProvider(this));
		}catch(Exception e){
			e.printStackTrace();
		}
		return listCategories;
	}
	
	@SuppressWarnings("unused")
  private List<NewsletterSubscriptionConfig> getListSubscription(String categoryName){
	  List<NewsletterSubscriptionConfig> listSubscription = new ArrayList<NewsletterSubscriptionConfig>();
    try{
      listSubscription = subscriptionHandler.getSubscriptionsByCategory(NewsLetterUtil.getPortalName(), categoryName, Utils.getSessionProvider(this));
    }catch(Exception e){
      e.printStackTrace();
    }
    return listSubscription;
  }
	
	static  public class AddCategoryActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource();
      UICategoryForm categoryForm = uiCategories.createUIComponent(UICategoryForm.class, null, null);
      Utils.createPopupWindow(uiCategories, categoryForm, UINewsletterConstant.CATEGORY_FORM_POPUP_WINDOW, 450, 298);
		}
	}
  
  static  public class AddSubcriptionActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiCategories = event.getSource();
      UISubcriptionForm subcriptionForm = uiCategories.createUIComponent(UISubcriptionForm.class, null, null);
      Utils.createPopupWindow(uiCategories, subcriptionForm, UINewsletterConstant.SUBSCRIPTION_FORM_POPUP_WINDOW, 450, 300);
    }
  }
	
	static  public class OpenCategoryActionListener extends EventListener<UICategories> {
	  public void execute(Event<UICategories> event) throws Exception {
	    UICategories uiCategories = event.getSource();
	    String categoryName = event.getRequestContext().getRequestParameter(OBJECTID);
	    UINewsletterManagerPortlet newsletterManagerPortlet = uiCategories.getAncestorOfType(UINewsletterManagerPortlet.class);
	    UISubscriptions subsriptions = newsletterManagerPortlet.getChild(UISubscriptions.class);
	    subsriptions.setRendered(true);
	    subsriptions.setCategory(uiCategories.categoryHandler.getCategoryByName(NewsLetterUtil.getPortalName(), categoryName, Utils.getSessionProvider(uiCategories)));
	    newsletterManagerPortlet.getChild(UICategories.class).setRendered(false);
	    event.getRequestContext().addUIComponentToUpdateByAjax(newsletterManagerPortlet);
	  }
	}
	
	static  public class ManagerUsersActionListener extends EventListener<UICategories> {
	  public void execute(Event<UICategories> event) throws Exception {
	    UICategories uiCategories = event.getSource();
      UIManagerUsers managerUsers = uiCategories.createUIComponent(UIManagerUsers.class, null, null);
      managerUsers.setInfor(null, null);
      Utils.createPopupWindow(uiCategories, managerUsers, UINewsletterConstant.MANAGER_USERS_POPUP_WINDOW, 600, 350);
	  }
	}
	
	public static class AddEntryActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiCategories = event.getSource();
      UINewsletterEntryContainer entryContainer = uiCategories.createUIComponent(UINewsletterEntryContainer.class, null, null);
      Utils.createPopupWindow(uiCategories, entryContainer, UINewsletterConstant.ENTRY_FORM_POPUP_WINDOW, 800, 600);
      entryContainer.setCategoryConfig(null);
    }
  }
	
	public static class SelectSubscriptionActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiCategory = event.getSource();
      String categoryAndSubscription = event.getRequestContext().getRequestParameter(OBJECTID);
      UINewsletterManagerPortlet newsletterManagerPortlet = uiCategory.getAncestorOfType(UINewsletterManagerPortlet.class);
      UINewsletterEntryManager newsletterManager = newsletterManagerPortlet.getChild(UINewsletterEntryManager.class);
      newsletterManager.setRendered(true);
      
      String categoryName = categoryAndSubscription.split("/")[0];
      String subscriptionName = categoryAndSubscription.split("/")[1];
      SessionProvider sessionProvider = Utils.getSessionProvider(uiCategory);
      newsletterManager.setCategoryConfig(
                        uiCategory.categoryHandler.getCategoryByName(
                                                                     uiCategory.portalName,
                                                                     categoryName, sessionProvider));
      newsletterManager.setSubscriptionConfig(
                        uiCategory.subscriptionHandler.getSubscriptionsByName(uiCategory.portalName,
                                                                              categoryName,
                                                                              subscriptionName, sessionProvider));
      newsletterManager.init();
      newsletterManagerPortlet.getChild(UICategories.class).setRendered(false);
      newsletterManagerPortlet.getChild(UISubscriptions.class).setRendered(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(newsletterManagerPortlet);
    }
  }
}
