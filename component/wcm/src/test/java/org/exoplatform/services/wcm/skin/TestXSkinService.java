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

package org.exoplatform.services.wcm.skin;

import java.util.Date;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.portal.webui.skin.SkinService;
import org.exoplatform.services.wcm.BaseWCMTestCase;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;

// TODO: Auto-generated Javadoc
/**
 * The Class TestXJavaScriptService.
 * 
 * Created by The eXo Platform SAS
 * Author : Ngoc.Tran
 * ngoc.tran@exoplatform.com
 * July 21, 2008
 */
public class TestXSkinService extends BaseWCMTestCase {

	/** The skin service. */
	private XSkinService skinService;
	
	/** The Constant WEB_CONTENT_NODE_NAME. */
	private static final String WEB_CONTENT_NODE_NAME = "webContent";
	
	/* (non-Javadoc)
	 * @see org.exoplatform.services.wcm.BaseWCMTestCase#setUp()
	 */
	public void setUp() throws Exception {
		
		super.setUp();
		
		skinService = getService(XSkinService.class);
	}
	
	/**
	 * Test get active Stylesheet_01.
	 * 
	 * When parameter input is null
	 */
	public void testGetActiveStylesheet_01() {

		try {
			skinService.getActiveStylesheet(null);
			fail();
		} catch (Exception e) {
			assertNotNull(e.getStackTrace());
		}
	}

	/**
	 * Test get active Stylesheet_02.
	 * 
	 * When node input node type is not exo:webcontent.
	 */
	public void testGetActiveStylesheet_02() {
	
		try {
			
			Node root = session.getRootNode();
			Node nodeInput = root.addNode(WEB_CONTENT_NODE_NAME);
			session.save();
			
			String cssData = skinService.getActiveStylesheet(nodeInput);
			
			assertEquals("", cssData);
		} catch(Exception e) {

			fail("Exception can't raise here!!!");
		}
	}
	
	/**
	 * Test get active Stylesheet_03.
	 * 
	 * When node input is exo:webcontent and have some child node but does not content mixin type.
	 */
	public void testGetActiveStylesheet_03() {
	
		try {
			
			Node root = session.getRootNode();
			Node webContent = root.addNode(WEB_CONTENT_NODE_NAME, "exo:webContent");
			
			webContent.setProperty("exo:title", WEB_CONTENT_NODE_NAME);
			webContent.addNode("js", "exo:jsFolder");
			webContent.addNode("css", "exo:cssFolder");
			webContent.addNode("medias");
			session.save();
			
			String cssData = skinService.getActiveStylesheet(webContent);

			assertEquals("", cssData);
		} catch(Exception e) {

			fail("Exception can't raise here!!!");
		}
	}
	
	/**
	 * Test get active Stylesheet_04.
	 * 
	 * When node input is exo:webcontent and have some child node but have mixin type does not exo:cssFile.
	 */
	public void testGetActiveStylesheet_04() {
	
		try {
			
			Node root = session.getRootNode();
			Node webContent = root.addNode(WEB_CONTENT_NODE_NAME, "exo:webContent");
			
			webContent.setProperty("exo:title", WEB_CONTENT_NODE_NAME);
			Node jsFolder = webContent.addNode("css", "exo:cssFolder");
		    Node jsNode = jsFolder.addNode("default.css", "nt:file");
		    jsNode.addMixin("exo:jsFile");
		    jsNode.setProperty("exo:active", true);
		    jsNode.setProperty("exo:priority", 1);
		    jsNode.setProperty("exo:sharedJS", true);
		    
		    Node jsContent = jsNode.addNode("jcr:content", "nt:resource");
		    jsContent.setProperty("jcr:encoding", "UTF-8");
		    jsContent.setProperty("jcr:mimeType", "text/css");
		    jsContent.setProperty("jcr:lastModified", new Date().getTime());
		    jsContent.setProperty("jcr:data", "This is the default.css file.");
		    
		    session.save();

			String jsData = skinService.getActiveStylesheet(webContent);

			assertEquals("", jsData);
		} catch(Exception e) {

			fail("Exception can't raise here!!!");
		}
	}

	/**
	 * Test get active Stylesheet_05.
	 * 
	 * Child node have properties normal and value of exo:active is:
	 * - "exo:active": false
	 */
	public void testGetActiveStylesheet_05() {
		
		try {
			Node root = session.getRootNode();
			Node webContent = createWebcontentNode(root, WEB_CONTENT_NODE_NAME, null, null, null);
			Node jsNode = webContent.getNode("css").getNode("default.css");
			
			jsNode.setProperty("exo:active", false);
			
			session.save();
			
			String cssData = skinService.getActiveStylesheet(webContent);

			assertEquals("", cssData);
		} catch(Exception e) {

			fail("Exception can't raise here!!!");
		}
	}
	
	/**
	 * Test get active Stylesheet_06.
	 * 
	 * Child node have properties normal and value of jcr:mimeType is:
	 * - "jcr:mimeType": text/html
	 */
	public void testGetActiveStylesheet_06() {
		
		try {
			Node root = session.getRootNode();
			Node webContent = createWebcontentNode(root, WEB_CONTENT_NODE_NAME, null, null, null);
			Node jsNode = webContent.getNode("css").getNode("default.css");
			Node jsContent = jsNode.getNode("jcr:content");
			
			jsContent.setProperty("jcr:mimeType", "text/html");
			
			session.save();
			
			String cssData = skinService.getActiveStylesheet(webContent);

			assertEquals("", cssData);
		} catch(Exception e) {

			fail("Exception can't raise here!!!");
		}
	}
	
	/**
	 * Test get active Stylesheet_07.
	 * 
	 * Child node have properties normal and value of jcr:mimeType is:
	 * - "jcr:mimeType": text/css
	 */
	public void testGetActiveStylesheet_07() {
		
		try {
			Node root = session.getRootNode();
			Node webContent = createWebcontentNode(root, WEB_CONTENT_NODE_NAME, null, null, null);
			Node jsNode = webContent.getNode("css").getNode("default.css");
			Node jsContent = jsNode.getNode("jcr:content");
			
			jsContent.setProperty("jcr:mimeType", "text/css");
			
			session.save();
			
			String cssData = skinService.getActiveStylesheet(webContent);

			assertEquals("This is the default.css file.", cssData);
		} catch(Exception e) {

			fail("Exception can't raise here!!!");
		}
	}
	
	/**
	 * Test get active Stylesheet_08.
	 * 
	 * Child node have properties normal and value of jcr:data is ""
	 */
	public void testGetActiveStylesheet_08() {
		
		try {
			Node root = session.getRootNode();
			Node webContent = createWebcontentNode(root, WEB_CONTENT_NODE_NAME, null, null, null);
			Node jsNode = webContent.getNode("css").getNode("default.css");
			Node jsContent = jsNode.getNode("jcr:content");
			
			jsContent.setProperty("jcr:data", "");
			
			session.save();
			
			String cssData = skinService.getActiveStylesheet(webContent);

			assertEquals("", cssData);
		} catch(Exception e) {

			fail("Exception can't raise here!!!");
		}
	}
	
	/**
	 * Test get active Stylesheet_09.
	 * 
	 * In case normal
	 */
	public void testGetActiveStylesheet_09() {
		
		try {
			Node root = session.getRootNode();
			Node webContent = createWebcontentNode(root, WEB_CONTENT_NODE_NAME, null, null, null);
			
			String cssData = skinService.getActiveStylesheet(webContent);
			assertEquals("This is the default.css file.", cssData);
		} catch (Exception e) {
			fail();
		}
	}

	/**
	 * Test update portal Skin on modify_01.
	 * When node input is null.
	 */
	public void testUpdatePortalSkinOnModify_01() {
		try {
			Node root = session.getRootNode();
			Node classicNode = root.getNode("sites content").getNode("live").getNode("classic");
			Node portal = getPortalNode(classicNode);
			skinService.updatePortalSkinOnModify(null, portal);
			fail();
		} catch(Exception e) {
			assertNotNull(e.getStackTrace());
		}
	}
	
	/**
	 * Test update portal Skin on modify_02.
	 * When Node portal input is null.
	 */
	public void testUpdatePortalSkinOnModify_02() {
		try {
			Node root = session.getRootNode();
			Node webContent = createWebcontentNode(root, WEB_CONTENT_NODE_NAME, null, null, null);
			Node jsNode = webContent.getNode("css").getNode("default.css");
			skinService.updatePortalSkinOnModify(jsNode, null);
			fail();
		} catch(Exception e) {
			assertNotNull(e.getStackTrace());
		}
	}
	
	/**
	 * Test update portal Skin on modify_03.
	 * When Node input does not cssFile.
	 */
	public void testUpdatePortalSkinOnModify_03() {
		try {
			Node root = session.getRootNode();
			Node classicNode = root.getNode("sites content").getNode("live").getNode("classic");
			Node portal = getPortalNode(classicNode);
			Node webContent = createWebcontentNode(classicNode, WEB_CONTENT_NODE_NAME + "1", null, null, null);
			createWebcontentNode(classicNode, WEB_CONTENT_NODE_NAME + "2", null, null, null);
			Node jsFolder = webContent.getNode("css");
			skinService.updatePortalSkinOnModify(jsFolder, portal);
			fail();
		} catch(Exception e) {
			assertNotNull(e.getStackTrace());
		}
	}
	
	/**
	 * Test update portal Skin on modify_04.
	 * When node input have jcr:data is "".
	 */
	public void testUpdatePortalSkinOnModify_04() {
		try {
			Node root = session.getRootNode();
			Node classicNode = root.getNode("sites content").getNode("live").getNode("classic");
			Node portal = getPortalNode(classicNode);
			SkinService configService = null;
			Node cssFolder;
		    try{
		      cssFolder = classicNode.getNode("css");
		    }catch(Exception ex){
		      cssFolder = classicNode.addNode("css", "exo:cssFolder");
		    }

		    Node cssNode = createNode(cssFolder,"default1.css", "");
			
			createNode(cssFolder,"default2.css", "This is the default.css file.");
		    
			session.save();
			configService = getService(SkinService.class);
			configService.addSkin("", "Default", "", "");
			
			skinService.updatePortalSkinOnModify(cssNode, portal);
			session.save();

			String cssData = configService.getMergedCSS("/portlet_app_1/css/jcr/classic/Default/Stylesheet.css");
			assertEquals("This is the default.css file.", cssData);
			
		} catch(Exception e) {
			fail();
		}
	}
	
	/**
	 * Test update portal Skin on modify_05.
	 * When node input have jcr:data is "Test XSkin Service".
	 */
	public void testUpdatePortalSkinOnModify_05() {
		try {
			Node root = session.getRootNode();
			Node classicNode = root.getNode("sites content").getNode("live").getNode("classic");
			Node portal = getPortalNode(classicNode);
			SkinService configService = null;
			Node cssFolder;
		    try{
		      cssFolder = classicNode.getNode("css");
		    }catch(Exception ex){
		      cssFolder = classicNode.addNode("css", "exo:cssFolder");
		    }

		    Node cssNode = createNode(cssFolder,"default1.css", "Test XSkin Service");
			
			createNode(cssFolder,"default2.css", "This is the default.css file.");
		    
			session.save();
			configService = getService(SkinService.class);
			configService.addSkin("", "Default", "", "");
			
			skinService.updatePortalSkinOnModify(cssNode, portal);
			session.save();

			String cssData = configService.getMergedCSS("/portlet_app_1/css/jcr/classic/Default/Stylesheet.css");
			assertEquals("This is the default.css file.Test XSkin Service", cssData);
			
		} catch(Exception e) {
			fail();
		}
	}
	
	/**
	 * Test update portal Skin on remove_01.
	 * When node input is null.
	 */
	public void testUpdatePortalSkinOnRemove_01() {
		try {
			Node root = session.getRootNode();
			Node classicNode = root.getNode("sites content").getNode("live").getNode("classic");
			Node portal = getPortalNode(classicNode);
			skinService.updatePortalSkinOnRemove(null, portal);
			fail();
		} catch(Exception e) {
			assertNotNull(e.getStackTrace());
		}
	}
	
	/**
	 * Test update portal Skin on remove_02.
	 * When Node portal input is null.
	 */
	public void testUpdatePortalSkinOnRemove_02() {
		try {
			Node root = session.getRootNode();
			Node webContent = createWebcontentNode(root, WEB_CONTENT_NODE_NAME, null, null, null);
			Node jsNode = webContent.getNode("css").getNode("default.css");
			skinService.updatePortalSkinOnRemove(jsNode, null);
			fail();
		} catch(Exception e) {
			assertNotNull(e.getStackTrace());
		}
	}
	
	/**
	 * Test update portal Skin on remove_03.
	 * When Node input does not cssFile.
	 */
	public void testUpdatePortalSkinOnRemove_03() {
		try {
			Node root = session.getRootNode();
			Node classicNode = root.getNode("sites content").getNode("live").getNode("classic");
			Node portal = getPortalNode(classicNode);
			Node webContent = createWebcontentNode(classicNode, WEB_CONTENT_NODE_NAME + "1", null, null, null);
			createWebcontentNode(classicNode, WEB_CONTENT_NODE_NAME + "2", null, null, null);
			Node jsFolder = webContent.getNode("css");
			skinService.updatePortalSkinOnRemove(jsFolder, portal);
			fail();
		} catch(Exception e) {
			assertNotNull(e.getStackTrace());
		}
	}
	
	/**
	 * Test update portal Skin on remove_04.
	 * When node input have jcr:data is "".
	 */
	public void testUpdatePortalSkinOnRemove_04() {
		try {
			Node root = session.getRootNode();
			Node classicNode = root.getNode("sites content").getNode("live").getNode("classic");
			Node portal = getPortalNode(classicNode);
			SkinService configService = null;
			
			Node cssFolder;
		    try{
		      cssFolder = classicNode.getNode("css");
		    }catch(Exception ex){
		      cssFolder = classicNode.addNode("css", "exo:cssFolder");
		    }

		    Node cssNode = createNode(cssFolder,"default1.css", "");
			
			createNode(cssFolder,"default2.css", "This is the default.css file.");
		    
			session.save();
			configService = getService(SkinService.class);
			configService.addSkin("", "Default", "", "");
			
			skinService.updatePortalSkinOnRemove(cssNode, portal);
			session.save();

			String cssData = configService.getMergedCSS("/portlet_app_1/css/jcr/classic/Default/Stylesheet.css");
			assertEquals("This is the default.css file.", cssData);
			
		} catch(Exception e) {
			fail();
		}
	}
	
	/**
	 * Test update portal Skin on remove_05.
	 * When node input have jcr:data is "Test XSkin Service".
	 */
	public void testUpdatePortalSkinOnRemove_05() {
		try {
			Node root = session.getRootNode();
			Node classicNode = root.getNode("sites content").getNode("live").getNode("classic");
			Node portal = getPortalNode(classicNode);
			SkinService configService = null;
			Node cssFolder;
		    try{
		      cssFolder = classicNode.getNode("css");
		    }catch(Exception ex){
		      cssFolder = classicNode.addNode("css", "exo:cssFolder");
		    }

		    Node cssNode = createNode(cssFolder,"default1.css", "Test XSkin Service");
			
			createNode(cssFolder,"default2.css", "This is the default.css file.");
			session.save();
			configService = getService(SkinService.class);
			configService.addSkin("", "Default", "", "");
			
			skinService.updatePortalSkinOnModify(cssNode, portal);
			session.save();

			String cssDataOnModify = configService.getMergedCSS("/portlet_app_1/css/jcr/classic/Default/Stylesheet.css");
			assertEquals("This is the default.css file.Test XSkin Service", cssDataOnModify);
			
			skinService.updatePortalSkinOnRemove(cssNode, portal);
			session.save();

			String cssData = configService.getMergedCSS("/portlet_app_1/css/jcr/classic/Default/Stylesheet.css");
			assertEquals("This is the default.css file.", cssData);
			
		} catch(Exception e) {
			fail();
		}
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	public void tearDown() throws Exception {
		
		super.tearDown();
		Node rootNode = session.getRootNode();
		if(rootNode.hasNode(WEB_CONTENT_NODE_NAME))
			rootNode.getNode(WEB_CONTENT_NODE_NAME).remove();
		Node sharedNode = rootNode.getNode("sites content").getNode("live").getNode("classic");
		NodeIterator nodeIterator = sharedNode.getNodes();
		while(nodeIterator.hasNext()) {
			nodeIterator.nextNode().remove();
		}
		session.save();
	}
	
	/**
	 * Gets the portal node.
	 * 
	 * @param parent the parent
	 * 
	 * @return the portal node
	 * 
	 * @throws Exception the exception
	 */
	private Node getPortalNode(Node parent) throws Exception{    
	    
		LivePortalManagerService livePortalManagerService = getService(LivePortalManagerService.class);                
	    
	    return livePortalManagerService.getLivePortalByChild(parent);
	}
	
	/**
	 * Creates the node.
	 * 
	 * @param parentNode the parent node
	 * @param nodeName the node name
	 * @param test the test
	 * 
	 * @return the node
	 * 
	 * @throws Exception the exception
	 */
	private Node createNode(Node parentNode,String nodeName, String test) throws Exception {
		
		
		Node cssNode;
	    try{
	      cssNode = parentNode.getNode(nodeName);
	    }catch(Exception ex){
	      cssNode = parentNode.addNode(nodeName, "nt:file");
	    }
	    if(!cssNode.isNodeType("exo:cssFile"))cssNode.addMixin("exo:cssFile");
	    cssNode.setProperty("exo:active", true);
	    cssNode.setProperty("exo:priority", 1);
	    cssNode.setProperty("exo:sharedCSS", true);
	    
	    Node cssContent;
	    try{
	      cssContent = cssNode.getNode("jcr:content");
	    }catch(Exception ex){
	      cssContent = cssNode.addNode("jcr:content", "nt:resource");
	    }
	    cssContent.setProperty("jcr:encoding", "UTF-8");
	    cssContent.setProperty("jcr:mimeType", "text/css");
	    cssContent.setProperty("jcr:lastModified", new Date().getTime());
	    cssContent.setProperty("jcr:data", test);
	    session.save();
		return cssNode;
	}
}
