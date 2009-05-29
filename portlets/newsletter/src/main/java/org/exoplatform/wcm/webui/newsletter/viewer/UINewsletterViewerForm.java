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
package org.exoplatform.wcm.webui.newsletter.viewer;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterSubscriptionConfig;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          ngoc.tran@exoplatform.com
 * May 25, 2009  
 */
@ComponentConfig(
   lifecycle = UIFormLifecycle.class,
   template = "app:/groovy/webui/newsletter/NewsletterViewer/UINewsletterListViewer.gtmpl",
   events = { 
       @EventConfig(listeners = UINewsletterViewerForm.UpdateActionListener.class),
       @EventConfig(listeners = UINewsletterViewerForm.CancelActionListener.class)
     }
)
 public class UINewsletterViewerForm extends UIForm {
  
  UIFormCheckBoxInput<Boolean> checkBoxInput = null;
  UIFormStringInput newInput = null;
  static List<NewsletterCategoryConfig> listNewsletterCategories = null;
  static List<NewsletterSubscriptionConfig> listNewsletterSubcription = null;

  public UINewsletterViewerForm () {
  
    listNewsletterCategories = new ArrayList<NewsletterCategoryConfig>();
    listNewsletterSubcription = new ArrayList<NewsletterSubscriptionConfig>();
  
    //NewsletterManagerService newsletterService = getApplicationComponent(NewsletterManagerService.class);
    NewsletterSubscriptionConfig newslettrtSubcription = null;
    NewsletterCategoryConfig newsletterCategory = null;

    String nameInput = "inputID";
    String ckRemember = "ckRemember";
    newInput = new UIFormStringInput(nameInput, null);
     
    checkBoxInput = new UIFormCheckBoxInput<Boolean>(ckRemember, ckRemember, false);
     
    this.addChild(checkBoxInput);
    this.addChild(newInput);
  
    try {
      for (int i = 1; i < 4; i++) {
        
        newslettrtSubcription = new NewsletterSubscriptionConfig();
         
        newslettrtSubcription.setName("Subcription" + " " + String.valueOf(i));
        newslettrtSubcription.setDescription("Decription" + " " + String.valueOf(i));
  
        listNewsletterSubcription.add(newslettrtSubcription);
         
        checkBoxInput = new UIFormCheckBoxInput<Boolean>(newslettrtSubcription.getName(), newslettrtSubcription.getName(), false);
        this.addChild(checkBoxInput);
      }
  
      // listNewsletterCategories = newsletterService.getCategoryHandler().
      for (int i = 1; i < 3; i++) {
         
        newsletterCategory = new NewsletterCategoryConfig();
         
        newsletterCategory.setName("Category" + " " + String.valueOf(i));
        newsletterCategory.setDescription("Some text about category");
        newsletterCategory.setModerator("Moderator");
        newsletterCategory.setSubscriptions(listNewsletterSubcription);
         
        listNewsletterCategories.add(newsletterCategory);
      }
    } catch (Exception e) {
      System.out.println("\n\n\n\n------------------------->error");
      e.printStackTrace();
    }
    
    this.setActions(new String[] {"Update", "Cancel"});
  }
public static class UpdateActionListener extends EventListener<UINewsletterViewerForm> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    @SuppressWarnings("unchecked")
    public void execute(Event<UINewsletterViewerForm> event) throws Exception {
      UINewsletterViewerForm newsletterForm = event.getSource();
      System.out.println("\n\n\n\n-----------------> run action ok");
      // open a popup message:
      UIApplication uiApp = newsletterForm.getAncestorOfType(UIApplication.class);
      uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.messageNull", null, ApplicationMessage.WARNING));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      // reset this form
      event.getRequestContext().addUIComponentToUpdateByAjax(newsletterForm);
      List<UIFormCheckBoxInput> listCheckboxInput = new ArrayList<UIFormCheckBoxInput>();
      for (int i = 0; i < listNewsletterCategories.size(); i++) {
        
        for (int j = 0; j < listNewsletterSubcription.size(); j++) {
          
          listCheckboxInput.add(newsletterForm.getUIFormCheckBoxInput(listNewsletterSubcription.get(j).getName()));
        }
      }
      
      //String [] value = new String[listCheckboxInput.size()];
      List value = new ArrayList();

      for (int i = 0; i < listCheckboxInput.size(); i++) {
        
        if (listCheckboxInput.get(i).isChecked()) {

          value.add(listCheckboxInput.get(i).getName());
        }
      }
      System.out.println("--------------------------------->> Get value of checkbox is checked");
      if (value.size() == 0) {
        
        System.out.println("--------------------------------->> Count = 0");
        System.out.println("No value is checked");
      } else if (value.size() == 1){
        System.out.println("--------------------------------->> Count = 1");
        System.out.println("Value is:" + value.get(0).toString());
      } else {
        System.out.println("--------------------------------->> Count > 1");
        System.out.println("--------------------------------->> Count = " + value.size());
        System.out.println("Value of checkbox is checked: ");
        for (int i = 1; i <= value.size(); i++) {
            
            System.out.print("," + value.get(i).toString());
        }
      }
      
      String userName = newsletterForm.getUIStringInput("inputID").getValue();
      System.out.println("---------------------------------");
      System.out.println("Value of UserName: " + userName);
    }
  }
  
  public static class CancelActionListener extends EventListener<UINewsletterViewerForm> {
      
      /* (non-Javadoc)
       * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
       */
      @SuppressWarnings("unchecked")
      public void execute(Event<UINewsletterViewerForm> event) throws Exception {
        UINewsletterViewerForm newsletterForm = event.getSource();
        System.out.println("\n\n\n\n-----------------> Cancel action test");

        // reset this form
        event.getRequestContext().addUIComponentToUpdateByAjax(newsletterForm);
      }
    }  
 }
