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
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.NewsletterUserInfor;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterCategoryHandler;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterManageUserHandler;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.newsletter.UINewsletterConstant;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UITabPane;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 * ha.mai@exoplatform.com
 * Jun 10, 2009
 */

@ComponentConfig(
    //lifecycle = UIFormLifecycle.class ,
    template = "app:/groovy/webui/newsletter/NewsletterManager/UIManagerUsers.gtmpl",
    events = {
      @EventConfig(listeners = UIManagerUsers.UnBanUserActionListener.class),
      @EventConfig(listeners = UIManagerUsers.BanUserActionListener.class),
      @EventConfig(listeners = UIManagerUsers.DeleteUserActionListener.class),
      @EventConfig(listeners = UIManagerUsers.AddAdministratorActionListener.class),
      @EventConfig(listeners = UIManagerUsers.DeleteAdministratorActionListener.class),
      @EventConfig(listeners = UIManagerUsers.CloseActionListener.class)
    }
)
public class UIManagerUsers extends UITabPane {
  
  /** The TITL e_. */
  private String[] TITLE_  = {"Mail", "isBanned"};
  
  /** The ACTION s_. */
  private String[] ACTIONS_ = {"BanUser", "UnBanUser", "DeleteUser"};
  
  /** The MEMBE r_ titl e_. */
  private String[] MEMBER_TITLE_ = {"UserName", "FirstName", "LastName", "Email", "Role"};

  /** The MEMBE r_ action s_. */
  private String[] MEMBER_ACTIONS_ = {"AddAdministrator", "DeleteAdministrator"};
  
  /** The manager user handler. */
  private NewsletterManageUserHandler managerUserHandler = null;
  
  /** The category name. */
  private String categoryName ;
  
  /** The subscription name. */
  private String subscriptionName;
  
  /** The UIGRI d_ manage r_ users. */
  private String UIGRID_MANAGER_USERS = "UIManagerUsers";
  
  /** The UIGRI d_ manage r_ moderator. */
  private String UIGRID_MANAGER_MODERATOR = "UIManagerModerator";
  
  /** The is view tab. */
  public boolean isViewTab = false;
  
  /** The list moderator. */
  private List<String> listModerator = new ArrayList<String>();
  
  /** The permissions. */
  private String[] permissions ;

  /**
   * Gets the list public user.
   * 
   * @return the list public user
   */
  public void getListPublicUser(){
    try{
      UIGrid uiGrid = getChildById(UIGRID_MANAGER_USERS);
      ObjectPageList objPageList = 
                      new ObjectPageList(managerUserHandler.getUsers(Utils.getSessionProvider(this), NewsLetterUtil.getPortalName(), categoryName, subscriptionName), 5);
      uiGrid.getUIPageIterator().setPageList(objPageList);
    }catch(Exception ex){
      Utils.createPopupMessage(this, "UIManagerUsers.msg.get-list-users", null, ApplicationMessage.ERROR);
    }
  }
  
  /**
   * Update list user.
   * 
   * @throws Exception the exception
   */
  private void updateListUser() throws Exception{
    // get all administrator of newsletter
    List<String> listAdministrator = managerUserHandler.getAllAdministrator(Utils.getSessionProvider(this), NewsLetterUtil.getPortalName());
    
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

  /**
   * Instantiates a new uI manager users.
   * 
   * @throws Exception the exception
   */
  public UIManagerUsers() throws Exception{
    NewsletterManagerService newsletterManagerService = getApplicationComponent(NewsletterManagerService.class);
    managerUserHandler = newsletterManagerService.getManageUserHandler();
    NewsletterCategoryHandler categoryHandler = newsletterManagerService.getCategoryHandler();
    // get list of moderator
    for(NewsletterCategoryConfig categoryConfig : categoryHandler.getListCategories(NewsLetterUtil.getPortalName(), Utils.getSessionProvider(this))){
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
  
  /**
   * Sets the infor.
   * 
   * @param categoryName the category name
   * @param subscriptionName the subscription name
   */
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
        Utils.createPopupMessage(this, "UIManagerUsers.msg.set-infor-users", null, ApplicationMessage.ERROR);
      }
    }
  }
  
  /**
   * The listener interface for receiving unBanUserAction events.
   * The class that is interested in processing a unBanUserAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addUnBanUserActionListener<code> method. When
   * the unBanUserAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see UnBanUserActionEvent
   */
  static  public class UnBanUserActionListener extends EventListener<UIManagerUsers> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIManagerUsers> event) throws Exception {
      UIManagerUsers managerUsers = event.getSource();
      String email = event.getRequestContext().getRequestParameter(OBJECTID);
      managerUsers.managerUserHandler.changeBanStatus(Utils.getSessionProvider(managerUsers), NewsLetterUtil.getPortalName(), email, false);
      event.getRequestContext().addUIComponentToUpdateByAjax(managerUsers) ;
    }
  }
  
  /**
   * The listener interface for receiving banUserAction events.
   * The class that is interested in processing a banUserAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addBanUserActionListener<code> method. When
   * the banUserAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see BanUserActionEvent
   */
  static  public class BanUserActionListener extends EventListener<UIManagerUsers> {
	  
  	/* (non-Javadoc)
  	 * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
  	 */
  	public void execute(Event<UIManagerUsers> event) throws Exception {
		  UIManagerUsers managerUsers = event.getSource();
		  String email = event.getRequestContext().getRequestParameter(OBJECTID);
		  managerUsers.managerUserHandler.changeBanStatus(Utils.getSessionProvider(managerUsers), NewsLetterUtil.getPortalName(), email, true);
		  event.getRequestContext().addUIComponentToUpdateByAjax(managerUsers) ;
	  }
  }
  
  /**
   * The listener interface for receiving deleteUserAction events.
   * The class that is interested in processing a deleteUserAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addDeleteUserActionListener<code> method. When
   * the deleteUserAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see DeleteUserActionEvent
   */
  static  public class DeleteUserActionListener extends EventListener<UIManagerUsers> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIManagerUsers> event) throws Exception {
      UIManagerUsers managerUsers = event.getSource();
      String email = event.getRequestContext().getRequestParameter(OBJECTID);
      managerUsers.managerUserHandler.delete(Utils.getSessionProvider(managerUsers), NewsLetterUtil.getPortalName(), email);
      event.getRequestContext().addUIComponentToUpdateByAjax(managerUsers) ;
    }
  }
  
  /**
   * The listener interface for receiving addAdministratorAction events.
   * The class that is interested in processing a addAdministratorAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addAddAdministratorActionListener<code> method. When
   * the addAdministratorAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see AddAdministratorActionEvent
   */
  static  public class AddAdministratorActionListener extends EventListener<UIManagerUsers> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIManagerUsers> event) throws Exception {
      UIManagerUsers managerUsers = event.getSource();
      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      managerUsers.managerUserHandler.addAdministrator(Utils.getSessionProvider(managerUsers), NewsLetterUtil.getPortalName(), userId);
      managerUsers.updateListUser();
      event.getRequestContext().addUIComponentToUpdateByAjax(managerUsers.getChildById(managerUsers.UIGRID_MANAGER_MODERATOR)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(managerUsers) ;
    }
  }
  
  /**
   * The listener interface for receiving deleteAdministratorAction events.
   * The class that is interested in processing a deleteAdministratorAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addDeleteAdministratorActionListener<code> method. When
   * the deleteAdministratorAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see DeleteAdministratorActionEvent
   */
  static  public class DeleteAdministratorActionListener extends EventListener<UIManagerUsers> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIManagerUsers> event) throws Exception {
      UIManagerUsers managerUsers = event.getSource();
      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      managerUsers.managerUserHandler.deleteUserAddministrator(Utils.getSessionProvider(managerUsers), NewsLetterUtil.getPortalName(), userId);
      managerUsers.updateListUser();
      event.getRequestContext().addUIComponentToUpdateByAjax(managerUsers.getChildById(managerUsers.UIGRID_MANAGER_MODERATOR)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(managerUsers) ;
    }
  }
  
  /**
   * The listener interface for receiving closeAction events.
   * The class that is interested in processing a closeAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addCloseActionListener<code> method. When
   * the closeAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see CloseActionEvent
   */
  static  public class CloseActionListener extends EventListener<UIManagerUsers> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIManagerUsers> event) throws Exception {
      UIManagerUsers managerUsers = event.getSource();
      Utils.closePopupWindow(managerUsers, UINewsletterConstant.MANAGER_USERS_POPUP_WINDOW);
    }
  }
}
