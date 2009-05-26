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

  // Category nodetype
  public static final String CATEGORY_NODETYPE              = "exo:newsletterCategory";
  public static final String CATEGORY_PROPERTY_TITLE        = "exo:newsletterCategoryTitle";
  public static final String CATEGORY_PROPERTY_DESCRIPTION  = "exo:newsletterCategoryDescription";
  
  // Subscription nodetype
  public static final String SUBSCRIPTION_NODETYPE          = "exo:newsletterSubscription";
  public static final String SUBSCRIPTION_PROPERTY_USER     = "exo:newsletterSubscribedUser";
  
  // Entry nodetype
  public static final String ENTRY_NODETYPE                 = "exo:newsletterEntry";
  public static final String ENTRY_PROPERTY_TYPE            = "exo:newsletterEntryType";
  public static final String ENTRY_PROPERTY_DATE            = "exo:newsletterEntryDate";
  public static final String ENTRY_PROPERTY_STATUS          = "exo:newsletterEntryStatus";
  
  // User nodetype
  public static final String USER_NODETYPE                  = "exo:newsletterUser";
  public static final String USER_PROPERTY_MAIL             = "exo:newsletterUserMail";
  public static final String USER_PROPERTY_BANNED           = "exo:newsletterUserBanned";
  
  // Entry status
  public static final String STATUS_DRAFT                   = "draft";
  public static final String STATUS_AWAITING                = "awaiting";
  public static final String STATUS_SENT                    = "sent";

  // Newsletter application configuration
  public static final String BASE_PATH                      = "/collaboration/sites content/live/{portalName}/ApplicationData/NewsletterApplication";
 
  public static String generateCategoryPath(String portalName, String categoryParentPath, String categoryName) {
    if (categoryParentPath == null) categoryParentPath = "";
    if (categoryName == null) categoryName = "";
    return BASE_PATH.replaceAll("{portalName}", portalName) + categoryParentPath + categoryName;
  }
}
