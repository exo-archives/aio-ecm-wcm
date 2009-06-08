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
import org.exoplatform.services.wcm.newsletter.handler.NewsletterSubscriptionHandler;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          ha.mai@exoplatform.com
 * Jun 5, 2009  
 */

@ComponentConfig(
    template = "app:/groovy/webui/newsletter/NewsletterManager/UISubscriptions.gtmpl",
    events = {
        @EventConfig(listeners = UISubsriptions.BackToCategoriesActionListener.class)
    }
)
public class UISubsriptions extends UIContainer {
  NewsletterSubscriptionHandler subscriptionHandler;
  NewsletterCategoryConfig categoryConfig;
  
  public UISubsriptions()throws Exception{
    NewsletterManagerService newsletterManagerService = getApplicationComponent(NewsletterManagerService.class);
    subscriptionHandler = newsletterManagerService.getSubscriptionHandler();
  }
  
  public void setCategory(NewsletterCategoryConfig categoryConfig){
    this.categoryConfig = categoryConfig;
    System.out.println("\n\n\n\n--------------->category name:" + this.categoryConfig.getName());
  }
  
  @SuppressWarnings("unused")
  private List<NewsletterSubscriptionConfig> getListSubscription(){
    try{
      return subscriptionHandler.getSubscriptionsByCategory(NewsLetterUtil.getSesssionProvider(), NewsLetterUtil.getPortalName(), this.categoryConfig.getName());
    }catch(Exception e){
      e.printStackTrace();
      return new ArrayList<NewsletterSubscriptionConfig>();
    }
  }
  
  static  public class BackToCategoriesActionListener extends EventListener<UISubsriptions> {
    public void execute(Event<UISubsriptions> event) throws Exception {
      UISubsriptions subsriptions = event.getSource();
      UINewsletterManagerPortlet newsletterManagerPortlet = subsriptions.getAncestorOfType(UINewsletterManagerPortlet.class);
      UICategories categories = newsletterManagerPortlet.getChild(UICategories.class);
      categories.setRendered(true);
      subsriptions.setRendered(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(newsletterManagerPortlet);
    }
  }
}
