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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterTemplateHandler;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.newsletter.UINewsletterConstant;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 12, 2009  
 */
@ComponentConfig (
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/webui/newsletter/NewsletterManager/UINewsletterEntryDialogSelector.gtmpl",
    events = {
      @EventConfig (listeners = UINewsletterEntryDialogSelector.OpenWebcontentSelectorFormActionListener.class),
      @EventConfig (listeners = UINewsletterEntryDialogSelector.ChangeTemplateActionListener.class)
    }
)
public class UINewsletterEntryDialogSelector extends UIForm {

  public static final String NEWSLETTER_ENTRY_TEMPLATE = "UINewsletterEntryTemplate";
  
  public static final String NEWSLETTER_ENTRY_SEND_DATE = "UINewsletterEntrySendDate";
  
  private NewsletterManagerService newsletterManagerService;
  
  private NewsletterTemplateHandler newsletterTemplateHandler;
  
  private String dialog = "dialog1";

  public String getDialog() {
    return dialog;
  }

  public void setDialog(String dialog) {
    this.dialog = dialog;
  }
  
  public UINewsletterEntryDialogSelector() throws Exception {
    newsletterManagerService = getApplicationComponent(NewsletterManagerService.class);
    newsletterTemplateHandler = newsletterManagerService.getTemplateHandler();
    List<SelectItemOption<String>> dialogs = new ArrayList<SelectItemOption<String>>();
    List<Node> dialogNodes = newsletterTemplateHandler.getDialogs();
    for (Node node : dialogNodes) {
      SelectItemOption<String> selectItemOption = new SelectItemOption<String>(node.getName());
      dialogs.add(selectItemOption);
    }
    UIFormSelectBox newsletterEntryTemplate = new UIFormSelectBox(NEWSLETTER_ENTRY_TEMPLATE, NEWSLETTER_ENTRY_TEMPLATE, dialogs);
    newsletterEntryTemplate.setOnChange("ChangeTemplate");
    newsletterEntryTemplate.setMultiple(false);
    addChild(newsletterEntryTemplate);
    addChild(new UIFormDateTimeInput(NEWSLETTER_ENTRY_SEND_DATE, NEWSLETTER_ENTRY_SEND_DATE, null));
  }
  
  public static class ChangeTemplateActionListener extends EventListener<UINewsletterEntryDialogSelector> {
    public void execute(Event<UINewsletterEntryDialogSelector> event) throws Exception {
      UINewsletterEntryDialogSelector newsletterEntryDialogSelector = event.getSource();
      UINewsletterEntryContainer newsletterEntryContainer = newsletterEntryDialogSelector.getAncestorOfType(UINewsletterEntryContainer.class);
      UINewsletterEntryForm newsletterEntryForm = newsletterEntryContainer.getChild(UINewsletterEntryForm.class) ;
      newsletterEntryForm.getChildren().clear() ;
      newsletterEntryForm.resetProperties() ;
      UIFormSelectBox newsletterEntryTemplate = newsletterEntryDialogSelector.getChildById(NEWSLETTER_ENTRY_TEMPLATE);
      newsletterEntryDialogSelector.setDialog(newsletterEntryTemplate.getValue()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(newsletterEntryContainer) ;
    }
  }
  
  public static class OpenWebcontentSelectorFormActionListener extends EventListener<UINewsletterEntryDialogSelector> {
    public void execute(Event<UINewsletterEntryDialogSelector> event) throws Exception {
      UINewsletterEntryDialogSelector newsletterEntryDialogSelector = event.getSource();
      UIPopupContainer popupContainer = newsletterEntryDialogSelector.getAncestorOfType(UIPopupContainer.class);
      UIPopupWindow popupWindow = popupContainer.getChildById(UINewsletterConstant.WEBCONTENT_SELECTOR_FORM_POPUP_WINDOW);
      if (popupWindow == null) {
        UINewsletterEntryWebcontentSelectorForm newsletterEntryWebcontentSelector = newsletterEntryDialogSelector.createUIComponent(UINewsletterEntryWebcontentSelectorForm.class, null, null);
        Utils.createPopupWindow(popupContainer, newsletterEntryWebcontentSelector, event.getRequestContext(), UINewsletterConstant.WEBCONTENT_SELECTOR_FORM_POPUP_WINDOW, 300, 120);
      } else { 
        popupWindow.setShow(true);
      }
    }
  }
  
}
