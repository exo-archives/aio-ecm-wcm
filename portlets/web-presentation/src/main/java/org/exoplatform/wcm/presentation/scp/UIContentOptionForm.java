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

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Mar 31, 2008  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl"
)

public class UIContentOptionForm extends UIForm {
  
  static String FIELD_COMMENT = "Comment" ;
  static String FIELD_TAGMAP = "Tagmap" ;
  static String FIELD_VOTE = "Vote" ;
  static String FIELD_REFERENCED = "Referenced" ;
  static String FIELD_QUICKEDIT = "QuickEdit";
  
  public UIContentOptionForm() throws Exception {
    PortletRequestContext pContext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
    String isQuickEdit = pContext.getRequest().getPreferences().getValue("QuickEdit", "False").toString();
    //add checkboxes
    addUIFormInput(new UIFormCheckBoxInput(FIELD_COMMENT, null, null)) ;
    addUIFormInput(new UIFormCheckBoxInput(FIELD_TAGMAP, null, null)) ;
    addUIFormInput(new UIFormCheckBoxInput(FIELD_VOTE, null, null)) ;
    addUIFormInput(new UIFormCheckBoxInput(FIELD_REFERENCED, null, null)) ;    
    UIFormCheckBoxInput quickEditCheckBoxInput = new UIFormCheckBoxInput(FIELD_QUICKEDIT, null, null);
    quickEditCheckBoxInput.setChecked(Boolean.parseBoolean(isQuickEdit)); 
    addUIFormInput(quickEditCheckBoxInput ) ;
  }

}
