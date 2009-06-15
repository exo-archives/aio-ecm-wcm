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

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.form.UIDialogForm;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterTemplateHandler;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 11, 2009  
 */
@ComponentConfig (
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig (listeners = UINewsletterEntryForm.PreviewActionListener.class),
      @EventConfig (listeners = UINewsletterEntryForm.SaveActionListener.class),
      @EventConfig (listeners = UINewsletterEntryForm.SendActionListener.class)
    }
)
public class UINewsletterEntryForm extends UIDialogForm {

  private static final Log log  = ExoLogger.getLogger("wcm:UINewsletterEntryForm");

  private NewsletterManagerService newsletterManagerService;
  
  private NewsletterTemplateHandler newsletterTemplateHandler;
  
  public UINewsletterEntryForm() throws Exception {
    newsletterManagerService = getApplicationComponent(NewsletterManagerService.class);
    newsletterTemplateHandler = newsletterManagerService.getTemplateHandler();
  }
  
  public String getTemplate() {
    try {
      UINewsletterEntryContainer newsletterEntryContainer = getAncestorOfType(UINewsletterEntryContainer.class);
      UINewsletterEntryDialogSelector newsletterEntryDialogSelector = newsletterEntryContainer.getChild(UINewsletterEntryDialogSelector.class);
      Node dialogNode = newsletterTemplateHandler.getDialog(newsletterEntryDialogSelector.getDialog());
      return dialogNode.getPath();  
    } catch (Exception e) {
      log.error("Get template failed because of " + e.getMessage(), e);
    }
    return null;
  }

  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    try{
      if (resourceResolver == null) {
        RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
        ManageableRepository manageableRepository = repositoryService.getRepository("repository");
        String workspace = manageableRepository.getConfiguration().getSystemWorkspaceName();
        resourceResolver = new JCRResourceResolver("repository", workspace, TemplateService.EXO_TEMPLATE_FILE_PROP);
      }
    }catch(Exception e) {
      log.error("Get template resource resolver failed because of " + e.getMessage(), e);
    }
    return resourceResolver;
  }
  
  public static class PreviewActionListener extends EventListener<UINewsletterEntryContainer> {
    public void execute(Event<UINewsletterEntryContainer> event) throws Exception {
    }
  }
  
  public static class SaveActionListener extends EventListener<UINewsletterEntryContainer> {
    public void execute(Event<UINewsletterEntryContainer> event) throws Exception {
    }
  }
  
  public static class SendActionListener extends EventListener<UINewsletterEntryContainer> {
    public void execute(Event<UINewsletterEntryContainer> event) throws Exception {
    }
  }
  
}
