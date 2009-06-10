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
import org.exoplatform.services.wcm.newsletter.handler.NewsletterPublicUserHandler;
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

  /** UICheckBox Input*/
  private UIFormCheckBoxInput<Boolean>  checkBoxInput            = null;

  /** UITextBox Input*/
  private UIFormStringInput             inputEmail               = null;

  /** Email of user*/
  private String                        userMail                 = "";

  /** Check for subcription is updated*/
  private boolean                       isUpdated                = false;

  /** NewsletterCategoryHandler*/
  private NewsletterCategoryHandler     categoryHandler          = null;

  /** NewsletterSubscriptionHandler*/
  private NewsletterSubscriptionHandler subcriptionHandler       = null;

  /** NewsletterPublicUserHandler*/
  private NewsletterPublicUserHandler   publicUserHandler        = null;

  /** NewsletterManagerService*/
  NewsletterManagerService              newsletterManagerService = null;

  public UINewsletterViewerForm() throws Exception {

    newsletterManagerService = getApplicationComponent(NewsletterManagerService.class);
    categoryHandler = newsletterManagerService.getCategoryHandler();
    subcriptionHandler = newsletterManagerService.getSubscriptionHandler();
    publicUserHandler = newsletterManagerService.getPublicUserHandler();

    this.setActions(new String[] { "Subcribe" });
    inputEmail = (UIFormStringInput) new UIFormStringInput("inputEmail", "Email", null)
                                           .addValidator(MandatoryValidator.class)
                                           .addValidator(EmailAddressValidator.class);
    this.addChild(inputEmail);
  }

  public void init(List<NewsletterSubscriptionConfig> listNewsletterSubcription, String categoryName) throws Exception {

    String subcriptionPattern = null;
    for (NewsletterSubscriptionConfig newsletterSubcription : listNewsletterSubcription) {

      subcriptionPattern = categoryName + "#" + newsletterSubcription.getName();

      if (this.getChildById(subcriptionPattern) != null)
        continue;
      checkBoxInput = new UIFormCheckBoxInput<Boolean>(subcriptionPattern,
                                                       subcriptionPattern,
                                                       false);
      this.addChild(checkBoxInput);
    }
  }

  @SuppressWarnings("unused")
  private List<NewsletterCategoryConfig> getListCategories() {
    try {
      return categoryHandler.getListCategories(NewsLetterUtil.getPortalName(),
                                               NewsLetterUtil.getSesssionProvider());
    } catch (Exception e) {

      return new ArrayList<NewsletterCategoryConfig>();
    }
  }

  @SuppressWarnings("unused")
  private List<NewsletterSubscriptionConfig> getListSubscription(String categoryName) {

    try {

      List<NewsletterSubscriptionConfig> listSubscription = new ArrayList<NewsletterSubscriptionConfig>();
      listSubscription = subcriptionHandler.getSubscriptionsByCategory(NewsLetterUtil.getSesssionProvider(),
                                                                       NewsLetterUtil.getPortalName(),
                                                                       categoryName);
      this.init(listSubscription, categoryName);

      return listSubscription;
    } catch (Exception e) {
      e.printStackTrace();
      return new ArrayList<NewsletterSubscriptionConfig>();
    }
  }

  public static class ForgetEmailActionListener extends EventListener<UINewsletterViewerForm> {

    /*
     * (non-Javadoc)
     * 
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

  public static class ChangeSubcriptionsActionListener extends
                                                      EventListener<UINewsletterViewerForm> {

    /*
     * (non-Javadoc)
     * 
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    @SuppressWarnings("unchecked")
    public void execute(Event<UINewsletterViewerForm> event) throws Exception {

      UINewsletterViewerForm newsletterForm = event.getSource();

      List<NewsletterCategoryConfig> listNewsletterCategories = newsletterForm.getListCategories();
      List<NewsletterSubscriptionConfig> listNewsletterSubcription = null;

      List<String> listSubcriptionPattern = new ArrayList<String>();
      String subcriptionPattern = null;
      for (NewsletterCategoryConfig category : listNewsletterCategories) {
        listNewsletterSubcription = newsletterForm.getListSubscription(category.getName());
        for (NewsletterSubscriptionConfig subscriptionConfig : listNewsletterSubcription) {

          subcriptionPattern = category.getName() + "#" + subscriptionConfig.getName();
          UIFormCheckBoxInput checkboxInput = newsletterForm.getUIFormCheckBoxInput(subcriptionPattern);
          if (checkboxInput.isChecked()) {
            listSubcriptionPattern.add(checkboxInput.getName());
          }
        }
      }

      newsletterForm.publicUserHandler.updateSubscriptions(NewsLetterUtil.getPortalName(),
                                                           newsletterForm.userMail,
                                                           listSubcriptionPattern);

      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      UIApplication uiApp = context.getUIApplication();
      uiApp.addMessage(new ApplicationMessage("UINewsletterViewerForm.msg.updateSuccess",
                                              null,
                                              ApplicationMessage.INFO));

      event.getRequestContext().addUIComponentToUpdateByAjax(newsletterForm);
    }
  }

  public static class SubcribeActionListener extends EventListener<UINewsletterViewerForm> {

    /*
     * (non-Javadoc)
     * 
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    @SuppressWarnings("unchecked")
    public void execute(Event<UINewsletterViewerForm> event) throws Exception {

      UINewsletterViewerForm newsletterForm = event.getSource();

      newsletterForm.inputEmail.setRendered(false);
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      UIApplication uiApp = context.getUIApplication();

      newsletterForm.userMail = newsletterForm.getUIStringInput("inputEmail").getValue();

      uiApp.addMessage(new ApplicationMessage("UINewsletterViewerForm.msg.subcribed",
                                              null,
                                              ApplicationMessage.INFO));

      newsletterForm.isUpdated = true;
      newsletterForm.setActions(new String[] { "ForgetEmail", "ChangeSubcriptions" });

      event.getRequestContext().addUIComponentToUpdateByAjax(newsletterForm);
    }
  }
}
