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
import org.exoplatform.services.wcm.newsletter.handler.NewsletterManageUserHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterPublicUserHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterSubscriptionHandler;
import org.exoplatform.wcm.webui.newsletter.manager.NewsLetterUtil;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform
 * ngoc.tran@exoplatform.com May 25, 2009
 */
@ComponentConfig(
   lifecycle = UIFormLifecycle.class,
   template = "app:/groovy/webui/newsletter/NewsletterViewer/UINewsletterListViewer.gtmpl",
   events = {
        @EventConfig(listeners = UINewsletterViewerForm.SubcribeActionListener.class),
        @EventConfig(listeners = UINewsletterViewerForm.ForgetEmailActionListener.class),
        @EventConfig(listeners = UINewsletterViewerForm.ChangeSubcriptionsActionListener.class) }
)
public class UINewsletterViewerForm extends UIForm {
  public String userCode;
  public UIFormStringInput inputEmail;
  public String userMail = "";
  public boolean isUpdated = false;
  public NewsletterSubscriptionHandler subcriptionHandler ;
  public NewsletterPublicUserHandler publicUserHandler ;
  private UIFormCheckBoxInput<Boolean> checkBoxInput;
  private NewsletterCategoryHandler categoryHandler ;
  private NewsletterManageUserHandler managerUserHandler ;
  private NewsletterManagerService newsletterManagerService;
  private String linkToSendMail;
  private List<String> listIds;

  public UINewsletterViewerForm() throws Exception {
    newsletterManagerService = getApplicationComponent(NewsletterManagerService.class);
    categoryHandler = newsletterManagerService.getCategoryHandler();
    subcriptionHandler = newsletterManagerService.getSubscriptionHandler();
    publicUserHandler = newsletterManagerService.getPublicUserHandler();
    managerUserHandler = newsletterManagerService.getManageUserHandler();

    this.setActions(new String[] { "Subcribe" });
    inputEmail = new UIFormStringInput("inputEmail", "Email", null);
    inputEmail.addValidator(MandatoryValidator.class).addValidator(UINewsletterViewerEmailAddressValidator.class);
    this.addChild(inputEmail);
  }
  
  public void setListIds(List<String> listIds){
    this.listIds = listIds;
  }
  
  public void init(List<NewsletterSubscriptionConfig> listNewsletterSubcription, String categoryName) throws Exception {
    if(userCode != null && userCode.trim().length() > 0){ // run when confirm user code
      String subcriptionPattern;
      for (NewsletterSubscriptionConfig newsletterSubcription : listNewsletterSubcription) {
        subcriptionPattern = categoryName + "#" + newsletterSubcription.getName();
        if (this.getChildById(subcriptionPattern) != null) this.removeChildById(subcriptionPattern);
        checkBoxInput = new UIFormCheckBoxInput<Boolean>(subcriptionPattern, subcriptionPattern, false);
        if(listIds.contains(subcriptionPattern)) checkBoxInput.setChecked(true);
        else checkBoxInput.setChecked(false);
        this.addChild(checkBoxInput);
      }
    } else {
      String subcriptionPattern;
      for (NewsletterSubscriptionConfig newsletterSubcription : listNewsletterSubcription) {
        subcriptionPattern = categoryName + "#" + newsletterSubcription.getName();
        if (this.getChildById(subcriptionPattern) != null) continue;
        else{
          checkBoxInput = new UIFormCheckBoxInput<Boolean>(subcriptionPattern, subcriptionPattern, false);
          this.addChild(checkBoxInput);
        }
      }
    }
  }
  
  public void setInforConfirm(String userEmail, String userCode){
    this.userMail = userEmail;
    this.userCode = userCode;
  }
  
  @SuppressWarnings("unused")
  private void setActionAgain(){
    this.setActions(new String[] { "ForgetEmail", "ChangeSubcriptions" });
    this.isUpdated = true;
  }
  
  @SuppressWarnings({ "unused", "unchecked" })
  private List<String> listSubscriptionChecked(){
    List<String> listSubscription = new ArrayList<String>();
    for(UIComponent component : this.getChildren()){
      try{
        if(((UIFormCheckBoxInput<Boolean>) component).isChecked())
          listSubscription.add(component.getName());
      }catch(ClassCastException ex){};
    }
    return listSubscription;
  }

  @SuppressWarnings("unused")
  private List<NewsletterCategoryConfig> getListCategories() {
    try {
      return categoryHandler.getListCategories(NewsLetterUtil.getPortalName());
    } catch (Exception e) {

      return new ArrayList<NewsletterCategoryConfig>();
    }
  }

  @SuppressWarnings("unused")
  private List<NewsletterSubscriptionConfig> getListSubscription(String categoryName) {
    try {

      List<NewsletterSubscriptionConfig> listSubscription = 
                                          subcriptionHandler.getSubscriptionsByCategory(NewsLetterUtil.getPortalName(), categoryName);
      this.init(listSubscription, categoryName);

      return listSubscription;
    } catch (Exception e) {
      e.printStackTrace();
      return new ArrayList<NewsletterSubscriptionConfig>();
    }
  }
  
  @SuppressWarnings("unused")
  private void setLink(String url){
    this.linkToSendMail = NewsLetterUtil.generateLink(url);
  }

  public static class ForgetEmailActionListener extends EventListener<UINewsletterViewerForm> {
    public void execute(Event<UINewsletterViewerForm> event) throws Exception {
      UINewsletterViewerForm newsletterForm = event.getSource();
      
      newsletterForm.publicUserHandler.forgetEmail(NewsLetterUtil.getPortalName(), newsletterForm.userMail);
      
      newsletterForm.isUpdated = false;
      newsletterForm.inputEmail.setValue("");
      newsletterForm.inputEmail.setRendered(true);
      newsletterForm.userMail = "";
      newsletterForm.setActions(new String[] {"Subcribe"});
      event.getRequestContext().addUIComponentToUpdateByAjax(newsletterForm);
    }
  }

  public static class ChangeSubcriptionsActionListener extends EventListener<UINewsletterViewerForm> {
    public void execute(Event<UINewsletterViewerForm> event) throws Exception {
      UINewsletterViewerForm newsletterForm = event.getSource();
      List<String> listSubcriptionPattern = new ArrayList<String>();
      listSubcriptionPattern = newsletterForm.listSubscriptionChecked();
      newsletterForm.publicUserHandler.updateSubscriptions(NewsLetterUtil.getPortalName(), 
                                                           newsletterForm.inputEmail.getValue(), listSubcriptionPattern);
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      UIApplication uiApp = context.getUIApplication();
      uiApp.addMessage(new ApplicationMessage("UINewsletterViewerForm.msg.updateSuccess", null, ApplicationMessage.INFO));

      newsletterForm.isUpdated = true;
      newsletterForm.userMail = newsletterForm.userMail;
      newsletterForm.inputEmail.setRendered(false);
      newsletterForm.setActions(new String[] {"ForgetEmail", "ChangeSubcriptions" });
      
      event.getRequestContext().addUIComponentToUpdateByAjax(newsletterForm);
    }
  }

  public static class SubcribeActionListener extends EventListener<UINewsletterViewerForm> {
    public void execute(Event<UINewsletterViewerForm> event) throws Exception {
      UINewsletterViewerForm newsletterForm = event.getSource();
      String portalName = NewsLetterUtil.getPortalName();
      String userEmail = newsletterForm.inputEmail.getValue();
      List<String> listCategorySubscription = newsletterForm.listSubscriptionChecked();
      String contentOfMessage;
      boolean isExistedEmail = newsletterForm.managerUserHandler.checkExistedEmail(portalName, userEmail);
      
      if (!isExistedEmail) {
        if(listCategorySubscription.size() < 1){
          contentOfMessage = "UINewsletterViewerForm.msg.checkSubscriptionToProcess";
        }else{
          newsletterForm.publicUserHandler.subscribe(portalName, userEmail, listCategorySubscription, newsletterForm.linkToSendMail);
          newsletterForm.inputEmail.setRendered(false);
          newsletterForm.userMail = userEmail;
          newsletterForm.isUpdated = true;
          newsletterForm.setActions(new String[] { "ForgetEmail", "ChangeSubcriptions" });
          contentOfMessage = "UINewsletterViewerForm.msg.subcribed";
        }
      } else {
        contentOfMessage = "UINewsletterViewerForm.msg.alreadyExistedEmail";
      }

      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      UIApplication uiApp = context.getUIApplication();
      uiApp.addMessage(new ApplicationMessage(contentOfMessage, null, ApplicationMessage.INFO));
      event.getRequestContext().addUIComponentToUpdateByAjax(newsletterForm);
    }
  }
}
