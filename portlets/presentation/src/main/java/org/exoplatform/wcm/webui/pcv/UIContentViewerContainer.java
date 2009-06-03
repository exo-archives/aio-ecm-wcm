/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.wcm.webui.pcv;

import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.security.AccessControlException;
import java.util.HashMap;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.portlet.PortletMode;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.publication.PublicationState;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.Constant;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.Constant.SITE_MODE;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.WebUIPropertiesConfigService;
import org.exoplatform.wcm.webui.WebUIPropertiesConfigService.PopupWindowProperties;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS Author : Phan Le Thanh Chuong
 * chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com Nov 4, 2008
 */

@ComponentConfig(
  lifecycle = Lifecycle.class, 
  template = "app:/groovy/ParameterizedContentViewer/UIContentViewerContainer.gtmpl", 
  events = { 
    @EventConfig(listeners = UIContentViewerContainer.QuickEditActionListener.class) 
  }
)
public class UIContentViewerContainer extends UIContainer {

	/** Flag indicating the draft revision. */
	private boolean isDraftRevision = false;

	/** Flag indicating the obsolete revision. */
	private boolean isObsoletedContent = false;

	/** Content child of this content. */
	private UIContentViewer uiContentViewer;

	/**
	 * A flag used to display Print/Close buttons and hide Back one if its'
	 * value is <code>true</code>. In <code>false</code> case, the Back
	 * button will be shown only
	 */
	private boolean isPrint;

	/**
	 * Instantiates a new uI content viewer container.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public UIContentViewerContainer() throws Exception {
		addChild(UIContentViewer.class, null, null);
		uiContentViewer = getChild(UIContentViewer.class);
	}

	@Override
	public void processRender(WebuiRequestContext context) throws Exception {
		// PortletRequestContext portletRequestContext = (PortletRequestContext)
		// requestContext;
		// PortalRequestContext context = (PortalRequestContext)
		// portletRequestContext
		// .getParentAppRequestContext();

		PortletRequestContext porletRequestContext = (PortletRequestContext) context;
		HttpServletRequestWrapper requestWrapper = (HttpServletRequestWrapper) porletRequestContext
				.getRequest();
		PortalRequestContext portalRequestContext = Util
				.getPortalRequestContext();
		UIPortal uiPortal = Util.getUIPortal();
		String portalURI = portalRequestContext.getPortalURI();
		String requestURI = requestWrapper.getRequestURI();
		String pageNodeSelected = uiPortal.getSelectedNode().getName();
		String parameters = null;
		Object object = requestWrapper
				.getAttribute("ParameterizedContentViewerPortlet.data.object");

		try {
			parameters = URLDecoder.decode(StringUtils.substringAfter(
					requestURI, portalURI.concat(pageNodeSelected + "/")),
					"UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		if (!parameters.matches(UIContentViewer.PARAMETER_REGX)) {
			renderErrorMessage(context,
					UIContentViewer.CONTENT_NOT_FOUND_EXC);
			return;
		}
		String nodeIdentifier = null;
		String[] params = parameters.split("/");
		String repository = params[0];
		String workspace = params[1];
		Node currentNode = null;
		ThreadLocalSessionProviderService providerService = getApplicationComponent(ThreadLocalSessionProviderService.class);
		SessionProvider sessionProvider = providerService.getSessionProvider(null);
		Session session = null;		
		if (object instanceof ItemNotFoundException
				|| object instanceof AccessControlException
				|| object instanceof ItemNotFoundException || object == null) {
			try {
				RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
				ManageableRepository manageableRepository = repositoryService
						.getRepository(repository);
				session = sessionProvider.getSession(workspace,
						manageableRepository);
			} catch (AccessControlException ace) {
				renderErrorMessage(context,
						UIContentViewer.ACCESS_CONTROL_EXC);
				return;
			} catch (Exception e) {
				renderErrorMessage(context,
						UIContentViewer.CONTENT_NOT_FOUND_EXC);
				return;
			}
			if (params.length > 2) {
				StringBuffer identifier = new StringBuffer();
				for (int i = 2; i < params.length; i++) {
					identifier.append("/").append(params[i]);
				}
				nodeIdentifier = identifier.toString();
				boolean isUUID = false;
				try {
					currentNode = (Node) session.getItem(nodeIdentifier);
				} catch (Exception e) {
					isUUID = true;
				}
				if (isUUID) {
					try {
						String uuid = params[params.length - 1];
						currentNode = session.getNodeByUUID(uuid);
					} catch (ItemNotFoundException exc) {
						renderErrorMessage(context,
								UIContentViewer.CONTENT_NOT_FOUND_EXC);
						return;
					}
				}
			} else if (params.length == 2) {
				currentNode = session.getRootNode();
			}
		} else {
			currentNode = (Node) object;
		}

		TemplateService templateService = getApplicationComponent(TemplateService.class);
		List<String> documentTypes = templateService
				.getDocumentTemplates(repository);
		Boolean isDocumentType = false;
		for (String docType : documentTypes) {
			if (currentNode.isNodeType(docType)) {
				isDocumentType = true;
				break;
			}
		}
		if (currentNode.isNodeType("exo:hiddenable")) {
			renderErrorMessage(context,
					UIContentViewer.ACCESS_CONTROL_EXC);
			return;
		} else if (isDocumentType) { // content is document
			if (hasChildren()) {
				removeChild(UIContentViewerContainer.class);
			}
			PublicationService publicationService = uiPortal
					.getApplicationComponent(PublicationService.class);
			HashMap<String, Object> hmContext = new HashMap<String, Object>();
			if (Utils.isLiveMode()) {
				hmContext.put(Constant.RUNTIME_MODE, SITE_MODE.LIVE);
			} else {
				hmContext.put(Constant.RUNTIME_MODE, SITE_MODE.EDITING);
			}
			String lifeCycleName = publicationService
					.getNodeLifecycleName(currentNode);
			PublicationPlugin publicationPlugin = publicationService
					.getPublicationPlugins().get(lifeCycleName);
			if (publicationPlugin == null) {
				renderErrorMessage(context,
						UIContentViewer.CONTENT_NOT_PRINTED);
				return;
			}
			Node nodeView = publicationPlugin.getNodeView(currentNode,hmContext);
      boolean isLiveMode = Utils.isLiveMode();
      if(isLiveMode) {
        if(nodeView == null) {
          renderErrorMessage(context,UIContentViewer.CONTENT_NOT_PRINTED);
          return;
        }
        uiContentViewer.setNode(nodeView);
      }else {
        uiContentViewer.setNode(currentNode);
      }
			uiContentViewer.setRepository(repository);
			uiContentViewer.setWorkspace(workspace);
			uiContentViewer.setOrginalNode(currentNode);     			
			String state = PublicationState.getRevisionState(currentNode);
			if (Constant.OBSOLETE_STATE.equals(state)) {
				setObsoletedContent(true);
				renderErrorMessage(context,
						UIContentViewer.OBSOLETE_CONTENT);
				return;
			} else {
				setObsoletedContent(false);
				if (Constant.DRAFT_STATE.equals(state) && !isLiveMode) {
					setDraftRevision(true);
				} else {
					setDraftRevision(false);
				}
			}
			 HttpServletRequest request = context.getRequest();
			 isPrint = "isPrint=true".equals(request.getQueryString()) ? true :false;
			 super.processRender(context);
		} else { // content is folders
			renderErrorMessage(context, UIContentViewer.CONTENT_UNSUPPORT_EXC);
		}
	}

	/**
	 * Checks if is quick edit able.
	 * 
	 * @return true, if is quick edit able
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public boolean isQuickEditAble() throws Exception {
		PortletRequestContext context = (PortletRequestContext) WebuiRequestContext
				.getCurrentInstance();
		return Utils.turnOnQuickEditable(context, true);
	}

	/**
	 * The listener interface for receiving quickEditAction events. The class
	 * that is interested in processing a quickEditAction event implements this
	 * interface, and the object created with that class is registered with a
	 * component using the component's
	 * <code>addQuickEditActionListener<code> method. When
	 * the quickEditAction event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see QuickEditActionEvent
	 */
	public static class QuickEditActionListener extends
			EventListener<UIContentViewerContainer> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
		 */
		public void execute(Event<UIContentViewerContainer> event) throws Exception {
			UIContentViewerContainer uiContentViewerContainer = event.getSource();
			UIContentViewer uiContentViewer = uiContentViewerContainer.getChild(UIContentViewer.class);
			Node orginialNode = uiContentViewer.getOriginalNode();
			ManageableRepository manageableRepository = (ManageableRepository) orginialNode.getSession().getRepository();
			String repository = manageableRepository.getConfiguration().getName();
			String workspace = orginialNode.getSession().getWorkspace().getName();
			UIDocumentDialogForm uiDocumentForm = uiContentViewerContainer.createUIComponent(UIDocumentDialogForm.class, null, null);
			uiDocumentForm.setRepositoryName(repository);
			uiDocumentForm.setWorkspace(workspace);
			uiDocumentForm.setContentType(orginialNode.getPrimaryNodeType().getName());
			uiDocumentForm.setNodePath(orginialNode.getPath());
			uiDocumentForm.setStoredPath(orginialNode.getParent().getPath());
			uiDocumentForm.addNew(false);
			uiContentViewerContainer.addChild(uiDocumentForm);
			
		  UIParameterizedContentViewerPortlet uiportlet = uiContentViewerContainer.getAncestorOfType(UIParameterizedContentViewerPortlet.class);
		  uiportlet.activateMode(PortletMode.EDIT);
		  UIPopupContainer maskPopupContainer = uiportlet.getChild(UIPopupContainer.class);
		  WebUIPropertiesConfigService propertiesConfigService = uiContentViewerContainer.getApplicationComponent(WebUIPropertiesConfigService.class);
      PopupWindowProperties popupProperties = (PopupWindowProperties)propertiesConfigService.getProperties(WebUIPropertiesConfigService.SCV_POPUP_SIZE_QUICK_EDIT);
      maskPopupContainer.activate(uiDocumentForm, popupProperties.getWidth(), popupProperties.getHeight());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiportlet);
		}
	}

	/**
	 * Render error message.
	 * 
	 * @param context
	 *            the context
	 * @param keyBundle
	 *            the key bundle
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void renderErrorMessage(WebuiRequestContext context, String keyBundle)
			throws Exception {
		Writer writer = context.getWriter();
		String message = context.getApplicationResourceBundle().getString(
				keyBundle);
		writer
				.write("<div style=\"height: 55px; font-size: 13px; text-align: center; padding-top: 10px;\">");
		writer.write("<span>");
		writer.write(message);
		writer.write("</span>");
		writer.write("</div>");
		writer.close();
	}

	/**
	 * Gets <code>isPrint</code> value that is used to display Print/Close
	 * buttons and hide Back one if its' value is <code>True</code>. In
	 * <code>False</code> case, the Back button will be shown only.
	 * 
	 * @return <code>isPrint</code>
	 */
	public boolean getIsPrint() {
		return isPrint;
	}

	/**
	 * Sets <code>isPrint</code> value that is used to display Print/Close
	 * buttons and hide Back one if its' value is <code>True</code>. In
	 * <code>False</code> case, the Back button will be shown only.
	 */
	public void setIsPrint(boolean isPrint) {
		this.isPrint = isPrint;
	}

	/**
	 * Gets the draft revision value. If the revision is draft, an icon and one
	 * text is shown. Otherwise, false.
	 * 
	 * @return <code>isDraftRevision</code>
	 */
	public boolean isDraftRevision() {
		return isDraftRevision;
	}

	/**
	 * Sets the draft revision value. If the revision is draft, an icon and one
	 * text is shown. Otherwise, false.
	 * 
	 * @param <code>isDraftRevision</code>
	 */
	public void setDraftRevision(boolean isDraftRevision) {
		this.isDraftRevision = isDraftRevision;
	}

	/**
	 * Gets the draft obsolete value. If the revision is draft, the message is
	 * shown to inform users of this state. Otherwise, false.
	 * 
	 * @return <code>isDraftRevision</code>
	 */
	public boolean isObsoletedContent() {
		return isObsoletedContent;
	}

	/**
	 * Sets the draft obsolete value. If the revision is draft, the message is
	 * shown to inform users of this state. Otherwise, false.
	 * 
	 * @param <code>isObsoletedContent</code>
	 */
	public void setObsoletedContent(boolean isObsoletedContent) {
		this.isObsoletedContent = isObsoletedContent;
	}
}
