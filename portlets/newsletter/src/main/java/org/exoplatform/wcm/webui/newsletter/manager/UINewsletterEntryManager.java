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

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterConstant;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.NewsletterSubscriptionConfig;
import org.exoplatform.services.wcm.newsletter.config.NewsletterManagerConfig;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterEntryHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterTemplateHandler;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.newsletter.UINewsletterConstant;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPageIterator;
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
         @EventConfig(listeners = UINewsletterEntryManager.OpenNewsletterActionListener.class),
         @EventConfig(listeners = UINewsletterEntryManager.ConvertTemplateActionListener.class),
         @EventConfig(listeners = UINewsletterEntryManager.EditNewsletterEntryActionListener.class),
         @EventConfig(listeners = UINewsletterEntryManager.DeleteNewsletterEntryActionListener.class),
         @EventConfig(listeners = UINewsletterEntryManager.SelectNewsletterActionListener.class)
     }
 )
public class UINewsletterEntryManager extends UIForm {
  private UIFormCheckBoxInput<Boolean>  checkBoxInput;
  private NewsletterSubscriptionConfig subscriptionConfig;
  private NewsletterCategoryConfig categoryConfig;
  private List<NewsletterManagerConfig> listNewsletterConfig;
  private NewsletterEntryHandler newsletterEntryHandler ;
  private String PAGEITERATOR_ID = "NewsletterEntryManagerPageIterator";
  private UIPageIterator uiPageIterator_;

  public UINewsletterEntryManager () throws Exception {
    NewsletterManagerService newsletterManagerService = getApplicationComponent(NewsletterManagerService.class);
    newsletterEntryHandler = newsletterManagerService.getEntryHandler();
  }
  
  public void init() throws Exception{
    ObjectPageList objPageList = new ObjectPageList(setListNewsletterEntries(), 10) ;
    uiPageIterator_ = createUIComponent(UIPageIterator.class, null, PAGEITERATOR_ID);
    addChild(uiPageIterator_);
    uiPageIterator_.setPageList(objPageList) ;
  }
  
  private List<NewsletterManagerConfig> setListNewsletterEntries(){
    this.getChildren().clear();
    listNewsletterConfig = new ArrayList<NewsletterManagerConfig>();
    try{
      listNewsletterConfig.addAll(newsletterEntryHandler.getNewsletterEntriesBySubscription(NewsLetterUtil.getPortalName(), 
                                  categoryConfig.getName(), subscriptionConfig.getName()));
      for (NewsletterManagerConfig newletter : listNewsletterConfig) {
        checkBoxInput = new UIFormCheckBoxInput<Boolean>(newletter.getNewsletterName(), newletter.getNewsletterName(), false);
        this.addChild(checkBoxInput);
      }
    }catch(Exception ex){
      ex.printStackTrace();
    }
    return listNewsletterConfig;
  }
  
  @SuppressWarnings("unchecked")
  public List getListNewsletterEntries() throws Exception { 
    if(uiPageIterator_ != null)return uiPageIterator_.getCurrentPageData() ;
    else return new ArrayList<NewsletterManagerConfig>();
  }

  public List<String> getChecked(){
    List<String> newsletterId = new ArrayList<String>();
    UIFormCheckBoxInput<Boolean> checkbox = null;
    for(UIComponent component : this.getChildren()){
      try{
        checkbox = (UIFormCheckBoxInput<Boolean>)component;
        if(checkbox.isChecked()){
          newsletterId.add(checkbox.getName());
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
      UINewsletterEntryManager uiNewsletterEntryManager = event.getSource();
      UINewsletterManagerPortlet newsletterManagerPortlet = uiNewsletterEntryManager.getAncestorOfType(UINewsletterManagerPortlet.class);
      UISubscriptions uiSubscriptions = newsletterManagerPortlet.getChild(UISubscriptions.class);
      newsletterManagerPortlet.getChild(UICategories.class).setRendered(false);
      uiSubscriptions.setRendered(true);
      uiSubscriptions.setCategory(uiNewsletterEntryManager.categoryConfig);
      uiNewsletterEntryManager.setRendered(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(newsletterManagerPortlet);
    }
  }

  static  public class BackToCategoriesActionListener extends EventListener<UINewsletterEntryManager> {
    public void execute(Event<UINewsletterEntryManager> event) throws Exception {
      UINewsletterEntryManager newsletter = event.getSource();
      UINewsletterManagerPortlet newsletterManagerPortlet = newsletter.getAncestorOfType(UINewsletterManagerPortlet.class);
      UICategories uiCategories = newsletterManagerPortlet.getChild(UICategories.class);
      UISubscriptions subcription = newsletterManagerPortlet.getChild(UISubscriptions.class);
      uiCategories.setRendered(true);
      subcription.setRendered(false);
      newsletter.setRendered(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(newsletterManagerPortlet);
    }
  }

  static  public class OpenNewsletterActionListener extends EventListener<UINewsletterEntryManager> {
    public void execute(Event<UINewsletterEntryManager> event) throws Exception {
      UINewsletterEntryManager uiNewsletterEntryManager = event.getSource();
      List<String> subIds = uiNewsletterEntryManager.getChecked();
      if(subIds == null || subIds.size() != 1){
        Utils.createPopupMessage(uiNewsletterEntryManager, "UISubscription.msg.checkOnlyOneSubScriptionToOpen", null, ApplicationMessage.WARNING);
        return;
      }
      UINewsletterManagerPopup newsletterManagerPopup = uiNewsletterEntryManager.createUIComponent(UINewsletterManagerPopup.class, null, null);
      newsletterManagerPopup.setNewsletterInfor(uiNewsletterEntryManager.categoryConfig.getName(), uiNewsletterEntryManager.subscriptionConfig.getName(), subIds.get(0));
      Utils.createPopupWindow(uiNewsletterEntryManager, newsletterManagerPopup, UINewsletterConstant.UIVIEW_ENTRY_PUPUP_WINDOW, 800, 600);
    }
  }
  
  static  public class SelectNewsletterActionListener extends EventListener<UINewsletterEntryManager> {
    public void execute(Event<UINewsletterEntryManager> event) throws Exception {
      UINewsletterEntryManager uiNewsletterEntryManager = event.getSource();
      String newsletterName = event.getRequestContext().getRequestParameter(OBJECTID);
      UINewsletterManagerPopup newsletterManagerPopup = uiNewsletterEntryManager.createUIComponent(UINewsletterManagerPopup.class, null, null);
      newsletterManagerPopup.setNewsletterInfor(uiNewsletterEntryManager.categoryConfig.getName(), uiNewsletterEntryManager.subscriptionConfig.getName(), newsletterName);
      Utils.createPopupWindow(uiNewsletterEntryManager, newsletterManagerPopup, UINewsletterConstant.UIVIEW_ENTRY_PUPUP_WINDOW, 800, 600);
    }
  }
  
  public static class AddEntryActionListener extends EventListener<UINewsletterEntryManager> {
    public void execute(Event<UINewsletterEntryManager> event) throws Exception {
      UINewsletterEntryManager uiNewsletterEntryManager = event.getSource();
      UINewsletterEntryContainer entryContainer = uiNewsletterEntryManager.createUIComponent(UINewsletterEntryContainer.class, null, null);
      entryContainer.setCategoryConfig(uiNewsletterEntryManager.categoryConfig);
      UINewsletterEntryDialogSelector newsletterEntryDialogSelector = entryContainer.getChild(UINewsletterEntryDialogSelector.class);
      UIFormSelectBox categorySelectBox = newsletterEntryDialogSelector.getChildById(UINewsletterConstant.ENTRY_CATEGORY_SELECTBOX);
      categorySelectBox.setValue(uiNewsletterEntryManager.categoryConfig.getName());
      categorySelectBox.setDisabled(true);
      UIFormSelectBox subscriptionSelectBox = newsletterEntryDialogSelector.getChildById(UINewsletterConstant.ENTRY_SUBSCRIPTION_SELECTBOX);
      subscriptionSelectBox.setValue(uiNewsletterEntryManager.subscriptionConfig.getName());
      subscriptionSelectBox.setDisabled(true);
      Utils.createPopupWindow(uiNewsletterEntryManager, entryContainer, UINewsletterConstant.ENTRY_FORM_POPUP_WINDOW, 800, 600);
    }
  }
  
  public static class DeleteNewsletterEntryActionListener extends EventListener<UINewsletterEntryManager> {
    public void execute(Event<UINewsletterEntryManager> event) throws Exception {
      UINewsletterEntryManager uiNewsletterEntryManager = event.getSource();
      List<String> subIds = uiNewsletterEntryManager.getChecked();
      uiNewsletterEntryManager.newsletterEntryHandler.delete(NewsLetterUtil.getPortalName(), 
                                                             uiNewsletterEntryManager.categoryConfig.getName(), 
                                                             uiNewsletterEntryManager.subscriptionConfig.getName(), subIds);
      uiNewsletterEntryManager.init();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiNewsletterEntryManager) ;
    }
  }
  
  public static class EditNewsletterEntryActionListener extends EventListener<UINewsletterEntryManager> {
    public void execute(Event<UINewsletterEntryManager> event) throws Exception {
      UINewsletterEntryManager uiNewsletterEntryManager = event.getSource();
      List<String> subIds = uiNewsletterEntryManager.getChecked();
      if(subIds == null || subIds.size() != 1){
        Utils.createPopupMessage(uiNewsletterEntryManager, "UISubscription.msg.checkOnlyOneSubScriptionToOpen", null, ApplicationMessage.WARNING);
        return;
      }
      UINewsletterEntryContainer entryContainer = uiNewsletterEntryManager.createUIComponent(UINewsletterEntryContainer.class, null, null);
      entryContainer.setNewsletterInfor(NewsletterConstant.generateCategoryPath(NewsLetterUtil.getPortalName()) + "/"
                                        + uiNewsletterEntryManager.categoryConfig.getName() + "/" 
                                        + uiNewsletterEntryManager.getSubscriptionConfig().getName() + "/" 
                                        + subIds.get(0));
      UINewsletterEntryForm newsletterEntryForm = entryContainer.getChild(UINewsletterEntryForm.class);
      newsletterEntryForm.addNew(false);
      Utils.createPopupWindow(uiNewsletterEntryManager, entryContainer, UINewsletterConstant.ENTRY_FORM_POPUP_WINDOW, 800, 600);
    }
  }
  
  public static class ConvertTemplateActionListener extends EventListener<UINewsletterEntryManager> {
    public void execute(Event<UINewsletterEntryManager> event) throws Exception {
      UINewsletterEntryManager newsletterEntryManager = event.getSource();
      String categoryName = newsletterEntryManager.categoryConfig.getName();
      String subscriptionName = newsletterEntryManager.subscriptionConfig.getName();
      List<String> subIds = newsletterEntryManager.getChecked();
      if(subIds == null || subIds.size() != 1){
        UIApplication uiApp = newsletterEntryManager.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UISubscription.msg.checkOnlyOneSubScriptionToOpen", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      String newsletterName = subIds.get(0);
      NewsletterManagerService newsletterManagerService = newsletterEntryManager.getApplicationComponent(NewsletterManagerService.class);
      RepositoryService repositoryService = newsletterEntryManager.getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getRepository(newsletterManagerService.getRepositoryName()); 
      Session session = Utils.getSessionProvider(newsletterEntryManager).getSession(newsletterManagerService.getWorkspaceName(), manageableRepository);
      String newsletterPath = NewsletterConstant.generateNewsletterPath(Util.getUIPortal().getName(), categoryName, subscriptionName, newsletterName) ;
      Node newsletterNode = (Node) session.getItem(newsletterPath);
      NewsletterTemplateHandler newsletterTemplateHandler = newsletterManagerService.getTemplateHandler();
      newsletterTemplateHandler.convertAsTemplate(newsletterNode.getPath(), Util.getUIPortal().getName(), categoryName);
    }
  }
  
}
