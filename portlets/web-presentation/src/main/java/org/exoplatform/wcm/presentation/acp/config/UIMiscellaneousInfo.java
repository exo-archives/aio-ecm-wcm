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
    addUIFormInput(new UIFormCheckBoxInput("ShowTOC", "ShowTOC", null));
    addUIFormInput(new UIFormCheckBoxInput("ShowTags", "ShowTags", null));
    addUIFormInput(new UIFormCheckBoxInput("ShowCategory", "ShowCategory", null));
    addUIFormInput(new UIFormCheckBoxInput("AllowVoting", "AllowVoting", null));
    addUIFormInput(new UIFormCheckBoxInput("AllowComment", "AllowComment", null));
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences prefs = context.getRequest().getPreferences();
    String quickEdit = prefs.getValue("ShowQuickEdit", null);
    boolean isQuickEdit = Boolean.parseBoolean(quickEdit);
    UIFormCheckBoxInput uiFormCheckBoxInput = new UIFormCheckBoxInput("ShowQuickEdit", "ShowQuickEdit", null);
    uiFormCheckBoxInput.setChecked(isQuickEdit);
    addUIFormInput(uiFormCheckBoxInput);
  }
}


