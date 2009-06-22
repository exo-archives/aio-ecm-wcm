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
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;

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
        @EventConfig(listeners = UISubscriptions.AddEntryActionListener.class),
        @EventConfig(listeners = UISubscriptions.BackToCategoriesActionListener.class),
        @EventConfig(listeners = UISubscriptions.AddSubcriptionActionListener.class),
        @EventConfig(listeners = UISubscriptions.EditSubscriptionActionListener.class),
        @EventConfig(listeners = UISubscriptions.DeleteSubscriptionActionListener.class, confirm= "UISubscription.msg.confirmDeleteSubscription"),
        @EventConfig(listeners = UISubscriptions.DeleteCategoryActionListener.class, confirm= "UISubscription.msg.confirmDeleteCategory"),
        @EventConfig(listeners = UISubscriptions.OpenSubscriptionActionListener.class),
        @EventConfig(listeners = UISubscriptions.EditCategoryActionListener.class),
        @EventConfig(listeners = UISubscriptions.ManagerUsersActionListener.class)
    }
)
public class UISubscriptions extends UIForm {
  NewsletterSubscriptionHandler subscriptionHandler;
  NewsletterCategoryHandler categoryHandler;
  NewsletterCategoryConfig categoryConfig;
  NewsletterManageUserHandler userHandler = null;
  String portalName;

  public UISubscriptions()throws Exception{
    NewsletterManagerService newsletterManagerService = getApplicationComponent(NewsletterManagerService.class);
    subscriptionHandler = newsletterManagerService.getSubscriptionHandler();
    categoryHandler = newsletterManagerService.getCategoryHandler();
    userHandler = newsletterManagerService.getManageUserHandler();
    portalName = NewsLetterUtil.getPortalName();
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
  private int getNumberOfUser(String subscriptionName){
    return userHandler.getQuantityUserBySubscription(portalName, this.categoryConfig.getName(), subscriptionName);
  }

  @SuppressWarnings("unused")
  private List<NewsletterSubscriptionConfig> getListSubscription(){
    List<NewsletterSubscriptionConfig> listSubs = new ArrayList<NewsletterSubscriptionConfig>();
    try{
      listSubs = 
        subscriptionHandler.getSubscriptionsByCategory(NewsLetterUtil.getPortalName(), this.categoryConfig.getName());
      init(listSubs);
    }catch(Exception e){
      e.printStackTrace();
    }
    return listSubs;
  }

  @SuppressWarnings("unchecked")
  public String getChecked(){
    String subscriptionId = null;
    UIFormCheckBoxInput<Boolean> checkbox = null;
    for(UIComponent component : this.getChildren()){
      try{
        checkbox = (UIFormCheckBoxInput<Boolean>)component;
        if(checkbox.isChecked()){
          if(subscriptionId == null)subscriptionId = checkbox.getName(); 
          else return null;
        }
      }catch(Exception e){}
    }
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
      UIPopupWindow popupWindow = popupContainer.getChildById(UINewsletterConstant.CATEGORY_FORM_POPUP_WINDOW);
      if (popupWindow == null) {
        UICategoryForm categoryForm = popupContainer.createUIComponent(UICategoryForm.class, null, null);
        categoryForm.setCategoryInfo(subsriptions.categoryConfig);
        Utils.createPopupWindow(popupContainer, categoryForm, event.getRequestContext(), UINewsletterConstant.CATEGORY_FORM_POPUP_WINDOW, 450, 298);
      } else { 
        popupWindow.setShow(true);
      }
    }
  }

  static  public class DeleteCategoryActionListener extends EventListener<UISubscriptions> {
    public void execute(Event<UISubscriptions> event) throws Exception {
      UISubscriptions subsriptions = event.getSource();
      NewsletterManagerService newsletterManagerService = subsriptions.getApplicationComponent(NewsletterManagerService.class);
      NewsletterCategoryHandler categoryHandler = newsletterManagerService.getCategoryHandler();
      SessionProvider sessionProvider = NewsLetterUtil.getSesssionProvider();
      try{
        categoryHandler.delete(NewsLetterUtil.getPortalName(), subsriptions.categoryConfig.getName());
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
      UIPopupWindow popupWindow = popupContainer.getChildById(UINewsletterConstant.SUBSCRIPTION_FORM_POPUP_WINDOW);
      if (popupWindow == null) {
        UISubcriptionForm subcriptionForm = popupContainer.createUIComponent(UISubcriptionForm.class, null, null);
        UIFormSelectBox selectedCategoryName = subcriptionForm.getChildById("CategoryName");
        selectedCategoryName.setValue(subsriptions.categoryConfig.getName());
        selectedCategoryName.setDisabled(true);

        Utils.createPopupWindow(popupContainer, subcriptionForm, event.getRequestContext(), UINewsletterConstant.SUBSCRIPTION_FORM_POPUP_WINDOW, 450, 300);
      } else { 
        popupWindow.setShow(true);
      }
    }
  }

  static  public class EditSubscriptionActionListener extends EventListener<UISubscriptions> {
    public void execute(Event<UISubscriptions> event) throws Exception {
      UISubscriptions subsriptions = event.getSource();
      String subId = subsriptions.getChecked();
      if(subId == null){
        UIApplication uiApp = subsriptions.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UISubscription.msg.checkOnlyOneSubScriptionToEdit", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      UIPopupContainer popupContainer = subsriptions.getAncestorOfType(UINewsletterManagerPortlet.class).getChild(UIPopupContainer.class);
      UIPopupWindow popupWindow = popupContainer.getChildById(UINewsletterConstant.SUBSCRIPTION_FORM_POPUP_WINDOW);
      if (popupWindow == null) {
        UISubcriptionForm subcriptionForm = popupContainer.createUIComponent(UISubcriptionForm.class, null, null);
        NewsletterSubscriptionConfig subscriptionConfig = subsriptions.subscriptionHandler.getSubscriptionsByName(NewsLetterUtil.getPortalName(), subsriptions.categoryConfig.getName(), subId);
        subcriptionForm.setSubscriptionInfor(subscriptionConfig);
        Utils.createPopupWindow(popupContainer, subcriptionForm, event.getRequestContext(), UINewsletterConstant.SUBSCRIPTION_FORM_POPUP_WINDOW, 450, 280);
      } else { 
        popupWindow.setShow(true);
      }
    }
  }

  static  public class DeleteSubscriptionActionListener extends EventListener<UISubscriptions> {
    public void execute(Event<UISubscriptions> event) throws Exception {
      UISubscriptions subsriptions = event.getSource();
      boolean isChecked = false;
      UIFormCheckBoxInput<Boolean> checkbox = null;
      String portalName = NewsLetterUtil.getPortalName();
      for(UIComponent component : subsriptions.getChildren()){
        checkbox = (UIFormCheckBoxInput<Boolean>)component;
        if(checkbox.isChecked()){

          isChecked = true;

          NewsletterSubscriptionConfig subscriptionConfig = 
            subsriptions.subscriptionHandler.getSubscriptionsByName( portalName, subsriptions.categoryConfig.getName(), checkbox.getName());
          if (subscriptionConfig != null) {
            subsriptions.subscriptionHandler.delete( NewsLetterUtil.getPortalName(), subsriptions.categoryConfig.getName(),subscriptionConfig);
          } else {
            UIApplication uiApp = subsriptions.getAncestorOfType(UIApplication.class);
            uiApp.addMessage(new ApplicationMessage("UISubscription.msg.subscriptionNotfound", null, ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(subsriptions);
      if(isChecked == false){
        UIApplication uiApp = subsriptions.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UISubscription.msg.checkOnlyOneSubScriptionToDelete", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
    }
  }

  static  public class OpenSubscriptionActionListener extends EventListener<UISubscriptions> {
    public void execute(Event<UISubscriptions> event) throws Exception {
      UISubscriptions uiSubscription = event.getSource();
      String subId = uiSubscription.getChecked();
      if(subId == null){
        UIApplication uiApp = uiSubscription.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UISubscription.msg.checkOnlyOneSubScriptionToOpen", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }

      String portalName = NewsLetterUtil.getPortalName();

      UINewsletterManagerPortlet newsletterManagerPortlet = uiSubscription.getAncestorOfType(UINewsletterManagerPortlet.class);
      UINewsletterEntryManager newsletterManager = newsletterManagerPortlet.getChild(UINewsletterEntryManager.class);
      
      newsletterManager.setRendered(true);
      
      newsletterManager.setCategoryConfig(
                        uiSubscription.categoryHandler.getCategoryByName(
                                                                         portalName,
                                                                         uiSubscription.categoryConfig.getName()));
      
      newsletterManager.setSubscriptionConfig(
                        uiSubscription.subscriptionHandler.getSubscriptionsByName(portalName,
                                                                                  uiSubscription.categoryConfig.getName(),
                                                                                  subId));
      newsletterManagerPortlet.getChild(UICategories.class).setRendered(false);
      newsletterManagerPortlet.getChild(UISubscriptions.class).setRendered(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(newsletterManagerPortlet);
    }
  }
  
  static  public class ManagerUsersActionListener extends EventListener<UISubscriptions> {
    public void execute(Event<UISubscriptions> event) throws Exception {
      UISubscriptions uiSubscription = event.getSource();
      UIPopupContainer popupContainer = uiSubscription.getAncestorOfType(UINewsletterManagerPortlet.class).getChild(UIPopupContainer.class);
      UIPopupWindow popupWindow = popupContainer.getChildById(UINewsletterConstant.MANAGER_USERS_POPUP_WINDOW);
      if (popupWindow == null) {
        UIManagerUsers managerUsers = popupContainer.createUIComponent(UIManagerUsers.class, null, null);
        managerUsers.setInfor(uiSubscription.categoryConfig.getName(), null);
        Utils.createPopupWindow(popupContainer, managerUsers, event.getRequestContext(), 
                                UINewsletterConstant.MANAGER_USERS_POPUP_WINDOW, 550, 350);
      } else { 
        popupWindow.setShow(true);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }
  
  public static class AddEntryActionListener extends EventListener<UISubscriptions> {
    public void execute(Event<UISubscriptions> event) throws Exception {
      UISubscriptions uiSubscriptions = event.getSource();
      UIPopupContainer popupContainer = uiSubscriptions.getAncestorOfType(UINewsletterManagerPortlet.class).getChild(UIPopupContainer.class);
      UIPopupWindow popupWindow = popupContainer.getChildById(UINewsletterConstant.ENTRY_FORM_POPUP_WINDOW);
      if (popupWindow == null) {
        UINewsletterEntryContainer entryContainer = popupContainer.createUIComponent(UINewsletterEntryContainer.class, null, null);
        UINewsletterEntryDialogSelector newsletterEntryDialogSelector = entryContainer.getChild(UINewsletterEntryDialogSelector.class);
        UIFormSelectBox categorySelectBox = newsletterEntryDialogSelector.getChildById(UINewsletterConstant.ENTRY_CATEGORY_SELECTBOX);
        categorySelectBox.setValue(uiSubscriptions.categoryConfig.getName());
        categorySelectBox.setDisabled(true);
        Utils.createPopupWindow(popupContainer, entryContainer, event.getRequestContext(), UINewsletterConstant.ENTRY_FORM_POPUP_WINDOW, 800, 600);
      } else { 
        popupWindow.setShow(true);
      }
    }
  }
}