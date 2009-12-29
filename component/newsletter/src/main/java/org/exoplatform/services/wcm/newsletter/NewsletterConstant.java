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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 * chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * May 21, 2009
 */
public class NewsletterConstant {
  
  // Categories property
  /** The Constant CATEGORIES_PROPERTY_ADDMINISTRATOR. */
  public static final String  CATEGORIES_PROPERTY_ADDMINISTRATOR = "exo:newsletteraddministrator";

  /** The Constant CATEGORY_NODETYPE. */
  public static final String CATEGORY_NODETYPE                   = "exo:newsletterCategory";

  /** The Constant CATEGORY_PROPERTY_TITLE. */
  public static final String CATEGORY_PROPERTY_TITLE             = "exo:newsletterCategoryTitle";

  /** The Constant CATEGORY_PROPERTY_DESCRIPTION. */
  public static final String CATEGORY_PROPERTY_DESCRIPTION       = "exo:newsletterCategoryDescription";

  // Subscription nodetype
  /** The Constant SUBSCRIPTION_NODETYPE. */
  public static final String SUBSCRIPTION_NODETYPE               = "exo:newsletterSubscription";

  /** The Constant SUBSCRIPTION_PROPERTY_USER. */
  public static final String SUBSCRIPTION_PROPERTY_USER          = "exo:newsletterSubscribedUser";

  /** The Constant SUBSCRIPTION_PROPERTY_TITLE. */
  public static final String SUBSCRIPTION_PROPERTY_TITLE         = "exo:newsletterSubscriptionTitle";

  /** The Constant SUBSCRIPTION_PROPERTY_DECRIPTION. */
  public static final String SUBSCRIPTION_PROPERTY_DECRIPTION    = "exo:newsletterSubscriptionDecription";

  /** The Constant SUBSCRIPTION_PROPERTY_CATEGORY_NAME. */
  public static final String SUBSCRIPTION_PROPERTY_CATEGORY_NAME = "exo:newsletterSubscriptionCategoryName";

  // Entry nodetype
  /** The Constant ENTRY_NODETYPE. */
  public static final String ENTRY_NODETYPE                      = "exo:newsletterEntry";

  /** The Constant ENTRY_PROPERTY_TYPE. */
  public static final String ENTRY_PROPERTY_TYPE                 = "exo:newsletterEntryType";

  /** The Constant ENTRY_PROPERTY_DATE. */
  public static final String ENTRY_PROPERTY_DATE                 = "exo:newsletterEntryDate";

  /** The Constant ENTRY_PROPERTY_STATUS. */
  public static final String ENTRY_PROPERTY_STATUS               = "exo:newsletterEntryStatus";

  /** The Constant ENTRY_PROPERTY_SUBSCRIPTION_NAME. */
  public static final String ENTRY_PROPERTY_SUBSCRIPTION_NAME    = "exo:newsletterEntrySubscriptionName";

  /** The Constant ENTRY_PROPERTY_CATEGORY_NAME. */
  public static final String ENTRY_PROPERTY_CATEGORY_NAME        = "exo:newsletterEntryCategoryName";

  /** The Constant ENTRY_PROPERTY_CONTENT_MAIN. */
  public static final String ENTRY_PROPERTY_CONTENT_MAIN         = "exo:newsletterEntryContentMain";

  /** The Constant ENTRY_PROPERTY_NAME. */
  public static final String ENTRY_PROPERTY_NAME                 = "exo:newsletterEntryName";
  
  /** The Constant ENTRY_PROPERTY_TITLE. */
  public static final String ENTRY_PROPERTY_TITLE                = "exo:newsletterEntryTitle";

  // User nodetype
  /** The Constant USER_NODETYPE. */
  public static final String USER_NODETYPE                       = "exo:newsletterUser";
  
  /** The Constant USER_PROPERTY_MAIL. */
  public static final String USER_PROPERTY_MAIL                  = "exo:newsletterUserMail";
  
  /** The Constant USER_PROPERTY_BANNED. */
  public static final String USER_PROPERTY_BANNED                = "exo:newsletterUserBanned";
  
  /** The Constant USER_PROPERTY_VALIDATION_CODE. */
  public static final String USER_PROPERTY_VALIDATION_CODE       = "exo:newsletterUserValidationCode";
  
  public static final String USER_PROPERTY_IS_CONFIRM            = "exo:newsletterUserConfirm";
  
  // Entry status
  /** The Constant STATUS_DRAFT. */
  public static final String STATUS_DRAFT                        = "draft";

  /** The Constant STATUS_AWAITING. */
  public static final String STATUS_AWAITING                     = "awaiting";

  /** The Constant STATUS_SENT. */
  public static final String STATUS_SENT                         = "sent";

  // Newsletter application configuration
  /** The Constant PORTAL_NAME. */
  public static final String PORTAL_NAME                         = "portalName";
  
  /** The TEMPLATE base path. */
  public static String       TEMPLATE_BASE_PATH                  = "/sites content/live/" + PORTAL_NAME + "/ApplicationData/NewsletterApplication/DefaultTemplates";
  
  /** The CATEGORY base path. */
  public static String       CATEGORY_BASE_PATH                  = "/sites content/live/" + PORTAL_NAME + "/ApplicationData/NewsletterApplication/Categories";

  /** The USER base path. */
  public static String       USER_BASE_PATH                      = "/sites content/live/" + PORTAL_NAME + "/ApplicationData/NewsletterApplication/Users";
  
  
  
  /**
   * Generate default template path.
   * 
   * @param portalName the portal name
   * 
   * @return the string
   */
  public static String generateDefaultTemplatePath(String portalName) {
    return TEMPLATE_BASE_PATH.replaceAll(PORTAL_NAME, portalName);
  }
  
  /**
   * Generate category template base path.
   * 
   * @param portalName the portal name
   * @param categoryName the category name
   * 
   * @return the string
   */
  public static String generateCategoryTemplateBasePath(String portalName, String categoryName) {
    return generateCategoryPath(portalName).concat("/" + categoryName).concat("/Templates");
  }
  
  /**
   * Generate category path.
   * 
   * @param portalName the portal name
   * 
   * @return the string
   */
  public static String generateCategoryPath(String portalName) {
    return CATEGORY_BASE_PATH.replaceAll(PORTAL_NAME, portalName);
  }
  
  /**
   * Generate user path.
   * 
   * @param portalName the portal name
   * 
   * @return the string
   */
  public static String generateUserPath(String portalName) {
    return USER_BASE_PATH.replaceAll(PORTAL_NAME, portalName);
  }

  /**
   * Generate subscription path.
   * 
   * @param portalName the portal name
   * @param categoryName the category name
   * @param subscriptionName the subscription name
   * 
   * @return the string
   */
  public static String generateSubscriptionPath(String portalName, String categoryName, String subscriptionName) {
    return generateCategoryPath(portalName).concat("/" + categoryName).concat("/" + subscriptionName);
  }
  
  /**
   * Generate newsletter path.
   * 
   * @param portalName the portal name
   * @param categoryName the category name
   * @param subscriptionName the subscription name
   * @param newsletterName the newsletter name
   * 
   * @return the string
   */
  public static String generateNewsletterPath(String portalName, String categoryName, String subscriptionName, String newsletterName) {
    return generateSubscriptionPath(portalName, categoryName, subscriptionName).concat("/" + newsletterName);
  }
  
  /**
   * Get all permission of node, only get users, groups or membership who have all permissions per this node
   * @param node        Node which you want to get
   * @return            List of permission include: users, groups or membership
   * @throws Exception  The exception
   */
  @SuppressWarnings("unchecked")
  public static List<String> getAllPermissionOfNode(Node node) throws Exception{
    ExtendedNode webContent = (ExtendedNode)node;
    Iterator permissionIterator = webContent.getACL().getPermissionEntries().iterator();
    String currentIdentity;
    AccessControlEntry accessControlEntry;
    Map<String, Integer> mapPermission = new HashMap<String, Integer>();
    while (permissionIterator.hasNext()) {
      accessControlEntry = (AccessControlEntry) permissionIterator.next();
      currentIdentity = accessControlEntry.getIdentity();
      if(mapPermission.containsKey(currentIdentity)){
        mapPermission.put(currentIdentity, mapPermission.get(currentIdentity) + 1);
      } else {
        mapPermission.put(currentIdentity, 1);
      }
    }
    int size = PermissionType.ALL.length;
    List<String> listPermission = new ArrayList<String>();
    for(String key : mapPermission.keySet().toArray(new String[]{})){
      if(mapPermission.get(key) == size)
        listPermission.add(key);
    }
    return listPermission;
  }
  
  /**
   * Get all groups and membership where user is member
   * @param userId      User name
   * @return            List groups and memberships of <code>userId</code>
   * @throws Exception  The exception
   */
  public static List<String> getAllGroupAndMembershipOfCurrentUser(String userId) throws Exception{
    List<String> userGroupMembership = new ArrayList<String>();
    userGroupMembership.add(userId);
    String value = "";
    String id = "";
    Membership membership = null;
    OrganizationService organizationService_ = WCMCoreUtils.getService(OrganizationService.class);
    try{
    for(Object object : organizationService_.getMembershipHandler().findMembershipsByUser(userId).toArray()){
      id = object.toString();
      id = id.replace("Membership[", "").replace("]", "");
      membership = organizationService_.getMembershipHandler().findMembership(id);
      value = membership.getGroupId();
      userGroupMembership.add(value);
      value = membership.getMembershipType() + ":" + value;
      userGroupMembership.add(value);
    }
    }catch(Exception ex){ }
    return userGroupMembership;
  }
  
  /**
   * Check permission by comparing two lists permission. If in <code>list1</code> have any element of <code>list2</code>
   * will return <code>true</code> and return false if don't have any element which contained in <code>list1</code> but 
   * don't contained in <code>list2</code>
   * @param list1   List of permission
   * @param list2   List of Permission
   * @return        return <code>true</code> and return false if don't have any element which contained in <code>list1</code> but 
   *                don't contained in <code>list2</code>
   */
  public static boolean havePermission(List<String> list1, List<String> list2){
    for(String str : list1){
      if(list2.contains(str)) return true;
    }
    return false;
  }
  
  /**
   * Add all permissions of category into subscriptions of this category. All moderators after added into
   * category will be updated into subscription. All users will have permissions which you wanted in <code>permissions</code>
   * @param categoryNode  Category Node which have just updated
   * @param userIds       Arrays of users
   * @param permissions   Arrays of permission
   * @throws Exception    The exception
   */
  public static void addPermissionsFromCateToSubs(Node categoryNode, String[] userIds, String[] permissions) throws Exception{
    Node subscriptionNode;
    ExtendedNode extendSubscriptionNode;
    for(NodeIterator ni = categoryNode.getNodes(); ni.hasNext();){
      subscriptionNode = ni.nextNode();
      if(subscriptionNode.isNodeType(NewsletterConstant.SUBSCRIPTION_NODETYPE)){
        extendSubscriptionNode = ExtendedNode.class.cast(subscriptionNode);
        if (extendSubscriptionNode.canAddMixin("exo:privilegeable") || extendSubscriptionNode.isNodeType("exo:privilegeable")) {
          if(extendSubscriptionNode.canAddMixin("exo:privilegeable"))
            extendSubscriptionNode.addMixin("exo:privilegeable");
          for(String userId : userIds){
            extendSubscriptionNode.setPermission(userId, permissions);
          }
        }
      }
    }
  }
}
