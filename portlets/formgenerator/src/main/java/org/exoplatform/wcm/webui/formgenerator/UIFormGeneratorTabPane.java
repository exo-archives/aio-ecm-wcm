/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.wcm.webui.formgenerator;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.UIFormUploadInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.NameValidator;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 22, 2009  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/FormGeneratorPortlet/UIFormGeneratorTabPane.gtmpl",
    events = {
      @EventConfig(listeners = UIFormGeneratorTabPane.SaveActionListener.class),
      @EventConfig(listeners = UIFormGeneratorTabPane.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UIFormGeneratorTabPane extends UIFormTabPane {

  public UIFormGeneratorTabPane() throws Exception {
    super(UIFormGeneratorConstant.FORM_GENERATOR_TABPANE);
    
    UIFormInputSet formGeneratorGeneralTab = new UIFormInputSet(UIFormGeneratorConstant.FORM_GENERATOR_GENERAL_TAB);
    UIFormStringInput nameFormStringInput = new UIFormStringInput(UIFormGeneratorConstant.NAME_FORM_STRING_INPUT, UIFormGeneratorConstant.NAME_FORM_STRING_INPUT, null); 
    nameFormStringInput.addValidator(MandatoryValidator.class);
    nameFormStringInput.addValidator(NameValidator.class);
    formGeneratorGeneralTab.addUIFormInput(nameFormStringInput);
    List<SelectItemOption<String>> listNodetype = new ArrayList<SelectItemOption<String>>();
    listNodetype.add(new SelectItemOption<String>("exo:article"));
    listNodetype.add(new SelectItemOption<String>("exo:webContent"));
    formGeneratorGeneralTab.addUIFormInput(new UIFormSelectBox(UIFormGeneratorConstant.NODETYPE_FORM_SELECTBOX, UIFormGeneratorConstant.NODETYPE_FORM_SELECTBOX, listNodetype));
    formGeneratorGeneralTab.addUIFormInput(new UIFormWYSIWYGInput(UIFormGeneratorConstant.DESCRIPTION_FORM_WYSIWYG_INPUT, UIFormGeneratorConstant.DESCRIPTION_FORM_WYSIWYG_INPUT, null));
    formGeneratorGeneralTab.addUIFormInput(new UIFormUploadInput(UIFormGeneratorConstant.ICON_FORM_UPLOAD_INPUT, UIFormGeneratorConstant.ICON_FORM_UPLOAD_INPUT));
    addUIFormInput(formGeneratorGeneralTab);
    
    addChild(UIFormGeneratorDnDTab.class, null, null);
    
    UIFormInputSet formGeneratorOptionsTab = new UIFormInputSet(UIFormGeneratorConstant.FORM_GENERATOR_OPTIONS_TAB);
    formGeneratorOptionsTab.addUIFormInput(new UIFormCheckBoxInput<String>(UIFormGeneratorConstant.VOTE_FORM_CHECKBOX_INPUT, UIFormGeneratorConstant.VOTE_FORM_CHECKBOX_INPUT, null));
    formGeneratorOptionsTab.addUIFormInput(new UIFormCheckBoxInput<String>(UIFormGeneratorConstant.COMMENT_FORM_CHECKBOX_INPUT, UIFormGeneratorConstant.COMMENT_FORM_CHECKBOX_INPUT, null));
    addUIFormInput(formGeneratorOptionsTab);

    setSelectedTab(formGeneratorGeneralTab.getId());
  }
  
  public static class SaveActionListener extends EventListener<UIFormGeneratorTabPane> {
    public void execute(Event<UIFormGeneratorTabPane> event) throws Exception {
      
    }
  }
  
  public static class CancelActionListener extends EventListener<UIFormGeneratorTabPane> {
    public void execute(Event<UIFormGeneratorTabPane> event) throws Exception {
      
    }
  }
  
}
