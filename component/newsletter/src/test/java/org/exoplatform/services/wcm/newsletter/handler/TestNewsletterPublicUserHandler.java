package org.exoplatform.services.wcm.newsletter.handler;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;

import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.BaseWCMTestCase;
import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterConstant;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.NewsletterSubscriptionConfig;

public class TestNewsletterPublicUserHandler extends BaseWCMTestCase {
	
	private Node categoriesNode;
	private Node userHomeNode;
	private NewsletterManagerService newsletterManagerService;
	private NewsletterCategoryConfig newsletterCategoryConfig;
	private NewsletterCategoryHandler newsletterCategoryHandler;
	private NewsletterSubscriptionConfig newsletterSubscriptionConfig;
	private NewsletterSubscriptionHandler newsletterSubscriptionHandler;
	private NewsletterPublicUserHandler newsletterPublicUserHandler;
	private List<String> listSubs;
	
	private String userEmail = "test@local.com";
	
	public void setUp() throws Exception {
		super.setUp();
		Node newsletterApplicationNode = session.getRootNode().getNode("sites content/live").addNode("classic").addNode("ApplicationData")
																										.addNode("NewsletterApplication");
		categoriesNode = newsletterApplicationNode.addNode("Categories");
		userHomeNode  = newsletterApplicationNode.addNode("Users");
		session.save();
		
		newsletterManagerService = getService(NewsletterManagerService.class);
		newsletterCategoryHandler = newsletterManagerService.getCategoryHandler();
		newsletterSubscriptionHandler = newsletterManagerService.getSubscriptionHandler();
		newsletterPublicUserHandler = newsletterManagerService.getPublicUserHandler();
		
		SessionProvider sessionProvider = SessionProvider.createSystemProvider();
		
		newsletterCategoryConfig = new NewsletterCategoryConfig();
		newsletterCategoryConfig.setName("CatNameNewsletters");
		newsletterCategoryConfig.setTitle("TitleNewsletters");
		newsletterCategoryConfig.setDescription("DescriptionNewsletter");
		newsletterCategoryConfig.setModerator("root");
		newsletterCategoryHandler.add("classic", newsletterCategoryConfig, sessionProvider);
		
		listSubs = new ArrayList<String>();
		for(int i = 0; i < 5; i++) {
			newsletterSubscriptionConfig = new NewsletterSubscriptionConfig();
			newsletterSubscriptionConfig.setCategoryName("CatNameNewsletters");
			newsletterSubscriptionConfig.setName("SubNameSubscriptions_"+i);;
			newsletterSubscriptionConfig.setTitle("TitleNewletter_"+i);
			newsletterSubscriptionConfig.setDescription("DescriptionNewsletter_"+i);
			newsletterSubscriptionHandler.add(sessionProvider, "classic", newsletterSubscriptionConfig);
			listSubs.add(newsletterSubscriptionConfig.getCategoryName()+"#"+newsletterSubscriptionConfig.getName());
		}
	}
	
	public void testSubscribe() throws Exception {
		SessionProvider sessionProvider = SessionProvider.createSystemProvider();
		newsletterPublicUserHandler.subscribe("classic", userEmail, listSubs, "http://test.com", new String[]{"test", "adjasd", "asdasd"}, sessionProvider);
		List<NewsletterSubscriptionConfig> listSubscriptions = newsletterSubscriptionHandler.getSubscriptionIdsByPublicUser("classic", userEmail, sessionProvider);
		assertEquals(5, listSubscriptions.size());
	}
	
	public void testUpdateSubscriptions() throws Exception {
		SessionProvider sessionProvider = SessionProvider.createSystemProvider();
		newsletterPublicUserHandler.subscribe("classic", userEmail, listSubs, "http://test.com", 
													new String[]{"as","dsd", "asdasd"}, sessionProvider);
		List<NewsletterSubscriptionConfig> listSubscriptions = newsletterSubscriptionHandler.
																	getSubscriptionIdsByPublicUser("classic", userEmail, sessionProvider);
		assertEquals(5, listSubscriptions.size());
		
		List<String> listCategoryAndSubs = new ArrayList<String>();
		for(int j = 0; j < 3; j++) {
			listCategoryAndSubs.add(listSubs.get(j));
		}
		newsletterPublicUserHandler.updateSubscriptions("classic", userEmail, listCategoryAndSubs, sessionProvider);
		listSubscriptions = newsletterSubscriptionHandler.getSubscriptionIdsByPublicUser("classic", userEmail, sessionProvider);
		assertEquals(3, listSubscriptions.size());	
	}
	
	public void testClearEmailInSubscription() throws Exception {
		SessionProvider sessionProvider = SessionProvider.createSystemProvider();
		newsletterPublicUserHandler.subscribe("classic", userEmail, listSubs, "http://test.com", new String[]{"test", "sdasd", "asdasd"}, sessionProvider);
		newsletterPublicUserHandler.clearEmailInSubscription(userEmail, sessionProvider);
		List<NewsletterSubscriptionConfig> listSubscriptions =  newsletterSubscriptionHandler.getSubscriptionIdsByPublicUser("classic", userEmail, sessionProvider);
		assertEquals(0, listSubscriptions.size());
	}
	
	public void testConfirmPublicUser() throws Exception {
		SessionProvider sessionProvider = SessionProvider.createSystemProvider();
		newsletterPublicUserHandler.subscribe("classic", userEmail, listSubs, "http://test.com", new String[]{"test", "asdas", "ssss"}, sessionProvider);
		String userPath = NewsletterConstant.generateUserPath("classic");
	    Node userFolderNode = (Node)session.getItem(userPath);
	    Node node =  userFolderNode.getNode(userEmail);
		boolean isPublicUser = newsletterPublicUserHandler.confirmPublicUser(userEmail, node.getProperty(NewsletterConstant.USER_PROPERTY_VALIDATION_CODE).getString(), "classic", sessionProvider);
		assertEquals(true, isPublicUser);
	}
	
	public void testForgetEmail() throws Exception {
		SessionProvider sessionProvider = SessionProvider.createSystemProvider();
		newsletterPublicUserHandler.subscribe("classic", userEmail, listSubs, "http://test.com", new String[]{"test","fgfg", "wesad"}, sessionProvider);
		newsletterPublicUserHandler.forgetEmail("classic", userEmail, sessionProvider);
		List<NewsletterSubscriptionConfig> listSubcriptions = newsletterSubscriptionHandler.getSubscriptionIdsByPublicUser("classic", userEmail, sessionProvider);
		assertEquals(0, listSubcriptions.size());
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		session.getRootNode().getNode("sites content/live").getNode("classic").remove();
		session.save();
	}
}
