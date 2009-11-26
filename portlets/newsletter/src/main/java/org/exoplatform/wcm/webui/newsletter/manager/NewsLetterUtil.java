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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.webui.core.UIComponent;

/**
 * The Class NewsLetterUtil.
 */
public class NewsLetterUtil {
  /**
   * Get all group and membership of current user
   * @return
   * @throws Exception
   */
  public static List<String> getAllGroupAndMembershipOfCurrentUser() throws Exception{
    String userId = getCurrentUser();
    List<String> userGroupMembership = new ArrayList<String>();
    userGroupMembership.add(userId);
    String value = "";
    String id = "";
    Membership membership = null;
    OrganizationService organizationService_ = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
    for(Object object : organizationService_.getMembershipHandler().findMembershipsByUser(userId).toArray()){
      id = object.toString();
      id = id.replace("Membership[", "").replace("]", "");
      membership = organizationService_.getMembershipHandler().findMembership(id);
      value = membership.getGroupId();
      userGroupMembership.add(value);
      value = membership.getMembershipType() + ":" + value;
      userGroupMembership.add(value);
    }
    return userGroupMembership;
  }
  
  /**
   * Update access  permissions 
   * @param accessPermissions   list of user will be set access permission
   * @param component           UIComponent
   * @throws Exception          The exception
   */
  public static void updateAccessPermission(String[] accessPermissions, UIComponent component) throws Exception{
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
    page.setAccessPermissions(listAccess.toArray(new String[]{}));
    userService.update(page);
    
  }
	
	/**
	 * Gets the portal name.
	 * 
	 * @return the portal name
	 */
	public static String getPortalName() {
		UIPortal portal = Util.getUIPortal();
		return portal.getName();  
	}
	
	/**
	 * Generate link.
	 * 
	 * @param url the url
	 * 
	 * @return the string
	 */
	public static String generateLink(String url){
    String link = url.replaceFirst("Subcribe", "ConfirmUserCode")
                      .replaceFirst("UINewsletterViewerForm", "UINewsletterViewerPortlet")
                      .replaceAll("&amp;", "&");
    String selectedNode = Util.getUIPortal().getSelectedNode().getUri() ;
    String portalName = "/" + Util.getUIPortal().getName() ;
    if(link.indexOf(portalName) > 0) {
      if(link.indexOf(portalName + "/" + selectedNode) < 0){
        link = link.replaceFirst(portalName, portalName + "/" + selectedNode) ;
      }                 
    } 
    PortalRequestContext portalContext = Util.getPortalRequestContext();
    url = portalContext.getRequest().getRequestURL().toString();
    url = url.replaceFirst("http://", "") ;
    url = url.substring(0, url.indexOf("/")) ;
    link = "http://" + url + link;
    return link.replaceFirst("private", "public");
	}
	
	/**
	 * Get current user
	 * @return
	 * @throws Exception
	 */
	static public String getCurrentUser() throws Exception {
    return Util.getPortalRequestContext().getRemoteUser();
  }
	
	/**
	 * Check permission of current user with category.
	 * @param categoryConfig   The category which you want check
	 * @return                 <code>True</code> if current user is moderator of the category and <code>False</code> if not
	 * @throws Exception       The Exception
	 */
  public static boolean isModeratorOfCategory(NewsletterCategoryConfig categoryConfig) throws Exception{
    List<String> listModerators = Arrays.asList(categoryConfig.getModerator().split(","));
    if(listModerators.contains("any")) return true;
    else{
      for(String str : getAllGroupAndMembershipOfCurrentUser()){
        if(listModerators.contains(str)){
          return true;
        }
      }
    }
    return false;
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
    OrganizationService organizationService = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
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
    return users ;
  }
}
