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

import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterEntryHandler;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.newsletter.UINewsletterConstant;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          ngoc.tran@exoplatform.com
 * Jun 11, 2009  
 */
@ComponentConfig(
   lifecycle = UIFormLifecycle.class ,
   template = "app:/groovy/webui/newsletter/NewsletterManager/UINewsletterMangerPopup.gtmpl",
   events = {
     @EventConfig(listeners = UINewsletterManagerPopup.CloseActionListener.class, phase = Phase.DECODE)
   }
 )
public class UINewsletterManagerPopup extends UIForm implements UIPopupComponent {
  private String newsletterEntryContent_;
  public UINewsletterManagerPopup () {
    this.setActions(new String[]{"Close"});
  }
  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }
  
  public void setNewsletterInfor(String categoryName, String subscriptoinName, String newsletterName) throws Exception{
    NewsletterManagerService newsletterManagerService = getApplicationComponent(NewsletterManagerService.class);
    NewsletterEntryHandler newsletterEntryHandler = newsletterManagerService.getEntryHandler();
    newsletterEntryContent_ = 
                      newsletterEntryHandler.getContent(
                                                        NewsLetterUtil.getPortalName(),
                                                        categoryName, subscriptoinName,
                                                        newsletterName,
                                                        Utils.getSessionProvider(this));
  }
  

  static  public class CloseActionListener extends EventListener<UINewsletterManagerPopup> {
    public void execute(Event<UINewsletterManagerPopup> event) throws Exception {
      UINewsletterManagerPopup uiNewsletterManagerPopup = event.getSource();
      Utils.closePopupWindow(uiNewsletterManagerPopup, UINewsletterConstant.UIVIEW_ENTRY_PUPUP_WINDOW);
    }
  }

}
