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

import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.NewsletterSubscriptionConfig;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterCategoryHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterSubscriptionHandler;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(
		template = "app:/groovy/webui/newsletter/NewsletterManager/UICategories.gtmpl",
		events = {
				@EventConfig(listeners = UICategories.AddCategoryActionListener.class),
        @EventConfig(listeners = UICategories.AddSubcriptionActionListener.class),
				@EventConfig(listeners = UICategories.OpenCateogryActionListener.class)
		}
)
public class UICategories extends UIContainer {
	NewsletterCategoryHandler categoryHandler = null;
	NewsletterSubscriptionHandler subscriptionHandler = null;
	
	public UICategories()throws Exception{
		NewsletterManagerService newsletterManagerService = getApplicationComponent(NewsletterManagerService.class);
		categoryHandler = newsletterManagerService.getCategoryHandler();
		subscriptionHandler = newsletterManagerService.getSubscriptionHandler();
	}
	
	@SuppressWarnings("unused")
	private List<NewsletterCategoryConfig> getListCategories(){
		try{
			return categoryHandler.getListCategories(NewsLetterUtil.getPortalName(), NewsLetterUtil.getSesssionProvider());
		}catch(Exception e){
			e.printStackTrace();
			return new ArrayList<NewsletterCategoryConfig>();
		}
	}
	
	@SuppressWarnings("unused")
  private List<NewsletterSubscriptionConfig> getListSubscription(String categoryName){
    try{
      return subscriptionHandler.getSubscriptionsByCategory(NewsLetterUtil.getSesssionProvider(), NewsLetterUtil.getPortalName(), categoryName);
    }catch(Exception e){
      e.printStackTrace();
      return new ArrayList<NewsletterSubscriptionConfig>();
    }
  }
	
	static  public class AddCategoryActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource();
			UIPopupContainer popupContainer = uiCategories.getAncestorOfType(UINewsletterManagerPortlet.class).getChild(UIPopupContainer.class);
			UICategoryForm categoryForm = popupContainer.createUIComponent(UICategoryForm.class, null, null);
			popupContainer.setRendered(true);
			popupContainer.activate(categoryForm, 400, 300);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
		}
	}
  
  static  public class AddSubcriptionActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiCategories = event.getSource();
      UIPopupContainer popupContainer = uiCategories.getAncestorOfType(UINewsletterManagerPortlet.class).getChild(UIPopupContainer.class);
      UISubcriptionForm subcriptionForm = popupContainer.createUIComponent(UISubcriptionForm.class, null, null);
      popupContainer.setRendered(true);
      popupContainer.activate(subcriptionForm, 450, 300);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }
	
	static  public class OpenCateogryActionListener extends EventListener<UICategories> {
	  public void execute(Event<UICategories> event) throws Exception {
	    UICategories uiCategories = event.getSource();
	    String categoryName = event.getRequestContext().getRequestParameter(OBJECTID);
	    UINewsletterManagerPortlet newsletterManagerPortlet = uiCategories.getAncestorOfType(UINewsletterManagerPortlet.class);
	    UISubsriptions subsriptions = newsletterManagerPortlet.getChild(UISubsriptions.class);
	    subsriptions.setRendered(true);
	    subsriptions.setCategory(uiCategories.categoryHandler.getCategoryByName(NewsLetterUtil.getPortalName(), categoryName, NewsLetterUtil.getSesssionProvider()));
	    newsletterManagerPortlet.getChild(UICategories.class).setRendered(false);
	    event.getRequestContext().addUIComponentToUpdateByAjax(newsletterManagerPortlet);
	  }
	}
}
