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
package org.exoplatform.services.wcm;

import java.util.Date;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.test.BasicTestCase;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jul 14, 2009  
 */
public abstract class BaseWCMTestCase extends BasicTestCase {

  protected StandaloneContainer   container;
  
  protected Session               session;
  
  protected final String          REPO_NAME        = "repository".intern();

  protected final String          DMSSYSTEM_WS     = "dms-system".intern();
  
  protected final String          SYSTEM_WS        = "system".intern();

  protected final String          COLLABORATION_WS = "collaboration".intern();

  public void setUp() throws Exception {
    String containerConf = getClass().getResource("/conf/standalone/test-configuration.xml").toString();
    String loginConf = Thread.currentThread().getContextClassLoader().getResource("login.conf").toString();

    StandaloneContainer.addConfigurationURL(containerConf);
    container = StandaloneContainer.getInstance();

    if (System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config", loginConf);
    
    RepositoryService repositoryService = getService(RepositoryService.class);
    session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);
  }

  protected void checkMixins(String[] mixins, NodeImpl node) {
    try {
      String[] nodeMixins = node.getMixinTypeNames();
      assertEquals("Mixins count is different", mixins.length, nodeMixins.length);

      compareMixins(mixins, nodeMixins);
    } catch (RepositoryException e) {
      fail("Mixins isn't accessible on the node " + node.getPath());
    }
  }

  protected void compareMixins(String[] mixins, String[] nodeMixins) {
    nextMixin: for (String mixin : mixins) {
      for (String nodeMixin : nodeMixins) {
        if (mixin.equals(nodeMixin))
          continue nextMixin;
      }

      fail("Mixin '" + mixin + "' isn't accessible");
    }
  }

  protected String memoryInfo() {
    String info = "";
    info = "free: " + mb(Runtime.getRuntime().freeMemory()) + "M of "
    + mb(Runtime.getRuntime().totalMemory()) + "M (max: "
    + mb(Runtime.getRuntime().maxMemory()) + "M)";
    return info;
  }

  // bytes to Mbytes
  protected String mb(long mem) {
    return String.valueOf(Math.round(mem * 100d / (1024d * 1024d)) / 100d);
  }

  protected String execTime(long from) {
    return Math.round(((System.currentTimeMillis() - from) * 100.00d / 60000.00d)) / 100.00d
    + "min";
  }

  protected <T> T getService(Class<T> clazz) {
    return clazz.cast(container.getComponentInstanceOfType(clazz));
  }

  protected Node createWebcontentNode(Node parentNode, String nodeName, String htmlData, String cssData, String jsData) throws Exception {
    Node webcontent = parentNode.addNode(nodeName, "exo:webContent");
    webcontent.setProperty("exo:title", nodeName);

    Node htmlNode = webcontent.addNode("default.html", "nt:file");
    htmlNode.addMixin("exo:htmlFile");
    Node htmlContent = htmlNode.addNode("jcr:content", "nt:resource");
    htmlContent.setProperty("jcr:encoding", "UTF-8");
    htmlContent.setProperty("jcr:mimeType", "text/html");
    htmlContent.setProperty("jcr:lastModified", new Date().getTime());
    if (htmlData == null) htmlData = "This is the default.html file.";
    htmlContent.setProperty("jcr:data", htmlData);
    
    Node jsFolder = webcontent.addNode("js", "exo:jsFolder");
    Node jsNode = jsFolder.addNode("default.js", "nt:file");
    jsNode.addMixin("exo:jsFile");
    jsNode.setProperty("exo:active", true);
    jsNode.setProperty("exo:priority", 1);
    jsNode.setProperty("exo:sharedJS", true);
    
    Node jsContent = jsNode.addNode("jcr:content", "nt:resource");
    jsContent.setProperty("jcr:encoding", "UTF-8");
    jsContent.setProperty("jcr:mimeType", "text/javascript");
    jsContent.setProperty("jcr:lastModified", new Date().getTime());
    if (jsData == null) jsData = "This is the default.js file.";
    jsContent.setProperty("jcr:data", jsData);
    
    Node cssFolder = webcontent.addNode("css", "exo:cssFolder");
    Node cssNode = cssFolder.addNode("default.css", "nt:file");
    cssNode.addMixin("exo:cssFile");
    cssNode.setProperty("exo:active", true);
    cssNode.setProperty("exo:priority", 1);
    cssNode.setProperty("exo:sharedCSS", true);
    
    Node cssContent = cssNode.addNode("jcr:content", "nt:resource");
    cssContent.setProperty("jcr:encoding", "UTF-8");
    cssContent.setProperty("jcr:mimeType", "text/css");
    cssContent.setProperty("jcr:lastModified", new Date().getTime());
    if (cssData == null) cssData = "This is the default.css file.";
    cssContent.setProperty("jcr:data", cssData);
    
    Node mediaFolder = webcontent.addNode("medias");
    mediaFolder.addNode("images", "nt:folder");
    mediaFolder.addNode("videos", "nt:folder");
    mediaFolder.addNode("audio", "nt:folder");
    
    session.save();
    
    return webcontent;
  }

}
