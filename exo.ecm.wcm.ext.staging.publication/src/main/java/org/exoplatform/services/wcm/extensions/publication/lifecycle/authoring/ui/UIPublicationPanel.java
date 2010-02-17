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
package org.exoplatform.services.wcm.extensions.publication.lifecycle.authoring.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exoplatform.ecm.webui.utils.JCRExceptionManager;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.wcm.extensions.publication.PublicationManager;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.authoring.AuthoringPublicationConstant;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.impl.LifecyclesConfig.Lifecycle;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.impl.LifecyclesConfig.State;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationConstant;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.ui.UIPublicationContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormDateTimeInput;

/**
 * Created by The eXo Platform MEA Author : haikel.thamri@exoplatform.com
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "classpath:groovy/wcm/webui/publication/lifecycle/authoring/ui/UIPublicationPanel.gtmpl", events = {
    @EventConfig(listeners = UIPublicationPanel.DraftActionListener.class),
    @EventConfig(listeners = UIPublicationPanel.ArchivedActionListener.class),
    @EventConfig(listeners = UIPublicationPanel.UnpublishedActionListener.class),
    @EventConfig(listeners = UIPublicationPanel.StagedActionListener.class),
    @EventConfig(listeners = UIPublicationPanel.ApprovedActionListener.class),
    @EventConfig(listeners = UIPublicationPanel.PendingActionListener.class),
    @EventConfig(listeners = UIPublicationPanel.LiveActionListener.class),
    @EventConfig(name = "obsolete", listeners = UIPublicationPanel.ObsoleteActionListener.class),
    @EventConfig(listeners = UIPublicationPanel.ChangeVersionActionListener.class),
    @EventConfig(listeners = UIPublicationPanel.PreviewVersionActionListener.class),
    @EventConfig(listeners = UIPublicationPanel.RestoreVersionActionListener.class),
    @EventConfig(listeners = UIPublicationPanel.SeeAllVersionActionListener.class),
    @EventConfig(listeners = UIPublicationPanel.SaveActionListener.class),
    @EventConfig(listeners = UIPublicationPanel.CloseActionListener.class) })
public class UIPublicationPanel
                               extends
                               org.exoplatform.services.wcm.publication.lifecycle.stageversion.ui.UIPublicationPanel {
  public static final String START_PUBLICATION = "UIPublicationPanelStartDateInput";

  public static final String END_PUBLICATION   = "UIPublicationPanelEndDateInput";

  public static final Log    LOG               = LogFactory.getLog(UIPublicationPanel.class);

  /**
   * Instantiates a new uI publication panel.
   * 
   * @throws Exception the exception
   */
  public UIPublicationPanel() throws Exception {
    addUIFormInput(new UIFormDateTimeInput(START_PUBLICATION, START_PUBLICATION, null));
    addUIFormInput(new UIFormDateTimeInput(END_PUBLICATION, END_PUBLICATION, null));
    setActions(new String[] { "Save", "Close" });
  }

  public void init(Node node) throws Exception {
    Calendar startDate = null;
    Calendar endDate = null;
    String nodeVersionUUID = null;
    super.init(node);
    if (PublicationDefaultStates.PUBLISHED.equals(node.getProperty(StageAndVersionPublicationConstant.CURRENT_STATE)
                                                      .getString())) {
      nodeVersionUUID = node.getProperty(StageAndVersionPublicationConstant.LIVE_REVISION_PROP)
                            .getString();
      if (!"".equals(nodeVersionUUID)) {
        Node revision = this.getRevisionByUUID(nodeVersionUUID);
        this.setCurrentRevision(revision);
      }
    }
    if (node.hasProperty(AuthoringPublicationConstant.END_TIME_PROPERTY)) {
      endDate = node.getProperty(AuthoringPublicationConstant.END_TIME_PROPERTY).getDate();
    }
    if (node.hasProperty(AuthoringPublicationConstant.START_TIME_PROPERTY)) {
      startDate = node.getProperty(AuthoringPublicationConstant.START_TIME_PROPERTY).getDate();
    }
    if (startDate != null) {
      ((UIFormDateTimeInput) getChildById(START_PUBLICATION)).setCalendar(startDate);
    }
    if (endDate != null) {
      ((UIFormDateTimeInput) getChildById(END_PUBLICATION)).setCalendar(endDate);
    }
    
  }

  /**
   * The listener interface for receiving draftAction events. The class that is
   * interested in processing a draftAction event implements this interface, and
   * the object created with that class is registered with a component using the
   * component's <code>addDraftActionListener<code> method. When
   * the draftAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see DraftActionEvent
   */
  public static class ArchivedActionListener extends EventListener<UIPublicationPanel> {

    /*
     * (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform
     * .webui.event.Event)
     */
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();
      Node currentNode = publicationPanel.getCurrentNode();
      PublicationService publicationService = publicationPanel.getApplicationComponent(PublicationService.class);
      PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins()
                                                              .get(AuthoringPublicationConstant.LIFECYCLE_NAME);
      HashMap<String, String> context = new HashMap<String, String>();
      Node currentRevision = publicationPanel.getCurrentRevision();
      if (currentRevision != null) {
        context.put(AuthoringPublicationConstant.CURRENT_REVISION_NAME, currentRevision.getName());
      }
      try {
        publicationPlugin.changeState(currentNode, PublicationDefaultStates.ARCHIVED, context);
        currentNode.setProperty("publication:lastUser", event.getRequestContext().getRemoteUser());
        publicationPanel.updatePanel();
      } catch (Exception e) {
        UIApplication uiApp = publicationPanel.getAncestorOfType(UIApplication.class);
        JCRExceptionManager.process(uiApp, e);
      }
      UIPublicationContainer publicationContainer = publicationPanel.getAncestorOfType(UIPublicationContainer.class);
      publicationContainer.setActiveTab(publicationPanel, event.getRequestContext());
    }
  }

  /**
   * The listener interface for receiving draftAction events. The class that is
   * interested in processing a draftAction event implements this interface, and
   * the object created with that class is registered with a component using the
   * component's <code>addDraftActionListener<code> method. When
   * the draftAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see DraftActionEvent
   */
  public static class UnpublishedActionListener extends EventListener<UIPublicationPanel> {

    /*
     * (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform
     * .webui.event.Event)
     */
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();
      Node currentNode = publicationPanel.getCurrentNode();
      PublicationService publicationService = publicationPanel.getApplicationComponent(PublicationService.class);
      PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins()
                                                              .get(AuthoringPublicationConstant.LIFECYCLE_NAME);
      HashMap<String, String> context = new HashMap<String, String>();
      Node currentRevision = publicationPanel.getCurrentRevision();
      if (currentRevision != null) {
        context.put(AuthoringPublicationConstant.CURRENT_REVISION_NAME, currentRevision.getName());
      }
      try {
        publicationPlugin.changeState(currentNode, PublicationDefaultStates.UNPUBLISHED, context);
        currentNode.setProperty("publication:lastUser", event.getRequestContext().getRemoteUser());
        publicationPanel.updatePanel();
      } catch (Exception e) {
        UIApplication uiApp = publicationPanel.getAncestorOfType(UIApplication.class);
        JCRExceptionManager.process(uiApp, e);
      }
      UIPublicationContainer publicationContainer = publicationPanel.getAncestorOfType(UIPublicationContainer.class);
      publicationContainer.setActiveTab(publicationPanel, event.getRequestContext());
    }
  }

  /**
   * The listener interface for receiving draftAction events. The class that is
   * interested in processing a draftAction event implements this interface, and
   * the object created with that class is registered with a component using the
   * component's <code>addDraftActionListener<code> method. When
   * the draftAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see DraftActionEvent
   */
  public static class StagedActionListener extends EventListener<UIPublicationPanel> {

    /*
     * (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform
     * .webui.event.Event)
     */
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();
      Node currentNode = publicationPanel.getCurrentNode();
      PublicationService publicationService = publicationPanel.getApplicationComponent(PublicationService.class);
      PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins()
                                                              .get(AuthoringPublicationConstant.LIFECYCLE_NAME);
      HashMap<String, String> context = new HashMap<String, String>();
      Node currentRevision = publicationPanel.getCurrentRevision();
      if (currentRevision != null) {
        context.put(AuthoringPublicationConstant.CURRENT_REVISION_NAME, currentRevision.getName());
      }
      try {
        publicationPlugin.changeState(currentNode, PublicationDefaultStates.STAGED, context);
        currentNode.setProperty("publication:lastUser", event.getRequestContext().getRemoteUser());
        publicationPanel.updatePanel();
      } catch (Exception e) {
        UIApplication uiApp = publicationPanel.getAncestorOfType(UIApplication.class);
        JCRExceptionManager.process(uiApp, e);
      }
      UIPublicationContainer publicationContainer = publicationPanel.getAncestorOfType(UIPublicationContainer.class);
      publicationContainer.setActiveTab(publicationPanel, event.getRequestContext());
    }
  }

  /**
   * The listener interface for receiving ApprovedAction events. The class that
   * is interested in processing a ApprovedAction event implements this
   * interface, and the object created with that class is registered with a
   * component using the component's
   * <code>addApprovedActionListener<code> method. When
   * the ApprovedAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see ApprovedActionEvent
   */
  public static class ApprovedActionListener extends EventListener<UIPublicationPanel> {

    /*
     * (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform
     * .webui.event.Event)
     */
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();
      Node currentNode = publicationPanel.getCurrentNode();
      PublicationService publicationService = publicationPanel.getApplicationComponent(PublicationService.class);
      PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins()
                                                              .get(AuthoringPublicationConstant.LIFECYCLE_NAME);
      HashMap<String, String> context = new HashMap<String, String>();
      Node currentRevision = publicationPanel.getCurrentRevision();
      if (currentRevision != null) {
        context.put(AuthoringPublicationConstant.CURRENT_REVISION_NAME, currentRevision.getName());
      }
      try {
        publicationPlugin.changeState(currentNode, PublicationDefaultStates.APPROVED, context);
        currentNode.setProperty("publication:lastUser", event.getRequestContext().getRemoteUser());
        publicationPanel.updatePanel();
      } catch (Exception e) {
        UIApplication uiApp = publicationPanel.getAncestorOfType(UIApplication.class);
        JCRExceptionManager.process(uiApp, e);
      }
      UIPublicationContainer publicationContainer = publicationPanel.getAncestorOfType(UIPublicationContainer.class);
      publicationContainer.setActiveTab(publicationPanel, event.getRequestContext());
    }
  }

  /**
   * The listener interface for receiving PendingAction events. The class that
   * is interested in processing a PendingAction event implements this
   * interface, and the object created with that class is registered with a
   * component using the component's
   * <code>addPendingActionListener<code> method. When
   * the PendingAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see PendingAction
   */
  public static class PendingActionListener extends EventListener<UIPublicationPanel> {

    /*
     * (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform
     * .webui.event.Event)
     */
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();
      Node currentNode = publicationPanel.getCurrentNode();
      PublicationService publicationService = publicationPanel.getApplicationComponent(PublicationService.class);
      PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins()
                                                              .get(AuthoringPublicationConstant.LIFECYCLE_NAME);
      HashMap<String, String> context = new HashMap<String, String>();
      Node currentRevision = publicationPanel.getCurrentRevision();
      if (currentRevision != null) {
        context.put(AuthoringPublicationConstant.CURRENT_REVISION_NAME, currentRevision.getName());
      }
      try {
        publicationPlugin.changeState(currentNode, PublicationDefaultStates.PENDING, context);
        currentNode.setProperty("publication:lastUser", event.getRequestContext().getRemoteUser());
        publicationPanel.updatePanel();
      } catch (Exception e) {
        UIApplication uiApp = publicationPanel.getAncestorOfType(UIApplication.class);
        JCRExceptionManager.process(uiApp, e);
      }
      UIPublicationContainer publicationContainer = publicationPanel.getAncestorOfType(UIPublicationContainer.class);
      publicationContainer.setActiveTab(publicationPanel, event.getRequestContext());
    }
  }

  /**
   * The listener interface for receiving draftAction events. The class that is
   * interested in processing a draftAction event implements this interface, and
   * the object created with that class is registered with a component using the
   * component's <code>addDraftActionListener<code> method. When
   * the draftAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see DraftActionEvent
   */
  public static class DraftActionListener extends EventListener<UIPublicationPanel> {

    /*
     * (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform
     * .webui.event.Event)
     */
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();
      Node currentNode = publicationPanel.getCurrentNode();
      PublicationService publicationService = publicationPanel.getApplicationComponent(PublicationService.class);
      PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins()
                                                              .get(AuthoringPublicationConstant.LIFECYCLE_NAME);
      HashMap<String, String> context = new HashMap<String, String>();
      Node currentRevision = publicationPanel.getCurrentRevision();
      if (currentRevision != null) {
        context.put(AuthoringPublicationConstant.CURRENT_REVISION_NAME, currentRevision.getName());
      }
      try {
        publicationPlugin.changeState(currentNode, PublicationDefaultStates.DRAFT, context);
        currentNode.setProperty("publication:lastUser", event.getRequestContext().getRemoteUser());
        publicationPanel.updatePanel();
      } catch (Exception e) {
        UIApplication uiApp = publicationPanel.getAncestorOfType(UIApplication.class);
        JCRExceptionManager.process(uiApp, e);
      }
      UIPublicationContainer publicationContainer = publicationPanel.getAncestorOfType(UIPublicationContainer.class);
      publicationContainer.setActiveTab(publicationPanel, event.getRequestContext());
    }
  }

  /**
   * The listener interface for receiving liveAction events. The class that is
   * interested in processing a liveAction event implements this interface, and
   * the object created with that class is registered with a component using the
   * component's <code>addLiveActionListener<code> method. When
   * the liveAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see LiveActionEvent
   */
  public static class LiveActionListener extends EventListener<UIPublicationPanel> {

    /*
     * (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform
     * .webui.event.Event)
     */
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();
      Node currentNode = publicationPanel.getCurrentNode();
      PublicationService publicationService = publicationPanel.getApplicationComponent(PublicationService.class);
      PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins()
                                                              .get(AuthoringPublicationConstant.LIFECYCLE_NAME);
      HashMap<String, String> context = new HashMap<String, String>();
      Node currentRevision = publicationPanel.getCurrentRevision();
      if (currentRevision != null) {
        context.put(AuthoringPublicationConstant.CURRENT_REVISION_NAME, currentRevision.getName());
      }
      try {
        publicationPlugin.changeState(currentNode, PublicationDefaultStates.PUBLISHED, context);
        currentNode.setProperty("publication:lastUser", event.getRequestContext().getRemoteUser());
        publicationPanel.updatePanel();
      } catch (Exception e) {
        UIApplication uiApp = publicationPanel.getAncestorOfType(UIApplication.class);
        JCRExceptionManager.process(uiApp, e);
      }
      UIPublicationContainer publicationContainer = publicationPanel.getAncestorOfType(UIPublicationContainer.class);
      publicationContainer.setActiveTab(publicationPanel, event.getRequestContext());
    }
  }

  public static class SaveActionListener extends EventListener<UIPublicationPanel> {
    public void execute(Event<UIPublicationPanel> event) throws Exception {
      UIPublicationPanel publicationPanel = event.getSource();
      UIFormDateTimeInput startPublication = publicationPanel.getChildById(START_PUBLICATION);
      UIFormDateTimeInput endPublication = publicationPanel.getChildById(END_PUBLICATION);
      Calendar startDate = startPublication.getCalendar();
      Calendar endDate = endPublication.getCalendar();
      Node node = publicationPanel.getCurrentNode();
      try {
        if (!"".equals(startPublication.getValue())) {
          startDate.getTime();
          node.setProperty(AuthoringPublicationConstant.START_TIME_PROPERTY, startDate);
          node.getSession().save();
        }
        if (!"".equals(endPublication.getValue())) {
          endDate.getTime();
          node.setProperty(AuthoringPublicationConstant.END_TIME_PROPERTY, endDate);
          node.getSession().save();
        }
      } catch (NullPointerException e) {
        UIApplication uiApp = publicationPanel.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UIPublicationPanel.msg.invalid-format", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      UIPopupContainer uiPopupContainer = (UIPopupContainer) publicationPanel.getAncestorOfType(UIPopupContainer.class);
      uiPopupContainer.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer);
    }
  }

  public List<State> getStates(Node cNode) throws Exception {
    List<State> states = new ArrayList<State>();
    String lifecycleName = getLifeCycle(cNode);
    PublicationManager publicationManagerImpl = getApplicationComponent(PublicationManager.class);
    Lifecycle lifecycle = publicationManagerImpl.getLifecycle(lifecycleName);
    states = lifecycle.getStates();
    return states;
  }

  private String getLifeCycle(Node cNode) throws Exception {
    String lifecycleName = null;
    try {
      lifecycleName = cNode.getProperty("publication:lifecycle").getString();
    } catch (Exception e) {
      LOG.error("Failed to get States for node " + cNode, e);
    }
    return lifecycleName;
  }

}
