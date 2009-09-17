package org.exoplatform.services.wcm.newsletter.handler;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.BaseWCMTestCase;
import org.exoplatform.services.wcm.core.NodetypeUtils;
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
		NodetypeUtils.displayAllNode("collaboration", "repository");
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
		newsletterCategoryHandler.add("classic", newsletterCategoryConfig, sessionProvider);

		newsletterSubscriptionConfig = new NewsletterSubscriptionConfig();
		for(int i = 0; i < 5; i++) {
			newsletterSubscriptionConfig.setCategoryName("CategoryName");
			newsletterSubscriptionConfig.setName("SubcriptionName_"+i);
			newsletterSubscriptionConfig.setTitle("SubscriptionTitle_"+i);
			newsletterSubscriptionConfig.setDescription("SubscriptionDescription_"+i);
			newsSubscriptionHandler.add("classic", newsletterSubscriptionConfig, sessionProvider);
			listSubs.add(newsletterSubscriptionConfig.getCategoryName()+"#"+newsletterSubscriptionConfig.getName());
		}
		newsletterPublicUserHandler.subscribe("classic", userEmail, listSubs, "http://test.com", new String[]{"a", "b", "c"}, sessionProvider);
		
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
		newsletterManageUserHandler.addAdministrator("classic", "test01", sessionProvider);
		newsletterManageUserHandler.addAdministrator("classic", "test02", sessionProvider);
		List<String> listUser = newsletterManageUserHandler.getAllAdministrator("classic", sessionProvider);
		assertEquals(2, listUser.size());
	}
	
	/**
	 * Test delete user addministrator.
	 * 
	 * @throws Exception the exception
	 */
	public void testDeleteUserAddministrator() throws Exception {
		newsletterManageUserHandler.addAdministrator("classic", "userId01", sessionProvider);
		newsletterManageUserHandler.addAdministrator("classic", "userId02", sessionProvider);
		List<String> listUserAdd = newsletterManageUserHandler.getAllAdministrator("classic", sessionProvider);
		assertEquals(2,	listUserAdd.size());
		newsletterManageUserHandler.deleteUserAddministrator("classic", "userId02", sessionProvider);
		List<String> listUserDelete = newsletterManageUserHandler.getAllAdministrator("classic", sessionProvider);
		assertEquals(1, listUserDelete.size());
	}
	
	/**
	 * Test get all administrator.
	 * 
	 * @throws Exception the exception
	 */
	public void testGetAllAdministrator() throws Exception {
		newsletterManageUserHandler.addAdministrator("classic", "user01", sessionProvider);
		newsletterManageUserHandler.addAdministrator("classic", "user02", sessionProvider);
		newsletterManageUserHandler.addAdministrator("classic", "user03", sessionProvider);
		List<String> listUser = newsletterManageUserHandler.getAllAdministrator("classic", sessionProvider);
		assertEquals(3, listUser.size());
	}
	
	
	/**
	 * Test add.
	 * 
	 * @throws Exception the exception
	 */
	public void testAdd() throws Exception {
		newsletterManageUserHandler.add("classic", userEmail, sessionProvider);
		boolean isCorrectUser = newsletterPublicUserHandler.confirmPublicUser(userEmail, userNode.getProperty(NewsletterConstant.USER_PROPERTY_VALIDATION_CODE).getString(), "classic", sessionProvider);
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
		newsletterManageUserHandler.changeBanStatus("classic", userEmail, true, sessionProvider);
		isBanned = userNode.getProperty(NewsletterConstant.USER_PROPERTY_BANNED).getBoolean();
		assertTrue(isBanned);
	}
	
	/**
	 * Test delete.
	 * 
	 * @throws Exception the exception
	 */
	public void testDelete() throws Exception {
		newsletterManageUserHandler.delete("classic", userEmail, sessionProvider);
		List<NewsletterSubscriptionConfig> listSubscription = newsSubscriptionHandler.getSubscriptionIdsByPublicUser("classic", userEmail, sessionProvider);
		assertEquals(0, listSubscription.size());
	}
	
	/**
	 * Test get users.
	 * 
	 * @throws Exception the exception
	 */
	public void testGetUsers() throws Exception {
		List<NewsletterUserConfig> listUser = newsletterManageUserHandler
		  .getUsers("classic", newsletterCategoryConfig.getName(), newsletterSubscriptionConfig.getName(), sessionProvider);
		assertEquals(1, listUser.size());
	}
	
	/**
	 * Test get quantity user by subscription.
	 * 
	 * @throws Exception the exception
	 */
	public void testGetQuantityUserBySubscription() throws Exception {
		int countUser = newsletterManageUserHandler
		  .getQuantityUserBySubscription("classic", newsletterCategoryConfig.getName(), newsletterSubscriptionConfig.getName(), sessionProvider);
		assertEquals(1, countUser);
	}
	
	/**
	 * Test check existed email.
	 * 
	 * @throws Exception the exception
	 */
	public void testCheckExistedEmail() throws Exception {
		boolean existEmail = newsletterManageUserHandler.checkExistedEmail("classic", userEmail, sessionProvider);
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
