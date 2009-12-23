/*
 * Copyright (C) 2003-2008 eXo Platform SAS. This program is free software; you
 * can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version. This program
 * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wcm.webui.search;

import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Nov 10, 2008
 */
@ComponentConfig(
	lifecycle = Lifecycle.class, 
	template = "app:/groovy/webui/wcm-search/UISearchBoxContainer.gtmpl", 
	events = { 
		@EventConfig(listeners = UISearchBoxContainer.QuickEditActionListener.class) 
	}
)
public class UISearchBoxContainer extends UIContainer {

	/** The Constant SEARCH_BOX. */
	public static final String	SEARCH_BOX	= "uiSearchBox".intern();

	/**
	 * Instantiates a new uI search box container.
	 * 
	 * @throws Exception the exception
	 */
	public UISearchBoxContainer() throws Exception {
		/*PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
		PortletPreferences portletPreferences = context.getRequest().getPreferences();
		String searchBoxTemplatePath = portletPreferences.getValue(	UIWCMSearchPortlet.SEARCH_BOX_TEMPLATE_PATH, null);
		UISearchBox uiSearchBox = addChild(UISearchBox.class, null, SEARCH_BOX);
		uiSearchBox.setTemplatePath(searchBoxTemplatePath);*/
	}

	/**
	 * Gets the portlet id.
	 * 
	 * @return the portlet id
	 */
	public String getPortletId() {
		PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
		return pContext.getWindowId();
	}

	/**
	 * The listener interface for receiving quickEditAction events. The class that
	 * is interested in processing a quickEditAction event implements this
	 * interface, and the object created with that class is registered with a
	 * component using the component's <code>addQuickEditActionListener<code>
	 * method. When the quickEditAction event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see QuickEditActionEvent
	 */
	public static class QuickEditActionListener extends EventListener<UISearchBoxContainer> {

		/*
		 * (non-Javadoc)
		 * @see
		 * org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui
		 * .event.Event)
		 */
		public void execute(Event<UISearchBoxContainer> event) throws Exception {
			PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
			context.setApplicationMode(PortletMode.EDIT);
		}
	}
}
