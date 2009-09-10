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

package org.exoplatform.services.wcm.javascript;

import java.util.Date;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.BaseWCMTestCase;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.exoplatform.web.application.javascript.JavascriptConfigService;

// TODO: Auto-generated Javadoc
/**
 * The Class TestXJavaScriptService.
 * 
 * Created by The eXo Platform SAS
 * Author : Ngoc.Tran
 * ngoc.tran@exoplatform.com
 * July 15, 2008
 */
public class TestXJavaScriptService extends BaseWCMTestCase {

	/** The javascript service. */
	private XJavascriptService javascriptService;
	
	/** The Constant WEB_CONTENT_NODE_NAME. */
	private static final String WEB_CONTENT_NODE_NAME = "webContent";
	
	SessionProvider sessionProvider;
	
	/* (non-Javadoc)
	 * @see org.exoplatform.services.wcm.BaseWCMTestCase#setUp()
	 */
	public void setUp() throws Exception {
		
		super.setUp();
		sessionProvider = WCMCoreUtils.getSessionProvider();
		javascriptService = getService(XJavascriptService.class);
	}
	
	/**
	 * Test get active java script_01.
	 * 
	 * When parameter input is null
	 */
	public void testGetActiveJavaScript_01() {
		try {
			javascriptService.getActiveJavaScript(null);
			fail();
		} catch (Exception e) {
			assertNotNull(e.getStackTrace());
		}
	}

	/**
	 * Test get active java script_02.
	 * 
	 * When node input node type is not exo:webcontent.
	 */
	public void testGetActiveJavaScript_02() {
		try {
			Node root = session.getRootNode();
			Node nodeInput = root.addNode(WEB_CONTENT_NODE_NAME);
			session.save();
			String jsData = javascriptService.getActiveJavaScript(nodeInput);
			assertEquals("", jsData);
		} catch(Exception e) {
			fail();
		}
	}
	
	/**
	 * Test get active java script_03.
	 * 
	 * When node input is exo:webcontent and have some child node but does not content mixin type.
	 */
	public void testGetActiveJavaScript_03() {
		try {
			Node root = session.getRootNode();
			Node webContent = root.addNode(WEB_CONTENT_NODE_NAME, "exo:webContent");
			
			webContent.setProperty("exo:title", WEB_CONTENT_NODE_NAME);
			webContent.addNode("js", "exo:jsFolder");
			webContent.addNode("css", "exo:cssFolder");
			webContent.addNode("medias");
			session.save();
			
			String jsData = javascriptService.getActiveJavaScript(webContent);

			assertEquals("", jsData);
		} catch(Exception e) {
			fail();
		}
	}
	
	/**
	 * Test get active java script_04.
	 * 
	 * When node input is exo:webcontent and have some child node but have mixin type does not exo:jsFile.
	 */
	public void testGetActiveJavaScript_04() {
		try {
			Node root = session.getRootNode();
			Node webContent = root.addNode(WEB_CONTENT_NODE_NAME, "exo:webContent");
			webContent.setProperty("exo:title", WEB_CONTENT_NODE_NAME);
			Node jsFolder = webContent.addNode("js", "exo:jsFolder");
	    Node jsNode = jsFolder.addNode("default.js", "nt:file");
	    
	    jsNode.addMixin("exo:cssFile");
	    jsNode.setProperty("exo:active", true);
	    jsNode.setProperty("exo:priority", 1);
	    jsNode.setProperty("exo:sharedCSS", true);
	    
	    Node jsContent = jsNode.addNode("jcr:content", "nt:resource");
	    jsContent.setProperty("jcr:encoding", "UTF-8");
	    jsContent.setProperty("jcr:mimeType", "text/javascript");
	    jsContent.setProperty("jcr:lastModified", new Date().getTime());
	    jsContent.setProperty("jcr:data", "This is the default.js file.");
	    session.save();

			String jsData = javascriptService.getActiveJavaScript(webContent);
			assertEquals("", jsData);
		} catch(Exception e) {
			fail();
		}
	}

	/**
	 * Test get active java script_05.
	 * 
	 * Child node have properties normal and value of exo:active is:
	 * - "exo:active": false
	 */
	public void testGetActiveJavaScript_05() {
		try {
			Node root = session.getRootNode();
			Node webContent = createWebcontentNode(root, WEB_CONTENT_NODE_NAME, null, null, null);
			Node jsNode = webContent.getNode("js").getNode("default.js");
			jsNode.setProperty("exo:active", false);
			session.save();
			
			String jsData = javascriptService.getActiveJavaScript(webContent);
			assertEquals("", jsData);
		} catch(Exception e) {
			fail();
		}
	}
	
	/**
	 * Test get active java script_06.
	 * 
	 * Child node have properties normal and value of jcr:mimeType is:
	 * - "jcr:mimeType": text/html
	 */
	public void testGetActiveJavaScript_06() {
		try {
			Node root = session.getRootNode();
			Node webContent = createWebcontentNode(root, WEB_CONTENT_NODE_NAME, null, null, null);
			Node jsNode = webContent.getNode("js").getNode("default.js");
			Node jsContent = jsNode.getNode("jcr:content");
			jsContent.setProperty("jcr:mimeType", "text/html");
			session.save();
			
			String jsData = javascriptService.getActiveJavaScript(webContent);
			assertEquals("", jsData);
		} catch(Exception e) {
			fail();
		}
	}
	
	/**
	 * Test get active java script_07.
	 * 
	 * Child node have properties normal and value of jcr:mimeType is:
	 * - "jcr:mimeType": text/javascript
	 */
	public void testGetActiveJavaScript_07() {
		try {
			Node root = session.getRootNode();
			Node webContent = createWebcontentNode(root, WEB_CONTENT_NODE_NAME, null, null, null);
			Node jsNode = webContent.getNode("js").getNode("default.js");
			Node jsContent = jsNode.getNode("jcr:content");
			jsContent.setProperty("jcr:mimeType", "text/javascript");
			session.save();
			
			String jsData = javascriptService.getActiveJavaScript(webContent);
			assertEquals("This is the default.js file.", jsData);
		} catch(Exception e) {
			fail();
		}
	}
	
	/**
	 * Test get active java script_08.
	 * 
	 * Child node have properties normal and value of jcr:mimeType is:
	 * - "jcr:mimeType": application/x-javascript
	 */
	public void testGetActiveJavaScript_08() {
		try {
			Node root = session.getRootNode();
			Node webContent = createWebcontentNode(root, WEB_CONTENT_NODE_NAME, null, null, null);
			Node jsNode = webContent.getNode("js").getNode("default.js");
			Node jsContent = jsNode.getNode("jcr:content");
			jsContent.setProperty("jcr:mimeType", "application/x-javascript");
			session.save();
			
			String jsData = javascriptService.getActiveJavaScript(webContent);
			assertEquals("This is the default.js file.", jsData);
		} catch(Exception e) {
			fail();
		}
	}
	
	/**
	 * Test get active java script_09.
	 * 
	 * Child node have properties normal and value of jcr:mimeType is:
	 * - "jcr:mimeType": text/ecmascript
	 */
	public void testGetActiveJavaScript_09() {
		try {
			Node root = session.getRootNode();
			Node webContent = createWebcontentNode(root, WEB_CONTENT_NODE_NAME, null, null, null);
			Node jsNode = webContent.getNode("js").getNode("default.js");
			Node jsContent = jsNode.getNode("jcr:content");
			jsContent.setProperty("jcr:mimeType", "text/ecmascript");
			session.save();
			
			String jsData = javascriptService.getActiveJavaScript(webContent);
			assertEquals("This is the default.js file.", jsData);
		} catch(Exception e) {
			fail();
		}
	}
	
	/**
	 * Test get active java script_10.
	 * 
	 * Child node have properties normal and value of jcr:data is ""
	 */
	public void testGetActiveJavaScript_10() {
		try {
			Node root = session.getRootNode();
			Node webContent = createWebcontentNode(root, WEB_CONTENT_NODE_NAME, null, null, null);
			Node jsNode = webContent.getNode("js").getNode("default.js");
			Node jsContent = jsNode.getNode("jcr:content");
			jsContent.setProperty("jcr:data", "");
			session.save();
			
			String jsData = javascriptService.getActiveJavaScript(webContent);
			assertEquals("", jsData);
		} catch(Exception e) {
			fail();
		}
	}
	
	/**
	 * Test get active java script_11.
	 * 
	 * Child node have properties normal and value of jcr:data is:
	 * - "jcr:data": This is the default.js file.
	 */
	public void testGetActiveJavaScript_11() {
		try {
			Node root = session.getRootNode();
			Node webContent = createWebcontentNode(root, WEB_CONTENT_NODE_NAME, null, null, null);
			Node jsNode = webContent.getNode("js").getNode("default.js");
			Node jsContent = jsNode.getNode("jcr:content");
			jsContent.setProperty("jcr:data", "This is the default.js file.");
			session.save();
			
			String jsData = javascriptService.getActiveJavaScript(webContent);
			assertEquals("This is the default.js file.", jsData);
		} catch(Exception e) {
			fail();
		}
	}
	
	/**
	 * Test get active java script_12.
	 * 
	 * Child node have properties normal and value of jcr:mimeType is:
	 * - "jcr:data": alert('Test method getActiveJavaScript()');.
	 */
	public void testGetActiveJavaScript_12() {
		try {
			Node root = session.getRootNode();
			Node webContent = createWebcontentNode(root, WEB_CONTENT_NODE_NAME, null, null, "alert('Test method getActiveJavaScript()');");
			session.save();
			String jsData = javascriptService.getActiveJavaScript(webContent);
			assertEquals("alert('Test method getActiveJavaScript()');", jsData);
		} catch(Exception e) {
			fail();
		}
	}
	
	/**
	 * Test get active java script_13.
	 * 
	 * In case normal
	 */
	public void testGetActiveJavaScript_13() {
		try {
			Node root = session.getRootNode();
			Node webContent = createWebcontentNode(root, WEB_CONTENT_NODE_NAME, null, null, null);
			String jsData = javascriptService.getActiveJavaScript(webContent);
			assertEquals("This is the default.js file.", jsData);
		} catch (Exception e) {
			fail();
		}
	}

	/**
	 * Test update portal js on modify_01.
	 * When node input is null.
	 */
	public void testUpdatePortalJSOnModify_01() {
		try {
			javascriptService.updatePortalJSOnModify(null, sessionProvider);
			fail();
		} catch(Exception e) {
			assertNotNull(e.getStackTrace());
		}
	}
	
	/**
	 * Test update portal js on modify_02.
	 * When SessionProvider input is null.
	 */
	public void testUpdatePortalJSOnModify_02() {
		try {
			Node root = session.getRootNode();
			Node webContent = createWebcontentNode(root, WEB_CONTENT_NODE_NAME, null, null, null);
			Node jsNode = webContent.getNode("js").getNode("default.js");
			javascriptService.updatePortalJSOnModify(jsNode, null);
			fail();
		} catch(Exception e) {
			assertNotNull(e.getStackTrace());
		}
	}
	
	/**
	 * Test update portal js on modify_03.
	 * When Node input does not jsFile.
	 */
	public void testUpdatePortalJSOnModify_03() {
		try {
			Node root = session.getRootNode();
			Node webContent = createWebcontentNode(root, WEB_CONTENT_NODE_NAME + "1", null, null, null);
			createWebcontentNode(root, WEB_CONTENT_NODE_NAME + "2", null, null, null);
			Node jsFolder = webContent.getNode("js");
			javascriptService.updatePortalJSOnModify(jsFolder, sessionProvider);
			fail();
		} catch(Exception e) {
			assertNotNull(e.getStackTrace());
		}
	}
	
	/**
	 * Test update portal js on modify_04.
	 * When node input have jcr:data is "".
	 */
	public void testUpdatePortalJSOnModify_04() {
		try {
			Node root = session.getRootNode();
			JavascriptConfigService configService = null;
			Node liveNode = root.getNode("sites content").getNode("live");
			Node webContent = createWebcontentNode(liveNode, WEB_CONTENT_NODE_NAME + "1", null, null, "");
			createWebcontentNode(liveNode, WEB_CONTENT_NODE_NAME + "2", null, null, null);
			Node jsNode = webContent.getNode("js").getNode("default.js");
			javascriptService.updatePortalJSOnModify(jsNode, sessionProvider);
			session.save();
			
			configService = getService(JavascriptConfigService.class);
			String jsData = new String(configService.getMergedJavascript());
			assertEquals("\nThis is the default.js file.", jsData);
		} catch(Exception e) {
			fail();
		}
	}
	
	/**
	 * Test update portal js on modify_05.
	 * When node input have jcr:data is "When perform testUpdatePortalJSOnModify...".
	 */
	public void testUpdatePortalJSOnModify_05() {
		try {
			Node root = session.getRootNode();
			JavascriptConfigService configService = null;
			Node liveNode = root.getNode("sites content").getNode("live");
			Node webContent = createWebcontentNode(liveNode, WEB_CONTENT_NODE_NAME + "1", null, null, "When perform testUpdatePortalJSOnModify...");
			createWebcontentNode(liveNode, WEB_CONTENT_NODE_NAME + "2", null, null, null);
			
			Node jsNode = webContent.getNode("js").getNode("default.js");
			javascriptService.updatePortalJSOnModify(jsNode, sessionProvider);
			session.save();
			configService = getService(JavascriptConfigService.class);
			String jsData = new String(configService.getMergedJavascript());
			assertEquals("\nThis is the default.js file.When perform testUpdatePortalJSOnModify...", jsData);
		} catch(Exception e) {
			fail();
		}
	}
	
	/**
	 * Test update portal js on modify_06.
	 * When node input have jcr:data is "alert('testUpdatePortalJSOnModify...');".
	 */
	public void testUpdatePortalJSOnModify_06() {
		try {
			Node root = session.getRootNode();
			JavascriptConfigService configService = null;
			Node liveNode = root.getNode("sites content").getNode("live");
			Node webContent = createWebcontentNode(liveNode, WEB_CONTENT_NODE_NAME + "1", null, null, "alert('testUpdatePortalJSOnModify...');");
			createWebcontentNode(liveNode, WEB_CONTENT_NODE_NAME + "1", null, null, null);
			Node jsNode = webContent.getNode("js").getNode("default.js");
			javascriptService.updatePortalJSOnModify(jsNode, sessionProvider);
			session.save();
			
			configService = getService(JavascriptConfigService.class);
			String jsData = new String(configService.getMergedJavascript());
			assertEquals("\nThis is the default.js file.alert('testUpdatePortalJSOnModify...');", jsData);
		} catch(Exception e) {
			fail();
		}
	}
	
	/**
	 * Test update portal js on remove_01.
	 * When node input is null.
	 */
	public void testUpdatePortalJSOnRemove_01() {
		try {
			javascriptService.updatePortalJSOnRemove(null, sessionProvider);
			fail();
		} catch(Exception e) {
			assertNotNull(e.getStackTrace());
		}
	}
	
	/**
	 * Test update portal js on remove_02.
	 * When Session Provider input is null.
	 */
	public void testUpdatePortalJSOnRemove_02() {
		try {
			Node root = session.getRootNode();
			Node webContent = createWebcontentNode(root, WEB_CONTENT_NODE_NAME, null, null, null);
			Node jsNode = webContent.getNode("js").getNode("default.js");
			javascriptService.updatePortalJSOnRemove(jsNode, null);
			fail();
		} catch(Exception e) {
			assertNotNull(e.getStackTrace());
		}
	}
	
	/**
	 * Test update portal js on remove_03.
	 * When Node input does not jsFile.
	 */
	public void testUpdatePortalJSOnRemove_03() {
		try {
			JavascriptConfigService configService = null;
			Node root = session.getRootNode();
			Node liveNode = root.getNode("sites content").getNode("live");
			Node webContent = createWebcontentNode(liveNode, WEB_CONTENT_NODE_NAME + "1", null, null, null);
			createWebcontentNode(root, WEB_CONTENT_NODE_NAME + "2", null, null, null);
			Node jsFolder = webContent.getNode("js");
			javascriptService.updatePortalJSOnRemove(jsFolder, sessionProvider);
			session.save();
			String jsData = "";
			configService = getService(JavascriptConfigService.class);
			jsData = new String(configService.getMergedJavascript());
			assertEquals("\nThis is the default.js file.", jsData);
		} catch(Exception e) {
			fail();
		}
	}
	
	/**
	 * Test update portal js on remove_04.
	 * When node input have jcr:data is "".
	 */
	public void testUpdatePortalJSOnRemove_04() {
		try {
			JavascriptConfigService configService = null;
			Node root = session.getRootNode();
			Node liveNode = root.getNode("sites content").getNode("live");
			Node webContent = createWebcontentNode(liveNode, WEB_CONTENT_NODE_NAME + "1", null, null, "");
			createWebcontentNode(liveNode, WEB_CONTENT_NODE_NAME + "2", null, null, null);
			Node jsNode = webContent.getNode("js").getNode("default.js");
			javascriptService.updatePortalJSOnModify(jsNode, sessionProvider);
			session.save();
			String jsData = "";
			configService = getService(JavascriptConfigService.class);
			jsData = new String(configService.getMergedJavascript());
			assertEquals("\nThis is the default.js file.", jsData);
			javascriptService.updatePortalJSOnRemove(jsNode, sessionProvider);
			session.save();
			jsData = new String(configService.getMergedJavascript());
			assertEquals("\nThis is the default.js file.", jsData);
		} catch(Exception e) {
			fail();
		}
	}
	
	/**
	 * Test update portal js on remove_05.
	 * When node input have jcr:data is "alert('testUpdatePortalJSOnModify...');".
	 */
	public void testUpdatePortalJSOnRemove_05() {
		try {
			JavascriptConfigService configService = null;
			Node root = session.getRootNode();
			Node liveNode = root.getNode("sites content").getNode("live");
			Node webContent = createWebcontentNode(liveNode, WEB_CONTENT_NODE_NAME + "1", null, null, "alert('testUpdatePortalJSOnModify...');");
			createWebcontentNode(liveNode, WEB_CONTENT_NODE_NAME + "2", null, null, null);
			Node jsNode = webContent.getNode("js").getNode("default.js");
			javascriptService.updatePortalJSOnModify(jsNode, sessionProvider);
			session.save();
			String jsData = "";
			configService = getService(JavascriptConfigService.class);
			jsData = new String(configService.getMergedJavascript());
			assertEquals("\nThis is the default.js file.alert('testUpdatePortalJSOnModify...');", jsData);
			javascriptService.updatePortalJSOnRemove(jsNode, sessionProvider);
			session.save();
			jsData = new String(configService.getMergedJavascript());
			assertEquals("\nThis is the default.js file.", jsData);
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
		Node sharedNode = rootNode.getNode("sites content").getNode("live");
		NodeIterator nodeIterator = sharedNode.getNodes();
		while(nodeIterator.hasNext()) {
			nodeIterator.nextNode().remove();
		}
		session.save();
		sessionProvider.close();
	}
}
