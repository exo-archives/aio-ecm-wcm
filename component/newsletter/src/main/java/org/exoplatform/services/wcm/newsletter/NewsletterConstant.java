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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.exoplatform.webui.core.UIComponent;

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
  
  private static UIComponent component;
  
  private static PortalContainer manager;
  
  public static PortalContainer getManager() {
    return PortalContainer.getInstance();
  }

  public static void setManager(PortalContainer manager) {
    NewsletterConstant.manager = manager;
  }

  private static Session session;
  
  public static Session getSession() {
    return session;
  }

  public static void setSession(Session session) {
    NewsletterConstant.session = session;
  }

  public static UIComponent getComponent() {
    return component;
  }

  public static void setComponent(UIComponent component) {
    NewsletterConstant.component = component;
  }

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
    OrganizationService organizationService = WCMCoreUtils.getService(OrganizationService.class);
    ((ComponentRequestLifecycle) organizationService).startRequest(manager);
    List<String> userMemberships = new ArrayList<String> ();
    userMemberships.add(userId);
    Collection<?> memberships = organizationService.getMembershipHandler().findMembershipsByUser(userId);
    if(memberships == null || memberships.size() < 0) return userMemberships;
    Object[] objects = memberships.toArray();
    for(int i = 0; i < objects.length; i ++ ){
      Membership membership = (Membership)objects[i];
      String role = membership.getMembershipType() + ":" + membership.getGroupId();
      userMemberships.add(role);     
    }
    ((ComponentRequestLifecycle) organizationService).endRequest(manager);
    return userMemberships;
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
  
  /**
   * Update access  permissions 
   * @param accessPermissions   list of user will be set access permission
   * @param component           UIComponent
   * @throws Exception          The exception
   */
  public static void updateAccessPermission(String[] accessPermissions, UIComponent component) throws Exception{
    NewsletterConstant.setComponent(component);
    UserPortalConfigService userService = (UserPortalConfigService)component.getApplicationComponent(UserPortalConfigService.class);
    Page page = userService.getPage(Util.getUIPortal().getSelectedNode().getPageReference());
    List<String> listAccess = new ArrayList<String>();
    WCMConfigurationService wcmConfigurationService = component.getApplicationComponent(WCMConfigurationService.class);
    String editSitePermission = Util.getUIPortal().getEditPermission();
    String redactorMembershipType = wcmConfigurationService.getRuntimeContextParam(WCMConfigurationService.REDACTOR_MEMBERSHIP_TYPE);
    String groupName = null;
    if (editSitePermission.indexOf(":") != -1) {
      groupName = editSitePermission.substring(editSitePermission.indexOf(":") + 1);
    }
    OrganizationService organizationService = component.getApplicationComponent(OrganizationService.class) ;
    ((ComponentRequestLifecycle) organizationService).startRequest(manager);
    MembershipHandler memberShipHandler = organizationService.getMembershipHandler();
    Group group = organizationService.getGroupHandler().findGroupById(groupName);
    MembershipType membershipType = organizationService.getMembershipTypeHandler().findMembershipType(redactorMembershipType);
    UserHandler userHandler = organizationService.getUserHandler();
    listAccess.addAll(Arrays.asList(page.getAccessPermissions()));
    List<String> userViewAdminToolBar = getUserPermission(new String[]{editSitePermission});
    for(String acc : accessPermissions){
      if(listAccess.contains(acc)) continue;
      // update view admintoolbar permission
      for(String uid : getUserPermission(new String[]{acc})){
          if(userViewAdminToolBar.contains(uid)) continue;
          userViewAdminToolBar.add(uid);
          memberShipHandler.linkMembership(userHandler.findUserByName(uid),
                                         group, membershipType, true);
      }
      // update access portlet permission
      listAccess.add(acc);
    }
    ((ComponentRequestLifecycle) organizationService).endRequest(manager);
    page.setAccessPermissions(listAccess.toArray(new String[]{}));
    userService.update(page);
  }
  
  /**
   * Remove access  permissions 
   * @param accessPermissions   list of user will be set access permission
   * @throws Exception          The exception
   */
  public static void removeAccessPermission(String[] removePermissions) {
    if(removePermissions == null) {
      return;
    }
    UserPortalConfigService userService = (UserPortalConfigService)component.getApplicationComponent(UserPortalConfigService.class);
    Page page;
    try {
      page = userService.getPage(Util.getUIPortal().getSelectedNode().getPageReference());
      List<String> listAccess = new ArrayList<String>();
      OrganizationService organizationService = component.getApplicationComponent(OrganizationService.class) ;
      ((ComponentRequestLifecycle) organizationService).startRequest(manager);
      MembershipHandler memberShipHandler = organizationService.getMembershipHandler();
      listAccess.addAll(Arrays.asList(page.getAccessPermissions()));
      for(String acc : removePermissions){
        if(listAccess.contains(acc)) {
          listAccess.remove(acc);
          memberShipHandler.removeMembershipByUser(acc, true);
        }
      }
      ((ComponentRequestLifecycle) organizationService).endRequest(manager);
      page.setAccessPermissions(listAccess.toArray(new String[]{}));
      userService.update(page);
    } catch (Exception e) {
      return;
    }
  }
  
  /**
   * Get moderator in user,group,membership become list user
   * 
   * @param userGroupMembership is string user input to interface
   * @return list users
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public static List<String> getUserPermission(String[] userGroupMembership) throws Exception {
    List<String> users = new ArrayList<String> () ;
    if(userGroupMembership == null || userGroupMembership.length <= 0 || 
        (userGroupMembership.length == 1 && userGroupMembership[0].equals(" "))) return users ; 
    OrganizationService organizationService = WCMCoreUtils.getService(OrganizationService.class);
    ((ComponentRequestLifecycle) organizationService).startRequest(manager);
    for(String str : userGroupMembership) {
      str = str.trim();
      if(str.indexOf("/") >= 0) {
        if(str.indexOf(":") >= 0) { //membership
          String[] array = str.split(":") ;
          List<User> userList = organizationService.getUserHandler().findUsersByGroup(array[1]).getAll() ;
          if(array[0].length() > 1){
            for(User user: userList) {
              if(!users.contains(user.getUserName())){
                Collection<Membership> memberships = organizationService.getMembershipHandler().findMembershipsByUser(user.getUserName()) ;
                for(Membership member : memberships){
                  if(member.getMembershipType().equals(array[0])) {
                    users.add(user.getUserName()) ;
                    break ;
                  }
                }           
              }
            }
          }else {
            if(array[0].charAt(0)== 42) {
              for(User user: userList) {
                if(!users.contains(user.getUserName())){
                  users.add(user.getUserName()) ;
                }
              }
            }
          }
        }else { //group
          List<User> userList = organizationService.getUserHandler().findUsersByGroup(str).getAll() ;
          for(User user: userList) {
            if(!users.contains(user.getUserName())){
              users.add(user.getUserName()) ;
            }
          }
        }
      }else {//user
        if(!users.contains(str)){
          users.add(str) ;
        }
      }
    }
    ((ComponentRequestLifecycle) organizationService).endRequest(manager);
    return users ;
  }
  
  public static NodeIterator getAllCategories(Session session) throws Exception {
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    String sqlQuery = "Select * from " + NewsletterConstant.CATEGORY_NODETYPE;
    Query query = queryManager.createQuery(sqlQuery, Query.SQL);
    QueryResult queryResult = query.execute();
    return queryResult.getNodes();
  }
  
  /**
   * Get all redactors in all subscriptions of newsletter
   * @param portalName        name of portal
   * @param sessionProvider   The SessionProvider
   * @return                  List of redactor
   * @throws Exception        The exception
   */
  public static List<String> getAllRedactor(String portalName, Session session)throws Exception{
    List<String> list = new ArrayList<String>();
    String path = NewsletterConstant.generateCategoryPath(portalName);
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    String sqlQuery = "select * from " + NewsletterConstant.SUBSCRIPTION_NODETYPE + 
                      " where jcr:path LIKE '" + path + "[%]/%'";
    Query query = queryManager.createQuery(sqlQuery, Query.SQL);
    QueryResult queryResult = query.execute();
    Node subNode;
    for(NodeIterator nodeIterator = queryResult.getNodes(); nodeIterator.hasNext();){
      subNode = nodeIterator.nextNode();
      for(String str : NewsletterConstant.getAllPermissionOfNode(subNode)){
        if(list.contains(str)) continue;
        list.add(str);
      }
    }
    return list;
  }
  
  public static List<String> removePermission(Node subscriptionNode,
                                        Node categoryNode,
                                        List<String> candidateRemove,
                                        boolean isAddNew,
                                        String portalName,
                                        Session session) {
    if(candidateRemove == null) candidateRemove = new ArrayList<String>();
    try {
     NodeIterator categoriesIterator = NewsletterConstant.getAllCategories(session);
     List<String> allRedactor = NewsletterConstant.getAllRedactor(portalName, session);
     List<String> allpermissions = null;
     while(categoriesIterator.hasNext()) {
       allpermissions = NewsletterConstant.getAllPermissionOfNode(categoriesIterator.nextNode());
       for(String permission : allpermissions) {
         if(candidateRemove.isEmpty()) break;
         if(candidateRemove.contains(permission)) {
           candidateRemove.remove(permission);
         } 
       }
     }
     for(String redactor : allRedactor) {
       if(candidateRemove.isEmpty()) break;
       if(candidateRemove.contains(redactor)) {
         candidateRemove.remove(redactor);
       }
     }
    } catch (Exception e) {
      return new ArrayList<String>();
    }
    return candidateRemove;
  }
  
  public static List<String> convertValuesToArray(Value[] values){
    List<String> listString = new ArrayList<String>();
    for(Value value : values){
      try {
        listString.add(value.getString());
      }catch (Exception e) {
        return null;
      }
    }
    return listString;
  }
}
