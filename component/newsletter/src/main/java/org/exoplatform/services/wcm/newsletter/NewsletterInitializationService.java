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

import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterCategoryHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterSubscriptionHandler;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * May 21, 2009  
 */
public class NewsletterInitializationService implements Startable {
  
  private List<NewsletterCategoryConfig> categoryConfigs;
  private NewsletterManagerService managerService;
  private static Log log = ExoLogger.getLogger(NewsletterInitializationService.class);
  
  @SuppressWarnings("unchecked")
  public NewsletterInitializationService(InitParams initParams, NewsletterManagerService managerService) {
    categoryConfigs = initParams.getObjectParamValues(NewsletterCategoryConfig.class);
    this.managerService = managerService;
  }

  public void start() {
    log.info("Starting NewsletterInitializationService ... ");
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    try {
      /*NewsletterCategoryHandler categoryHandler = managerService.getCategoryHandler();
      NewsletterSubscriptionHandler subscriptionHandler = managerService.getSubscriptionHandler();
      for (NewsletterCategoryConfig categoryConfig : categoryConfigs) {
        // TODO: Needs to change
        categoryHandler.add("", categoryConfig, sessionProvider);
        for (NewsletterSubscriptionConfig subscriptionConfig : categoryConfig.getSubscriptions()) {
          subscriptionHandler.add(sessionProvider);
        }
      }*/
    } catch (Throwable e) {
      
    } finally {
      sessionProvider.close();
    }
  }

  public void stop() {
    log.info("Stopping NewsletterInitializationService ... ");
  }
  
}