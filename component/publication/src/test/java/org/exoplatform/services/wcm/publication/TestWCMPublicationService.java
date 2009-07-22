package org.exoplatform.services.wcm.publication;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.application.Preference;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.ecm.publication.NotInPublicationLifecycleException;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.portletcontainer.pci.ExoWindowID;
import org.exoplatform.services.wcm.BaseWCMTestCase;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationConstant;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationPlugin;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class TestWCMPublicationService.
 */
public class TestWCMPublicationService extends BaseWCMTestCase {
	
  /** The wcm publication service. */
  private WCMPublicationService wcmPublicationService;
  
  /** The repository service. */
  private RepositoryService repositoryService;
  
  /** The collaboration session. */
  private Session collaborationSession;
  
  /** The user portal config service. */
  private UserPortalConfigService userPortalConfigService;
  
  /** The data storage. */
  private DataStorage dataStorage;
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.BaseWCMTestCase#setUp()
   */
  public void setUp() throws Exception{
    super.setUp();
    
    wcmPublicationService = getService(WCMPublicationService.class);
    repositoryService = getService(RepositoryService.class);
    userPortalConfigService = getService(UserPortalConfigService.class);
    dataStorage = StageAndVersionPublicationUtil.getServices(DataStorage.class);
    
    collaborationSession = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);
  }
  
  /* (non-Javadoc)
   * @see junit.framework.TestCase#tearDown()
   */
  public void tearDown() throws Exception {
  }
  
  /**
   * Test add publication plugin.
   * 
   * @throws Exception the exception
   */
  public void testAddPublicationPlugin() throws Exception{
    WebpagePublicationPlugin publicationPlugin = new StageAndVersionPublicationPlugin();
    publicationPlugin.setName(StageAndVersionPublicationConstant.LIFECYCLE_NAME);
    wcmPublicationService.addPublicationPlugin(publicationPlugin);

    assertEquals(1, wcmPublicationService.getWebpagePublicationPlugins().size());
    assertSame(
        publicationPlugin,
        wcmPublicationService.getWebpagePublicationPlugins().get(StageAndVersionPublicationConstant.LIFECYCLE_NAME));
  }

  /**
   * Test publish content scv.
   * 
   * @throws Exception the exception
   */
  public void testPublishContentSCV() throws Exception {
    Node testNode = createWebcontentNode(collaborationSession.getRootNode(), "testSCV", null, null, null); 
    collaborationSession.save();

    createPageNavigation();
    Page page = createPage();
    
    int oldPorletsNumber = getNumberPortletsOfPage(page, "SCVPortlet");

    prepareNodeStatus(testNode);
    
    wcmPublicationService.publishContentSCV(testNode, page, "classic");
    
    int newPorletsNumber = getNumberPortletsOfPage(page, "SCVPortlet");
    
    assertEquals("enrolled", testNode.getProperty("publication:currentState").getString());
    assertEquals("testSCV", testNode.getProperty("exo:title").getString());
    assertTrue(checkContentIdentifier(page, testNode.getUUID(), "SCVPortlet"));
    assertEquals(oldPorletsNumber + 1, newPorletsNumber);
    
    userPortalConfigService.remove(page);
    testNode.remove();
    collaborationSession.save();
  }
  
  /**
   * Test publish content cl v_01.
   * 
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public void testPublishContentCLV_01() throws Exception{
  	Node testNode = createWebcontentNode(collaborationSession.getRootNode(), "test", null, null, null); 
    collaborationSession.save();
 
    Page page = createPage();
    createPageNavigation();

    prepareNodeStatus(testNode);
    
    WCMConfigurationService configurationService = StageAndVersionPublicationUtil.getServices(WCMConfigurationService.class);
    StringBuilder windowId = new StringBuilder();
    windowId.append(PortalConfig.PORTAL_TYPE)
            .append("#")
            .append("classic")
            .append(":")
            .append(configurationService.getRuntimeContextParam("CLVPortlet"))
            .append("/")
            .append(IdGenerator.generate());
    
    ArrayList<Preference> preferences = new ArrayList<Preference>();
    preferences.add(addPreference("repository", ((ManageableRepository) testNode.getSession().getRepository()).getConfiguration().getName()));
    preferences.add(addPreference("workspace", testNode.getSession().getWorkspace().getName()));
    preferences.add(addPreference("nodeIdentifier", testNode.getUUID()));
    preferences.add(addPreference("mode", "ManualViewerMode"));
    preferences.add(addPreference("folderPath", 
    		"/sites content/live/classic/web contents/site artifacts/banner/Default;/sites content/live/classic/web contents/site artifacts/searchbox/Default;"));

    preferences.add(addPreference("contents",
    		"/sites content/live/classic/web contents/site artifacts/banner/Default",
    		"/sites content/live/classic/web contents/site artifacts/searchbox/Default"));
    
    PortletPreferences portletPreferences = new PortletPreferences();
    portletPreferences.setWindowId(windowId.toString());
    portletPreferences.setOwnerType(PortalConfig.PORTAL_TYPE);
    portletPreferences.setOwnerId("classic");
    portletPreferences.setPreferences(preferences);
    dataStorage.save(portletPreferences);

    Application portlet = new Application();
    portlet.setApplicationType(org.exoplatform.web.application.Application.EXO_PORTLET_TYPE);
    portlet.setShowInfoBar(false);
    portlet.setInstanceId(windowId.toString());
    
    ArrayList<Object> listPortlet = page.getChildren();
    listPortlet.add(portlet);
    page.setChildren(listPortlet);
    userPortalConfigService.update(page);
 
    wcmPublicationService.publishContentCLV(testNode, page, windowId.toString(), "classic", "root");
    
  	PortletPreferences  newPortletPreferences = dataStorage.getPortletPreferences(new ExoWindowID(windowId.toString()));      
  	if (newPortletPreferences != null) {
  		for (Preference preference : (List<Preference>)newPortletPreferences.getPreferences()) {
  			if ("contents".equals(preference.getName())){
  				assertTrue(preference.getValues().indexOf(testNode.getPath()) == 0);
  			} else if ("folderPath".equals(preference.getName())){
  				assertTrue(preference.getValues().get(0).toString().indexOf(testNode.getPath()) == 0);
  			}
  		}
  	}
  	
    userPortalConfigService.remove(page);
    testNode.remove();
    collaborationSession.save();
  }

  /**
   * Test publish content cl v_02.
   * 
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public void testPublishContentCLV_02() throws Exception{
    Node testNode = createWebcontentNode(collaborationSession.getRootNode(), "test", null, null, null); 
    collaborationSession.save();

    Page page = createPage();
    
    prepareNodeStatus(testNode);
    
    WCMConfigurationService configurationService = StageAndVersionPublicationUtil.getServices(WCMConfigurationService.class);
    StringBuilder windowId = new StringBuilder();
    windowId.append(PortalConfig.PORTAL_TYPE)
            .append("#")
            .append("classic")
            .append(":")
            .append(configurationService.getRuntimeContextParam("CLVPortlet"))
            .append("/")
            .append(IdGenerator.generate());
    
    Application portlet = new Application();
    portlet.setApplicationType(org.exoplatform.web.application.Application.EXO_PORTLET_TYPE);
    portlet.setShowInfoBar(false);
    portlet.setInstanceId(windowId.toString());
    
    ArrayList<Object> listPortlet = page.getChildren();
    listPortlet.add(portlet);
    page.setChildren(listPortlet);
    userPortalConfigService.update(page);
 
    wcmPublicationService.publishContentCLV(testNode, page, windowId.toString(), "classic", "root");

  	PortletPreferences  newPortletPreferences = dataStorage.getPortletPreferences(new ExoWindowID(windowId.toString()));      
  	if (newPortletPreferences != null) {
  		for (Preference preference : (List<Preference>)newPortletPreferences.getPreferences()) {
  			if ("contents".equals(preference.getName())){
  				assertTrue(preference.getValues().indexOf(testNode.getPath()) == 0);
  			} else if ("folderPath".equals(preference.getName())){
  				assertTrue(preference.getValues().get(0).toString().indexOf(testNode.getPath()) == 0);
  			}
  		}
  	}

  	userPortalConfigService.remove(page);
    testNode.remove();
    collaborationSession.save();
  }
  
  /**
   * Test suspend published content from page_01.
   * 
   * @throws Exception the exception
   */
  public void testSuspendPublishedContentFromPage_01() throws Exception {
  	suspendSCV();
  	suspendCLV();
  }

  /**
   * Suspend scv.
   * 
   * @throws Exception the exception
   * @throws RepositoryException the repository exception
   * @throws AccessDeniedException the access denied exception
   * @throws ItemExistsException the item exists exception
   * @throws ConstraintViolationException the constraint violation exception
   * @throws InvalidItemStateException the invalid item state exception
   * @throws VersionException the version exception
   * @throws LockException the lock exception
   * @throws NoSuchNodeTypeException the no such node type exception
   * @throws NotInPublicationLifecycleException the not in publication lifecycle exception
   * @throws ValueFormatException the value format exception
   * @throws PathNotFoundException the path not found exception
   * @throws UnsupportedRepositoryOperationException the unsupported repository operation exception
   */
  private void suspendSCV() throws Exception, RepositoryException, AccessDeniedException, ItemExistsException,
  ConstraintViolationException, InvalidItemStateException, VersionException, LockException, NoSuchNodeTypeException,
  NotInPublicationLifecycleException, ValueFormatException, PathNotFoundException,
  UnsupportedRepositoryOperationException {
  	Node testNode = createWebcontentNode(collaborationSession.getRootNode(), "testSCV", null, null, null); 
  	collaborationSession.save();

  	Page page = createPage();

  	int oldPorletsNumber = getNumberPortletsOfPage(page, "SCVPortlet");

  	prepareNodeStatus(testNode);

  	wcmPublicationService.publishContentSCV(testNode, page, "classic");

  	int newPorletsNumber = getNumberPortletsOfPage(page, "SCVPortlet");

  	assertEquals("enrolled", testNode.getProperty("publication:currentState").getString());
  	assertEquals("testSCV", testNode.getProperty("exo:title").getString());
  	assertTrue(checkContentIdentifier(page, testNode.getUUID(), "SCVPortlet"));
  	assertEquals(oldPorletsNumber + 1, newPorletsNumber);

  	wcmPublicationService.suspendPublishedContentFromPage(testNode, page, "root");
  	
  	userPortalConfigService.remove(page);
    testNode.remove();
    collaborationSession.save();
  }

  /**
   * Suspend clv.
   * 
   * @throws Exception the exception
   * @throws RepositoryException the repository exception
   * @throws AccessDeniedException the access denied exception
   * @throws ItemExistsException the item exists exception
   * @throws ConstraintViolationException the constraint violation exception
   * @throws InvalidItemStateException the invalid item state exception
   * @throws VersionException the version exception
   * @throws LockException the lock exception
   * @throws NoSuchNodeTypeException the no such node type exception
   * @throws UnsupportedRepositoryOperationException the unsupported repository operation exception
   * @throws NotInPublicationLifecycleException the not in publication lifecycle exception
   */
  @SuppressWarnings("unchecked")
  private void suspendCLV() throws Exception, RepositoryException, AccessDeniedException, ItemExistsException,
  ConstraintViolationException, InvalidItemStateException, VersionException, LockException, NoSuchNodeTypeException,
  UnsupportedRepositoryOperationException, NotInPublicationLifecycleException {
  	Node testNode = createWebcontentNode(collaborationSession.getRootNode(), "test", null, null, null); 
  	collaborationSession.save();

  	Page page = createPage();

  	prepareNodeStatus(testNode);

  	WCMConfigurationService configurationService = StageAndVersionPublicationUtil.getServices(WCMConfigurationService.class);
  	StringBuilder windowId = new StringBuilder();
  	windowId.append(PortalConfig.PORTAL_TYPE)
  	.append("#")
  	.append("classic")
  	.append(":")
  	.append(configurationService.getRuntimeContextParam("CLVPortlet"))
  	.append("/")
  	.append(IdGenerator.generate());

  	ArrayList<Preference> preferences = new ArrayList<Preference>();
  	preferences.add(addPreference("repository", ((ManageableRepository) testNode.getSession().getRepository()).getConfiguration().getName()));
  	preferences.add(addPreference("workspace", testNode.getSession().getWorkspace().getName()));
  	preferences.add(addPreference("nodeIdentifier", testNode.getUUID()));
  	preferences.add(addPreference("mode", "ManualViewerMode"));
  	preferences.add(addPreference("folderPath", 
  	"/sites content/live/classic/web contents/site artifacts/banner/Default;/sites content/live/classic/web contents/site artifacts/searchbox/Default;"));

  	preferences.add(addPreference("contents",
  			"/sites content/live/classic/web contents/site artifacts/banner/Default",
  	"/sites content/live/classic/web contents/site artifacts/searchbox/Default"));

  	PortletPreferences portletPreferences = new PortletPreferences();
  	portletPreferences.setWindowId(windowId.toString());
  	portletPreferences.setOwnerType(PortalConfig.PORTAL_TYPE);
  	portletPreferences.setOwnerId("classic");
  	portletPreferences.setPreferences(preferences);
  	dataStorage.save(portletPreferences);

  	Application portlet = new Application();
  	portlet.setApplicationType(org.exoplatform.web.application.Application.EXO_PORTLET_TYPE);
  	portlet.setShowInfoBar(false);
  	portlet.setInstanceId(windowId.toString());

  	ArrayList<Object> listPortlet = page.getChildren();
  	listPortlet.add(portlet);
  	page.setChildren(listPortlet);
  	userPortalConfigService.update(page);

  	wcmPublicationService.publishContentCLV(testNode, page, windowId.toString(), "classic", "root");

  	PortletPreferences  newPortletPreferences = dataStorage.getPortletPreferences(new ExoWindowID(windowId.toString()));      
  	if (newPortletPreferences != null) {
  		for (Preference preference : (List<Preference>)newPortletPreferences.getPreferences()) {
  			if ("contents".equals(preference.getName())){
  				assertTrue(preference.getValues().indexOf(testNode.getPath()) == 0);
  			} else if ("folderPath".equals(preference.getName())){
  				assertTrue(preference.getValues().get(0).toString().indexOf(testNode.getPath()) == 0);
  			}
  		}
  	}

  	wcmPublicationService.suspendPublishedContentFromPage(testNode, page, "root");

  	newPortletPreferences = dataStorage.getPortletPreferences(new ExoWindowID(windowId.toString()));      
  	if (newPortletPreferences != null) {
  		for (Preference preference : (List<Preference>)newPortletPreferences.getPreferences()) {
  			if ("contents".equals(preference.getName())){
  				assertTrue(preference.getValues().indexOf(testNode.getPath()) < 0);
  			} else if ("folderPath".equals(preference.getName())){
  				assertTrue(preference.getValues().get(0).toString().indexOf(testNode.getPath()) < 0);
  			}
  		}
  	}
  }

	/**
	 * Creates the page.
	 * 
	 * @return the page
	 * 
	 * @throws Exception the exception
	 */
	private Page createPage() throws Exception {
	  Page page = new Page();
		page.setPageId("portal::classic::testpage");
    page.setName("testpage");
    page.setOwnerType("portal");
    page.setOwnerId("classic");
    userPortalConfigService.create(page);
	  return page;
  }

	/**
	 * Creates the page navigation.
	 * 
	 * @return the page navigation
	 * 
	 * @throws Exception the exception
	 */
	private PageNavigation createPageNavigation() throws Exception {
		PageNavigation navigation = userPortalConfigService.getPageNavigation("portal", "classic");
  	PageNode pageNode = new PageNode();
  	pageNode.setPageReference("portal::classic::testpage");
  	pageNode.setName("testpage");
  	pageNode.setUri("testpage");
  	navigation.addNode(pageNode);
  	userPortalConfigService.update(navigation);
  	return navigation;
  }
	
	/**
	 * Prepare node status.
	 * 
	 * @param testNode the test node
	 * 
	 * @throws Exception the exception
	 */
	private void prepareNodeStatus(Node testNode) throws Exception {
	  WebpagePublicationPlugin publicationPlugin = new StageAndVersionPublicationPlugin();
    publicationPlugin.setName(StageAndVersionPublicationConstant.LIFECYCLE_NAME);
    
    wcmPublicationService.addPublicationPlugin(publicationPlugin);
    wcmPublicationService.enrollNodeInLifecycle(testNode, StageAndVersionPublicationConstant.LIFECYCLE_NAME);
  }
  
  /**
   * Adds the preference.
   * 
   * @param name the name
   * @param values the values
   * 
   * @return the preference
   */
  private Preference addPreference(String name, String... values ) {
    Preference preference = new Preference();
    ArrayList<String> listValue = new ArrayList<String>();
    for (String value : values) {
    	listValue.add(value);
    }
    preference.setName(name);
    preference.setValues(listValue);
    return preference;
  }

  /**
   * Check content identifier.
   * 
   * @param page the page
   * @param contentUUID the content uuid
   * @param portletType the portlet type
   * 
   * @return true, if successful
   * 
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private static boolean checkContentIdentifier(Page page, String contentUUID, String portletType) throws Exception {
    WCMConfigurationService wcmConfigurationService = StageAndVersionPublicationUtil.getServices(WCMConfigurationService.class);
    DataStorage dataStorage = StageAndVersionPublicationUtil.getServices(DataStorage.class);
    List<String> scvPortletsId = StageAndVersionPublicationUtil.findAppInstancesByName(page, wcmConfigurationService.getRuntimeContextParam(portletType));
    for (String scvPortletId : scvPortletsId) {
      PortletPreferences portletPreferences = dataStorage.getPortletPreferences(new ExoWindowID(scvPortletId));      
      if (portletPreferences != null) {
        for (Preference preference : (List<Preference>)portletPreferences.getPreferences()) {
        	
        	if ("nodeIdentifier".equals(preference.getName())
        			&& preference.getValues().size() > 0
        			&& contentUUID.equals(preference.getValues().get(0).toString())) {
            return true;
          }
        }
      }
    }
    
    return false;
  }
  
  /**
   * Gets the number portlets of page.
   * 
   * @param page the page
   * @param portletType the portlet type
   * 
   * @return the number portlets of page
   */
  private static int getNumberPortletsOfPage(Page page, String portletType) {
  	 WCMConfigurationService wcmConfigurationService = StageAndVersionPublicationUtil.getServices(WCMConfigurationService.class);
     List<String> scvPortletsId = StageAndVersionPublicationUtil.findAppInstancesByName(page, wcmConfigurationService.getRuntimeContextParam(portletType));
     
      try {
	      return scvPortletsId.size();
      } catch (Exception e) {
	      return 0;
      }
  }
}