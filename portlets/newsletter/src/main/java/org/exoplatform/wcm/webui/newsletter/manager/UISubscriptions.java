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
import org.exoplatform.services.wcm.newsletter.handler.NewsletterSubscriptionHandler;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          ha.mai@exoplatform.com
 * Jun 5, 2009  
 */

@ComponentConfig(
    template = "app:/groovy/webui/newsletter/NewsletterManager/UISubscriptions.gtmpl",
    events = {
        @EventConfig(listeners = UISubscriptions.BackToCategoriesActionListener.class),
        @EventConfig(listeners = UISubscriptions.DeleteCategoryActionListener.class, confirm= "UISubscription.msg.confirmDeleteCategory"),
        @EventConfig(listeners = UISubscriptions.EditCategoryActionListener.class)
    }
)
public class UISubscriptions extends UIContainer {
  NewsletterSubscriptionHandler subscriptionHandler;
  NewsletterCategoryConfig categoryConfig;
  
  public UISubscriptions()throws Exception{
    NewsletterManagerService newsletterManagerService = getApplicationComponent(NewsletterManagerService.class);
    subscriptionHandler = newsletterManagerService.getSubscriptionHandler();
  }
  
  public void setCategory(NewsletterCategoryConfig categoryConfig){
    this.categoryConfig = categoryConfig;
  }
  
  private void init(List<NewsletterSubscriptionConfig> listSubScritpions){
    this.getChildren().clear();
    UIFormCheckBoxInput<Boolean> checkBoxInput = null;
    for(NewsletterSubscriptionConfig subscription : listSubScritpions){
      checkBoxInput = new UIFormCheckBoxInput<Boolean>(subscription.getName(), subscription.getName(), false);
      this.addChild(checkBoxInput);
    }
  }
  
  @SuppressWarnings("unused")
  private List<NewsletterSubscriptionConfig> getListSubscription(){
    List<NewsletterSubscriptionConfig> listSubs = new ArrayList<NewsletterSubscriptionConfig>();
    SessionProvider sessionProvider = NewsLetterUtil.getSesssionProvider();
    try{
      listSubs = 
        subscriptionHandler.getSubscriptionsByCategory(sessionProvider, NewsLetterUtil.getPortalName(), this.categoryConfig.getName());
      init(listSubs);
    }catch(Exception e){
      e.printStackTrace();
    }
    sessionProvider.close();
    return listSubs;
  }
  
  static  public class BackToCategoriesActionListener extends EventListener<UISubscriptions> {
    public void execute(Event<UISubscriptions> event) throws Exception {
      UISubscriptions subsriptions = event.getSource();
      UINewsletterManagerPortlet newsletterManagerPortlet = subsriptions.getAncestorOfType(UINewsletterManagerPortlet.class);
      UICategories categories = newsletterManagerPortlet.getChild(UICategories.class);
      categories.setRendered(true);
      subsriptions.setRendered(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(newsletterManagerPortlet);
    }
  }
  
  static  public class EditCategoryActionListener extends EventListener<UISubscriptions> {
    public void execute(Event<UISubscriptions> event) throws Exception {
      UISubscriptions subsriptions = event.getSource();
      UIPopupContainer popupContainer = subsriptions.getAncestorOfType(UINewsletterManagerPortlet.class).getChild(UIPopupContainer.class);
      UICategoryForm categoryForm = popupContainer.createUIComponent(UICategoryForm.class, null, null);
      categoryForm.setCategoryInfo(subsriptions.categoryConfig);
      popupContainer.setRendered(true);
      popupContainer.activate(categoryForm, 400, 300);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }
  
  static  public class DeleteCategoryActionListener extends EventListener<UISubscriptions> {
    public void execute(Event<UISubscriptions> event) throws Exception {
      UISubscriptions subsriptions = event.getSource();
      NewsletterManagerService newsletterManagerService = subsriptions.getApplicationComponent(NewsletterManagerService.class);
      NewsletterCategoryHandler categoryHandler = newsletterManagerService.getCategoryHandler();
      SessionProvider sessionProvider = NewsLetterUtil.getSesssionProvider();
      try{
        categoryHandler.delete(NewsLetterUtil.getPortalName(), subsriptions.categoryConfig.getName(), sessionProvider);
      }catch(Exception e){}
      sessionProvider.close();
      UINewsletterManagerPortlet newsletterManagerPortlet = subsriptions.getAncestorOfType(UINewsletterManagerPortlet.class);
      UICategories categories = newsletterManagerPortlet.getChild(UICategories.class);
      categories.setRendered(true);
      subsriptions.setRendered(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(newsletterManagerPortlet);
    }
  }
}
