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
package org.exoplatform.wcm.webui.newsletter.viewer;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.wcm.newsletter.NewsletterPublicUser;
import org.exoplatform.services.wcm.newsletter.NewsletterSubscriptionConfig;
import org.exoplatform.wcm.webui.newsletter.manager.NewsLetterUtil;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          ngoc.tran@exoplatform.com
 * Jun 1, 2009  
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  events = {
    @EventConfig(listeners = UINewsletterViewerPortlet.ConfirmUserCodeActionListener.class)
  }
)
public class UINewsletterViewerPortlet extends UIPortletApplication {

  public UINewsletterViewerPortlet() throws Exception {
    this.addChild(UINewsletterViewerForm.class, null, null);
  }
  
  public static class ConfirmUserCodeActionListener extends EventListener<UINewsletterViewerPortlet> {
    public void execute(Event<UINewsletterViewerPortlet> event) throws Exception {
      UINewsletterViewerPortlet newsletterViewerPortlet = event.getSource();
      UINewsletterViewerForm newsletterForm = newsletterViewerPortlet.getChild(UINewsletterViewerForm.class);
      String[] confirms = event.getRequestContext().getRequestParameter(OBJECTID).split("/");
      List<String> listIds = new ArrayList<String>();
      boolean correctUser = newsletterForm.publicUserHandler.confirmPublicUser(confirms[0], confirms[1], NewsLetterUtil.getPortalName());
      if(correctUser){
        List<NewsletterSubscriptionConfig> listSubscriptions = 
          newsletterForm.subcriptionHandler.getSubscriptionIdsByPublicUser(NewsLetterUtil.getPortalName(), confirms[0]);
        for(NewsletterSubscriptionConfig subscriptionConfig : listSubscriptions){
          listIds.add(subscriptionConfig.getCategoryName() + "#" + subscriptionConfig.getName());
        }
        newsletterForm.setListIds(listIds);
        newsletterForm.setActions(new String[] { "ForgetEmail", "ChangeSubcriptions" });
        newsletterForm.inputEmail.setRendered(false);
        newsletterForm.isUpdated = true;
        newsletterForm.setInforConfirm(confirms[0], confirms[1]);
        event.getRequestContext().addUIComponentToUpdateByAjax(newsletterForm);
      } else {
        System.out.println("\n\n\n\n--------------------> user is invalid");
        return ;
      }
    }
  }
}
