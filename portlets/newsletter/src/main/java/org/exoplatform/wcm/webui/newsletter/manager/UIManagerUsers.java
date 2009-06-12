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

import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.config.NewsletterUserConfig;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterManageUserHandler;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.newsletter.UINewsletterConstant;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.config.annotation.EventConfig;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          ha.mai@exoplatform.com
 * Jun 10, 2009  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class ,
    template = "app:/groovy/webui/newsletter/NewsletterManager/UIManagerUsers.gtmpl",
    events = {
      @EventConfig(listeners = UIManagerUsers.BanOrUnBanUserActionListener.class),
      @EventConfig(listeners = UIManagerUsers.DeleteUserActionListener.class),
      @EventConfig(listeners = UIManagerUsers.CloseActionListener.class)
    }
)
public class UIManagerUsers extends UIForm implements UIPopupComponent{
  private NewsletterManageUserHandler managerUserHandler = null;
  private String categoryName ;
  private String subscriptionName;

  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
  
  public UIManagerUsers() throws Exception{
    NewsletterManagerService newsletterManagerService = getApplicationComponent(NewsletterManagerService.class);
    managerUserHandler = newsletterManagerService.getManageUserHandler();
    this.setActions(new String[]{"Close"});
  }
  
  public void setInfor(String categoryName, String subscriptionName){
    this.categoryName = categoryName;
    this.subscriptionName = subscriptionName;
  }

  private List<NewsletterUserConfig> getListUser(){
    List<NewsletterUserConfig> listUsers = new ArrayList<NewsletterUserConfig>();
    SessionProvider sessionProvider = NewsLetterUtil.getSesssionProvider();
    try{
      listUsers = managerUserHandler.getUsers(NewsLetterUtil.getPortalName(), categoryName, subscriptionName, sessionProvider);
    }catch(Exception ex){
      ex.printStackTrace();
    }
    sessionProvider.close();
    return listUsers;
  }
  
  static  public class BanOrUnBanUserActionListener extends EventListener<UIManagerUsers> {
    public void execute(Event<UIManagerUsers> event) throws Exception {
      UIManagerUsers managerUsers = event.getSource();
      String email = event.getRequestContext().getRequestParameter(OBJECTID);
      System.out.println("\n\n\n\n------------>Email:" + email);
      managerUsers.managerUserHandler.changeBanStatus(NewsLetterUtil.getPortalName(), email);
      event.getRequestContext().addUIComponentToUpdateByAjax(managerUsers) ;
    }
  }
  
  static  public class DeleteUserActionListener extends EventListener<UIManagerUsers> {
    public void execute(Event<UIManagerUsers> event) throws Exception {
      UIManagerUsers managerUsers = event.getSource();
      String email = event.getRequestContext().getRequestParameter(OBJECTID);
      System.out.println("\n\n\n\n------------>Email:" + email);
      managerUsers.managerUserHandler.delete(NewsLetterUtil.getPortalName(), email);
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
