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
package org.exoplatform.wcm.webui.scv.config;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.wcm.webui.scv.UISingleContentViewerPortlet;
import org.exoplatform.web.application.RequestContext;
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
 * dzungdev@gmail.com
 * May 26, 2008
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/groovy/SingleContentViewer/config/UIWelcomeScreen.gtmpl",
    events = {
      @EventConfig(listeners = UIWelcomeScreen.NextActionListener.class),
      @EventConfig(listeners = UIWelcomeScreen.AbortActionListener.class)
    }
)
public class UIWelcomeScreen extends UIForm implements UISelectable {

  /**
   * Instantiates a new uI welcome screen.
   * 
   * @throws Exception the exception
   */
  public UIWelcomeScreen() throws Exception {}

  /**
   * Sets the create mode.
   * 
   * @param isNewConfig the is new config
   * 
   * @return the uI welcome screen
   * 
   * @throws Exception the exception
   */
  public UIWelcomeScreen setCreateMode(boolean isNewConfig) throws Exception {
    getChildren().clear();
    List<SelectItemOption<String>> option = new ArrayList<SelectItemOption<String>>();
    RequestContext context = RequestContext.<RequestContext>getCurrentInstance();
    ResourceBundle res = context.getApplicationResourceBundle();
    String labelQuickCreate = res.getString(getId() + ".label.QuickCreateWebContent");
    String labelSelectExistedWebContent = res.getString(getId() + ".label.SelectExistedWebContent");
    String labelSelectExistedDMS = res.getString(getId() + ".label.SelectExistedDMS");
    String labelEditContent = res.getString(getId() + ".label.EditWebContent");
    String labelSelectExistedContent = res.getString(getId() + ".label.ExistedContent");
    if(isNewConfig) {
      option.add(new SelectItemOption<String>(labelQuickCreate, "QuickCreateWebContent"));
      option.add(new SelectItemOption<String>(labelSelectExistedContent, "SelectExistedContent"));
      UIFormRadioBoxInput radioInput = new UIFormRadioBoxInput("radio", "radio", option);
      radioInput.setAlign(UIFormRadioBoxInput.VERTICAL_ALIGN);
      radioInput.setValue("QuickCreateWebContent");
      addUIFormInput(radioInput);
    }else {
      UISingleContentViewerPortlet uiPresentationPortlet = getAncestorOfType(UISingleContentViewerPortlet.class);
      Node node = uiPresentationPortlet.getReferencedContent();
      if(uiPresentationPortlet.canEditContent(node)) {
        option.add(new SelectItemOption<String>(labelEditContent, "EditCurrentWebContent"));
        option.add(new SelectItemOption<String>(labelSelectExistedContent, "SelectExistedContent"));
        UIFormRadioBoxInput radioInput = new UIFormRadioBoxInput("radio", "radio", option);
        radioInput.setAlign(UIFormRadioBoxInput.VERTICAL_ALIGN);
        radioInput.setValue("EditCurrentWebContent");
        addUIFormInput(radioInput);
      } else {
        option.add(new SelectItemOption<String>(labelSelectExistedWebContent, "SelectExistedWebContent"));
        option.add(new SelectItemOption<String>(labelSelectExistedDMS,"SelectExistedDMS"));
        UIFormRadioBoxInput radioInput = new UIFormRadioBoxInput("radio", "radio", option);
        radioInput.setAlign(UIFormRadioBoxInput.VERTICAL_ALIGN);
        radioInput.setValue("SelectExistedWebContent");
        addUIFormInput(radioInput);
      }
    }
    return this ;
  }

  /**
   * Sets the component.
   * 
   * @param type the type
   * @param config the config
   * @param id the id
   * 
   * @throws Exception the exception
   */
  public <T extends UIComponent> void setComponent(Class<T> type, String config, String id) throws Exception {
    UIPortletConfig uiConfig = getParent();
    uiConfig.getChildren().clear();
    uiConfig.addChild(type, config, id);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.selector.UISelectable#doSelect(java.lang.String, java.lang.Object)
   */
  public void doSelect(String arg0, Object arg1) throws Exception {
    // TODO Auto-generated method stub

  }

  /**
   * The listener interface for receiving startProcessAction events.
   * The class that is interested in processing a startProcessAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addStartProcessActionListener<code> method. When
   * the startProcessAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see StartProcessActionEvent
   */
  public static class NextActionListener extends EventListener<UIWelcomeScreen> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIWelcomeScreen> event) throws Exception {
      UIWelcomeScreen uiWelcomeScreen = event.getSource();
      String radioValue = uiWelcomeScreen.<UIFormRadioBoxInput>getUIInput("radio").getValue();
      UIPortletConfig uiPortletConfig = uiWelcomeScreen.getAncestorOfType(UIPortletConfig.class);      
      if("QuickCreateWebContent".equals(radioValue)) {
        uiWelcomeScreen.setRendered(false);
        UIQuickCreationWizard uiQuickCreationWizard = uiPortletConfig.addChild(UIQuickCreationWizard.class, null, null);
        uiQuickCreationWizard.getChild(UIWebConentNameTabForm.class).init();
      } else if ("EditCurrentWebContent".equals(radioValue)) {
        uiWelcomeScreen.setRendered(false);
        UIQuickCreationWizard uiQuickCreationWizard = uiPortletConfig.addChild(UIQuickCreationWizard.class, null, null);
        UIContentDialogForm contentDialogForm  = uiQuickCreationWizard.getChild(UIContentDialogForm.class);
        contentDialogForm.setEditNotIntegrity(true);
        contentDialogForm.init();
        uiQuickCreationWizard.viewStep(2);
      } else if("SelectExistedContent".equals(radioValue)) {
        uiWelcomeScreen.getChildren().clear();
        List<SelectItemOption<String>> option = new ArrayList<SelectItemOption<String>>();
        RequestContext context = RequestContext.<RequestContext>getCurrentInstance();
        ResourceBundle res = context.getApplicationResourceBundle();
        String labelSelectExistedWebContent = res.getString(uiWelcomeScreen.getId() + ".label.SelectExistedWebContent");
        String labelSelectExistedDMS = res.getString(uiWelcomeScreen.getId() + ".label.SelectExistedDMS");
        option.add(new SelectItemOption<String>(labelSelectExistedWebContent, "SelectExistedWebContent"));
        option.add(new SelectItemOption<String>(labelSelectExistedDMS, "SelectExistedDMS"));
        UIFormRadioBoxInput radioInput = new UIFormRadioBoxInput("radio", "radio", option);
        radioInput.setAlign(UIFormRadioBoxInput.VERTICAL_ALIGN);
        radioInput.setValue("SelectExistedWebContent");
        uiWelcomeScreen.addChild(radioInput);       
      } else if("SelectExistedWebContent".equals(radioValue)) {
        uiWelcomeScreen.setRendered(false);
        UIWebContentSelectorForm webContentSelectorForm = uiPortletConfig.addChild(UIWebContentSelectorForm.class, null, null);
        webContentSelectorForm.init();
      } else if("SelectExistedDMS".equals(radioValue)) {
        uiWelcomeScreen.setRendered(false);
        UIDMSSelectorForm dmSelectorForm = uiPortletConfig.addChild(UIDMSSelectorForm.class, null, null);
        dmSelectorForm.init();
      }
      uiPortletConfig.showPopup(event.getRequestContext());
    }
  }

  /**
   * The listener interface for receiving backAction events.
   * The class that is interested in processing a backAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addBackActionListener<code> method. When
   * the backAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see BackActionEvent
   */
  public static class AbortActionListener extends EventListener<UIWelcomeScreen> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIWelcomeScreen> event) throws Exception {
      UIWelcomeScreen uiWelcomeScreen = event.getSource();
      UIPortletConfig uiPortletConfig = uiWelcomeScreen.getAncestorOfType(UIPortletConfig.class);      
      uiPortletConfig.closePopupAndUpdateUI(event.getRequestContext(),true);
    }
  }

}
