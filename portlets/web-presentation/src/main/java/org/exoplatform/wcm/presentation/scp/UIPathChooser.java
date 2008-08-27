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
import java.util.ResourceBundle;

import org.exoplatform.dms.model.ContentStorePath;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Mar 27, 2008  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIPathChooser.ChangeOptionActionListener.class),
      @EventConfig(listeners = UIDocumentChooser.BrowseActionListener.class)
    }
)

public class UIPathChooser extends UIDocumentChooser {

  private boolean autoPath_ = true ;
  private String autoOption;
  private String specifyOption;

  public UIPathChooser() throws Exception {
    super(UIDocumentChooser.SELECT_PATH_MODE) ;
    getChildren().clear();
    RequestContext context = RequestContext.getCurrentInstance();
    ResourceBundle bundle = context.getApplicationResourceBundle();
    autoOption = bundle.getString("UIPathChooser.label.auto");
    specifyOption = bundle.getString("UIPathChooser.label.specify");
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>(2) ;      
    options.add(new SelectItemOption<String>(autoOption, autoOption, null)) ;
    options.add(new SelectItemOption<String>(specifyOption, specifyOption, null)) ;    
    UIFormSelectBox uiOptionInput = new UIFormSelectBox("PathOption", null, options).setValue(autoOption) ;
    uiOptionInput.setOnChange("ChangeOption") ;
    addUIFormInput(uiOptionInput) ;
    activateMode(UIDocumentChooser.SELECT_PATH_MODE) ;
    setAutoPath(true) ;
    setActions(new String [] {}) ;
  }
  
  public void setAutoPath(boolean auto) throws Exception {
    autoPath_ = auto ;
    this.<UIComponent>getChildById(FIELD_REPOSITORY).setRendered(!autoPath_) ;
    this.<UIComponent>getChildById(FIELD_WORKSPACE).setRendered(!autoPath_) ;
    this.<UIComponent>getChildById(FIELD_PATH).setRendered(!autoPath_) ;
  }

  public boolean isAutoPath() { return autoPath_ ; }

  public void invokeSetBindingBean() throws Exception {
    contentStorePath_ =  new ContentStorePath() ;
    if(!autoPath_) {
     invokeSetBindingBean(contentStorePath_) ;
    } else {
      contentStorePath_.setRepository("repository") ;
      contentStorePath_.setWorkspace("collaboration") ;
      String path = "/Web Content/Live/" + Util.getUIPortal().getName() + "/html" ;
      contentStorePath_.setPath(path) ;
    }
  }

  public static class ChangeOptionActionListener extends EventListener<UIPathChooser> {

    public void execute(Event<UIPathChooser> event) throws Exception {
      UIPathChooser uiForm = event.getSource() ;
      String selectedOption = uiForm.getUIFormSelectBox("PathOption").getValue() ;
      uiForm.setAutoPath(uiForm.autoOption.equals(selectedOption)) ;
    }

  }

}
