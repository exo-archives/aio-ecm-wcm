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

import javax.portlet.PortletPreferences;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
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
    template = "system:/groovy/webui/form/UIForm.gtmpl"
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
    addUIFormInput(new UIFormCheckBoxInput("ShowTOC", "ShowTOC", null).setChecked(isShowTOC));
    addUIFormInput(new UIFormCheckBoxInput("ShowTags", "ShowTags", null).setChecked(isShowTags));
    addUIFormInput(new UIFormCheckBoxInput("ShowCategories", "ShowCategories", null).setChecked(isShowCategories));
    addUIFormInput(new UIFormCheckBoxInput("AllowVoting", "AllowVoting", null).setChecked(isAllowVoting));
    addUIFormInput(new UIFormCheckBoxInput("AllowComment", "AllowComment", null).setChecked(isAllowComment));
    addUIFormInput(new UIFormCheckBoxInput("ShowQuickEdit", "ShowQuickEdit", null).setChecked(isQuickEdit));
  }
}
