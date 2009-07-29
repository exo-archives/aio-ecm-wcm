package org.exoplatform.services.wcm.newsletter.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.BaseWCMTestCase;
import org.exoplatform.services.wcm.core.NodetypeConstant;
import org.exoplatform.services.wcm.core.NodetypeUtils;
import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterConstant;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.NewsletterSubscriptionConfig;
import org.exoplatform.services.wcm.newsletter.config.NewsletterManagerConfig;

public class TestNewsletterEntryHandler extends BaseWCMTestCase {
	
	private Node newsletterApplicationNode;
	private Node categoriesNode;
	private Node categoryNode;
	private Node subscriptionNode;
	private Node nodeTemp;
	
	private NewsletterManagerService newsletterManagerService;
	private NewsletterCategoryHandler newsletterCategoryHandler;
	private NewsletterSubscriptionHandler newsletterSubscriptionHandler;
	private NewsletterEntryHandler newsletterEntryHandler;
	private NewsletterCategoryConfig newsletterCategoryConfig;
	private NewsletterSubscriptionConfig newsletterSubscriptionConfig;
	private static boolean isAdded = false;
	
	public void setUp() throws Exception {
		super.setUp();
		NodetypeUtils.displayAllNode("collaboration", "repository");
		newsletterApplicationNode = (Node) session.getItem("/sites content/live/classic/ApplicationData/NewsletterApplication");
		categoriesNode = newsletterApplicationNode.getNode("Categories");
		
		SessionProvider sessionProvider = SessionProvider.createSystemProvider();
		
		newsletterManagerService = getService(NewsletterManagerService.class);
		newsletterCategoryHandler = newsletterManagerService.getCategoryHandler();
		newsletterSubscriptionHandler = newsletterManagerService.getSubscriptionHandler();
		newsletterEntryHandler = newsletterManagerService.getEntryHandler();
		
		if(!isAdded){
			newsletterCategoryConfig = new NewsletterCategoryConfig();
			newsletterCategoryConfig.setName("CategoryName");
			newsletterCategoryConfig.setTitle("CategoryTitle");
			newsletterCategoryConfig.setDescription("CategoryDescription");
			newsletterCategoryConfig.setModerator("root");
			newsletterCategoryHandler.add("classic", newsletterCategoryConfig, sessionProvider);
			session.save();
			
			categoryNode = categoriesNode.getNode("CategoryName");
			
			newsletterSubscriptionConfig = new NewsletterSubscriptionConfig();
			newsletterSubscriptionConfig.setCategoryName("CategoryName");
			newsletterSubscriptionConfig.setName("SubscriptionName");
			newsletterSubscriptionConfig.setTitle("SubscriptionTitle");
			newsletterSubscriptionConfig.setDescription("SubscriptionDescription");
			newsletterSubscriptionHandler.add(sessionProvider, "classic", newsletterSubscriptionConfig);
			
			subscriptionNode = categoryNode.getNode("SubscriptionName");
			
			for(int i = 0; i < 5; i++) {
				nodeTemp = createWebcontentNode(subscriptionNode, "NewsletterEntry"+i, "test content of this node NewsletterEntry" + i, null, null);				
				nodeTemp.addMixin(NodetypeConstant.EXO_NEWSLETTER_ENTRY);
				nodeTemp.setProperty(NewsletterConstant.ENTRY_PROPERTY_DATE, Calendar.getInstance());
				nodeTemp.setProperty(NewsletterConstant.ENTRY_PROPERTY_STATUS, NewsletterConstant.STATUS_AWAITING);
			}
			session.save();
		} else {
			newsletterCategoryConfig = newsletterCategoryHandler.getCategoryByName("classic", "CategoryName", sessionProvider);
		}
		isAdded = true;
	}
	
	public void testDeleteNewsletterEntry() throws Exception {
		List<String> listIds = Arrays.asList(new String[]{"NewsletterEntry0", "NewsletterEntry1"});
		newsletterEntryHandler.delete("classic", "CategoryName", "SubscriptionName", listIds);
		long countEntry = subscriptionNode.getNodes().getSize();
		assertEquals(3, countEntry);
	}
	
	public void testGetNewsletterEntriesBySubscription() throws Exception {
		List<NewsletterManagerConfig> listNewslleterEntries = newsletterEntryHandler.getNewsletterEntriesBySubscription("classic", "CategoryName", "SubscriptionName");
		assertEquals(3, listNewslleterEntries.size());
	}
	
	public void testGetNewsletterEntry() throws Exception {
		NewsletterManagerConfig newsletterManagerConfig = newsletterEntryHandler.getNewsletterEntry("classic", "CategoryName", "SubscriptionName", "NewsletterEntry2");
		assertEquals("NewsletterEntry2", newsletterManagerConfig.getNewsletterName());
	}
	
	public void testGetNewsletterEntryByPath() throws Exception {
		String path = "/sites content/live/classic/ApplicationData/NewsletterApplication/Categories/CategoryName/SubscriptionName/NewsletterEntry2";
		NewsletterManagerConfig newsletterManagerConfig = newsletterEntryHandler.getNewsletterEntryByPath(path);
		assertEquals("NewsletterEntry2", newsletterManagerConfig.getNewsletterName());
	}
	
	public void testGetContent() throws Exception {
		String strContent = newsletterEntryHandler.getContent("classic", "CategoryName", "SubscriptionName", "NewsletterEntry2");
		assertTrue(strContent.indexOf("test content of this node NewsletterEntry2") > 0);
	}
	
	public void testGetContentEntry() throws Exception {
		if(nodeTemp == null) 
			nodeTemp = ((Node) session.getItem("/sites content/live/classic/ApplicationData/NewsletterApplication"))
							.getNode("Categories").getNode("CategoryName").getNode("SubscriptionName").getNode("NewsletterEntry2");
		String str = nodeTemp.getSession().getWorkspace().getName() + nodeTemp.getPath();
		String strContent = newsletterEntryHandler.getContent(nodeTemp);
		assertTrue(strContent.indexOf("test content of this node NewsletterEntry2") > 0);
	}
	
}
