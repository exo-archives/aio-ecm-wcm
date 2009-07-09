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

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.exoplatform.ecm.webui.form.field.UIFormCalendarField;
import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterTemplateHandler;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.form.UIFormDateTimeInput;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 10, 2009  
 */
@ComponentConfig (
    lifecycle = UIContainerLifecycle.class
)
public class UINewsletterEntryContainer extends UIContainer {

  private NewsletterCategoryConfig categoryConfig;
  private String newsletterPath = null;
  private boolean isUpdated = false;
  
  public UINewsletterEntryContainer() throws Exception {
    //init();
  }
  
  public void setUpdated(boolean isUpdated){
    this.isUpdated = isUpdated;
  }
  
  public boolean isUpdated(){
    return this.isUpdated;
  }
  
  public void setNewsletterInfor(String newsletterPath) throws Exception{
    this.newsletterPath = newsletterPath;
    init();
  }
  
  private void init() throws Exception{
    this.getChildren().clear();
    NewsletterManagerService newsletterManagerService = getApplicationComponent(NewsletterManagerService.class);
    UINewsletterEntryDialogSelector newsletterEntryDialogSelector = addChild(UINewsletterEntryDialogSelector.class, null, null);
    newsletterEntryDialogSelector.updateTemplateSelectBox();
    UINewsletterEntryForm newsletterEntryForm = createUIComponent(UINewsletterEntryForm.class, null, null);
    newsletterEntryForm.setRepositoryName(newsletterManagerService.getRepositoryName());
    newsletterEntryForm.setWorkspace(newsletterManagerService.getWorkspaceName());
    if(this.newsletterPath == null){
      NewsletterTemplateHandler newsletterTemplateHandler = newsletterManagerService.getTemplateHandler();
      this.newsletterPath = newsletterTemplateHandler.getTemplate(NewsLetterUtil.getPortalName(), categoryConfig, null).getPath();
      newsletterEntryForm.addNew(true);
    }else{
      UIFormDateTimeInput dateTimeInput = newsletterEntryDialogSelector.getChild(UIFormDateTimeInput.class);
      Calendar calendar = dateTimeInput.getCalendar().getInstance();
      calendar.setTime(newsletterManagerService.getEntryHandler().getNewsletterEntryByPath(this.newsletterPath).getNewsletterSentDate());
      dateTimeInput.setCalendar(calendar);
      newsletterEntryForm.addNew(false);
    }
    newsletterEntryForm.setNodePath(newsletterPath);
    newsletterEntryForm.getChildren().clear();
    newsletterEntryForm.resetProperties();
    addChild(newsletterEntryForm);
    this.newsletterPath = null;
  }

  public NewsletterCategoryConfig getCategoryConfig() {
    return categoryConfig;
  }
  
  public void setCategoryConfig(NewsletterCategoryConfig categoryConfig) throws Exception {
    this.categoryConfig = categoryConfig;
    init();
  }
}
