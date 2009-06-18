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
import java.util.ResourceBundle;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterConstant;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.NewsletterUserInfor;
import org.exoplatform.services.wcm.newsletter.config.NewsletterUserConfig;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterCategoryHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterManageUserHandler;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.newsletter.UINewsletterConstant;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UITabPane;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.services.organization.User;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          ha.mai@exoplatform.com
 * Jun 10, 2009  
 */

@ComponentConfig(
    //lifecycle = UIFormLifecycle.class ,
    template = "app:/groovy/webui/newsletter/NewsletterManager/UIManagerUsers.gtmpl",
    events = {
      @EventConfig(listeners = UIManagerUsers.BanOrUnBanUserActionListener.class),
      @EventConfig(listeners = UIManagerUsers.DeleteUserActionListener.class),
      @EventConfig(listeners = UIManagerUsers.AddAdministratorActionListener.class),
      @EventConfig(listeners = UIManagerUsers.DeleteAdministratorActionListener.class),
      @EventConfig(listeners = UIManagerUsers.CloseActionListener.class)
    }
)
public class UIManagerUsers extends UITabPane implements UIPopupComponent{
  private String[] TITLE_  = {"Mail", "isBanned"};
  private String[] ACTIONS_ = {"Edit", "BanOrUnBanUser", "DeleteUser"};
  private String[] MEMBER_TITLE_ = {"UserName", "FirstName", "LastName", "Email", "Role"};
  private String[] MEMBER_ACTIONS_ = {"Edit", "AddAdministrator", "DeleteAdministrator"};
  private NewsletterManageUserHandler managerUserHandler = null;
  private String categoryName ;
  private String subscriptionName;
  private String UIGRID_MANAGER_USERS = "UIManagerUsers";
  private String UIGRID_MANAGER_MODERATOR = "UIManagerModerator";
  private boolean isViewTab = false;
  private List<String> listModerator = new ArrayList<String>();
  private String[] permissions ;

  private void getListPublicUser(){
    try{
      UIGrid uiGrid = getChildById(UIGRID_MANAGER_USERS);
      ObjectPageList objPageList = 
                      new ObjectPageList(managerUserHandler.getUsers(NewsLetterUtil.getPortalName(), categoryName, subscriptionName), 5);
      uiGrid.getUIPageIterator().setPageList(objPageList);
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }
  
  private void updateListUser() throws Exception{
    // get all administrator of newsletter
    List<String> listAdministrator = managerUserHandler.getAllAdministrator(NewsLetterUtil.getPortalName());
    
    UIGrid uiGrid = this.getChildById(UIGRID_MANAGER_MODERATOR);
    UIPageIterator uiIterator_ = uiGrid.getUIPageIterator();
    uiIterator_.setPageList(null);
    // get list of users
    OrganizationService service = getApplicationComponent(OrganizationService.class) ;
    service.getUserHandler().findUsers(new Query()).getAll();
    List<NewsletterUserInfor> userInfors = new ArrayList<NewsletterUserInfor>();
    User user;
    NewsletterUserInfor userInfor;
    for(Object obj : service.getUserHandler().findUsers(new Query()).getAll()){
      user = (User) obj;
      userInfor = new NewsletterUserInfor();
      userInfor.setEmail(user.getEmail());
      userInfor.setFirstName(user.getFirstName());
      userInfor.setLastName(user.getLastName());
      userInfor.setUserName(user.getUserName());
      if(listAdministrator.contains(user.getUserName())) userInfor.setRole(permissions[0]);
      else if(listModerator.contains(userInfor.getUserName())) userInfor.setRole(permissions[1]);
      else userInfor.setRole(permissions[2]);
      userInfors.add(userInfor);
    }
    // set all user into grid
    ObjectPageList objPageList = new ObjectPageList(userInfors, 5) ;
    uiIterator_.setPageList(objPageList) ;
    
    this.setSelectedTab(UIGRID_MANAGER_USERS);
  }

  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
  
  public UIManagerUsers() throws Exception{
    NewsletterManagerService newsletterManagerService = getApplicationComponent(NewsletterManagerService.class);
    managerUserHandler = newsletterManagerService.getManageUserHandler();
    NewsletterCategoryHandler categoryHandler = newsletterManagerService.getCategoryHandler();
    // get list of moderator
    for(NewsletterCategoryConfig categoryConfig : categoryHandler.getListCategories(NewsLetterUtil.getPortalName())){
      for(String str : categoryConfig.getModerator().split(",")){
        if(!listModerator.contains(str)) listModerator.add(str);
      }
    }
    // get name of permission from resource bundle
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    ResourceBundle res = context.getApplicationResourceBundle() ;
    permissions = new String[]{res.getString("UIManagerUsers.role.Administrator"),
                                res.getString("UIManagerUsers.role.Moderator"),
                                res.getString("UIManagerUsers.role.Redactor")};
    // add public user grid
    UIGrid uiGrid = createUIComponent(UIGrid.class, null, UIGRID_MANAGER_USERS);
    uiGrid.getUIPageIterator().setId("UsersIterator");
    uiGrid.configure("Mail", TITLE_, ACTIONS_);
    addChild(uiGrid);
  }
  
  public void setInfor(String categoryName, String subscriptionName){
    this.categoryName = categoryName;
    this.subscriptionName = subscriptionName;
    if(categoryName == null){
      // add public user grid
      try{
        // set all user into grid
        UIGrid uiGrid = createUIComponent(UIGrid.class, null, UIGRID_MANAGER_MODERATOR);
        UIPageIterator uiIterator_ = uiGrid.getUIPageIterator();
        uiIterator_.setId("ModeratorsIterator") ;
        uiGrid.configure("UserName", MEMBER_TITLE_, MEMBER_ACTIONS_);
        addChild(uiGrid);
        isViewTab = true;
        this.setSelectedTab(UIGRID_MANAGER_USERS);
        
        updateListUser();
      }catch(Exception ex){
        ex.printStackTrace();
      }
    }
  }
  
  static  public class BanOrUnBanUserActionListener extends EventListener<UIManagerUsers> {
    public void execute(Event<UIManagerUsers> event) throws Exception {
      UIManagerUsers managerUsers = event.getSource();
      String email = event.getRequestContext().getRequestParameter(OBJECTID);
      managerUsers.managerUserHandler.changeBanStatus(NewsLetterUtil.getPortalName(), email);
      event.getRequestContext().addUIComponentToUpdateByAjax(managerUsers) ;
    }
  }
  
  static  public class DeleteUserActionListener extends EventListener<UIManagerUsers> {
    public void execute(Event<UIManagerUsers> event) throws Exception {
      UIManagerUsers managerUsers = event.getSource();
      String email = event.getRequestContext().getRequestParameter(OBJECTID);
      managerUsers.managerUserHandler.delete(NewsLetterUtil.getPortalName(), email);
      event.getRequestContext().addUIComponentToUpdateByAjax(managerUsers) ;
    }
  }
  
  static  public class AddAdministratorActionListener extends EventListener<UIManagerUsers> {
    public void execute(Event<UIManagerUsers> event) throws Exception {
      UIManagerUsers managerUsers = event.getSource();
      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      System.out.println("\n\n\n\n------------------------->userId:" + userId);
      managerUsers.managerUserHandler.addAdministrator(NewsLetterUtil.getPortalName(), userId);
      managerUsers.updateListUser();
      event.getRequestContext().addUIComponentToUpdateByAjax(managerUsers.getChildById(managerUsers.UIGRID_MANAGER_MODERATOR)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(managerUsers) ;
    }
  }
  
  static  public class DeleteAdministratorActionListener extends EventListener<UIManagerUsers> {
    public void execute(Event<UIManagerUsers> event) throws Exception {
      UIManagerUsers managerUsers = event.getSource();
      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      System.out.println("\n\n\n\n------------------------->userId:" + userId);
      managerUsers.managerUserHandler.deleteUserAddministrator(NewsLetterUtil.getPortalName(), userId);
      managerUsers.updateListUser();
      event.getRequestContext().addUIComponentToUpdateByAjax(managerUsers.getChildById(managerUsers.UIGRID_MANAGER_MODERATOR)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(managerUsers) ;
    }
  }
  
  static  public class CloseActionListener extends EventListener<UIManagerUsers> {
    public void execute(Event<UIManagerUsers> event) throws Exception {
      UIManagerUsers managerUsers = event.getSource();
      UIPopupContainer popupContainer = managerUsers.getAncestorOfType(UIPopupContainer.class);
      Utils.closePopupWindow(popupContainer, UINewsletterConstant.MANAGER_USERS_POPUP_WINDOW);
    }
  }
}
