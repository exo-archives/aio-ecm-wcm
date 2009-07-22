package org.exoplatform.services.wcm.publication;


import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.application.Preference;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.portletcontainer.pci.ExoWindowID;
import org.exoplatform.services.wcm.BaseWCMTestCase;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationConstant;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationPlugin;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationUtil;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform
 * thang.do@exoplatform.com, thangcof@gmail.com Jul 15, 2009
 */
public class TestWCMPublicationService extends BaseWCMTestCase {
  private WCMPublicationService wcmPublicationService;
  
  public void setUp() throws Exception{
    super.setUp();
    wcmPublicationService = getService(WCMPublicationService.class);
  }
  
  public void testAddPublicationPlugin() {
    WebpagePublicationPlugin publicationPlugin = new StageAndVersionPublicationPlugin();
    publicationPlugin.setName(StageAndVersionPublicationConstant.LIFECYCLE_NAME);
    wcmPublicationService.addPublicationPlugin(publicationPlugin);

    assertEquals(1, wcmPublicationService.getWebpagePublicationPlugins().size());
    assertSame(
        publicationPlugin,
        wcmPublicationService.getWebpagePublicationPlugins().get(StageAndVersionPublicationConstant.LIFECYCLE_NAME));
  }

  public void testPublishContentSCV() throws Exception {
    RepositoryService repositoryService = getService(RepositoryService.class);

    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);
    Node testNode = createWebcontentNode(session.getRootNode(), "test", null, null, null); 
    session.save();
    
    Page page = new Page();
    page.setPageId("portal::classic::testpage");
    page.setName("testpage");
    page.setOwnerType("portal");
    page.setOwnerId("classic");
    UserPortalConfigService userPortalConfigService = getService(UserPortalConfigService.class);
    userPortalConfigService.create(page);
    
    int oldPorletsNumber = getNumberPortletsOfPage(page, "SCVPortlet");

    WebpagePublicationPlugin publicationPlugin = new StageAndVersionPublicationPlugin();
    publicationPlugin.setName(StageAndVersionPublicationConstant.LIFECYCLE_NAME);
    wcmPublicationService.addPublicationPlugin(publicationPlugin);
    
    wcmPublicationService.enrollNodeInLifecycle(testNode, StageAndVersionPublicationConstant.LIFECYCLE_NAME);
    wcmPublicationService.publishContentSCV(testNode, page, "classic");
    
    int newPorletsNumber = getNumberPortletsOfPage(page, "SCVPortlet");
    
    assertEquals("enrolled", testNode.getProperty("publication:currentState").getString());
    assertEquals("test", testNode.getProperty("exo:title").getString());
    assertTrue(checkContentIdentifier(page, testNode.getUUID(), "SCVPortlet"));
    assertEquals(oldPorletsNumber + 1, newPorletsNumber);
    
    userPortalConfigService.remove(page);
    testNode.remove();
    session.save();
  }
  
  @SuppressWarnings("unchecked")
  public void testPublishContentCLV_01() throws Exception{
  	RepositoryService repositoryService = getService(RepositoryService.class);

    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);
    Node testNode = createWebcontentNode(session.getRootNode(), "test", null, null, null); 
    session.save();
    
    Page page = new Page();
    page.setPageId("portal::classic::testpage");
    page.setName("testpage");
    page.setOwnerType("portal");
    page.setOwnerId("classic");
    UserPortalConfigService userPortalConfigService = getService(UserPortalConfigService.class);
    userPortalConfigService.create(page);
    
    WebpagePublicationPlugin publicationPlugin = new StageAndVersionPublicationPlugin();
    publicationPlugin.setName(StageAndVersionPublicationConstant.LIFECYCLE_NAME);
    wcmPublicationService.addPublicationPlugin(publicationPlugin);
    
    wcmPublicationService.enrollNodeInLifecycle(testNode, StageAndVersionPublicationConstant.LIFECYCLE_NAME);
    
    DataStorage dataStorage = StageAndVersionPublicationUtil.getServices(DataStorage.class);
  	
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
    session.save();
  }
  
  @SuppressWarnings("unchecked")
  public void testPublishContentCLV_02() throws Exception{
  	RepositoryService repositoryService = getService(RepositoryService.class);

    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);
    Node testNode = createWebcontentNode(session.getRootNode(), "test", null, null, null); 
    session.save();
    
    Page page = new Page();
    page.setPageId("portal::classic::testpage");
    page.setName("testpage");
    page.setOwnerType("portal");
    page.setOwnerId("classic");
    UserPortalConfigService userPortalConfigService = getService(UserPortalConfigService.class);
    userPortalConfigService.create(page);
    
    WebpagePublicationPlugin publicationPlugin = new StageAndVersionPublicationPlugin();
    publicationPlugin.setName(StageAndVersionPublicationConstant.LIFECYCLE_NAME);
    wcmPublicationService.addPublicationPlugin(publicationPlugin);
    
    wcmPublicationService.enrollNodeInLifecycle(testNode, StageAndVersionPublicationConstant.LIFECYCLE_NAME);
    
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
    
    DataStorage dataStorage = StageAndVersionPublicationUtil.getServices(DataStorage.class);
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
    session.save();
  }
  
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