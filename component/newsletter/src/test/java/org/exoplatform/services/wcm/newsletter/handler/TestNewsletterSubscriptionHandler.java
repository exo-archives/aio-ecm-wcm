package org.exoplatform.services.wcm.newsletter.handler;

import java.util.ArrayList;
import java.util.Calendar;

import javax.jcr.Node;

import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.BaseWCMTestCase;
import org.exoplatform.services.wcm.core.NodetypeConstant;
import org.exoplatform.services.wcm.core.NodetypeUtils;
import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterConstant;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.NewsletterSubscriptionConfig;

import antlr.collections.List;

public class TestNewsletterSubscriptionHandler extends BaseWCMTestCase {
	
	private NewsletterSubscriptionConfig newsletterSubscriptionConfig;
	private NewsletterSubscriptionHandler newsletterSubscriptionHandler;
	private Node categoriesNode;
	private Node userHomeNode;
	NewsletterManagerService newsletterManagerService;
	public void setUp() throws Exception {
		super.setUp();
		NodetypeUtils.displayAllNode("collaboration", "repository");
		Node newsletterApplicationNode = (Node) session.getItem("/sites content/live/classic/ApplicationData/NewsletterApplication");
		categoriesNode = newsletterApplicationNode.getNode("Categories");
		userHomeNode = newsletterApplicationNode.getNode("Users");
		session.save();
		
		SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
		newsletterManagerService = getService(NewsletterManagerService.class);
		NewsletterCategoryHandler newsletterCategoryHandler = newsletterManagerService.getCategoryHandler();
		NewsletterCategoryConfig newsletterCategoryConfig = new NewsletterCategoryConfig();
		newsletterCategoryConfig.setName("CategoryNameNewsletterSubcription");
		newsletterCategoryConfig.setTitle("TitleCategory");
		newsletterCategoryConfig.setDescription("DescriptionCategory");
		newsletterCategoryConfig.setModerator("root");
		newsletterCategoryHandler.add("classic", newsletterCategoryConfig, sessionProvider);
		
		
		newsletterSubscriptionHandler = newsletterManagerService.getSubscriptionHandler();
		newsletterSubscriptionConfig  = new NewsletterSubscriptionConfig();
		newsletterSubscriptionConfig.setCategoryName("CategoryNameNewsletterSubcription");
		newsletterSubscriptionConfig.setName("NameNewsletterSubcription");
		newsletterSubscriptionConfig.setTitle("TitleNewsletterSubcription");
		newsletterSubscriptionConfig.setDescription("DescriptionNewsletterSubcription");
	}
	
	public void testAddSubscription() throws Exception {
		SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
		newsletterSubscriptionHandler.add(sessionProvider, "classic", newsletterSubscriptionConfig);
		Node sub = (Node)categoriesNode.getNode("CategoryNameNewsletterSubcription").getNode("NameNewsletterSubcription");
		assertNotNull(sub);
		assertEquals(sub.getName(), newsletterSubscriptionConfig.getName());
		assertEquals(sub.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_CATEGORY_NAME).getString(), newsletterSubscriptionConfig.getCategoryName());
		assertEquals(sub.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_TITLE).getString(), newsletterSubscriptionConfig.getTitle());
		assertEquals(sub.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_DECRIPTION).getString(), newsletterSubscriptionConfig.getDescription());
	}
	
	public void testEditSubscription() throws Exception {
		SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
		newsletterSubscriptionHandler.add(sessionProvider, "classic", newsletterSubscriptionConfig);
		
		newsletterSubscriptionConfig.setTitle("Sub Title");
		newsletterSubscriptionConfig.setDescription("Sub Desc");
		
		newsletterSubscriptionHandler.edit("classic", newsletterSubscriptionConfig, sessionProvider);
		Node sub = (Node)categoriesNode.getNode("CategoryNameNewsletterSubcription").getNode("NameNewsletterSubcription");
		assertEquals(sub.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_TITLE).getString(), newsletterSubscriptionConfig.getTitle());
		assertEquals(sub.getProperty(NewsletterConstant.SUBSCRIPTION_PROPERTY_DECRIPTION).getString(), newsletterSubscriptionConfig.getDescription());
	}
	
	public void testDeleteSubscription() throws Exception {
		SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
		newsletterSubscriptionHandler.add(sessionProvider, "classic", newsletterSubscriptionConfig);
		
		newsletterSubscriptionHandler.delete("classic", "CategoryNameNewsletterSubcription", newsletterSubscriptionConfig, sessionProvider);
		
		assertEquals(3, categoriesNode.getNode("CategoryNameNewsletterSubcription").getNodes().getSize());
	}
	
	public void testGetSubscriptionsByCategory() throws Exception {
		SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
		for(int i = 0 ; i < 5; i++) {
			newsletterSubscriptionConfig = new	NewsletterSubscriptionConfig();
			newsletterSubscriptionConfig.setCategoryName("CategoryNameNewsletterSubcription");
			newsletterSubscriptionConfig.setName("Sub_"+i);
			newsletterSubscriptionConfig.setTitle("Title_"+i);
			newsletterSubscriptionConfig.setDescription("Desc_"+i);
			newsletterSubscriptionHandler.add(sessionProvider, "classic", newsletterSubscriptionConfig);
		}
		
		assertEquals(7, newsletterSubscriptionHandler.getSubscriptionsByCategory("classic", "CategoryNameNewsletterSubcription", sessionProvider).size());
	}
	
	public void testGetSubscriptionIdsByPublicUser() throws Exception {
		SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
		java.util.List<String> list = new ArrayList<String>();
		for(int i = 0 ; i < 5; i++) {
			newsletterSubscriptionConfig = new	NewsletterSubscriptionConfig();
			newsletterSubscriptionConfig.setCategoryName("CategoryNameNewsletterSubcription");
			newsletterSubscriptionConfig.setName("Sub_"+i);
			newsletterSubscriptionConfig.setTitle("Title_"+i);
			newsletterSubscriptionConfig.setDescription("Desc_"+i);
			newsletterSubscriptionHandler.add(sessionProvider, "classic", newsletterSubscriptionConfig);
			list.add(newsletterSubscriptionConfig.getCategoryName() + "#" + newsletterSubscriptionConfig.getName());
		}

		NewsletterPublicUserHandler newsletterPublicUserHandler = newsletterManagerService.getPublicUserHandler();
		newsletterPublicUserHandler.subscribe("classic", "abc@local.com", list, "http://asdasd.com", 
												new String[]{"2df12 ads", "21df21asdf#2d1f#asdf", "adf asf"}, sessionProvider);
		assertEquals(5, newsletterSubscriptionHandler.getSubscriptionIdsByPublicUser("classic", "abc@local.com", sessionProvider).size());
	}
	
	public void testGetSubscriptionsByName() throws Exception {
		SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
		newsletterSubscriptionHandler.add(sessionProvider, "classic", newsletterSubscriptionConfig);
		NewsletterSubscriptionConfig sub = newsletterSubscriptionHandler.getSubscriptionsByName("classic", "CategoryNameNewsletterSubcription", newsletterSubscriptionConfig.getName(), sessionProvider);
		assertEquals("NameNewsletterSubcription", sub.getName());
	}
	
	public void testGetNumberOfNewslettersWaiting() throws Exception {

		SessionProvider sessionProvider = SessionProvider.createSystemProvider();
		NewsletterCategoryConfig newsletterCategoryConfig = new NewsletterCategoryConfig();
		NewsletterCategoryHandler newsletterCategoryHandler = newsletterManagerService.getCategoryHandler();
		newsletterCategoryConfig.setName("CategoryName");
		newsletterCategoryConfig.setTitle("CategoryTitle");
		newsletterCategoryConfig.setDescription("CategoryDescription");
		newsletterCategoryConfig.setModerator("root");
		newsletterCategoryHandler.add("classic", newsletterCategoryConfig, sessionProvider);
		
		Node categoryNode = categoriesNode.getNode("CategoryName");
		
		newsletterSubscriptionConfig = new NewsletterSubscriptionConfig();
		newsletterSubscriptionConfig.setCategoryName("CategoryName");
		newsletterSubscriptionConfig.setName("SubscriptionName");
		newsletterSubscriptionConfig.setTitle("SubscriptionTitle");
		newsletterSubscriptionConfig.setDescription("SubscriptionDescription");
		newsletterSubscriptionHandler.add(sessionProvider, "classic", newsletterSubscriptionConfig);
		
		Node subscriptionNode = categoryNode.getNode("SubscriptionName");
		Node nodeTemp;
		for(int i = 0; i < 5; i++) {
			nodeTemp = createWebcontentNode(subscriptionNode, "NewsletterEntry"+i, "test content of this node NewsletterEntry" + i, null, null);				
			nodeTemp.addMixin(NodetypeConstant.EXO_NEWSLETTER_ENTRY);
			nodeTemp.setProperty(NewsletterConstant.ENTRY_PROPERTY_DATE, Calendar.getInstance());
			nodeTemp.setProperty(NewsletterConstant.ENTRY_PROPERTY_STATUS, NewsletterConstant.STATUS_AWAITING);
		}
		session.save();
		
		long numNewsletterWaiting = newsletterSubscriptionHandler.getNumberOfNewslettersWaiting("classic", "CategoryName", "SubscriptionName", sessionProvider);
		assertEquals(5, numNewsletterWaiting);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		session.save();
	}
}
