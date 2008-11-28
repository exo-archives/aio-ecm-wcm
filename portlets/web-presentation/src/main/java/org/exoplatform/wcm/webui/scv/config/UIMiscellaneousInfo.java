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

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
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
  public UIMiscellaneousInfo() throws Exception {
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences prefs = context.getRequest().getPreferences();
    boolean isShowTOC = Boolean.parseBoolean(prefs.getValue("ShowTOC", null));
    boolean isShowTags = Boolean.parseBoolean(prefs.getValue("ShowTags", null));
    boolean isShowCategories = Boolean.parseBoolean(prefs.getValue("ShowCategories", null));
    boolean isAllowVoting = Boolean.parseBoolean(prefs.getValue("AllowVoting", null));
    boolean isAllowComment = Boolean.parseBoolean(prefs.getValue("AllowComment", null));
    boolean isQuickEdit = Boolean.parseBoolean(prefs.getValue("ShowQuickEdit", null));
    UIFormCheckBoxInput<Boolean> showTocBox = new UIFormCheckBoxInput("ShowTOC", "ShowTOC", null).setChecked(isShowTOC);
    UIFormCheckBoxInput<Boolean> ShowTagBox = new UIFormCheckBoxInput("ShowTags", "ShowTags", null).setChecked(isShowTags);
    UIFormCheckBoxInput<Boolean> ShowCategoryBox = new UIFormCheckBoxInput("ShowCategories", "ShowCategories", null).setChecked(isShowCategories);
    UIFormCheckBoxInput<Boolean> AllowVotingBox = new UIFormCheckBoxInput("AllowVoting", "AllowVoting", null).setChecked(isAllowVoting);
    UIFormCheckBoxInput<Boolean> AllowCommentBox = new UIFormCheckBoxInput("AllowComment", "AllowComment", null).setChecked(isAllowComment);
    UIFormCheckBoxInput<Boolean> ShowQuickEditBox = new UIFormCheckBoxInput("ShowQuickEdit", "ShowQuickEdit", null).setChecked(isQuickEdit);
    showTocBox.setOnChange("SaveToPortletPreference");
    ShowTagBox.setOnChange("SaveToPortletPreference");
    ShowCategoryBox.setOnChange("SaveToPortletPreference");
    AllowVotingBox.setOnChange("SaveToPortletPreference");
    AllowCommentBox.setOnChange("SaveToPortletPreference");
    ShowQuickEditBox.setOnChange("SaveToPortletPreference");
    addUIFormInput(showTocBox);
    addUIFormInput(ShowTagBox);
    addUIFormInput(ShowCategoryBox);
    addUIFormInput(AllowVotingBox);
    addUIFormInput(AllowCommentBox);
    addUIFormInput(ShowQuickEditBox);
    setActions(new String[] {} );
  }
  
  static public class SaveToPortletPreferenceActionListener extends EventListener<UIMiscellaneousInfo> {
    public void execute(Event<UIMiscellaneousInfo> event) throws Exception {
      UIMiscellaneousInfo uiMiscellaneousInfo = event.getSource();
      boolean isShowTOC = uiMiscellaneousInfo.getUIFormCheckBoxInput("ShowTOC").isChecked();
      boolean isQuickEdit = uiMiscellaneousInfo.getUIFormCheckBoxInput("ShowQuickEdit").isChecked();
      boolean isShowTags = uiMiscellaneousInfo.getUIFormCheckBoxInput("ShowTags").isChecked();
      boolean isShowCategories = uiMiscellaneousInfo.getUIFormCheckBoxInput("ShowCategories").isChecked();
      boolean isAllowVoting = uiMiscellaneousInfo.getUIFormCheckBoxInput("AllowVoting").isChecked();
      boolean isAllowComment = uiMiscellaneousInfo.getUIFormCheckBoxInput("AllowComment").isChecked();
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      PortletPreferences prefs = context.getRequest().getPreferences();
      prefs.setValue("ShowTOC", Boolean.toString(isShowTOC));
      prefs.setValue("ShowQuickEdit", Boolean.toString(isQuickEdit));
      prefs.setValue("ShowTags", Boolean.toString(isShowTags));
      prefs.setValue("ShowCategories", Boolean.toString(isShowCategories));
      prefs.setValue("AllowVoting", Boolean.toString(isAllowVoting));
      prefs.setValue("AllowComment", Boolean.toString(isAllowComment));
      prefs.store();    
    }     
  }
}
