package org.exoplatform.services.wcm.newsletter.handler;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.BaseWCMTestCase;
import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterConstant;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.NewsletterSubscriptionConfig;
import org.exoplatform.services.wcm.newsletter.config.NewsletterUserConfig;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;

/**
 * The Class TestNewsletterManageUserHandler.
 */
public class TestNewsletterManageUserHandler extends BaseWCMTestCase {
	
	/** The categories node. */
	@SuppressWarnings("unused")
  private Node categoriesNode;
	
	/** The user home node. */
	@SuppressWarnings("unused")
  private Node userHomeNode;
	
	/** The newsletter category config. */
	private NewsletterCategoryConfig newsletterCategoryConfig;
	
	/** The newsletter category handler. */
	private NewsletterCategoryHandler newsletterCategoryHandler;
	
	/** The newsletter subscription config. */
	private NewsletterSubscriptionConfig newsletterSubscriptionConfig;
	
	/** The news subscription handler. */
	private NewsletterSubscriptionHandler newsSubscriptionHandler;
	
	/** The list subs. */
	private List<String> listSubs = new ArrayList<String>();
	
	/** The user email. */
	private String userEmail = "test@local.com";
	
	/** The newsletter public user handler. */
	private NewsletterPublicUserHandler newsletterPublicUserHandler;
	
	/** The newsletter manage user handler. */
	private NewsletterManageUserHandler newsletterManageUserHandler;
	
	/** The user node. */
	private Node userNode;
	
	/** The newsletter manager service. */
	private NewsletterManagerService newsletterManagerService;
	
	/** The session provider. */
	private SessionProvider sessionProvider;
	
	
	/* (non-Javadoc)
	 * @see org.exoplatform.services.wcm.BaseWCMTestCase#setUp()
	 */
	public void setUp() throws Exception {
		super.setUp();
		Node newsletterApplicationNode = (Node) session.getItem("/sites content/live/classic/ApplicationData/NewsletterApplication");
		categoriesNode = newsletterApplicationNode.addNode("Categories");
		userHomeNode   = newsletterApplicationNode.addNode("Users");

		session.save();
			
		newsletterManagerService = getService(NewsletterManagerService.class);
		newsletterCategoryHandler = newsletterManagerService.getCategoryHandler();
		newsSubscriptionHandler = newsletterManagerService.getSubscriptionHandler();
		newsletterPublicUserHandler = newsletterManagerService.getPublicUserHandler();
		newsletterManageUserHandler = newsletterManagerService.getManageUserHandler();
		
		sessionProvider = WCMCoreUtils.getSessionProvider();
		
		newsletterCategoryConfig = new NewsletterCategoryConfig();
		newsletterCategoryConfig.setName("CategoryName");
		newsletterCategoryConfig.setTitle("CategoryTitle");
		newsletterCategoryConfig.setDescription("CategoryDescription");
		newsletterCategoryConfig.setModerator("root");
		newsletterCategoryHandler.add(sessionProvider, "classic", newsletterCategoryConfig);

		newsletterSubscriptionConfig = new NewsletterSubscriptionConfig();
		for(int i = 0; i < 5; i++) {
			newsletterSubscriptionConfig.setCategoryName("CategoryName");
			newsletterSubscriptionConfig.setName("SubcriptionName_"+i);
			newsletterSubscriptionConfig.setTitle("SubscriptionTitle_"+i);
			newsletterSubscriptionConfig.setDescription("SubscriptionDescription_"+i);
			newsSubscriptionHandler.add(sessionProvider, "classic", newsletterSubscriptionConfig);
			listSubs.add(newsletterSubscriptionConfig.getCategoryName()+"#"+newsletterSubscriptionConfig.getName());
		}
		newsletterPublicUserHandler.subscribe(sessionProvider, "classic", userEmail, listSubs, "http://test.com", new String[]{"a", "b", "c"});
		
		String userPath = NewsletterConstant.generateUserPath("classic");
	    Node userFolderNode = (Node)session.getItem(userPath);
	    userNode =  userFolderNode.getNode(userEmail);
	}

	/**
	 * Test add administrator.
	 * 
	 * @throws Exception the exception
	 */
	public void testAddAdministrator() throws Exception {
		newsletterManageUserHandler.addAdministrator(sessionProvider, "classic", "test01");
		newsletterManageUserHandler.addAdministrator(sessionProvider, "classic", "test02");
		List<String> listUser = newsletterManageUserHandler.getAllAdministrator(sessionProvider, "classic");
		assertEquals(2, listUser.size());
	}
	
	/**
	 * Test delete user addministrator.
	 * 
	 * @throws Exception the exception
	 */
	public void testDeleteUserAddministrator() throws Exception {
		newsletterManageUserHandler.addAdministrator(sessionProvider, "classic", "userId01");
		newsletterManageUserHandler.addAdministrator(sessionProvider, "classic", "userId02");
		List<String> listUserAdd = newsletterManageUserHandler.getAllAdministrator(sessionProvider, "classic");
		assertEquals(2,	listUserAdd.size());
		newsletterManageUserHandler.deleteUserAddministrator(sessionProvider, "classic", "userId02");
		List<String> listUserDelete = newsletterManageUserHandler.getAllAdministrator(sessionProvider, "classic");
		assertEquals(1, listUserDelete.size());
	}
	
	/**
	 * Test get all administrator.
	 * 
	 * @throws Exception the exception
	 */
	public void testGetAllAdministrator() throws Exception {
		newsletterManageUserHandler.addAdministrator(sessionProvider, "classic", "user01");
		newsletterManageUserHandler.addAdministrator(sessionProvider, "classic", "user02");
		newsletterManageUserHandler.addAdministrator(sessionProvider, "classic", "user03");
		List<String> listUser = newsletterManageUserHandler.getAllAdministrator(sessionProvider, "classic");
		assertEquals(3, listUser.size());
	}
	
	
	/**
	 * Test add.
	 * 
	 * @throws Exception the exception
	 */
	public void testAdd() throws Exception {
		newsletterManageUserHandler.add(sessionProvider, "classic", userEmail);
		boolean isCorrectUser = newsletterPublicUserHandler.confirmPublicUser(sessionProvider, userEmail, userNode.getProperty(NewsletterConstant.USER_PROPERTY_VALIDATION_CODE).getString(), "classic");
		assertEquals(true, isCorrectUser);
	}
	
	/**
	 * Test change ban statusl.
	 * 
	 * @throws Exception the exception
	 */
	public void testChangeBanStatusl() throws Exception {
		userNode.setProperty(NewsletterConstant.USER_PROPERTY_BANNED, false);
		session.save();
		boolean isBanned = userNode.getProperty(NewsletterConstant.USER_PROPERTY_BANNED).getBoolean();
		newsletterManageUserHandler.changeBanStatus(sessionProvider, "classic", userEmail, true);
		isBanned = userNode.getProperty(NewsletterConstant.USER_PROPERTY_BANNED).getBoolean();
		assertTrue(isBanned);
	}
	
	/**
	 * Test delete.
	 * 
	 * @throws Exception the exception
	 */
	public void testDelete() throws Exception {
		newsletterManageUserHandler.delete(sessionProvider, "classic", userEmail);
		List<NewsletterSubscriptionConfig> listSubscription = newsSubscriptionHandler.getSubscriptionIdsByPublicUser(sessionProvider, "classic", userEmail);
		assertEquals(0, listSubscription.size());
	}
	
	/**
	 * Test get users.
	 * 
	 * @throws Exception the exception
	 */
	public void testGetUsers() throws Exception {
		List<NewsletterUserConfig> listUser = newsletterManageUserHandler
		  .getUsers(sessionProvider, "classic", newsletterCategoryConfig.getName(), newsletterSubscriptionConfig.getName());
		assertEquals(1, listUser.size());
	}
	
	/**
	 * Test get quantity user by subscription.
	 * 
	 * @throws Exception the exception
	 */
	public void testGetQuantityUserBySubscription() throws Exception {
		int countUser = newsletterManageUserHandler
		  .getQuantityUserBySubscription(sessionProvider, "classic", newsletterCategoryConfig.getName(), newsletterSubscriptionConfig.getName());
		assertEquals(1, countUser);
	}
	
	/**
	 * Test check existed email.
	 * 
	 * @throws Exception the exception
	 */
	public void testCheckExistedEmail() throws Exception {
		boolean existEmail = newsletterManageUserHandler.checkExistedEmail(sessionProvider, "classic", userEmail);
		assertEquals(true, existEmail);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() {
		try {
      super.tearDown();
      session.getItem("/sites content/live/classic/ApplicationData/NewsletterApplication/Categories").remove();
      session.getItem("/sites content/live/classic/ApplicationData/NewsletterApplication/Users").remove();
      session.save();
    } catch (Exception e) {
      sessionProvider.close();
    } finally {
      sessionProvider.close();
    }
	}
}
