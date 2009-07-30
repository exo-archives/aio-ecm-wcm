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
package org.exoplatform.wcm.webui.pclv;

import javax.portlet.PortletMode;

import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform
 * chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com Jun 19, 2009
 */
@ComponentConfig(
	lifecycle = UIApplicationLifecycle.class
)
public class UIPCLVPortlet extends UIPortletApplication {

	/** The Constant PARAMETERIZED_MANAGEMENT_PORTLET_POPUP_WINDOW. */
	public static final String	PARAMETERIZED_MANAGEMENT_PORTLET_POPUP_WINDOW	= "UIParameterizedManagerPopupWindow";

	/** The Constant PARAMETERIZED_TARGET_PAGE_POPUP_WINDOW. */
	public static final String	PARAMETERIZED_TARGET_PAGE_POPUP_WINDOW				= "TagetPageInputPopupWindowForm";

	/** The Constant REPOSITORY. */
	public final static String	PREFERENCE_REPOSITORY													= "repository";

	/** The Constant PREFERENCE_TREE_NAME. */
	public final static String	PREFERENCE_TREE_NAME													= "treeName";

	/** The Constant WORKSPACE. */
	public final static String	WORKSPACE																			= "workspace";

	/** The Constant VIEWER_MODE. */
	public static final String	VIEWER_MODE																		= "mode";

	/** The Constant HEADER. */
	public final static String	HEADER																				= "header";

	/** The Constant SHOW_READMORE. */
	public final static String	SHOW_READMORE																	= "header";

	/** The Constant ORDER_TYPE. */
	public static final String	ORDER_TYPE																		= "orderType";

	/** The Constant ORDER_BY. */
	public static final String	ORDER_BY																			= "orderBy";

	/** The Constant ITEMS_PER_PAGE. */
	public final static String	ITEMS_PER_PAGE																= "itemsPerPage";

	/** The Constant SHOW_LINK. */
	public final static String	SHOW_LINK																			= "showLink";

	/** The Constant FORM_VIEW_TEMPLATE_PATH. */
	public final static String	FORM_VIEW_TEMPLATE_PATH												= "formViewTemplatePath";

	/** The Constant PAGINATOR_TEMPlATE_PATH. */
	public final static String	PAGINATOR_TEMPlATE_PATH												= "paginatorTemplatePath";

	/** The Constant SHOW_HEADER. */
	public final static String	SHOW_HEADER																		= "showHeader";

	/** The Constant SHOW_REFRESH_BUTTON. */
	public final static String	SHOW_REFRESH_BUTTON														= "showRefreshButton";

	/** The Constant SHOW_SUMMARY. */
	public final static String	SHOW_SUMMARY																	= "showSummary";

	/** The Constant SHOW_THUMBNAILS_VIEW. */
	public final static String	SHOW_THUMBNAILS_VIEW													= "showThumbnailsView";

	/** The Constant SHOW_TITLE. */
	public final static String	SHOW_TITLE																		= "showTitle";

	/** The Constant SHOW_DATE_CREATED. */
	public final static String	SHOW_DATE_CREATED															= "showDateCreated";

	/** The Constant SHOW_MORE_LINK. */
	public final static String	SHOW_MORE_LINK																= "showMoreLink";

	/** The Constant SHOW_RSS_LINK. */
	public final static String	SHOW_RSS_LINK																	= "showRssLink";

	/** The Constant SHOW_AUTO_DETECT. */
	public final static String	SHOW_AUTO_DETECT															= "showAutoDetect";

	/** The Constant TARGET_PAGE. */
	public final static String	TARGET_PAGE																		= "targetPage";

	/** The Constant FOLDER_PATH. */
	public final static String	FOLDER_PATH																		= "folderPath";

	/** The Constant REPOSITORY. */
	public final static String	REPOSITORY																		= "repository";

	/** The Constant PARAMETERIZED_CONTENT_LIST_VIEWER_FORM. */
	public final static String	PARAMETERIZED_CONTENT_LIST_VIEWER_FORM				= "UIParameterizedContentListViewerForm";

	/** The mode. */
	private PortletMode					mode																					= PortletMode.VIEW;

	public UIPCLVPortlet() throws Exception {
		activateMode(mode);
	}

	public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
		PortletRequestContext pContext = (PortletRequestContext) context;
		PortletMode newMode = pContext.getApplicationMode();
		if (!mode.equals(newMode)) {
			activateMode(newMode);
			mode = newMode;
		}
		super.processRender(app, context);
	}

	/**
	 * Activate mode.
	 * 
	 * @param mode the mode
	 * @throws Exception the exception
	 */
	private void activateMode(PortletMode mode) throws Exception {
		getChildren().clear();
		addChild(UIPopupContainer.class, null, null);
		if (PortletMode.VIEW.equals(mode)) {
			UIPCLVContainer container = addChild(UIPCLVContainer.class, null, null);
			container.init();
		} else if (PortletMode.EDIT.equals(mode)) {
			addChild(UIPCLVConfig.class, null, null);
		}
	}
}
