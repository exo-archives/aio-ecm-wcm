/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.wcm.core;

import javax.jcr.Node;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.BaseWCMTestCase;
import org.exoplatform.services.wcm.javascript.JSFileHandler;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.exoplatform.services.wcm.skin.CSSFileHandler;
import org.exoplatform.services.wcm.webcontent.HTMLFileSchemaHandler;
import org.exoplatform.services.wcm.webcontent.WebContentSchemaHandler;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 * chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jul 15, 2009
 */
public class TestWebSchemaConfigService extends BaseWCMTestCase {

  private WebSchemaConfigService webSchemaConfigService;
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.core.BaseWCMTestCase#setUp()
   */
  public void setUp() throws Exception {
    super.setUp();
    webSchemaConfigService = getService(WebSchemaConfigService.class);
    webSchemaConfigService.getAllWebSchemaHandler().clear();
  }
  
  /**
   * Test add css file schema handler.
   * 
   * @throws Exception the exception
   */
  public void testAddCSSFileSchemaHandler() throws Exception {
    ComponentPlugin componentPlugin = new CSSFileHandler();
    webSchemaConfigService.addWebSchemaHandler(componentPlugin);
    assertEquals(1, webSchemaConfigService.getAllWebSchemaHandler().size());  }
  
  /**
   * Test add double css file schema handler.
   * 
   * @throws Exception the exception
   */
  public void testAddDoubleCSSFileSchemaHandler() throws Exception {
    ComponentPlugin componentPlugin = new CSSFileHandler();
    webSchemaConfigService.addWebSchemaHandler(componentPlugin);
    ComponentPlugin componentPlugin2 = new CSSFileHandler();
    webSchemaConfigService.addWebSchemaHandler(componentPlugin2);
    assertEquals(1, webSchemaConfigService.getAllWebSchemaHandler().size());  }
  
  /**
   * Test add js file schema handler.
   * 
   * @throws Exception the exception
   */
  public void testAddJSFileSchemaHandler() throws Exception {
    ComponentPlugin componentPlugin = new JSFileHandler();
    webSchemaConfigService.addWebSchemaHandler(componentPlugin);
    assertEquals(1, webSchemaConfigService.getAllWebSchemaHandler().size());  }
  
  /**
   * Test add double js file schema handler.
   * 
   * @throws Exception the exception
   */
  public void testAddDoubleJSFileSchemaHandler() throws Exception {
    ComponentPlugin componentPlugin = new JSFileHandler();
    webSchemaConfigService.addWebSchemaHandler(componentPlugin);
    ComponentPlugin componentPlugin2 = new JSFileHandler();
    webSchemaConfigService.addWebSchemaHandler(componentPlugin2);
    assertEquals(1, webSchemaConfigService.getAllWebSchemaHandler().size());
  }
  
  /**
   * Test add html file schema handler.
   * 
   * @throws Exception the exception
   */
  public void testAddHTMLFileSchemaHandler() throws Exception {
    ComponentPlugin componentPlugin = new HTMLFileSchemaHandler();
    webSchemaConfigService.addWebSchemaHandler(componentPlugin);
    assertEquals(1, webSchemaConfigService.getAllWebSchemaHandler().size());
  }
  
  /**
   * Test add double html file schema handler.
   * 
   * @throws Exception the exception
   */
  public void testAddDoubleHTMLFileSchemaHandler() throws Exception {
    ComponentPlugin componentPlugin = new HTMLFileSchemaHandler();
    webSchemaConfigService.addWebSchemaHandler(componentPlugin);
    ComponentPlugin componentPlugin2 = new HTMLFileSchemaHandler();
    webSchemaConfigService.addWebSchemaHandler(componentPlugin2);
    assertEquals(1, webSchemaConfigService.getAllWebSchemaHandler().size());  
  }

  /**
   * Test add portal folder schema handler.
   * 
   * @throws Exception the exception
   */
  public void testAddPortalFolderSchemaHandler() throws Exception {
    ComponentPlugin componentPlugin = new PortalFolderSchemaHandler();
    webSchemaConfigService.addWebSchemaHandler(componentPlugin);
    assertEquals(1, webSchemaConfigService.getAllWebSchemaHandler().size());
  }
  
  /**
   * Test add double portal folder schema handler.
   * 
   * @throws Exception the exception
   */
  public void testAddDoublePortalFolderSchemaHandler() throws Exception {
    ComponentPlugin componentPlugin = new PortalFolderSchemaHandler();
    webSchemaConfigService.addWebSchemaHandler(componentPlugin);
    ComponentPlugin componentPlugin2 = new PortalFolderSchemaHandler();
    webSchemaConfigService.addWebSchemaHandler(componentPlugin2);
    assertEquals(1, webSchemaConfigService.getAllWebSchemaHandler().size());  
  }
  
  /**
   * Test add web content schema handler.
   * 
   * @throws Exception the exception
   */
  public void testAddWebcontentSchemaHandler() throws Exception {
    ComponentPlugin componentPlugin = new WebContentSchemaHandler();
    webSchemaConfigService.addWebSchemaHandler(componentPlugin);
    assertEquals(1, webSchemaConfigService.getAllWebSchemaHandler().size());
  }
  
  /**
   * Test add double web content schema handler.
   * 
   * @throws Exception the exception
   */
  public void testAddDoubleWebcontentSchemaHandler() throws Exception {
    ComponentPlugin componentPlugin = new WebContentSchemaHandler();
    webSchemaConfigService.addWebSchemaHandler(componentPlugin);
    ComponentPlugin componentPlugin2 = new WebContentSchemaHandler();
    webSchemaConfigService.addWebSchemaHandler(componentPlugin2);
    assertEquals(1, webSchemaConfigService.getAllWebSchemaHandler().size());  
  }
  
  /**
   * Test get all web schema handler.
   */
  public void testGetAllWebSchemaHandler() throws Exception {
    webSchemaConfigService.addWebSchemaHandler(new JSFileHandler());
    webSchemaConfigService.addWebSchemaHandler(new CSSFileHandler());
    webSchemaConfigService.addWebSchemaHandler(new HTMLFileSchemaHandler());
    webSchemaConfigService.addWebSchemaHandler(new PortalFolderSchemaHandler());
    webSchemaConfigService.addWebSchemaHandler(new WebContentSchemaHandler());
    assertEquals(5, webSchemaConfigService.getAllWebSchemaHandler().size());
  }
  
  /**
   * Test get web schema handler by type.
   */
  public void testGetWebSchemaHandlerByType() throws Exception {
    webSchemaConfigService.addWebSchemaHandler(new JSFileHandler());
    webSchemaConfigService.addWebSchemaHandler(new CSSFileHandler());
    webSchemaConfigService.addWebSchemaHandler(new HTMLFileSchemaHandler());
    webSchemaConfigService.addWebSchemaHandler(new PortalFolderSchemaHandler());
    webSchemaConfigService.addWebSchemaHandler(new WebContentSchemaHandler());
    
    CSSFileHandler cssFileSchemaHandler = webSchemaConfigService.getWebSchemaHandlerByType(CSSFileHandler.class);
    assertTrue(cssFileSchemaHandler instanceof CSSFileHandler);
    
    JSFileHandler jsFileSchemaHandler = webSchemaConfigService.getWebSchemaHandlerByType(JSFileHandler.class);
    assertTrue(jsFileSchemaHandler instanceof JSFileHandler);
    
    HTMLFileSchemaHandler htmlFileSchemaHandler = webSchemaConfigService.getWebSchemaHandlerByType(HTMLFileSchemaHandler.class);
    assertTrue(htmlFileSchemaHandler instanceof HTMLFileSchemaHandler);
    
    PortalFolderSchemaHandler portalFolderSchemaHandler= webSchemaConfigService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
    assertTrue(portalFolderSchemaHandler instanceof PortalFolderSchemaHandler);
    
    WebContentSchemaHandler webContentSchemaHandler = webSchemaConfigService.getWebSchemaHandlerByType(WebContentSchemaHandler.class);
    assertTrue(webContentSchemaHandler instanceof WebContentSchemaHandler);
  }
  
  /**
   * Test create css file schema handler
   */
  public void testCreateSchema() throws Exception {
    webSchemaConfigService.addWebSchemaHandler(new CSSFileHandler());
    Node cssNode = session.getRootNode().addNode("jsNode", "nt:file");
    SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
    webSchemaConfigService.createSchema(cssNode, sessionProvider);
  }
  
  /**
   * Test update schema on modify.
   */
  public void testUpdateSchemaOnModify() {}
  
  /**
   * Test update schema on remove.
   */
  public void testUpdateSchemaOnRemove() {}

  /* (non-Javadoc)
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }
}
