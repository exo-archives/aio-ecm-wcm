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

import javax.portlet.PortletPreferences;

import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 * dzungdev@gmail.com
 * May 29, 2008
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIMiscellaneousInfo.SaveToPortletPreferenceActionListener.class)
    }
)
public class UIMiscellaneousInfo extends UIForm {

  /**
   * Instantiates a new uI miscellaneous info.
   * 
   * @throws Exception the exception
   */
  public UIMiscellaneousInfo() throws Exception {
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences prefs = context.getRequest().getPreferences();
    boolean isShowTitle = Boolean.parseBoolean(prefs.getValue("ShowTitle", null));
//    boolean isShowTags = Boolean.parseBoolean(prefs.getValue("ShowTags", null));
//    boolean isShowCategories = Boolean.parseBoolean(prefs.getValue("ShowCategories", null));
    boolean isQuickEdit = Boolean.parseBoolean(prefs.getValue("ShowQuickEdit", null));
    addUIFormInput(new UIFormCheckBoxInput("ShowTitle", "ShowTitle", null).setChecked(isShowTitle));
    // because WCM remove UITagging, UICategorizing, we don't add UIFormInput for them
//    addUIFormInput(new UIFormCheckBoxInput("ShowTags", "ShowTags", null).setChecked(isShowTags));
//    addUIFormInput(new UIFormCheckBoxInput("ShowCategories", "ShowCategories", null).setChecked(isShowCategories));
    addUIFormInput(new UIFormCheckBoxInput("ShowQuickEdit", "ShowQuickEdit", null).setChecked(isQuickEdit));
  }

  /**
   * The listener interface for receiving saveToPortletPreferenceAction events.
   * The class that is interested in processing a saveToPortletPreferenceAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addSaveToPortletPreferenceActionListener<code> method. When
   * the saveToPortletPreferenceAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SaveToPortletPreferenceActionEvent
   */
  static public class SaveToPortletPreferenceActionListener extends EventListener<UIMiscellaneousInfo> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIMiscellaneousInfo> event) throws Exception {
      UIMiscellaneousInfo uiMiscellaneousInfo = event.getSource();
      boolean isShowTitle = uiMiscellaneousInfo.getUIFormCheckBoxInput("ShowTitle").isChecked();
      boolean isQuickEdit = uiMiscellaneousInfo.getUIFormCheckBoxInput("ShowQuickEdit").isChecked();
//      boolean isShowTags = uiMiscellaneousInfo.getUIFormCheckBoxInput("ShowTags").isChecked();
//      boolean isShowCategories = uiMiscellaneousInfo.getUIFormCheckBoxInput("ShowCategories").isChecked();
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      PortletPreferences prefs = context.getRequest().getPreferences();
      prefs.setValue("ShowTitle", Boolean.toString(isShowTitle));
      prefs.setValue("ShowQuickEdit", Boolean.toString(isQuickEdit));
//      prefs.setValue("ShowTags", Boolean.toString(isShowTags));
//      prefs.setValue("ShowCategories", Boolean.toString(isShowCategories));
      prefs.store();
      UIApplication uiApplication = uiMiscellaneousInfo.getAncestorOfType(UIApplication.class);
      uiApplication.addMessage(new ApplicationMessage("UIMiscellaneousInfo.msg-successfully", null, ApplicationMessage.INFO));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
    }     
  }


}
