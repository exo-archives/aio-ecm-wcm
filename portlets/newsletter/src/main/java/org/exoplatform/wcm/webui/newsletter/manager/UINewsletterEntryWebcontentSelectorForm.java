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
package org.exoplatform.wcm.webui.newsletter.manager;

import org.exoplatform.ecm.webui.form.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.newsletter.UINewsletterConstant;
import org.exoplatform.wcm.webui.selector.webcontent.UIWebContentPathSelector;
import org.exoplatform.wcm.webui.selector.webcontent.UIWebContentTabSelector;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 15, 2009  
 */
@ComponentConfig (
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig (listeners = UINewsletterEntryWebcontentSelectorForm.SelectWebcontentActionListener.class)
    }
)
public class UINewsletterEntryWebcontentSelectorForm extends UIForm implements UISelectable {
  
  private String popupId;
  
  public static final String FORM_WEBCONTENT_SELECTOR = "FormWebcontentSelector";
  
  public static final String INPUT_WEBCONTENT_SELECTOR = "WebcontentSelector";
  
  public UINewsletterEntryWebcontentSelectorForm() {
    UIFormStringInput inputWebcontentSelector = new UIFormStringInput(INPUT_WEBCONTENT_SELECTOR, INPUT_WEBCONTENT_SELECTOR, null);
    inputWebcontentSelector.setEditable(false);
    
    UIFormInputSetWithAction formWebcontentSelector = new UIFormInputSetWithAction(FORM_WEBCONTENT_SELECTOR);
    formWebcontentSelector.addChild(inputWebcontentSelector);
    formWebcontentSelector.setActionInfo(INPUT_WEBCONTENT_SELECTOR, new String[] {"SelectWebcontent"});
    formWebcontentSelector.showActionInfo(true);
    
    addChild(formWebcontentSelector);
  }

  public String getPopupId() {
    return popupId;
  }

  public void setPopupId(String popupId) {
    this.popupId = popupId;
  }
  
  public void doSelect(String selectField, Object value) throws Exception {
    getUIStringInput(selectField).setValue((String) value);
    UIPopupContainer popupContainer = getAncestorOfType(UIPopupContainer.class);
    Utils.closePopupWindow(popupContainer, popupId);
  }
  
  public static class SelectWebcontentActionListener extends EventListener<UINewsletterEntryWebcontentSelectorForm> {
    public void execute(Event<UINewsletterEntryWebcontentSelectorForm> event) throws Exception {
      UINewsletterEntryWebcontentSelectorForm newsletterEntryWebcontentSelector = event.getSource();
      UIPopupContainer popupContainer = newsletterEntryWebcontentSelector.getAncestorOfType(UIPopupContainer.class);
      UIPopupWindow popupWindow = popupContainer.getChildById(UINewsletterConstant.WEBCONTENT_SELECTOR_POPUP_WINDOW);
      if (popupWindow == null) {
        UIWebContentTabSelector webContentTabSelector = newsletterEntryWebcontentSelector.createUIComponent(UIWebContentTabSelector.class, null, null);
        webContentTabSelector.init();
        UIWebContentPathSelector webContentPathSelector = webContentTabSelector.getChild(UIWebContentPathSelector.class);
        webContentPathSelector.setSourceComponent(newsletterEntryWebcontentSelector, new String[] {INPUT_WEBCONTENT_SELECTOR});
        Utils.createPopupWindow(popupContainer, webContentTabSelector, event.getRequestContext(), UINewsletterConstant.WEBCONTENT_SELECTOR_POPUP_WINDOW, 650, 270);
      } else { 
        popupWindow.setShow(true);
      }
      newsletterEntryWebcontentSelector.setPopupId(UINewsletterConstant.WEBCONTENT_SELECTOR_POPUP_WINDOW);
    }
  }
}
