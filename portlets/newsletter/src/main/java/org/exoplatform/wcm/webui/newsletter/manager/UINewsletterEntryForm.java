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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.form.UIDialogForm;
import org.exoplatform.ecm.webui.utils.DialogFormUtil;
import org.exoplatform.portal.webui.util.Util;
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
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.newsletter.UINewsletterConstant;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
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

  public UINewsletterEntryForm() throws Exception {
  }

  public String getTemplate() {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String userName = Util.getPortalRequestContext().getRemoteUser();
    try{
      NewsletterManagerService newsletterManagerService = getApplicationComponent(NewsletterManagerService.class);
      String repositoryName = newsletterManagerService.getRepositoryName();
      return templateService.getTemplatePathByUser(true, "exo:webContent", userName, repositoryName);
    } catch(Exception e) {
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

  private Node saveContent() throws Exception {
    // Prepare node store location
    UINewsletterEntryContainer newsletterEntryContainer = getAncestorOfType(UINewsletterEntryContainer.class);
    UINewsletterEntryDialogSelector newsletterEntryDialogSelector = newsletterEntryContainer.getChild(UINewsletterEntryDialogSelector.class);
    String selectedCategory = ((UIFormSelectBox)newsletterEntryDialogSelector.getChildById(UINewsletterConstant.ENTRY_CATEGORY_SELECTBOX)).getValue();
    String selectedSubsctiption = ((UIFormSelectBox)newsletterEntryDialogSelector.getChildById(UINewsletterConstant.ENTRY_SUBSCRIPTION_SELECTBOX)).getValue();
    setStoredPath(NewsletterConstant.generateSubscriptionPath(Util.getUIPortal().getName(), selectedCategory, selectedSubsctiption));
    
    // Prepare node: use title as a node name
    Map<String, JcrInputProperty> inputProperties = DialogFormUtil.prepareMap(getChildren(), getInputProperties());
    if(isAddNew()){
      String nodeName = Utils.cleanString(getUIStringInput("title").getValue());
      inputProperties.get("/node").setValue(nodeName);
    }
    
    // Store node
    String storedPath = getStoredPath().replace(NewsletterConstant.PORTAL_NAME, NewsLetterUtil.getPortalName());
    ThreadLocalSessionProviderService threadLocalSessionProviderService = getApplicationComponent(ThreadLocalSessionProviderService.class);
    SessionProvider sessionProvider = threadLocalSessionProviderService.getSessionProvider(null);
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName);
    Session session = sessionProvider.getSession(workspaceName, manageableRepository);
    Node storedNode = (Node)session.getItem(storedPath);
    CmsService cmsService = getApplicationComponent(CmsService.class);
    String newsletterNodePath = cmsService.storeNode("exo:webContent", storedNode, inputProperties, isAddNew(), repositoryName);

    // Add newsletter mixin type
    Node newsletterNode = (Node)session.getItem(newsletterNodePath);
    if(newsletterNode.canAddMixin(NewsletterConstant.ENTRY_NODETYPE)) newsletterNode.addMixin(NewsletterConstant.ENTRY_NODETYPE);
    newsletterNode.setProperty(NewsletterConstant.ENTRY_PROPERTY_CATEGORY_NAME, selectedCategory);
    newsletterNode.setProperty(NewsletterConstant.ENTRY_PROPERTY_SUBSCRIPTION_NAME, selectedSubsctiption);
    newsletterNode.setProperty(NewsletterConstant.ENTRY_PROPERTY_STATUS, NewsletterConstant.STATUS_DRAFT);
    newsletterNode.setProperty(NewsletterConstant.ENTRY_PROPERTY_TYPE, newsletterEntryDialogSelector.getDialog());
    newsletterNode.setProperty(NewsletterConstant.ENTRY_PROPERTY_DATE, ((UIFormDateTimeInput)newsletterEntryDialogSelector.getChildById(UINewsletterEntryDialogSelector.NEWSLETTER_ENTRY_SEND_DATE)).getCalendar().getInstance());
    session.save();
    
    // Close popup and update UI
    UIPopupContainer popupContainer = getAncestorOfType(UIPopupContainer.class);
    UINewsletterManagerPortlet managerPortlet = popupContainer.getAncestorOfType(UINewsletterManagerPortlet.class);
    UINewsletterEntryManager entryManager = managerPortlet.getChild(UINewsletterEntryManager.class);
    if(entryManager.isRendered()) entryManager.init();
    Utils.closePopupWindow(popupContainer, UINewsletterConstant.ENTRY_FORM_POPUP_WINDOW);
    
    return newsletterNode;
  }
  
  public static class PreviewActionListener extends EventListener<UINewsletterEntryForm> {
    public void execute(Event<UINewsletterEntryForm> event) throws Exception {
    }
  }

  public static class SaveActionListener extends EventListener<UINewsletterEntryForm> {
    public void execute(Event<UINewsletterEntryForm> event) throws Exception {
      UINewsletterEntryForm newsletterEntryForm = event.getSource();
      newsletterEntryForm.saveContent();
    }
  }

  public static class SendActionListener extends EventListener<UINewsletterEntryForm> {
    public void execute(Event<UINewsletterEntryForm> event) throws Exception {
      UINewsletterEntryForm newsletterEntryForm = event.getSource();
      Node newsletterNode = newsletterEntryForm.saveContent();
      Session session = newsletterNode.getSession();
      
      UINewsletterEntryContainer newsletterEntryContainer = newsletterEntryForm.getAncestorOfType(UINewsletterEntryContainer.class);
      UINewsletterEntryDialogSelector newsletterEntryDialogSelector = newsletterEntryContainer.getChild(UINewsletterEntryDialogSelector.class);
      Date currentDate = new Date();
      //DateFormat dateFormat = new SimpleDateFormat(ISO8601.SIMPLE_DATETIME_FORMAT);
      UIFormDateTimeInput formDateTimeInput = newsletterEntryDialogSelector.getChild(UIFormDateTimeInput.class);
      Calendar calendar = formDateTimeInput.getCalendar();
      
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
          NewsletterManagerService newsletterManagerService = newsletterEntryForm.getApplicationComponent(NewsletterManagerService.class); 
          message.setBody(newsletterManagerService.getEntryHandler().getContent(newsletterNode)) ;
          message.setMimeType("text/html") ;
          try {
            mailService.sendMessage(message);
          }catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
      session.save();
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
