/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.exoplatform.dms.model.ContentStorePath;
import org.exoplatform.dms.webui.utils.Utils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Nov 8, 2006 10:16:18 AM 
 */

@ComponentConfig ( lifecycle = UIContainerLifecycle.class )
//TODO: Refer to ECM.UIDocumentFormController
public class UIDocumentController extends UIContainer {

  private String defaultDocument_ ;
  private ContentStorePath storePath_ ;

  public UIDocumentController() throws Exception {
    addChild(UISelectDocumentForm.class, null, null) ;
    UIDocumentForm uiDocumentForm = createUIComponent(UIDocumentForm.class, null, null) ;
    uiDocumentForm.addNew(true) ;
    addChild(uiDocumentForm) ;
  }
  
  public void setStorePath(ContentStorePath storePath) { storePath_ = storePath  ; }
  
  public void init() throws Exception {
    UIDocumentForm uiDocumentForm = getChild(UIDocumentForm.class) ;
    uiDocumentForm.setContentStorePath(storePath_) ;
    uiDocumentForm.setTemplateNode(defaultDocument_) ;
    uiDocumentForm.resetProperties();
  }

  public void initPopup(UIComponent uiComp) throws Exception {
    removeChildById("PopupComponent") ;
    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, "PopupComponent") ;
    uiPopup.setUIComponent(uiComp) ;
    uiPopup.setWindowSize(640, 300) ;
    uiPopup.setShow(true) ;
    uiPopup.setResizable(true) ;
  }

  public List<SelectItemOption<String>> getListFileType() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>(3) ;
    Map<String, String> map = Utils.getListFileType(storePath_) ;
    Iterator<Entry<String, String>> itr = map.entrySet().iterator() ;
    while(itr.hasNext()) {
      Entry<String, String> template = itr.next() ;
      options.add(new SelectItemOption<String>(template.getValue(), template.getKey())) ;
    }
    UISelectDocumentForm uiSelectForm = getChild(UISelectDocumentForm.class) ;
    UIFormSelectBox uiSelectBoxInput = uiSelectForm.getUIFormSelectBox(UISelectDocumentForm.FIELD_SELECT) ;
    uiSelectBoxInput.setOptions(options) ;
    if(options.size() > 0) {
      defaultDocument_ = options.get(0).getValue() ;
      uiSelectBoxInput.setValue(defaultDocument_) ;
    }
    return options ;
  }
  
}
