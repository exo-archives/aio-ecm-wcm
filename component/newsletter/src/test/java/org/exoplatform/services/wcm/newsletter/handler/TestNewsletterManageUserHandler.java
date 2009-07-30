package org.exoplatform.services.wcm.newsletter.handler;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.*;
import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterConstant;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.NewsletterSubscriptionConfig;
import org.exoplatform.services.wcm.newsletter.config.NewsletterUserConfig;
import org.exoplatform.services.wcm.BaseWCMTestCase;

public class TestNewsletterManageUserHandler extends BaseWCMTestCase {
	
	private Node categoriesNode;
	private Node userHomeNode;
	private NewsletterCategoryConfig newsletterCategoryConfig;
	private NewsletterCategoryHandler newsletterCategoryHandler;
	private NewsletterSubscriptionConfig newsletterSubscriptionConfig;
	private NewsletterSubscriptionHandler newsSubscriptionHandler;
	private List<String> listSubs = new ArrayList<String>();
	private String userEmail = "test@local.com";
	private NewsletterPublicUserHandler newsletterPublicUserHandler;
	private NewsletterManageUserHandler newsletterManageUserHandler;
	private Node userNode;
	private NewsletterManagerService newsletterManagerService;
	private SessionProvider sessionProvider;
	
	
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
		
		sessionProvider = SessionProvider.createSystemProvider();
		
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
			newsSubscriptionHandler.add(sessionProvider, "classic", newsletterSubscriptionConfig);
			listSubs.add(newsletterSubscriptionConfig.getCategoryName()+"#"+newsletterSubscriptionConfig.getName());
		}
		newsletterPublicUserHandler.subscribe("classic", userEmail, listSubs, "http://test.com", new String[]{"a", "b", "c"}, sessionProvider);
		
		String userPath = NewsletterConstant.generateUserPath("classic");
	    Node userFolderNode = (Node)session.getItem(userPath);
	    userNode =  userFolderNode.getNode(userEmail);
	}

	public void testAddAdministrator() throws Exception {
		newsletterManageUserHandler.addAdministrator("classic", "test01");
		newsletterManageUserHandler.addAdministrator("classic", "test02");
		List<String> listUser = newsletterManageUserHandler.getAllAdministrator("classic");
		assertEquals(2, listUser.size());
	}
	
	public void testDeleteUserAddministrator() throws Exception {
		newsletterManageUserHandler.addAdministrator("classic", "userId01");
		newsletterManageUserHandler.addAdministrator("classic", "userId02");
		List<String> listUserAdd = newsletterManageUserHandler.getAllAdministrator("classic");
		assertEquals(2,	listUserAdd.size());
		newsletterManageUserHandler.deleteUserAddministrator("classic", "userId02");
		List<String> listUserDelete = newsletterManageUserHandler.getAllAdministrator("classic");
		assertEquals(1, listUserDelete.size());
	}
	
	public void testGetAllAdministrator() throws Exception {
		newsletterManageUserHandler.addAdministrator("classic", "user01");
		newsletterManageUserHandler.addAdministrator("classic", "user02");
		newsletterManageUserHandler.addAdministrator("classic", "user03");
		List<String> listUser = newsletterManageUserHandler.getAllAdministrator("classic");
		assertEquals(3, listUser.size());
	}
	
	
	public void testAdd() throws Exception {
		newsletterManageUserHandler.add("classic", userEmail, sessionProvider);
		boolean isCorrectUser = newsletterPublicUserHandler.confirmPublicUser(userEmail, userNode.getProperty(NewsletterConstant.USER_PROPERTY_VALIDATION_CODE).getString(), "classic", sessionProvider);
		assertEquals(true, isCorrectUser);
	}
	
	public void testChangeBanStatusl() throws Exception {
		userNode.setProperty(NewsletterConstant.USER_PROPERTY_BANNED, false);
		session.save();
		boolean isBanned = userNode.getProperty(NewsletterConstant.USER_PROPERTY_BANNED).getBoolean();
		newsletterManageUserHandler.changeBanStatus("classic", userEmail, true);
		isBanned = userNode.getProperty(NewsletterConstant.USER_PROPERTY_BANNED).getBoolean();
		assertTrue(isBanned);
	}
	
	public void testDelete() throws Exception {
		SessionProvider sessionProvider = SessionProvider.createSystemProvider();
		newsletterManageUserHandler.delete("classic", userEmail);
		List<NewsletterSubscriptionConfig> listSubscription = newsSubscriptionHandler.getSubscriptionIdsByPublicUser("classic", userEmail, sessionProvider);
		assertEquals(0, listSubscription.size());
	}
	
	public void testGetUsers() throws Exception {
		List<NewsletterUserConfig> listUser = newsletterManageUserHandler.getUsers("classic", newsletterCategoryConfig.getName(),	newsletterSubscriptionConfig.getName());
		assertEquals(1, listUser.size());
	}
	
	public void testGetQuantityUserBySubscription() throws Exception {
		int countUser = newsletterManageUserHandler.getQuantityUserBySubscription("classic", newsletterCategoryConfig.getName(), newsletterSubscriptionConfig.getName());
		assertEquals(1, countUser);
	}
	
	public void testCheckExistedEmail() throws Exception {
		boolean existEmail = newsletterManageUserHandler.checkExistedEmail("classic", userEmail);
		assertEquals(true, existEmail);
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		session.getItem("/sites content/live/classic/ApplicationData/NewsletterApplication/Categories").remove();
		session.getItem("/sites content/live/classic/ApplicationData/NewsletterApplication/Users").remove();
		session.save();
	}
}
