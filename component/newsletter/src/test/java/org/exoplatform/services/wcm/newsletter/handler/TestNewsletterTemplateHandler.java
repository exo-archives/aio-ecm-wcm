package org.exoplatform.services.wcm.newsletter.handler;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.BaseWCMTestCase;
import org.exoplatform.services.wcm.core.NodetypeConstant;
import org.exoplatform.services.wcm.core.NodetypeUtils;
import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.NewsletterSubscriptionConfig;

public class TestNewsletterTemplateHandler extends BaseWCMTestCase {
	
	private NewsletterCategoryHandler newsletterCategoryHandler;
	private NewsletterCategoryConfig newsletterCategoryConfig;
	private NewsletterSubscriptionConfig newsletterSubscriptionConfig;
	private NewsletterSubscriptionHandler newsletterSubscriptionHandler;
	private NewsletterManagerService newsletterManagerService;
	private NewsletterTemplateHandler newsletterTemplateHandler; 
	private Node subscriptionNode;
	private Node newsletterApplicationNode ;
	private Node categoriesNode;
	private Node nodesTemp;
	private List<Node> listNode = new ArrayList<Node>();
	private static boolean isAdded = false;
	
	public void setUp() throws Exception {
		super.setUp();
		NodetypeUtils.displayAllNode("collaboration", "repository");
		SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
		newsletterManagerService = getService(NewsletterManagerService.class);
		newsletterCategoryHandler = newsletterManagerService.getCategoryHandler();
		newsletterSubscriptionHandler = newsletterManagerService.getSubscriptionHandler();
		newsletterTemplateHandler = newsletterManagerService.getTemplateHandler();
		newsletterApplicationNode = (Node) session.getItem("/sites content/live/classic/ApplicationData/NewsletterApplication");
		categoriesNode = newsletterApplicationNode.getNode("Categories");
		if(!isAdded){
			newsletterCategoryConfig = new NewsletterCategoryConfig();
			newsletterCategoryConfig.setName("CategoryName");
			newsletterCategoryConfig.setTitle("CategoryTitle");
			newsletterCategoryConfig.setDescription("CategoryDescription");
			newsletterCategoryConfig.setModerator("root");
			newsletterCategoryHandler.add("classic", newsletterCategoryConfig, sessionProvider);
			
			newsletterSubscriptionConfig = new NewsletterSubscriptionConfig();
			newsletterSubscriptionConfig.setCategoryName("CategoryName");
			newsletterSubscriptionConfig.setName("SubscriptionName");
			newsletterSubscriptionConfig.setTitle("SubscriptionTitle");
			newsletterSubscriptionConfig.setDescription("SubScriptionDescription");
			newsletterSubscriptionHandler.add(sessionProvider, "classic", newsletterSubscriptionConfig);
			
			subscriptionNode = categoriesNode.getNode("CategoryName/SubscriptionName");
			nodesTemp = subscriptionNode.addNode("testTemplate", NodetypeConstant.EXO_WEBCONTENT);
			nodesTemp.addMixin(NodetypeConstant.EXO_NEWSLETTER_ENTRY);
			session.save();
			for(int i = 0 ; i < 5; i++) {
				try{
					nodesTemp = subscriptionNode.getNode("testTemplate "+i);
				}catch(Exception ex){
					nodesTemp = subscriptionNode.addNode("testTemplate "+i, NodetypeConstant.EXO_WEBCONTENT);
				}
				nodesTemp.addMixin(NodetypeConstant.EXO_NEWSLETTER_ENTRY);
				session.save();
				listNode.add(nodesTemp);
				newsletterTemplateHandler.convertAsTemplate(nodesTemp.getPath(), "classic", newsletterCategoryConfig.getName());
			}
		} else {
			newsletterCategoryConfig = newsletterCategoryHandler.getCategoryByName("classic", "CategoryName", sessionProvider);
		}
		isAdded = true;
	}
	
	public void testGetTemplate() throws Exception {
		nodesTemp = newsletterTemplateHandler.getTemplate("classic", newsletterCategoryConfig, nodesTemp.getName());
		assertNotNull(nodesTemp);
	}
	
	public void testGetTemplates() throws Exception {
		List listTemplates = newsletterTemplateHandler.getTemplates("classic", newsletterCategoryConfig);
		assertEquals(5, listTemplates.size());
	}
	
	public void testConvertAsTemplate() throws Exception {
		List<Node> listTemplates = newsletterTemplateHandler.getTemplates("classic", newsletterCategoryConfig);
		for(Node node : listTemplates) {
			newsletterTemplateHandler.convertAsTemplate(node.getPath(), "classic", "CategoryName");
		}
	}
}

