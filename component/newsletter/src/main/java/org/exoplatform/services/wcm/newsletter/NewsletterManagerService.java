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
package org.exoplatform.services.wcm.newsletter;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterCategoryHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterEntryHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterManageUserHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterPublicUserHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterSubscriptionHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterTemplateHandler;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * May 21, 2009  
 */
public class NewsletterManagerService {
  
  private NewsletterCategoryHandler categoryHandler;
  private NewsletterSubscriptionHandler subscriptionHandler;
  private NewsletterEntryHandler entryHandler;
  private NewsletterTemplateHandler templateHandler;
  private NewsletterManageUserHandler manageUserHandler;
  private NewsletterPublicUserHandler publicUserHandler;
  private static Log log = ExoLogger.getLogger(NewsletterManagerService.class);

  public NewsletterManagerService(InitParams initParams) {
    log.info("Starting NewsletterManagerService ... ");
    String repository = initParams.getValueParam("repository").getValue();
    String workspace = initParams.getValueParam("workspace").getValue();
    
    categoryHandler = new NewsletterCategoryHandler(repository, workspace);
    subscriptionHandler = new NewsletterSubscriptionHandler(repository, workspace);
    entryHandler = new NewsletterEntryHandler(repository, workspace);
    templateHandler = new NewsletterTemplateHandler(repository, workspace);
    manageUserHandler = new NewsletterManageUserHandler(repository, workspace);
    publicUserHandler = new NewsletterPublicUserHandler(repository, workspace);
  }          
  
  public NewsletterCategoryHandler getCategoryHandler() {
    return categoryHandler;
  }
  
  public NewsletterSubscriptionHandler getSubscriptionHandler() {
    return subscriptionHandler;
  }
  
  public NewsletterEntryHandler getEntryHandler() {
    return entryHandler;
  }
  
  public NewsletterTemplateHandler getTemplateHandler() {
    return templateHandler;
  }
  
  public NewsletterManageUserHandler getManageUserHandler() {
    return manageUserHandler;
  }
  
  public NewsletterPublicUserHandler getPublicUserHandler() {
    return publicUserHandler;
  }
  
}
