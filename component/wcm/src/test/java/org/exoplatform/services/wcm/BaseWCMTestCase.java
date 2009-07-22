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
import org.exoplatform.services.wcm.core.NodetypeConstant;
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
    boolean isInPortal = false;
    if (parentNode.getPath().indexOf("/sites content/live") >= 0) isInPortal = true;
    
    Node webcontent = parentNode.addNode(nodeName, NodetypeConstant.EXO_WEBCONTENT);
    webcontent.setProperty(NodetypeConstant.EXO_TITLE, nodeName);

    Node htmlNode = webcontent.addNode("default.html", NodetypeConstant.NT_FILE);
    htmlNode.addMixin(NodetypeConstant.EXO_HTML_FILE);
    Node htmlContent = htmlNode.addNode(NodetypeConstant.JCR_CONTENT, NodetypeConstant.NT_RESOURCE);
    htmlContent.setProperty(NodetypeConstant.JCR_ENCODING, "UTF-8");
    htmlContent.setProperty(NodetypeConstant.JCR_MIME_TYPE, "text/html");
    htmlContent.setProperty(NodetypeConstant.JCR_LAST_MODIFIED, new Date().getTime());
    if (htmlData == null) htmlData = "This is the default.html file.";
    htmlContent.setProperty(NodetypeConstant.JCR_DATA, htmlData);
    
    if (jsData == null) jsData = "This is the default.js file.";
    if (cssData == null) cssData = "This is the default.css file.";
    
    Node jsFolder = null;
    Node cssFolder = null;
    if (isInPortal) {
      jsFolder = webcontent.getNode("js");
      cssFolder = webcontent.getNode("css");
    } else {
      jsFolder = webcontent.addNode("js", NodetypeConstant.EXO_JS_FOLDER);
      cssFolder = webcontent.addNode("css", NodetypeConstant.EXO_CSS_FOLDER);
      Node mediaFolder = webcontent.addNode("medias", NodetypeConstant.EXO_MULTIMEDIA_FOLDER);
      mediaFolder.addNode("images", NodetypeConstant.NT_FOLDER).addMixin(NodetypeConstant.EXO_PICTURE_FOLDER);
      mediaFolder.addNode("videos", NodetypeConstant.NT_FOLDER).addMixin(NodetypeConstant.EXO_VIDEO_FOLDER);
      mediaFolder.addNode("audio", NodetypeConstant.NT_FOLDER).addMixin(NodetypeConstant.EXO_MUSIC_FOLDER);
      webcontent.addNode("documents", NodetypeConstant.NT_UNSTRUCTURED).addMixin(NodetypeConstant.EXO_DOCUMENT_FOLDER);
    }
    
    Node jsNode = jsFolder.addNode("default.js", NodetypeConstant.NT_FILE);
    jsNode.setProperty(NodetypeConstant.EXO_ACTIVE, true);
    jsNode.setProperty(NodetypeConstant.EXO_PRIORITY, 1);
    jsNode.setProperty(NodetypeConstant.EXO_SHARED_JS, true);
    Node jsContent = jsNode.addNode(NodetypeConstant.JCR_CONTENT, NodetypeConstant.NT_RESOURCE);
    jsContent.setProperty(NodetypeConstant.JCR_ENCODING, "UTF-8");
    jsContent.setProperty(NodetypeConstant.JCR_MIME_TYPE, "text/javascript");
    jsContent.setProperty(NodetypeConstant.JCR_LAST_MODIFIED, new Date().getTime());
    jsContent.setProperty(NodetypeConstant.JCR_DATA, jsData);
    
    Node cssNode = cssFolder.addNode("default.css", NodetypeConstant.NT_FILE);
    cssNode.setProperty(NodetypeConstant.EXO_ACTIVE, true);
    cssNode.setProperty(NodetypeConstant.EXO_PRIORITY, 1);
    cssNode.setProperty(NodetypeConstant.EXO_SHARED_CSS, true);
    Node cssContent = cssNode.addNode(NodetypeConstant.JCR_CONTENT, NodetypeConstant.NT_RESOURCE);
    cssContent.setProperty(NodetypeConstant.JCR_ENCODING, "UTF-8");
    cssContent.setProperty(NodetypeConstant.JCR_MIME_TYPE, "text/css");
    cssContent.setProperty(NodetypeConstant.JCR_LAST_MODIFIED, new Date().getTime());
    cssContent.setProperty(NodetypeConstant.JCR_DATA, cssData);
    
    session.save();
    
    return webcontent;
  }
  
}
