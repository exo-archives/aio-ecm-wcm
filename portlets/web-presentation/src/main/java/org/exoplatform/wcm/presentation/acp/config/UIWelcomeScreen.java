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
package org.exoplatform.wcm.presentation.acp.config;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.wcm.presentation.acp.UIAdvancedPresentationPortlet;
import org.exoplatform.wcm.presentation.acp.config.quickcreation.UIQuickCreationWizard;
import org.exoplatform.wcm.presentation.acp.config.selector.UIDMSSelectorForm;
import org.exoplatform.wcm.presentation.acp.config.selector.UIWebContentSelectorForm;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * May 26, 2008  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/groovy/advancedPresentation/config/UIWelcomeScreen.gtmpl",
    events = {
      @EventConfig(listeners = UIWelcomeScreen.StartProcessActionListener.class),
      @EventConfig(listeners = UIWelcomeScreen.BackActionListener.class)
    }
)
public class UIWelcomeScreen extends UIForm {

  public UIWelcomeScreen() throws Exception {}

  public UIWelcomeScreen setCreateMode(boolean isNewConfig) throws Exception {
    getChildren().clear();
    List<SelectItemOption<String>> option = new ArrayList<SelectItemOption<String>>();
    RequestContext context = RequestContext.<RequestContext>getCurrentInstance();
    ResourceBundle res = context.getApplicationResourceBundle();
    String labelQuickCreate = res.getString(getId() + ".label.QuickCreateWebContent");
    String labelSelectExistedWebContent = res.getString(getId() + ".label.SelectExistedWebContent");
    String labelSelectExistedDMS = res.getString(getId() + ".label.SelectExistedDMS");
    String labelEditContent = res.getString(getId() + ".label.EditWebContent");
    String labelSelectOther = res.getString(getId() + ".label.SelectOtherWebContent");

    if(isNewConfig) {
      option.add(new SelectItemOption<String>(labelQuickCreate, "QuickCreateWebContent"));
      option.add(new SelectItemOption<String>(labelSelectExistedWebContent, "SelectExistedWebContent"));
      option.add(new SelectItemOption<String>(labelSelectExistedDMS,"SelectExistedDMS"));
      UIFormRadioBoxInput radioInput = new UIFormRadioBoxInput("radio", "radio", option);
      radioInput.setAlign(UIFormRadioBoxInput.VERTICAL_ALIGN);
      radioInput.setValue("QuickCreateWebContent");
      addUIFormInput(radioInput);
    }else {
      PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
      PortletPreferences prefs = pContext.getRequest().getPreferences();
      String repositoryName = prefs.getValue(UIAdvancedPresentationPortlet.REPOSITORY, null);
      String workspaceName = prefs.getValue(UIAdvancedPresentationPortlet.WORKSPACE, null);
      String UUID = prefs.getValue(UIAdvancedPresentationPortlet.UUID, null);
      RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName);
      Session session = SessionProviderFactory.createSystemProvider().getSession(workspaceName, manageableRepository);
      Node node = session.getNodeByUUID(UUID);
      if(node.getPrimaryNodeType().equals("exo:webContent")) {
        option.add(new SelectItemOption<String>(labelEditContent, "EditCurrentWebContent"));
        option.add(new SelectItemOption<String>(labelSelectOther, "SelectOtherWebContent"));
        option.add(new SelectItemOption<String>(labelSelectExistedDMS,"SelectExistedDMS"));
        UIFormRadioBoxInput radioInput = new UIFormRadioBoxInput("radio", "radio", option);
        radioInput.setAlign(UIFormRadioBoxInput.VERTICAL_ALIGN);
        radioInput.setValue("EditCurrentWebContent");
        addUIFormInput(radioInput);
      } else {
        option.add(new SelectItemOption<String>(labelSelectOther, "SelectOtherWebContent"));
        option.add(new SelectItemOption<String>(labelSelectExistedDMS,"SelectExistedDMS"));
        UIFormRadioBoxInput radioInput = new UIFormRadioBoxInput("radio", "radio", option);
        radioInput.setAlign(UIFormRadioBoxInput.VERTICAL_ALIGN);
        radioInput.setValue("SelectOtherWebContent");
        addUIFormInput(radioInput);
      }
    }

    return this ;
  }

  public <T extends UIComponent> void setComponent(Class<T> type, String config, String id) throws Exception {
    UIPortletConfig uiConfig = getParent();
    uiConfig.getChildren().clear();
    uiConfig.addChild(type, config, id);
  }

  public static class StartProcessActionListener extends EventListener<UIWelcomeScreen> {
    public void execute(Event<UIWelcomeScreen> event) throws Exception {
      UIWelcomeScreen uiWelcomeScreen = event.getSource();
      String radioValue = uiWelcomeScreen.<UIFormRadioBoxInput>getUIInput("radio").getValue();
      UIPortletConfig uiPortletConfig = uiWelcomeScreen.getAncestorOfType(UIPortletConfig.class);
      if(radioValue.equals("QuickCreateWebContent") || radioValue.equals("EditCurrentWebContent")) {
        uiWelcomeScreen.setRendered(false);
        UIQuickCreationWizard uiQuickCreationWizard = uiPortletConfig.addChild(UIQuickCreationWizard.class, null, null);
        uiQuickCreationWizard.init();
      } else if(radioValue.equals("SelectOtherWebContent") || radioValue.equals("SelectExistedWebContent")) {
        uiWelcomeScreen.setRendered(false);
        uiPortletConfig.addChild(UIWebContentSelectorForm.class, null, null);
      } else if(radioValue.equals("SelectExistedDMS")) {
        uiWelcomeScreen.setRendered(false);
        uiPortletConfig.addChild(UIDMSSelectorForm.class, null, null);
      }
    }
  }
  
  public static class BackActionListener extends EventListener<UIWelcomeScreen> {
    public void execute(Event<UIWelcomeScreen> event) throws Exception {
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      context.setApplicationMode(PortletMode.VIEW);
    }
  }
}
