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
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          ha.mai@exoplatform.com
 * Jun 5, 2009  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/newsletter/NewsletterManager/UISubscriptions.gtmpl",
    events = {
        @EventConfig(listeners = UISubscriptions.BackToCategoriesActionListener.class),
        @EventConfig(listeners = UISubscriptions.AddSubcriptionActionListener.class),
        @EventConfig(listeners = UISubscriptions.EditSubscriptionActionListener.class),
        @EventConfig(listeners = UISubscriptions.DeleteSubscriptionActionListener.class, confirm= "UISubscription.msg.confirmDeleteSubscription"),
        @EventConfig(listeners = UISubscriptions.DeleteCategoryActionListener.class, confirm= "UISubscription.msg.confirmDeleteCategory"),
        @EventConfig(listeners = UISubscriptions.EditCategoryActionListener.class)
    }
)
public class UISubscriptions extends UIForm {
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
  
  public String getChecked(){
    String subscriptionId = null;
    UIFormCheckBoxInput<Boolean> checkbox = null;
    for(UIComponent component : this.getChildren()){
      try{
        checkbox = (UIFormCheckBoxInput<Boolean>)component;
        System.out.println("~~~~~~~~~~>id:" + checkbox.getId());
        if(checkbox.isChecked()){
          if(subscriptionId == null)subscriptionId = checkbox.getName(); 
          else return null;
        }
      }catch(Exception e){}
    }
    System.out.println("\n\n\n\n------------------>subscription name:" + subscriptionId);
    return subscriptionId;
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
  
  static  public class AddSubcriptionActionListener extends EventListener<UISubscriptions> {
    public void execute(Event<UISubscriptions> event) throws Exception {
      UISubscriptions subsriptions = event.getSource();
      UIPopupContainer popupContainer = subsriptions.getAncestorOfType(UINewsletterManagerPortlet.class).getChild(UIPopupContainer.class);
      UISubcriptionForm subcriptionForm = popupContainer.createUIComponent(UISubcriptionForm.class, null, null);
      popupContainer.setRendered(true);
      popupContainer.activate(subcriptionForm, 450, 300);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }
  
  static  public class EditSubscriptionActionListener extends EventListener<UISubscriptions> {
    public void execute(Event<UISubscriptions> event) throws Exception {
      System.out.println("\n\n\n\n-------------->run edit subscriptioin");
      UISubscriptions subsriptions = event.getSource();
      String subId = subsriptions.getChecked();
      if(subId == null){
        UIApplication uiApp = subsriptions.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UISubscription.msg.checkOnlyOneSubScriptionToEdit", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      UIPopupContainer popupContainer = subsriptions.getAncestorOfType(UINewsletterManagerPortlet.class).getChild(UIPopupContainer.class);
      UISubcriptionForm subcriptionForm = popupContainer.createUIComponent(UISubcriptionForm.class, null, null);
      SessionProvider sessionProvider = NewsLetterUtil.getSesssionProvider();
      NewsletterSubscriptionConfig subscriptionConfig =
        subsriptions.subscriptionHandler.getSubscriptionsByName(sessionProvider, NewsLetterUtil.getPortalName(), subsriptions.categoryConfig.getName(), subId);
      subcriptionForm.setSubscriptionInfor(subscriptionConfig);
      popupContainer.setRendered(true);
      popupContainer.activate(subcriptionForm, 450, 300);
      sessionProvider.close();
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }
  
  static  public class DeleteSubscriptionActionListener extends EventListener<UISubscriptions> {
    public void execute(Event<UISubscriptions> event) throws Exception {
      UISubscriptions subsriptions = event.getSource();
      String subId = subsriptions.getChecked();
      if(subId == null){
        UIApplication uiApp = subsriptions.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UISubscription.msg.checkOnlyOneSubScriptionToDelete", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      SessionProvider sessionProvider = NewsLetterUtil.getSesssionProvider();
      String portalName = NewsLetterUtil.getPortalName();
      NewsletterSubscriptionConfig subscriptionConfig = 
        subsriptions.subscriptionHandler.getSubscriptionsByName(sessionProvider, portalName, subsriptions.categoryConfig.getName(), subId);
      subsriptions.subscriptionHandler.delete(sessionProvider, NewsLetterUtil.getPortalName(), subsriptions.categoryConfig.getName(),subscriptionConfig);
      event.getRequestContext().addUIComponentToUpdateByAjax(subsriptions);
    }
  }
}
