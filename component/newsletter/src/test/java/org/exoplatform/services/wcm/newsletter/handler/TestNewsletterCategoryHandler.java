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
package org.exoplatform.services.wcm.newsletter.handler;

import javax.jcr.Node;

import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.BaseWCMTestCase;
import org.exoplatform.services.wcm.core.NodetypeUtils;
import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterConstant;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform
 * chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com Jul 14, 2009
 */
public class TestNewsletterCategoryHandler extends BaseWCMTestCase {
	
	private NewsletterCategoryConfig newsletterCategoryConfig;
	private Node categoriesNode;
	private NewsletterCategoryHandler newsletterCategoryHandler;
	
	public void setUp() throws Exception {
		super.setUp();
		categoriesNode = session.getRootNode().getNode("sites content/live/classic/ApplicationData/NewsletterApplication/Categories");
		session.save();	
		
		NewsletterManagerService  newsletterManagerService = getService(NewsletterManagerService.class);
		newsletterCategoryHandler = newsletterManagerService.getCategoryHandler();
		
		newsletterCategoryConfig = new NewsletterCategoryConfig();
		newsletterCategoryConfig.setName("newsletter01");
		newsletterCategoryConfig.setTitle("title01");
		newsletterCategoryConfig.setDescription("description01");
		newsletterCategoryConfig.setModerator("root");
	}
	
	public void testAddCategory() throws Exception {
		SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
		newsletterCategoryHandler.add("classic", newsletterCategoryConfig,sessionProvider);

		Node node = categoriesNode.getNode(newsletterCategoryConfig.getName());
		assertNotNull(node);
		assertEquals(node.getName(), newsletterCategoryConfig.getName());
		assertEquals(node.getProperty(NewsletterConstant.CATEGORY_PROPERTY_DESCRIPTION).getString(), newsletterCategoryConfig.getDescription());
		assertEquals(node.getProperty(NewsletterConstant.CATEGORY_PROPERTY_TITLE).getString(), newsletterCategoryConfig.getTitle());	
	}
	
	public void testEditCategory()  throws Exception {
		SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
		newsletterCategoryHandler.add("classic", newsletterCategoryConfig, sessionProvider);

		newsletterCategoryConfig.setTitle("Sport News");
		newsletterCategoryConfig.setDescription("Soccer,tennis,...");
		newsletterCategoryConfig.setModerator("john");
		newsletterCategoryHandler.edit("classic", newsletterCategoryConfig, sessionProvider);
		
		// get node and edit
		Node node = categoriesNode.getNode(newsletterCategoryConfig.getName());
		assertNotNull(node);
		assertEquals(node.getProperty(NewsletterConstant.CATEGORY_PROPERTY_TITLE).getString(), newsletterCategoryConfig.getTitle());
		assertEquals(node.getProperty(NewsletterConstant.CATEGORY_PROPERTY_DESCRIPTION).getString(), newsletterCategoryConfig.getDescription());
	}

	public void testDeleteCategory() throws Exception {
		SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
		newsletterCategoryHandler.add("classic", newsletterCategoryConfig, sessionProvider);
		newsletterCategoryHandler.delete("classic", newsletterCategoryConfig.getName(), sessionProvider);
		assertEquals(0, categoriesNode.getNodes().getSize());
	}
	
	public void testGetCategoryByName() throws Exception {
		SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
		newsletterCategoryHandler.add("classic", newsletterCategoryConfig, sessionProvider);
		NewsletterCategoryConfig cat = newsletterCategoryHandler.getCategoryByName("classic", newsletterCategoryConfig.getName(), sessionProvider); 
		assertEquals("newsletter01", cat.getName());
	}
	
	public void testGetListCategoriesByName() throws Exception {
		SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
		for(int i =0; i < 5; i ++) {
			NewsletterCategoryConfig newsletterCategoryConfig = new NewsletterCategoryConfig();
			newsletterCategoryConfig.setName("cat_" + i);
			newsletterCategoryConfig.setTitle("title_" + i);
			newsletterCategoryConfig.setDescription("description_" + i);
			newsletterCategoryConfig.setModerator("root");
			newsletterCategoryHandler.add("classic", newsletterCategoryConfig, sessionProvider);
		}
		assertEquals(5, newsletterCategoryHandler.getListCategories("classic", sessionProvider).size());
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		try {
		  session.getRootNode().getNode("sites content/live/classic/ApplicationData/NewsletterApplication/Categories/newsletter01").remove();
		  session.save();
    } catch (Exception e) {}
	}
}

