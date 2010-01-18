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

import java.util.HashMap;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.utils.JCRExceptionManager;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.authoring.AuthoringPublicationConstant;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.ui.UIPublicationContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform
 * chuong_phan@exoplatform.com Mar 2, 2009
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
	@EventConfig(listeners = UIPublicationPanel.CloseActionListener.class) })
public class UIPublicationPanel extends org.exoplatform.services.wcm.publication.lifecycle.stageversion.ui.UIPublicationPanel {

    /**
     * Instantiates a new uI publication panel.
     * 
     * @throws Exception
     *             the exception
     */
    public UIPublicationPanel() throws Exception {
    }

    /**
     * The listener interface for receiving draftAction events. The class that
     * is interested in processing a draftAction event implements this
     * interface, and the object created with that class is registered with a
     * component using the component's
     * <code>addDraftActionListener<code> method. When
     * the draftAction event occurs, that object's appropriate
   * method is invoked.
     * 
     * @see DraftActionEvent
     */
    public static class ArchivedActionListener extends EventListener<UIPublicationPanel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.exoplatform.webui.event.EventListener#execute(org.exoplatform
	 * .webui.event.Event)
	 */
	public void execute(Event<UIPublicationPanel> event) throws Exception {
	    UIPublicationPanel publicationPanel = event.getSource();
	    Node currentNode = publicationPanel.getCurrentNode();
	    PublicationService publicationService = publicationPanel.getApplicationComponent(PublicationService.class);
	    PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(AuthoringPublicationConstant.LIFECYCLE_NAME);
	    HashMap<String, String> context = new HashMap<String, String>();
	    Node currentRevision = publicationPanel.getCurrentRevision();
	    if (currentRevision != null) {
		context.put(AuthoringPublicationConstant.CURRENT_REVISION_NAME, currentRevision.getName());
	    }
	    try {
		publicationPlugin.changeState(currentNode, PublicationDefaultStates.ARCHIVED, context);
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
     * The listener interface for receiving draftAction events. The class that
     * is interested in processing a draftAction event implements this
     * interface, and the object created with that class is registered with a
     * component using the component's
     * <code>addDraftActionListener<code> method. When
     * the draftAction event occurs, that object's appropriate
   * method is invoked.
     * 
     * @see DraftActionEvent
     */
    public static class UnpublishedActionListener extends EventListener<UIPublicationPanel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.exoplatform.webui.event.EventListener#execute(org.exoplatform
	 * .webui.event.Event)
	 */
	public void execute(Event<UIPublicationPanel> event) throws Exception {
	    UIPublicationPanel publicationPanel = event.getSource();
	    Node currentNode = publicationPanel.getCurrentNode();
	    PublicationService publicationService = publicationPanel.getApplicationComponent(PublicationService.class);
	    PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(AuthoringPublicationConstant.LIFECYCLE_NAME);
	    HashMap<String, String> context = new HashMap<String, String>();
	    Node currentRevision = publicationPanel.getCurrentRevision();
	    if (currentRevision != null) {
		context.put(AuthoringPublicationConstant.CURRENT_REVISION_NAME, currentRevision.getName());
	    }
	    try {
		publicationPlugin.changeState(currentNode, PublicationDefaultStates.UNPUBLISHED, context);
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
     * The listener interface for receiving draftAction events. The class that
     * is interested in processing a draftAction event implements this
     * interface, and the object created with that class is registered with a
     * component using the component's
     * <code>addDraftActionListener<code> method. When
     * the draftAction event occurs, that object's appropriate
   * method is invoked.
     * 
     * @see DraftActionEvent
     */
    public static class StagedActionListener extends EventListener<UIPublicationPanel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.exoplatform.webui.event.EventListener#execute(org.exoplatform
	 * .webui.event.Event)
	 */
	public void execute(Event<UIPublicationPanel> event) throws Exception {
	    UIPublicationPanel publicationPanel = event.getSource();
	    Node currentNode = publicationPanel.getCurrentNode();
	    PublicationService publicationService = publicationPanel.getApplicationComponent(PublicationService.class);
	    PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(AuthoringPublicationConstant.LIFECYCLE_NAME);
	    HashMap<String, String> context = new HashMap<String, String>();
	    Node currentRevision = publicationPanel.getCurrentRevision();
	    if (currentRevision != null) {
		context.put(AuthoringPublicationConstant.CURRENT_REVISION_NAME, currentRevision.getName());
	    }
	    try {
		publicationPlugin.changeState(currentNode, PublicationDefaultStates.STAGED, context);
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
     * The listener interface for receiving ApprovedAction events. The class
     * that is interested in processing a ApprovedAction event implements this
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
	 * 
	 * @see
	 * org.exoplatform.webui.event.EventListener#execute(org.exoplatform
	 * .webui.event.Event)
	 */
	public void execute(Event<UIPublicationPanel> event) throws Exception {
	    UIPublicationPanel publicationPanel = event.getSource();
	    Node currentNode = publicationPanel.getCurrentNode();
	    PublicationService publicationService = publicationPanel.getApplicationComponent(PublicationService.class);
	    PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(AuthoringPublicationConstant.LIFECYCLE_NAME);
	    HashMap<String, String> context = new HashMap<String, String>();
	    Node currentRevision = publicationPanel.getCurrentRevision();
	    if (currentRevision != null) {
		context.put(AuthoringPublicationConstant.CURRENT_REVISION_NAME, currentRevision.getName());
	    }
	    try {
		publicationPlugin.changeState(currentNode, PublicationDefaultStates.APPROVED, context);
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
	 * 
	 * @see
	 * org.exoplatform.webui.event.EventListener#execute(org.exoplatform
	 * .webui.event.Event)
	 */
	public void execute(Event<UIPublicationPanel> event) throws Exception {
	    UIPublicationPanel publicationPanel = event.getSource();
	    Node currentNode = publicationPanel.getCurrentNode();
	    PublicationService publicationService = publicationPanel.getApplicationComponent(PublicationService.class);
	    PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(AuthoringPublicationConstant.LIFECYCLE_NAME);
	    HashMap<String, String> context = new HashMap<String, String>();
	    Node currentRevision = publicationPanel.getCurrentRevision();
	    if (currentRevision != null) {
		context.put(AuthoringPublicationConstant.CURRENT_REVISION_NAME, currentRevision.getName());
	    }
	    try {
		publicationPlugin.changeState(currentNode, PublicationDefaultStates.PENDING, context);
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
     * The listener interface for receiving draftAction events. The class that
     * is interested in processing a draftAction event implements this
     * interface, and the object created with that class is registered with a
     * component using the component's
     * <code>addDraftActionListener<code> method. When
     * the draftAction event occurs, that object's appropriate
   * method is invoked.
     * 
     * @see DraftActionEvent
     */
    public static class DraftActionListener extends EventListener<UIPublicationPanel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.exoplatform.webui.event.EventListener#execute(org.exoplatform
	 * .webui.event.Event)
	 */
	public void execute(Event<UIPublicationPanel> event) throws Exception {
	    UIPublicationPanel publicationPanel = event.getSource();
	    Node currentNode = publicationPanel.getCurrentNode();
	    PublicationService publicationService = publicationPanel.getApplicationComponent(PublicationService.class);
	    PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(AuthoringPublicationConstant.LIFECYCLE_NAME);
	    HashMap<String, String> context = new HashMap<String, String>();
	    Node currentRevision = publicationPanel.getCurrentRevision();
	    if (currentRevision != null) {
		context.put(AuthoringPublicationConstant.CURRENT_REVISION_NAME, currentRevision.getName());
	    }
	    try {
		publicationPlugin.changeState(currentNode, PublicationDefaultStates.DRAFT, context);
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
     * interested in processing a liveAction event implements this interface,
     * and the object created with that class is registered with a component
     * using the component's <code>addLiveActionListener<code> method. When
     * the liveAction event occurs, that object's appropriate
   * method is invoked.
     * 
     * @see LiveActionEvent
     */
    public static class LiveActionListener extends EventListener<UIPublicationPanel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.exoplatform.webui.event.EventListener#execute(org.exoplatform
	 * .webui.event.Event)
	 */
	public void execute(Event<UIPublicationPanel> event) throws Exception {
	    UIPublicationPanel publicationPanel = event.getSource();
	    Node currentNode = publicationPanel.getCurrentNode();
	    PublicationService publicationService = publicationPanel.getApplicationComponent(PublicationService.class);
	    PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(AuthoringPublicationConstant.LIFECYCLE_NAME);
	    HashMap<String, String> context = new HashMap<String, String>();
	    Node currentRevision = publicationPanel.getCurrentRevision();
	    if (currentRevision != null) {
		context.put(AuthoringPublicationConstant.CURRENT_REVISION_NAME, currentRevision.getName());
	    }
	    try {
		publicationPlugin.changeState(currentNode, PublicationDefaultStates.PUBLISHED, context);
		publicationPanel.updatePanel();
	    } catch (Exception e) {
		UIApplication uiApp = publicationPanel.getAncestorOfType(UIApplication.class);
		JCRExceptionManager.process(uiApp, e);
	    }
	    UIPublicationContainer publicationContainer = publicationPanel.getAncestorOfType(UIPublicationContainer.class);
	    publicationContainer.setActiveTab(publicationPanel, event.getRequestContext());
	}
    }

}
