package org.exoplatform.services.wcm.newsletter.handler;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.BaseWCMTestCase;
import org.exoplatform.services.wcm.core.NodetypeConstant;
import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.NewsletterSubscriptionConfig;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;

import com.sun.org.apache.xalan.internal.xsltc.NodeIterator;

/**
 * The Class TestNewsletterTemplateHandler.
 */
public class TestNewsletterTemplateHandler extends BaseWCMTestCase {
	
  /** The session provider. */
  private SessionProvider sessionProvider;
	
	/** The newsletter category handler. */
	private NewsletterCategoryHandler newsletterCategoryHandler;
	
	/** The newsletter category config. */
	private NewsletterCategoryConfig newsletterCategoryConfig;
	
	/** The newsletter subscription config. */
	private NewsletterSubscriptionConfig newsletterSubscriptionConfig;
	
	/** The newsletter subscription handler. */
	private NewsletterSubscriptionHandler newsletterSubscriptionHandler;
	
	/** The newsletter manager service. */
	private NewsletterManagerService newsletterManagerService;
	
	/** The newsletter template handler. */
	private NewsletterTemplateHandler newsletterTemplateHandler; 
	
	/** The subscription node. */
	private Node subscriptionNode;
	
	/** The newsletter application node. */
	private Node newsletterApplicationNode ;
	
	/** The categories node. */
	private Node categoriesNode;
	
	/** The nodes temp. */
	private Node nodesTemp;
	
	/** The list node. */
	private List<Node> listNode = new ArrayList<Node>();
	
	/** The is added. */
	private static boolean isAdded = false;
	
	/* (non-Javadoc)
	 * @see org.exoplatform.services.wcm.BaseWCMTestCase#setUp()
	 */
	public void setUp() throws Exception {
		super.setUp();
		sessionProvider = WCMCoreUtils.getSessionProvider();
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
			newsletterCategoryHandler.add(sessionProvider, "classic", newsletterCategoryConfig);
			
			newsletterSubscriptionConfig = new NewsletterSubscriptionConfig();
			newsletterSubscriptionConfig.setCategoryName("CategoryName");
			newsletterSubscriptionConfig.setName("SubscriptionName");
			newsletterSubscriptionConfig.setTitle("SubscriptionTitle");
			newsletterSubscriptionConfig.setDescription("SubScriptionDescription");
			newsletterSubscriptionHandler.add(sessionProvider, "classic", newsletterSubscriptionConfig);
			
			subscriptionNode = categoriesNode.getNode("CategoryName/SubscriptionName");
			nodesTemp 	= createWebcontentNode(subscriptionNode, "testTemplate", null, null, null);
			session.save();
			for(int i = 0 ; i < 5; i++) {
				try{
					nodesTemp = subscriptionNode.getNode("testTemplate "+i);
				}catch(Exception ex){
					nodesTemp = createWebcontentNode(subscriptionNode, "testTemplate"+i, null, null, null);
				}
				session.save();
				listNode.add(nodesTemp);
				newsletterTemplateHandler.convertAsTemplate(sessionProvider, nodesTemp.getPath(), "classic", newsletterCategoryConfig.getName());
			}
		} else {
			newsletterCategoryConfig = newsletterCategoryHandler.getCategoryByName(sessionProvider, "classic", "CategoryName");
		}
		isAdded = true;
	}
	
	/**
	 * Test get template.
	 * 
	 * @throws Exception the exception
	 */
	public void testGetTemplate() throws Exception {
		nodesTemp = newsletterTemplateHandler.getTemplate(sessionProvider, "classic", newsletterCategoryConfig, nodesTemp.getName());
		assertNotNull(nodesTemp);
	}
	
	/**
	 * Test get templates.
	 * 
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unchecked")
  public void testGetTemplates() throws Exception {
		List listTemplates = newsletterTemplateHandler.getTemplates(sessionProvider, "classic", newsletterCategoryConfig);
		assertEquals(5, listTemplates.size());
	}
	
	/**
	 * Test convert as template.
	 * 
	 * @throws Exception the exception
	 */
	public void testConvertAsTemplate() throws Exception {
		List<Node> listTemplates = newsletterTemplateHandler.getTemplates(sessionProvider, "classic", newsletterCategoryConfig);
		for(Node node : listTemplates) {
			newsletterTemplateHandler.convertAsTemplate(sessionProvider, node.getPath(), "classic", "CategoryName");
		}
	}
	
	/**
	 * Tear dow.
	 */
	public void tearDow() {
	  try {
      super.tearDown();
      javax.jcr.NodeIterator nodeIterator = newsletterApplicationNode.getNodes();
      while(nodeIterator.hasNext()) {
    	  nodeIterator.nextNode().remove();
      }
      session.save();
    } catch (Exception e) {
      sessionProvider.close();
    } finally {
      sessionProvider.close();
    }
	}
}
