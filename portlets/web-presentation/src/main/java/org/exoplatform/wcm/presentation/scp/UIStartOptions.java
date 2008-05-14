/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.wcm.presentation.scp;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.dms.webui.form.UIFormInputSetWithAction;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Apr 4, 2008  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = UIStartOptions.StartExistingActionListener.class),
      @EventConfig(listeners = UIStartOptions.StartNewActionListener.class),
      @EventConfig(listeners = UIStartOptions.StartEditActionListener.class),
      @EventConfig(listeners = UIStartOptions.StartSelectActionListener.class)
    }
)

public class UIStartOptions extends UIForm {
  
  private static String FIELD_CREATE_EXISTING = "Existing" ;
  private static String FIELD_CREATE_NEW = "New" ;
  private static String FIELD_EDIT_CURRENT = "EditContent" ;
  private static String FIELD_EDIT_OTHER = "SelectOther" ;
  
  public UIStartOptions() throws Exception {}
    
  public UIStartOptions setCreateMode(boolean create) throws Exception {
    getChildren().clear() ;
    UIFormInputSetWithAction uiInputWithAction ;
    if(create) {
      uiInputWithAction = new UIFormInputSetWithAction("ExistingSet") ;
      uiInputWithAction.addUIFormInput(new UIFormInputInfo(FIELD_CREATE_EXISTING, null, null)) ;
      uiInputWithAction.setActionInfo(FIELD_CREATE_EXISTING, new String [] {"StartExisting"}) ;
      addUIComponentInput(uiInputWithAction) ;
      uiInputWithAction = new UIFormInputSetWithAction("NewSet") ;
      uiInputWithAction.addUIFormInput(new UIFormInputInfo(FIELD_CREATE_NEW, null, null)) ; 
      uiInputWithAction.setActionInfo(FIELD_CREATE_NEW, new String [] {"StartNew"}) ;
      addUIComponentInput(uiInputWithAction) ;
    } else {
      uiInputWithAction = new UIFormInputSetWithAction("EditContentSet") ;
      uiInputWithAction.addUIFormInput(new UIFormInputInfo(FIELD_EDIT_CURRENT, null, null)) ;
      uiInputWithAction.setActionInfo(FIELD_EDIT_CURRENT, new String [] {"StartEdit"}) ;
      addUIComponentInput(uiInputWithAction) ;
      uiInputWithAction = new UIFormInputSetWithAction("SelectSet") ;
      uiInputWithAction.addUIFormInput(new UIFormInputInfo(FIELD_EDIT_OTHER, null, null)) ;
      uiInputWithAction.setActionInfo(FIELD_EDIT_OTHER, new String [] {"StartSelect"}) ;
      addUIComponentInput(uiInputWithAction) ;
    }    
    
    return this ;
  }
  
  public Node getNode() throws Exception { 
    PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPreferences = pContext.getRequest().getPreferences();
    String repository = portletPreferences.getValue(UISimplePresentationPortlet.REPOSITORY, "repository");
    String worksapce = portletPreferences.getValue(UISimplePresentationPortlet.WORKSPACE, "collaboration");
    String uuid = portletPreferences.getValue(UISimplePresentationPortlet.UUID, "") ;
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Session session = SessionProviderFactory.createSystemProvider().getSession(worksapce, manageableRepository) ;
    return session.getNodeByUUID(uuid) ;
  }  
  
  public <T extends UIComponent> void setComponent(Class<T> type, String config, String id) throws Exception {
    UIPortletConfig uiConfig = getParent() ;
    uiConfig.getChildren().clear() ;
    uiConfig.addChild(type, config, id) ;
  }
  
  public static class StartExistingActionListener extends EventListener<UIStartOptions> {
    public void execute(Event<UIStartOptions> event) throws Exception {
     UIStartOptions uiOptions = event.getSource() ;
     uiOptions.setComponent(UIContentChooser.class, null, null) ;
    }    
  }

  public static class StartNewActionListener extends EventListener<UIStartOptions> {
    public void execute(Event<UIStartOptions> event) throws Exception {
      UIStartOptions uiOptions = event.getSource() ;
      uiOptions.setComponent(UIContentCreationWizard.class, null, null) ;
    }    
  }
  
  public static class StartEditActionListener extends EventListener<UIStartOptions> {
    public void execute(Event<UIStartOptions> event) throws Exception {
      try{
        event.getSource().getNode();
        UIStartOptions uiOptions = event.getSource() ;
        uiOptions.setComponent(UIContentEditWizard.class, null, null) ;
      }
      catch (ItemNotFoundException e){ }
    }
  }

  public static class StartSelectActionListener extends EventListener<UIStartOptions> {
    public void execute(Event<UIStartOptions> event) throws Exception {
      UIStartOptions uiOptions = event.getSource() ;
      uiOptions.setComponent(UIContentChooser.class, null, null) ;
    }    
  }
  
}
