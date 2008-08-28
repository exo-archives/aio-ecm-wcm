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


import org.exoplatform.wcm.presentation.acp.config.quickcreation.UIQuickCreationWizard;
import org.exoplatform.wcm.presentation.acp.config.selector.UIWebContentSelectorForm;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
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
      @EventConfig(listeners = UIWelcomeScreen.StartProcessActionListener.class)
    }
)
public class UIWelcomeScreen extends UIForm {

  public UIWelcomeScreen() throws Exception {}

  public UIWelcomeScreen setCreateMode(boolean isNewConfig) throws Exception {
    getChildren().clear();
    List<SelectItemOption<String>> option = new ArrayList<SelectItemOption<String>>();
    RequestContext context = RequestContext.<RequestContext>getCurrentInstance();
    ResourceBundle res = context.getApplicationResourceBundle();
    String labelQuickCreate = res.getString(getId() + ".label.QuickCreate");
    String labelSelectExisted = res.getString(getId() + ".label.SelectExisted");
    String labelEditContent = res.getString(getId() + ".label.EditContent");
    String labelSelectOther = res.getString(getId() + ".label.SelectOther");

    if(isNewConfig) {
      option.add(new SelectItemOption<String>(labelQuickCreate, "QuickCreate"));
      option.add(new SelectItemOption<String>(labelSelectExisted, "SelectExisted"));
      UIFormRadioBoxInput radioInput = new UIFormRadioBoxInput("radio", "radio", option);
      radioInput.setAlign(UIFormRadioBoxInput.VERTICAL_ALIGN);
      radioInput.setValue("SelectExisted");
      addUIFormInput(radioInput);
    }else {
      option.add(new SelectItemOption<String>(labelEditContent, "EditCurrentContent"));
      option.add(new SelectItemOption<String>(labelSelectOther, "SelectOtherContent"));
      UIFormRadioBoxInput radioInput = new UIFormRadioBoxInput("radio", "radio", option);
      radioInput.setAlign(UIFormRadioBoxInput.VERTICAL_ALIGN);
      radioInput.setValue("SelectOtherContent");
      addUIFormInput(radioInput);
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
      UIApplication uiApplication = uiWelcomeScreen.getAncestorOfType(UIApplication.class);
      if(radioValue.equals("QuickCreate") || radioValue.equals("EditCurrentContent")) {
        uiWelcomeScreen.setRendered(false);
        UIQuickCreationWizard uiQuickCreationWizard = uiPortletConfig.addChild(UIQuickCreationWizard.class, null, null);
        uiQuickCreationWizard.init();
      } else if(radioValue.equals("SelectOtherContent") || radioValue.equals("SelectExisted")) {
        uiWelcomeScreen.setRendered(false);
        uiPortletConfig.addChild(UIWebContentSelectorForm.class, null, null);
      }
    }
  }
}
