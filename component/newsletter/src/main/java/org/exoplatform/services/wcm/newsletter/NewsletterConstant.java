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

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * May 21, 2009  
 */
public class NewsletterConstant {
  
  public static final String PORTAL_NAME = "portalName";
  
  // Categories property
  public static final String  CATEGORIES_PROPERTY_ADDMINISTRATOR = "exo:newsletteraddministrator";
  // Category nodetype
  public static final String CATEGORY_NODETYPE                   = "exo:newsletterCategory";

  public static final String CATEGORY_PROPERTY_TITLE             = "exo:newsletterCategoryTitle";

  public static final String CATEGORY_PROPERTY_DESCRIPTION       = "exo:newsletterCategoryDescription";

  // Subscription nodetype
  public static final String SUBSCRIPTION_NODETYPE               = "exo:newsletterSubscription";

  public static final String SUBSCRIPTION_PROPERTY_USER          = "exo:newsletterSubscribedUser";

  public static final String SUBSCRIPTION_PROPERTY_TITLE         = "exo:newsletterSubscriptionTitle";

  public static final String SUBSCRIPTION_PROPERTY_DECRIPTION    = "exo:newsletterSubscriptionDecription";

  public static final String SUBSCRIPTION_PROPERTY_CATEGORY_NAME = "exo:newsletterSubscriptionCategoryName";

  // Entry nodetype
  public static final String ENTRY_NODETYPE                      = "exo:newsletterEntry";

  public static final String ENTRY_PROPERTY_TYPE                 = "exo:newsletterEntryType";

  public static final String ENTRY_PROPERTY_DATE                 = "exo:newsletterEntryDate";

  public static final String ENTRY_PROPERTY_STATUS               = "exo:newsletterEntryStatus";

  public static final String ENTRY_PROPERTY_SUBSCRIPTION_NAME    = "exo:newsletterEntrySubscriptionName";

  public static final String ENTRY_PROPERTY_CATEGORY_NAME        = "exo:newsletterEntryCategoryName";

  public static final String ENTRY_PROPERTY_CONTENT_MAIN         = "exo:newsletterEntryContentMain";

  public static final String ENTRY_PROPERTY_NAME                 = "exo:newsletterEntryName";

  // User nodetype
  public static final String USER_NODETYPE                       = "exo:newsletterUser";
  public static final String USER_PROPERTY_MAIL                  = "exo:newsletterUserMail";
  public static final String USER_PROPERTY_BANNED                = "exo:newsletterUserBanned";
  public static final String USER_PROPERTY_VALIDATION_CODE       = "exo:newsletterUserValidationCode";
  
  // Entry status
  public static final String STATUS_DRAFT                        = "draft";

  public static final String STATUS_AWAITING                     = "awaiting";

  public static final String STATUS_SENT                         = "sent";

  // Newsletter application configuration
  public static String       CATEGORY_BASE_PATH                  = "/sites content/live/portalName/ApplicationData/NewsletterApplication/Categories";

  public static String       USER_BASE_PATH                      = "/sites content/live/portalName/ApplicationData/NewsletterApplication/Users";
  
  public static String generateCategoryPath(String portalName) {
    return CATEGORY_BASE_PATH.replaceAll(PORTAL_NAME, portalName);
  }
  
  public static String generateUserPath(String portalName) {
    return USER_BASE_PATH.replaceAll(PORTAL_NAME, portalName);
  }
}
