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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.form.UIDialogForm;
import org.exoplatform.ecm.webui.utils.DialogFormUtil;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.wcm.newsletter.NewsletterConstant;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterTemplateHandler;
import org.exoplatform.services.wcm.utils.SQLQueryBuilder;
import org.exoplatform.services.wcm.utils.AbstractQueryBuilder.LOGICAL;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.newsletter.UINewsletterConstant;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormSelectBox;

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
                    @EventConfig (listeners = UINewsletterEntryForm.SendActionListener.class),
                    @EventConfig (listeners = UINewsletterEntryForm.CancelActionListener.class, phase = Phase.DECODE)
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
      e.printStackTrace();
      log.error("Get template failed because of " + e.getMessage(), e);
    }
    return null;
  }

  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    try{
      if (resourceResolver == null) {
        NewsletterManagerService newsletterManagerService = getApplicationComponent(NewsletterManagerService.class);
        String repositoryName = newsletterManagerService.getRepositoryName();
        DMSConfiguration dmsConfiguration = getApplicationComponent(DMSConfiguration.class);
        String workspace = dmsConfiguration.getConfig(repositoryName).getSystemWorkspace();
        resourceResolver = new JCRResourceResolver(repositoryName, workspace, TemplateService.EXO_TEMPLATE_FILE_PROP);
      }
    }catch(Exception e) {
      e.printStackTrace();
      log.error("Get template resource resolver failed because of " + e.getMessage(), e);
    }
    return resourceResolver;
  }

  public static class PreviewActionListener extends EventListener<UINewsletterEntryForm> {
    public void execute(Event<UINewsletterEntryForm> event) throws Exception {
    }
  }

  public static class SaveActionListener extends EventListener<UINewsletterEntryForm> {
    public void execute(Event<UINewsletterEntryForm> event) throws Exception {
      UINewsletterEntryForm newsletterEntryForm = event.getSource();
      String storedPath = newsletterEntryForm.getStoredPath().replace(NewsletterConstant.PORTAL_NAME, NewsLetterUtil.getPortalName());
      String repositoryName = newsletterEntryForm.repositoryName;
      ThreadLocalSessionProviderService threadLocalSessionProviderService = newsletterEntryForm.getApplicationComponent(ThreadLocalSessionProviderService.class);
      SessionProvider sessionProvider = threadLocalSessionProviderService.getSessionProvider(null);
      RepositoryService repositoryService = newsletterEntryForm.getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName);
      Session session = sessionProvider.getSession(newsletterEntryForm.workspaceName, manageableRepository);
      Node storedNode = (Node)session.getItem(storedPath);
      Map<String, JcrInputProperty> inputProperties = DialogFormUtil.prepareMap(newsletterEntryForm.getChildren(), newsletterEntryForm.getInputProperties());
      CmsService cmsService = newsletterEntryForm.getApplicationComponent(CmsService.class);
      String newsletterNodePath = 
        cmsService.storeNode("exo:webContent", storedNode, inputProperties, newsletterEntryForm.isAddNew(), repositoryName);
      session.save();
      // add mixin for newsletter entry node
      UINewsletterEntryContainer newsletterEntryContainer = newsletterEntryForm.getAncestorOfType(UINewsletterEntryContainer.class);
      UINewsletterEntryDialogSelector newsletterEntryDialogSelector = newsletterEntryContainer.getChild(UINewsletterEntryDialogSelector.class);
      Node newsletterNode = (Node)session.getItem(newsletterNodePath);
      if(!newsletterNode.isNodeType(NewsletterConstant.ENTRY_NODETYPE))
        newsletterNode.addMixin(NewsletterConstant.ENTRY_NODETYPE);
      newsletterNode.setProperty(NewsletterConstant.ENTRY_PROPERTY_CATEGORY_NAME, 
                                 ((UIFormSelectBox)newsletterEntryDialogSelector.getChildById(UINewsletterConstant.ENTRY_CATEGORY_SELECTBOX)).getValue());
      newsletterNode.setProperty(NewsletterConstant.ENTRY_PROPERTY_SUBSCRIPTION_NAME, 
                                 ((UIFormSelectBox)newsletterEntryDialogSelector.getChildById(UINewsletterConstant.ENTRY_CATEGORY_SELECTBOX)).getValue());
      newsletterNode.setProperty(NewsletterConstant.ENTRY_PROPERTY_STATUS, NewsletterConstant.STATUS_DRAFT);
      newsletterNode.setProperty(NewsletterConstant.ENTRY_PROPERTY_TYPE, newsletterEntryDialogSelector.getDialog());
      newsletterNode.setProperty(NewsletterConstant.ENTRY_PROPERTY_DATE, 
                                 ((UIFormDateTimeInput)newsletterEntryDialogSelector.getChildById(UINewsletterEntryDialogSelector.NEWSLETTER_ENTRY_SEND_DATE)).getCalendar().getInstance());
      session.save();
      UIPopupContainer popupContainer = newsletterEntryForm.getAncestorOfType(UIPopupContainer.class);
      UINewsletterManagerPortlet managerPortlet = popupContainer.getAncestorOfType(UINewsletterManagerPortlet.class);
      UINewsletterEntryManager entryManager = managerPortlet.getChild(UINewsletterEntryManager.class);
      if(entryManager.isRendered())
        entryManager.init();
      Utils.closePopupWindow(popupContainer, UINewsletterConstant.ENTRY_FORM_POPUP_WINDOW);
    }
  }

  public static class SendActionListener extends EventListener<UINewsletterEntryForm> {
    public void execute(Event<UINewsletterEntryForm> event) throws Exception {
      UINewsletterEntryForm newsletterEntryForm = event.getSource();
      String storedPath = newsletterEntryForm.getStoredPath().replace(NewsletterConstant.PORTAL_NAME, NewsLetterUtil.getPortalName());
      String repositoryName = newsletterEntryForm.repositoryName;
      ThreadLocalSessionProviderService threadLocalSessionProviderService = newsletterEntryForm.getApplicationComponent(ThreadLocalSessionProviderService.class);
      SessionProvider sessionProvider = threadLocalSessionProviderService.getSessionProvider(null);
      RepositoryService repositoryService = newsletterEntryForm.getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName);
      Session session = sessionProvider.getSession(newsletterEntryForm.workspaceName, manageableRepository);
      Node storedNode = (Node)session.getItem(storedPath);
      Map<String, JcrInputProperty> inputProperties = DialogFormUtil.prepareMap(newsletterEntryForm.getChildren(), newsletterEntryForm.getInputProperties());
      CmsService cmsService = newsletterEntryForm.getApplicationComponent(CmsService.class);
      String newsletterNodePath = 
        cmsService.storeNode("exo:webContent", storedNode, inputProperties, newsletterEntryForm.isAddNew(), repositoryName);
      session.save();
      // add mixin for newsletter entry node
      UINewsletterEntryContainer newsletterEntryContainer = newsletterEntryForm.getAncestorOfType(UINewsletterEntryContainer.class);
      UINewsletterEntryDialogSelector newsletterEntryDialogSelector = newsletterEntryContainer.getChild(UINewsletterEntryDialogSelector.class);
      Node newsletterNode = (Node)session.getItem(newsletterNodePath);
      if(!newsletterNode.isNodeType(NewsletterConstant.ENTRY_NODETYPE))
        newsletterNode.addMixin(NewsletterConstant.ENTRY_NODETYPE);
      newsletterNode.setProperty(NewsletterConstant.ENTRY_PROPERTY_CATEGORY_NAME, 
                                 ((UIFormSelectBox)newsletterEntryDialogSelector.getChildById(UINewsletterConstant.ENTRY_CATEGORY_SELECTBOX)).getValue());
      newsletterNode.setProperty(NewsletterConstant.ENTRY_PROPERTY_SUBSCRIPTION_NAME, 
                                 ((UIFormSelectBox)newsletterEntryDialogSelector.getChildById(UINewsletterConstant.ENTRY_CATEGORY_SELECTBOX)).getValue());
      newsletterNode.setProperty(NewsletterConstant.ENTRY_PROPERTY_TYPE, newsletterEntryDialogSelector.getDialog());

      Date currentDate = new Date();
      //DateFormat dateFormat = new SimpleDateFormat(ISO8601.SIMPLE_DATETIME_FORMAT);
      Calendar calendar = ((UIFormDateTimeInput)newsletterEntryDialogSelector
                            .getChildById(UINewsletterEntryDialogSelector.NEWSLETTER_ENTRY_SEND_DATE)).getCalendar();
      if(calendar==null) calendar = Calendar.getInstance();
      newsletterNode.setProperty(NewsletterConstant.ENTRY_PROPERTY_DATE, calendar);
      if(calendar.getTimeInMillis() > currentDate.getTime()){
        newsletterNode.setProperty(NewsletterConstant.ENTRY_PROPERTY_STATUS, NewsletterConstant.STATUS_AWAITING);
      }else{
        newsletterNode.setProperty(NewsletterConstant.ENTRY_PROPERTY_STATUS, NewsletterConstant.STATUS_SENT);
        ExoContainer container = ExoContainerContext.getCurrentContainer() ;
        MailService mailService = (MailService)container.getComponentInstanceOfType(MailService.class) ;
        Message message = null;
        List<String> listEmailAddress = new ArrayList<String>();
        String receiver = "";
        Node subscriptionNode = newsletterNode.getParent();

        if(subscriptionNode.hasProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_USER)){
          Property subscribedUserProperty = subscriptionNode.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_USER);
          for(Value value : subscribedUserProperty.getValues()){
            try {
              listEmailAddress.add(value.getString());
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
        if (listEmailAddress.size() > 0) {
          message = new Message() ;
          message.setTo(listEmailAddress.get(0));
          for (int i = 1; i < listEmailAddress.size(); i ++) {
            receiver += listEmailAddress.get(i) + ",";
          }
          message.setBCC(receiver);
          message.setSubject(newsletterNode.getName()) ;
          message.setBody(newsletterEntryForm.newsletterManagerService.getEntryHandler().getContent(newsletterNode)) ;
          message.setMimeType("text/html") ;
          try {
            mailService.sendMessage(message);
          }catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
      session.save();
      UIPopupContainer popupContainer = newsletterEntryForm.getAncestorOfType(UIPopupContainer.class);
      UINewsletterManagerPortlet managerPortlet = popupContainer.getAncestorOfType(UINewsletterManagerPortlet.class);
      UINewsletterEntryManager entryManager = managerPortlet.getChild(UINewsletterEntryManager.class);
      if(entryManager.isRendered())
        entryManager.init();
      Utils.closePopupWindow(popupContainer, UINewsletterConstant.ENTRY_FORM_POPUP_WINDOW);
    }
  }

  public static class CancelActionListener extends EventListener<UINewsletterEntryForm> {
    public void execute(Event<UINewsletterEntryForm> event) throws Exception {
      UINewsletterEntryForm newsletterEntryForm = event.getSource();
      UIPopupContainer popupContainer = newsletterEntryForm.getAncestorOfType(UIPopupContainer.class);
      Utils.closePopupWindow(popupContainer, UINewsletterConstant.ENTRY_FORM_POPUP_WINDOW);
    }
  }

}
