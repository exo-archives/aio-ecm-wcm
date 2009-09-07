package org.exoplatform.services.wcm.newsletter.handler;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.BaseWCMTestCase;
import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterConstant;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.NewsletterSubscriptionConfig;

// TODO: Auto-generated Javadoc
/**
 * The Class TestNewsletterPublicUserHandler.
 */
public class TestNewsletterPublicUserHandler extends BaseWCMTestCase {
	
  /** The session provider. */
  private SessionProvider sessionProvider;
	
	/** The categories node. */
	private Node categoriesNode;
	
	/** The user home node. */
	private Node userHomeNode;
	
	/** The newsletter manager service. */
	private NewsletterManagerService newsletterManagerService;
	
	/** The newsletter category config. */
	private NewsletterCategoryConfig newsletterCategoryConfig;
	
	/** The newsletter category handler. */
	private NewsletterCategoryHandler newsletterCategoryHandler;
	
	/** The newsletter subscription config. */
	private NewsletterSubscriptionConfig newsletterSubscriptionConfig;
	
	/** The newsletter subscription handler. */
	private NewsletterSubscriptionHandler newsletterSubscriptionHandler;
	
	/** The newsletter public user handler. */
	private NewsletterPublicUserHandler newsletterPublicUserHandler;
	
	/** The list subs. */
	private List<String> listSubs;
	
	/** The user email. */
	private String userEmail = "test@local.com";
	
	/* (non-Javadoc)
	 * @see org.exoplatform.services.wcm.BaseWCMTestCase#setUp()
	 */
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
		
		sessionProvider = SessionProviderFactory.createSystemProvider();
		
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
			newsletterSubscriptionHandler.add("classic", newsletterSubscriptionConfig, sessionProvider);
			listSubs.add(newsletterSubscriptionConfig.getCategoryName()+"#"+newsletterSubscriptionConfig.getName());
		}
	}
	
	/**
	 * Test subscribe.
	 * 
	 * @throws Exception the exception
	 */
	public void testSubscribe() throws Exception {
		newsletterPublicUserHandler.subscribe("classic", userEmail, listSubs, "http://test.com", new String[]{"test", "adjasd", "asdasd"}, sessionProvider);
		List<NewsletterSubscriptionConfig> listSubscriptions = newsletterSubscriptionHandler.getSubscriptionIdsByPublicUser("classic", userEmail, sessionProvider);
		assertEquals(5, listSubscriptions.size());
	}
	
	/**
	 * Test update subscriptions.
	 * 
	 * @throws Exception the exception
	 */
	public void testUpdateSubscriptions() throws Exception {
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
	
	/**
	 * Test clear email in subscription.
	 * 
	 * @throws Exception the exception
	 */
	public void testClearEmailInSubscription() throws Exception {
		newsletterPublicUserHandler.subscribe("classic", userEmail, listSubs, "http://test.com", new String[]{"test", "sdasd", "asdasd"}, sessionProvider);
		newsletterPublicUserHandler.clearEmailInSubscription(userEmail, sessionProvider);
		List<NewsletterSubscriptionConfig> listSubscriptions =  newsletterSubscriptionHandler.getSubscriptionIdsByPublicUser("classic", userEmail, sessionProvider);
		assertEquals(0, listSubscriptions.size());
	}
	
	/**
	 * Test confirm public user.
	 * 
	 * @throws Exception the exception
	 */
	public void testConfirmPublicUser() throws Exception {
		newsletterPublicUserHandler.subscribe("classic", userEmail, listSubs, "http://test.com", new String[]{"test", "asdas", "ssss"}, sessionProvider);
		String userPath = NewsletterConstant.generateUserPath("classic");
	    Node userFolderNode = (Node)session.getItem(userPath);
	    Node node =  userFolderNode.getNode(userEmail);
		boolean isPublicUser = newsletterPublicUserHandler.confirmPublicUser(userEmail, node.getProperty(NewsletterConstant.USER_PROPERTY_VALIDATION_CODE).getString(), "classic", sessionProvider);
		assertEquals(true, isPublicUser);
	}
	
	/**
	 * Test forget email.
	 * 
	 * @throws Exception the exception
	 */
	public void testForgetEmail() throws Exception {
		newsletterPublicUserHandler.subscribe("classic", userEmail, listSubs, "http://test.com", new String[]{"test","fgfg", "wesad"}, sessionProvider);
		newsletterPublicUserHandler.forgetEmail("classic", userEmail, sessionProvider);
		List<NewsletterSubscriptionConfig> listSubcriptions = newsletterSubscriptionHandler.getSubscriptionIdsByPublicUser("classic", userEmail, sessionProvider);
		assertEquals(0, listSubcriptions.size());
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() {
		try {
      super.tearDown();
      session.getRootNode().getNode("sites content/live").getNode("classic").remove();
      session.save();
    } catch (Exception e) {
      sessionProvider.close();
    } finally {
      sessionProvider.close();
    }
	}
}
