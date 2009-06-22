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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterSubscriptionConfig;
import org.exoplatform.services.wcm.newsletter.config.NewsletterManagerConfig;
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
 *          ngoc.tran@exoplatform.com
 * Jun 9, 2009  
 */
@ComponentConfig(
                 lifecycle = UIFormLifecycle.class,
                 template = "app:/groovy/webui/newsletter/NewsletterManager/UINewsletterEntryManager.gtmpl",
                 events = {
                     @EventConfig(listeners = UINewsletterEntryManager.AddEntryActionListener.class),
                     @EventConfig(listeners = UINewsletterEntryManager.BackToSubcriptionsActionListener.class),
                     @EventConfig(listeners = UINewsletterEntryManager.BackToCategoriesActionListener.class),
                     @EventConfig(listeners = UINewsletterEntryManager.OpenNewsletterActionListener.class)
                 }
             )
public class UINewsletterEntryManager extends UIForm {

  private UIFormCheckBoxInput<Boolean>  checkBoxInput            = null;
  
  private NewsletterSubscriptionConfig subscriptionConfig;
  
  private NewsletterCategoryConfig categoryConfig;
  
  private List<NewsletterManagerConfig> listNewsletterConfig = new ArrayList<NewsletterManagerConfig>();

  public UINewsletterEntryManager () {
      
      NewsletterManagerConfig newletter = null;
      
      SimpleDateFormat simpleDate = new SimpleDateFormat("dd/MM/yyyy - hh:mm:ss");
      Date date = new Date();
      for (int i = 1; i< 10; i++) {
        
        newletter = new NewsletterManagerConfig();
        
        newletter.setNewsletterName("Letter" + String.valueOf(i));
        newletter.setNewsletterTitle("Letter" + String.valueOf(i));
        newletter.setStatus("Awaiting");
        newletter.setNewsletterSentDate(simpleDate.format(date));

        listNewsletterConfig.add(newletter);
        checkBoxInput = new UIFormCheckBoxInput<Boolean>(newletter.getNewsletterName(), newletter.getNewsletterName(), false);
        this.addChild(checkBoxInput);
      }
  }

  public String getChecked(){
    String newsletterId = null;
    UIFormCheckBoxInput<Boolean> checkbox = null;
    for(UIComponent component : this.getChildren()){
      try{
        checkbox = (UIFormCheckBoxInput<Boolean>)component;
        System.out.println("~~~~~~~~~~>id:" + checkbox.getId());
        if(checkbox.isChecked()){
          if(newsletterId == null)newsletterId = checkbox.getName();
          else return null;
        }
      }catch(Exception e){}
    }
    return newsletterId;
  }

  public NewsletterSubscriptionConfig getSubscriptionConfig() {
    return subscriptionConfig;
  }

  public void setSubscriptionConfig(NewsletterSubscriptionConfig subscriptionConfig) {
    this.subscriptionConfig = subscriptionConfig;
  }
  
  public NewsletterCategoryConfig getCategoryConfig() {
    return categoryConfig;
  }

  public void setCategoryConfig(NewsletterCategoryConfig categoryConfig) {
    this.categoryConfig = categoryConfig;
  }
  
  static  public class BackToSubcriptionsActionListener extends EventListener<UINewsletterEntryManager> {
    public void execute(Event<UINewsletterEntryManager> event) throws Exception {
      UINewsletterEntryManager newsletter = event.getSource();
      UINewsletterManagerPortlet newsletterManagerPortlet = newsletter.getAncestorOfType(UINewsletterManagerPortlet.class);
      UISubscriptions subcription = newsletterManagerPortlet.getChild(UISubscriptions.class);
      subcription.setRendered(true);
      newsletter.setRendered(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(newsletterManagerPortlet);
    }
  }

  static  public class BackToCategoriesActionListener extends EventListener<UINewsletterEntryManager> {
    public void execute(Event<UINewsletterEntryManager> event) throws Exception {
      UINewsletterEntryManager newsletter = event.getSource();
      UINewsletterManagerPortlet newsletterManagerPortlet = newsletter.getAncestorOfType(UINewsletterManagerPortlet.class);
      UICategories categories = newsletterManagerPortlet.getChild(UICategories.class);
      UISubscriptions subcription = newsletterManagerPortlet.getChild(UISubscriptions.class);
      categories.setRendered(true);
      subcription.setRendered(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(newsletterManagerPortlet);
    }
  }

  static  public class OpenNewsletterActionListener extends EventListener<UINewsletterEntryManager> {
    public void execute(Event<UINewsletterEntryManager> event) throws Exception {
      UINewsletterEntryManager newsletter = event.getSource();

      String subId = newsletter.getChecked();
      if(subId == null){
        UIApplication uiApp = newsletter.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UISubscription.msg.checkOnlyOneSubScriptionToOpen", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }

      UIPopupContainer popupContainer = newsletter.getAncestorOfType(UINewsletterManagerPortlet.class).getChild(UIPopupContainer.class);
      UINewsletterManagerPopup newsletterManagerPopup = popupContainer.createUIComponent(UINewsletterManagerPopup.class, null, null);
      popupContainer.setRendered(true);
      popupContainer.activate(newsletterManagerPopup, 800, 600);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }
  
  public static class AddEntryActionListener extends EventListener<UINewsletterEntryManager> {
    public void execute(Event<UINewsletterEntryManager> event) throws Exception {
      UINewsletterEntryManager uiNewsletterEntryManager = event.getSource();
      UIPopupContainer popupContainer = uiNewsletterEntryManager.getAncestorOfType(UINewsletterManagerPortlet.class).getChild(UIPopupContainer.class);
      UIPopupWindow popupWindow = popupContainer.getChildById(UINewsletterConstant.ENTRY_FORM_POPUP_WINDOW);
      if (popupWindow == null) {
        UINewsletterEntryContainer entryContainer = popupContainer.createUIComponent(UINewsletterEntryContainer.class, null, null);
        UINewsletterEntryDialogSelector newsletterEntryDialogSelector = entryContainer.getChild(UINewsletterEntryDialogSelector.class);
        UIFormSelectBox categorySelectBox = newsletterEntryDialogSelector.getChildById(UINewsletterConstant.ENTRY_CATEGORY_SELECTBOX);
        categorySelectBox.setValue(uiNewsletterEntryManager.categoryConfig.getName());
        categorySelectBox.setDisabled(true);
        UIFormSelectBox subscriptionSelectBox = newsletterEntryDialogSelector.getChildById(UINewsletterConstant.ENTRY_SUBSCRIPTION_SELECTBOX);
        subscriptionSelectBox.setValue(uiNewsletterEntryManager.subscriptionConfig.getName());
        subscriptionSelectBox.setDisabled(true);
        Utils.createPopupWindow(popupContainer, entryContainer, event.getRequestContext(), UINewsletterConstant.ENTRY_FORM_POPUP_WINDOW, 800, 600);
      } else { 
        popupWindow.setShow(true);
      }
    }
  }
}
