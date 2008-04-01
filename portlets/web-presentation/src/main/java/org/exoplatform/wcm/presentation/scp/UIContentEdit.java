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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.dms.webui.form.UISCPForm;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormRadioBoxInput;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Mar 24, 2008  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIContentEdit.StartEditActionListener.class)
    }
)
public class UIContentEdit extends UISCPForm {

  public UIContentEdit() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>(2) ;
    options.add(new SelectItemOption<String>("Edit Content", "EditContent")) ;
    options.add(new SelectItemOption<String>("Select Other", "SelectOther")) ;
    addUIFormInput(new UIFormRadioBoxInput("StartOptions", "EditContent", options).setAlign(UIFormRadioBoxInput.VERTICAL_ALIGN)) ;

  }

  public static class StartEditActionListener extends EventListener<UIContentEdit> {

    public void execute(Event<UIContentEdit> event) throws Exception {
      UIContentEdit uiForm = event.getSource() ;
      UIPortletConfig uiConfig = uiForm.getParent() ;
      uiConfig.getChildren().clear() ;
      String option = uiForm.<UIFormRadioBoxInput>getUIInput("StartOptions").getValue() ;
      if("EditContent".equals(option)) uiConfig.addChild(UIContentEditWizard.class, null, null) ;
      else if("SelectOther".equals(option)) uiConfig.addChild(UIContentChooser.class, null, null) ;
    }

  }
}

