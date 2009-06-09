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
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          ngoc.tran@exoplatform.com
 * Jun 4, 2009  
 */
@ComponentConfig(
                lifecycle = UIFormLifecycle.class ,
                template = "app:/groovy/webui/newsletter/NewsletterManager/UISubcriptionForm.gtmpl",
                events = {
                    @EventConfig(listeners = UISubcriptionForm.SaveActionListener.class),
                    @EventConfig(listeners = UISubcriptionForm.CancelActionListener.class)
                }
)
public class UISubcriptionForm extends UIForm implements UIPopupComponent {

  private static final String INPUT_SUBCRIPTION_NAME = "SubcriptionName";
  
  private static final String INPUT_SUBCRIPTION_DESCRIPTION = "SubcriptionDescription"; 
  
  private static final String INPUT_SUBCRIPTION_TITLE = "SubcriptionTitle";
  
  private static final String SELECT_CATEGORIES_NAME = "CategoryName";
  
  private NewsletterCategoryHandler categoryHandler = null;
  
  public UISubcriptionForm() throws Exception{

    List<NewsletterCategoryConfig> categories = getListCategories();
    List<SelectItemOption<String>> listCategoriesName = new ArrayList<SelectItemOption<String>>();
    SelectItemOption<String> option = null;
    for (NewsletterCategoryConfig category: categories) {
      option = new SelectItemOption<String>(category.getName());
      listCategoriesName.add(option);
    }

    UIFormStringInput inputSubcriptionName = new UIFormStringInput(INPUT_SUBCRIPTION_NAME, null);
    UIFormTextAreaInput inputSubcriptionDescription = new UIFormTextAreaInput(INPUT_SUBCRIPTION_DESCRIPTION, null, null);
    UIFormStringInput inputSubcriptionTitle = new UIFormStringInput(INPUT_SUBCRIPTION_TITLE, null);
    
    UIFormSelectBox categoryName = new UIFormSelectBox(SELECT_CATEGORIES_NAME, SELECT_CATEGORIES_NAME, listCategoriesName);

    addChild(categoryName);
    addChild(inputSubcriptionName);
    addChild(inputSubcriptionTitle);
    addChild(inputSubcriptionDescription);
    
    setActions(new String[]{"Save", "Cancel"});
  }

  public void activate() throws Exception {    
  }

  public void deActivate() throws Exception {
  }

  private List<NewsletterCategoryConfig> getListCategories(){
    NewsletterManagerService newsletterManagerService = getApplicationComponent(NewsletterManagerService.class);
    categoryHandler = newsletterManagerService.getCategoryHandler();
    try{
      return categoryHandler.getListCategories(NewsLetterUtil.getPortalName(), NewsLetterUtil.getSesssionProvider());
    }catch(Exception e){
      e.printStackTrace();
      return new ArrayList<NewsletterCategoryConfig>();
    }
  }
  
  static  public class SaveActionListener extends EventListener<UISubcriptionForm> {
    public void execute(Event<UISubcriptionForm> event) throws Exception {
      UISubcriptionForm uiSubcriptionForm = event.getSource();
      
      UINewsletterManagerPortlet newsletterPortlet = uiSubcriptionForm.getAncestorOfType(UINewsletterManagerPortlet.class);
      NewsletterManagerService newsletterManagerService = (NewsletterManagerService)newsletterPortlet.getApplicationComponent(NewsletterManagerService.class) ;
      NewsletterSubscriptionConfig subscriptionConfig = new NewsletterSubscriptionConfig();
      
      UIFormSelectBox formSelectBox = uiSubcriptionForm.getChildById(SELECT_CATEGORIES_NAME);
      String categoryName = formSelectBox.getSelectedValues()[0].toString();
      String subcriptionTitle = ((UIFormStringInput)uiSubcriptionForm.getChildById(INPUT_SUBCRIPTION_TITLE)).getValue();
      String subcriptionName = uiSubcriptionForm.getUIStringInput(INPUT_SUBCRIPTION_NAME).getValue();
      String subcriptionDecription = ((UIFormTextAreaInput)uiSubcriptionForm.getChildById(INPUT_SUBCRIPTION_DESCRIPTION)).getValue();

      UIApplication uiApp = uiSubcriptionForm.getAncestorOfType(UIApplication.class);
      if((subcriptionName == null) || (subcriptionName.trim().length() < 1)){
        uiApp.addMessage(new ApplicationMessage("UISubcriptionForm.msg.subcriptionNameIsNotEmpty", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } else if ((subcriptionTitle == null) || (subcriptionTitle.trim().length() < 1)) {
        
        uiApp.addMessage(new ApplicationMessage("UISubcriptionForm.msg.subcriptionTitleIsNotEmpty", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }  else {
        
        try {
          subscriptionConfig.setName(subcriptionName);
          subscriptionConfig.setDescription(subcriptionDecription);
          subscriptionConfig.setTitle(subcriptionTitle);
          subscriptionConfig.setCategoryName(categoryName);
        
          NewsletterSubscriptionHandler subscriptionHandler = newsletterManagerService.getSubscriptionHandler();
          subscriptionHandler.add(NewsLetterUtil.getSesssionProvider(), NewsLetterUtil.getPortalName(), subscriptionConfig);
          
          UIPopupContainer popupContainer = uiSubcriptionForm.getAncestorOfType(UIPopupContainer.class);
          popupContainer.deActivate();
  
          event.getRequestContext().addUIComponentToUpdateByAjax(newsletterPortlet) ;
        } catch (Exception e) {
          
          uiApp.addMessage(new ApplicationMessage("UISubcriptionForm.msg.subcriptionNameIsAlreadyExist", null, ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
      }
    }
  }

  static  public class CancelActionListener extends EventListener<UISubcriptionForm> {
    public void execute(Event<UISubcriptionForm> event) throws Exception {
      UISubcriptionForm uiSubcriptionForm = event.getSource();
      UIPopupContainer popupContainer = uiSubcriptionForm.getAncestorOfType(UIPopupContainer.class);
      popupContainer.deActivate();
    }
  }
}
