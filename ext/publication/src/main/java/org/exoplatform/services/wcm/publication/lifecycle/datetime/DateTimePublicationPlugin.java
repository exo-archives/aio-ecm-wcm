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
package org.exoplatform.services.wcm.publication.lifecycle.datetime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.services.ecm.publication.IncorrectStateUpdateLifecycleException;
import org.exoplatform.services.wcm.publication.WebpagePublicationPlugin;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Nov 17, 2009  
 */
public class DateTimePublicationPlugin extends WebpagePublicationPlugin {

	@Override
	public String getLifecycleType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getListPageNavigationUri(Page page, String remoteUser) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void publishContentToCLV(Node content,
																	Page page,
																	String clvPortletId,
																	String portalOwnerName,
																	String remoteUser) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void publishContentToSCV(Node content, Page page, String portalOwnerName) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void suspendPublishedContentFromPage(Node content, Page page, String remoteUser) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateLifecycleOnChangeNavigation(PageNavigation navigation, String remoteUser) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateLifecycleOnRemovePage(Page page, String remoteUser) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateLifecyleOnChangeContent(Node node, String remoteUser) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateLifecyleOnChangeContent(Node node, String remoteUser, String newState) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateLifecyleOnChangePage(Page page, String remoteUser) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateLifecyleOnCreateNavigation(PageNavigation navigation) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateLifecyleOnCreatePage(Page page, String remoteUser) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateLifecyleOnRemoveNavigation(PageNavigation navigation) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addMixin(Node node) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean canAddMixin(Node node) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void changeState(Node node, String newState, HashMap<String, String> context) throws IncorrectStateUpdateLifecycleException,
																																											Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getLocalizedAndSubstituteMessage(Locale locale, String key, String[] values) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getNodeView(Node node, Map<String, Object> context) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getPossibleStates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getStateImage(Node node, Locale locale) throws IOException,
																											 FileNotFoundException,
																											 Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UIForm getStateUI(Node node, UIComponent component) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUserInfo(Node node, Locale locale) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
}
