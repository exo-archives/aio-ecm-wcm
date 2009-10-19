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
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.application.Preference;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.portletcontainer.pci.ExoWindowID;
import org.exoplatform.wcm.webui.scv.UIPresentationContainer;
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
      @EventConfig(listeners = UIWelcomeScreen.SelectContentActionListener.class),
      @EventConfig(listeners = UIWelcomeScreen.CreateNewContentActionListener.class),
      @EventConfig(listeners = UIWelcomeScreen.AbortActionListener.class)
    }
)
public class UIWelcomeScreen extends UIForm implements UISelectable {

  /**
   * Instantiates a new uI welcome screen.
   * 
   * @throws Exception the exception
   */
  public UIWelcomeScreen() throws Exception {
    this.setActions(new String[]{"Abort"});
  }

  /** The is new content. */
  private boolean isNewContent = false;
  
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
    //String labelQuickCreate = res.getString(getId() + ".label.QuickCreateWebContent");
    //String labelSelectExistedWebContent = res.getString(getId() + ".label.SelectExistedWebContent");
    //String labelSelectExistedDMS = res.getString(getId() + ".label.SelectExistedDMS");
    String labelEditContent = res.getString(getId() + ".label.EditWebContent");
    String labelSelectExistedContent = res.getString(getId() + ".label.ExistedContent");
    if(isNewConfig) {
      isNewConfig = true;
    }else {
      UISingleContentViewerPortlet uiPresentationPortlet = getAncestorOfType(UISingleContentViewerPortlet.class);
      UIPresentationContainer presentationContainer = uiPresentationPortlet.getChild(UIPresentationContainer.class);
//      Node node = presentationContainer.getReferenceNode();
      Node node = presentationContainer.getNodeView();
      if(uiPresentationPortlet.canEditContent(node)) {
        option.add(new SelectItemOption<String>(labelEditContent, "EditCurrentWebContent"));
        option.add(new SelectItemOption<String>(labelSelectExistedContent, "SelectExistedContent"));
        UIFormRadioBoxInput radioInput = new UIFormRadioBoxInput("radio", "radio", option);
        radioInput.setAlign(UIFormRadioBoxInput.VERTICAL_ALIGN);
        radioInput.setValue("EditCurrentWebContent");
        addUIFormInput(radioInput);
      }
    }
    return this ;
  }
  
  /**
   * Goto edit wizard.
   * 
   * @param step the step
   * 
   * @throws Exception the exception
   */
  public void GotoEditWizard(int step) throws Exception {
      UIPortletConfig uiPortletConfig = this.getAncestorOfType(UIPortletConfig.class);      
      this.setRendered(false);
      UIQuickCreationWizard uiQuickCreationWizard = uiPortletConfig.addChild(UIQuickCreationWizard.class, null, null);
      UIContentDialogForm contentDialogForm  = uiQuickCreationWizard.getChild(UIContentDialogForm.class);
      contentDialogForm.setEditNotIntegrity(false);
      contentDialogForm.init();
      uiQuickCreationWizard.viewStep(step);
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
  public void doSelect(String arg0, Object arg1) throws Exception {}
  
  /**
   * The listener interface for receiving selectContentAction events.
   * The class that is interested in processing a selectContentAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addSelectContentActionListener<code> method. When
   * the selectContentAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SelectContentActionEvent
   */
  public static class SelectContentActionListener extends EventListener<UIWelcomeScreen> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIWelcomeScreen> event) throws Exception {
      UIWelcomeScreen uiWelcomeScreen = event.getSource();
      ((UIPortletConfig) uiWelcomeScreen.getParent()).initPopupWebcontentView();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWelcomeScreen.getParent());
    }
  }
  
  /**
   * The listener interface for receiving createNewContentAction events.
   * The class that is interested in processing a createNewContentAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addCreateNewContentActionListener<code> method. When
   * the createNewContentAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see CreateNewContentActionEvent
   */
  public static class CreateNewContentActionListener extends EventListener<UIWelcomeScreen> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIWelcomeScreen> event) throws Exception {
      UIWelcomeScreen uiWelcomeScreen = event.getSource();
      UIPortletConfig uiPortletConfig = uiWelcomeScreen.getAncestorOfType(UIPortletConfig.class);      
      uiWelcomeScreen.setRendered(false);
      UIQuickCreationWizard uiQuickCreationWizard = uiPortletConfig.addChild(UIQuickCreationWizard.class, null, null);
      uiQuickCreationWizard.getChild(UINameWebContentForm.class).init();
      uiPortletConfig.showPopup(event.getRequestContext());
    }
  }

  /**
   * The listener interface for receiving abortAction events.
   * The class that is interested in processing a abortAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addAbortActionListener<code> method. When
   * the abortAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see AbortActionEvent
   */
  public static class AbortActionListener extends EventListener<UIWelcomeScreen> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIWelcomeScreen> event) throws Exception {
      UIWelcomeScreen uiWelcomeScreen = event.getSource();
      UserPortalConfigService userPortalConfigService = uiWelcomeScreen.getApplicationComponent(UserPortalConfigService.class);
      UIPortal uiPortal = Util.getUIPortal();
      PageNode currentPageNode = uiPortal.getSelectedNode();
      Page currentPage = userPortalConfigService.getPage(currentPageNode.getPageReference());
      ArrayList<Object> applications = new ArrayList<Object>();
      applications.addAll(currentPage.getChildren());
      ArrayList<Object> applicationsTmp = currentPage.getChildren(); 
      Collections.reverse(applicationsTmp);
      DataStorage dataStorage = uiWelcomeScreen.getApplicationComponent(DataStorage.class);
      for (Object applicationObject : applicationsTmp) {
        if (applicationObject instanceof Container) continue;
        Application application = Application.class.cast(applicationObject);
        String applicationId = application.getInstanceId();
        PortletPreferences portletPreferences = dataStorage.getPortletPreferences(new ExoWindowID(applicationId));
        if (portletPreferences == null) continue;
        
        boolean isQuickCreate = false;
        String nodeIdentifier = null;
        
        for (Object preferenceObject : portletPreferences.getPreferences()) {
        	Preference preference = Preference.class.cast(preferenceObject);

        	if ("isQuickCreate".equals(preference.getName())) {
        		isQuickCreate = Boolean.valueOf(preference.getValues().get(0).toString());
        		if (!isQuickCreate) break;
        	}

        	if ("nodeIdentifier".equals(preference.getName())) {
        		nodeIdentifier = preference.getValues().get(0).toString();
        		if (nodeIdentifier == null || nodeIdentifier == "") break;
        	}
        }

        if (isQuickCreate && (nodeIdentifier == null || nodeIdentifier == "")) {
        	applications.remove(applicationObject);
        }
      }
      currentPage.setChildren(applications);
      userPortalConfigService.update(currentPage);
      UIPage uiPage = uiPortal.findFirstComponentOfType(UIPage.class);
      if (uiPage != null) {
      	uiPage.setChildren(null);
      	PortalDataMapper.toUIPage(uiPage, currentPage);
      }
      UIPortletConfig uiPortletConfig = uiWelcomeScreen.getAncestorOfType(UIPortletConfig.class);      
      uiPortletConfig.closePopupAndUpdateUI(event.getRequestContext(),true);
    }
  }

  /**
   * Checks if is new content.
   * 
   * @return true, if is new content
   */
  public boolean isNewContent() {
    return isNewContent;
  }

  /**
   * Sets the new content.
   * 
   * @param isNewContent the new new content
   */
  public void setNewContent(boolean isNewContent) {
    this.isNewContent = isNewContent;
  }
}