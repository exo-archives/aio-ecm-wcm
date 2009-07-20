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

import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.NewsletterSubscriptionConfig;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterCategoryHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterSubscriptionHandler;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.newsletter.UINewsletterConstant;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.NameValidator;

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
                    @EventConfig(listeners = UISubcriptionForm.CancelActionListener.class, phase = Phase.DECODE)
                }
)
public class UISubcriptionForm extends UIForm {

  private static final String          INPUT_SUBCRIPTION_NAME        = "SubcriptionName";

  private static final String          INPUT_SUBCRIPTION_DESCRIPTION = "SubcriptionDescription";

  private static final String          INPUT_SUBCRIPTION_TITLE       = "SubcriptionTitle";

  private static final String          SELECT_CATEGORIES_NAME        = "CategoryName";

  private NewsletterCategoryHandler    categoryHandler               = null;

  private NewsletterSubscriptionConfig subscriptionConfig            = null;
  
  public UISubcriptionForm() throws Exception{

    setActions(new String[]{"Save", "Cancel"});
    List<NewsletterCategoryConfig> categories = getListCategories();
    List<SelectItemOption<String>> listCategoriesName = new ArrayList<SelectItemOption<String>>();
    SelectItemOption<String> option = null;
    for (NewsletterCategoryConfig category: categories) {
      option = new SelectItemOption<String>(category.getTitle(), category.getName());
      listCategoriesName.add(option);
    }

    UIFormStringInput inputSubcriptionName = new UIFormStringInput(INPUT_SUBCRIPTION_NAME, null);
    inputSubcriptionName.addValidator(MandatoryValidator.class).addValidator(NameValidator.class);
                                                                      
    UIFormStringInput inputSubcriptionTitle = new UIFormStringInput(INPUT_SUBCRIPTION_TITLE, null);
    inputSubcriptionTitle.addValidator(MandatoryValidator.class);
                                                                        
    addChild(new UIFormSelectBox(SELECT_CATEGORIES_NAME, SELECT_CATEGORIES_NAME, listCategoriesName));
    addChild(inputSubcriptionName);
    addChild(inputSubcriptionTitle);
    addChild(new UIFormTextAreaInput(INPUT_SUBCRIPTION_DESCRIPTION, null, null));
  }

  public void setSubscriptionInfor(NewsletterSubscriptionConfig subscriptionConfig){
    
    this.subscriptionConfig = subscriptionConfig;
    
    UIFormStringInput inputName = this.getChildById(INPUT_SUBCRIPTION_NAME);
    
    inputName.setValue(subscriptionConfig.getName());
    inputName.setEditable(false);

    ((UIFormStringInput)this.getChildById(INPUT_SUBCRIPTION_TITLE)).setValue(subscriptionConfig.getTitle());
    ((UIFormTextAreaInput)this.getChildById(INPUT_SUBCRIPTION_DESCRIPTION)).setValue(subscriptionConfig.getDescription());
    UIFormSelectBox formSelectBox = this.getChildById(SELECT_CATEGORIES_NAME);
    formSelectBox.setValue(subscriptionConfig.getCategoryName());
    formSelectBox.setDisabled(true);
  }

  private List<NewsletterCategoryConfig> getListCategories(){
    
    NewsletterManagerService newsletterManagerService = getApplicationComponent(NewsletterManagerService.class);
    
    categoryHandler = newsletterManagerService.getCategoryHandler();
    
    try{
      ThreadLocalSessionProviderService threadLocalSessionProviderService = getApplicationComponent(ThreadLocalSessionProviderService.class);
      SessionProvider sessionProvider =  threadLocalSessionProviderService.getSessionProvider(null);
      return categoryHandler.getListCategories(NewsLetterUtil.getPortalName(), sessionProvider);
    }catch(Exception e){

      return new ArrayList<NewsletterCategoryConfig>();
    }
  }
  
  static  public class SaveActionListener extends EventListener<UISubcriptionForm> {
    public void execute(Event<UISubcriptionForm> event) throws Exception {
      
      UISubcriptionForm uiSubcriptionForm = event.getSource();
      
      UINewsletterManagerPortlet newsletterPortlet = uiSubcriptionForm.getAncestorOfType(UINewsletterManagerPortlet.class);
      NewsletterManagerService newsletterManagerService = (NewsletterManagerService)newsletterPortlet.getApplicationComponent(NewsletterManagerService.class) ;
      
      UIFormSelectBox formSelectBox = uiSubcriptionForm.getChildById(SELECT_CATEGORIES_NAME);
      String categoryName = formSelectBox.getSelectedValues()[0].toString();
      String subcriptionTitle = ((UIFormStringInput)uiSubcriptionForm.getChildById(INPUT_SUBCRIPTION_TITLE)).getValue();
      String subcriptionName = uiSubcriptionForm.getUIStringInput(INPUT_SUBCRIPTION_NAME).getValue();
      String subcriptionDecription = ((UIFormTextAreaInput)uiSubcriptionForm.getChildById(INPUT_SUBCRIPTION_DESCRIPTION)).getValue();

      UIApplication uiApp = uiSubcriptionForm.getAncestorOfType(UIApplication.class);

      NewsletterSubscriptionHandler subscriptionHandler = newsletterManagerService.getSubscriptionHandler();
      NewsletterSubscriptionConfig newsletterSubscriptionConfig = null;
      if(uiSubcriptionForm.subscriptionConfig == null) {

        newsletterSubscriptionConfig = subscriptionHandler
          .getSubscriptionsByName(NewsLetterUtil.getPortalName(), categoryName, subcriptionName);
        if (newsletterSubscriptionConfig != null) {

          uiApp.addMessage(new ApplicationMessage("UISubcriptionForm.msg.subcriptionNameIsAlreadyExist", null, ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
        
        newsletterSubscriptionConfig = new NewsletterSubscriptionConfig();

        newsletterSubscriptionConfig.setName(subcriptionName);
        newsletterSubscriptionConfig.setCategoryName(categoryName);
        newsletterSubscriptionConfig.setDescription(subcriptionDecription);
        newsletterSubscriptionConfig.setTitle(subcriptionTitle);

        subscriptionHandler.add(NewsLetterUtil.getSesssionProvider(), NewsLetterUtil.getPortalName(), newsletterSubscriptionConfig);
      } else {

        newsletterSubscriptionConfig = uiSubcriptionForm.subscriptionConfig;

        newsletterSubscriptionConfig.setCategoryName(categoryName);
        newsletterSubscriptionConfig.setDescription(subcriptionDecription);
        newsletterSubscriptionConfig.setTitle(subcriptionTitle);

        subscriptionHandler.edit(NewsLetterUtil.getPortalName(), newsletterSubscriptionConfig);
      }

      UIPopupContainer popupContainer = uiSubcriptionForm.getAncestorOfType(UIPopupContainer.class);
      Utils.closePopupWindow(popupContainer, UINewsletterConstant.SUBSCRIPTION_FORM_POPUP_WINDOW);

      event.getRequestContext().addUIComponentToUpdateByAjax(newsletterPortlet) ;
    }
  }

  static  public class CancelActionListener extends EventListener<UISubcriptionForm> {
    public void execute(Event<UISubcriptionForm> event) throws Exception {
      UISubcriptionForm uiSubcriptionForm = event.getSource();
      UIPopupContainer popupContainer = uiSubcriptionForm.getAncestorOfType(UIPopupContainer.class);

      if (uiSubcriptionForm.getSubmitAction().equals("Cancel")) {
        uiSubcriptionForm.getComponentConfig().setValidators(null);
        uiSubcriptionForm.getChildren().clear();
      }
      Utils.closePopupWindow(popupContainer, UINewsletterConstant.SUBSCRIPTION_FORM_POPUP_WINDOW);
    }
  }
}
