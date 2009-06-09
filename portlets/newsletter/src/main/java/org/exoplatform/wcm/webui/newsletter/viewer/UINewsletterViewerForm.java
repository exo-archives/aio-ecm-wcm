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
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.NewsletterSubscriptionConfig;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterCategoryHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterSubscriptionHandler;
import org.exoplatform.wcm.webui.newsletter.manager.NewsLetterUtil;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.EmailAddressValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;

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
       @EventConfig(listeners = UINewsletterViewerForm.SubcribeActionListener.class),
       @EventConfig(listeners = UINewsletterViewerForm.ForgetEmailActionListener.class),
       @EventConfig(listeners = UINewsletterViewerForm.ChangeSubcriptionsActionListener.class)
     }
)
 public class UINewsletterViewerForm extends UIForm {

  private UIFormCheckBoxInput<Boolean> checkBoxInput = null;
  
  private UIFormStringInput inputEmail = null;
  
  private String userMail = "";
  
  private boolean isUpdated = false;
  
  private NewsletterCategoryHandler categoryHandler = null;
  
  private NewsletterSubscriptionHandler subcriptionHandler = null;
  
  NewsletterManagerService newsletterManagerService = null;
  
  public UINewsletterViewerForm () throws Exception {
    
    newsletterManagerService = getApplicationComponent(NewsletterManagerService.class);
    categoryHandler = newsletterManagerService.getCategoryHandler();
    subcriptionHandler = newsletterManagerService.getSubscriptionHandler();
    this.setActions(new String[] {"Subcribe"});
    inputEmail = (UIFormStringInput) new UIFormStringInput("inputEmail", "Email", null).addValidator(MandatoryValidator.class).
    addValidator(EmailAddressValidator.class);
    this.addChild(inputEmail);
  }

  public void init(List<NewsletterSubscriptionConfig> listNewsletterSubcription) throws Exception {
    
    for (NewsletterSubscriptionConfig newsletterSubcription : listNewsletterSubcription) {
      
      if(this.getChildById(newsletterSubcription.getName()) != null) continue;
      checkBoxInput = new UIFormCheckBoxInput<Boolean>(newsletterSubcription.getName(), newsletterSubcription.getName(), false);
      this.addChild(checkBoxInput);
    }
  }

  @SuppressWarnings("unused")
  private List<NewsletterCategoryConfig> getListCategories(){
    try{
      return categoryHandler.getListCategories(NewsLetterUtil.getPortalName(), NewsLetterUtil.getSesssionProvider());
    }catch(Exception e){

      return new ArrayList<NewsletterCategoryConfig>();
    }
  }

  @SuppressWarnings("unused")
  private List<NewsletterSubscriptionConfig> getListSubscription(String categoryName){
    
    try{
      
      List<NewsletterSubscriptionConfig> listSubscription = new ArrayList<NewsletterSubscriptionConfig>();
      listSubscription = 
        subcriptionHandler.getSubscriptionsByCategory(NewsLetterUtil.getSesssionProvider(), NewsLetterUtil.getPortalName(), categoryName);
      this.init(listSubscription);
      
      return listSubscription;
    }catch(Exception e){
      
      return new ArrayList<NewsletterSubscriptionConfig>();
    }
  }

  public static class ForgetEmailActionListener extends EventListener<UINewsletterViewerForm> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    @SuppressWarnings("unchecked")
    public void execute(Event<UINewsletterViewerForm> event) throws Exception {
      
      UINewsletterViewerForm newsletterForm = event.getSource();
      newsletterForm.isUpdated = false;
      newsletterForm.userMail = "";
      newsletterForm.inputEmail.setRendered(true);
      
      event.getRequestContext().addUIComponentToUpdateByAjax(newsletterForm);
    }
  }
  
  public static class ChangeSubcriptionsActionListener extends EventListener<UINewsletterViewerForm> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    @SuppressWarnings("unchecked")
    public void execute(Event<UINewsletterViewerForm> event) throws Exception {
      
    }
  }

  public static class SubcribeActionListener extends EventListener<UINewsletterViewerForm> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    @SuppressWarnings("unchecked")
    public void execute(Event<UINewsletterViewerForm> event) throws Exception {
      
      UINewsletterViewerForm newsletterForm = event.getSource();

      newsletterForm.inputEmail.setRendered(false);
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      UIApplication uiApp = context.getUIApplication() ;

      newsletterForm.userMail = newsletterForm.getUIStringInput("inputEmail").getValue();

      uiApp.addMessage(new ApplicationMessage("UINewsletterViewerForm.msg.updatesuccess", null, ApplicationMessage.INFO));
      
      newsletterForm.isUpdated = true;
      newsletterForm.setActions(new String[] {"ForgetEmail", "ChangeSubcriptions"});
      
      event.getRequestContext().addUIComponentToUpdateByAjax(newsletterForm);
    }
   }
 }
