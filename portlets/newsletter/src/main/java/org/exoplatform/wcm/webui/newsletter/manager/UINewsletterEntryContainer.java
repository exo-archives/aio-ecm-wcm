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

import java.util.GregorianCalendar;

import javax.jcr.Session;

import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.config.NewsletterManagerConfig;
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

  private boolean isAddNew = true;
  private String childPath;
  
  public UINewsletterEntryContainer() throws Exception {
    NewsletterManagerService newsletterManagerService = getApplicationComponent(NewsletterManagerService.class);
    addChild(UINewsletterEntryDialogSelector.class, null, null);
    UINewsletterEntryForm newsletterEntryForm = createUIComponent(UINewsletterEntryForm.class, null, null);
    newsletterEntryForm.setStoredLocation(newsletterManagerService.getRepositoryName(), newsletterManagerService.getWorkspaceName(), 
      "/sites content/live/portalName/ApplicationData/NewsletterApplication/Categories/category1/subscription1");
    newsletterEntryForm.addNew(isAddNew);
    newsletterEntryForm.setNodePath(childPath);
    newsletterEntryForm.resetProperties();
    addChild(newsletterEntryForm);
  }

  public boolean isAddNew() {
    return isAddNew;
  }
  
  public void setAddNew(boolean isAddNew) {
    this.isAddNew = isAddNew;
  }
  
  public String getChildPath() {
    return childPath;
  }

  public void setChildPath(String childPath) {
    this.childPath = childPath;
  }
  
  public void init(String categoryName, String subscriptionName, String newsletterName) throws Exception{
    UINewsletterEntryForm newsletterEntryForm = this.getChild(UINewsletterEntryForm.class);
    newsletterEntryForm.addNew(isAddNew);
    newsletterEntryForm.setNodePath(childPath);
    NewsletterManagerService newsletterManagerService = getApplicationComponent(NewsletterManagerService.class);
    NewsletterManagerConfig newsletterManagerConfig = 
                          newsletterManagerService.getEntryHandler()
                          .getNewsletterEntry(NewsLetterUtil.getPortalName(), categoryName, subscriptionName, newsletterName);
    UINewsletterEntryDialogSelector newsletterEntryDialogSelector = this.getChild(UINewsletterEntryDialogSelector.class);
    UIFormDateTimeInput  dateTimeInput = newsletterEntryDialogSelector.
                                              getChildById(UINewsletterEntryDialogSelector.NEWSLETTER_ENTRY_SEND_DATE);
    GregorianCalendar cal = new GregorianCalendar() ;
    cal.setTime(newsletterManagerConfig.getNewsletterSentDate()) ;
    dateTimeInput.setCalendar(cal);
    newsletterEntryForm.resetProperties();
  }
  
}
