/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.wcm.htmlextractor;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.services.html.HTMLDocument;
import org.exoplatform.services.html.parser.HTMLParser;
import org.exoplatform.services.wcm.BaseTestCase;
import org.exoplatform.services.wcm.webcontent.TOCGeneratorService;
import org.exoplatform.services.wcm.webcontent.TOCGeneratorService.Heading;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * Jul 29, 2008  
 */
public class TestTOCGeneratorService extends BaseTestCase {
  public static final String content1 = "<html>" +
  "<head>" +
  "<title>My own HTML file</title>" +
  "</head>" +
  "<body>" +
  "<h1>the first h1 tag</h1>" +
  "<h2>the first h2 tag</h2>" +
  "<h2>the second h2 tag</h2>" +
  "<h1>the second h1 tag</h1>" +
  "<h2>the third second h2 tag</h2>" +
  "<h3>the first h3 tag</h3>" +
  "</body>"+
  "</html>";

  public static final String content2 = "<html>" +
  "<head>" +
  "<title>My own HTML file</title>" +
  "</head>" +
  "<body>" +
  "<h2>the first h1 tag</h2>" +
  "<h3>the first h2 tag</h3>" +
  "<h4>the second h2 tag</h4>" +
  "<h1>the second h1 tag </h1>" +
  "<h2>the third second h2 tag</h2>" +
  "<h3>the first h3 tag</h3>" +
  "</body>"+
  "</html>";

  public static final String content3 = "<html>" +
  "<head>" +
  "<title>My own HTML file</title>" +
  "</head>" +
  "<body>" +
  "<h2>Today news</h2>" +
  "<h3>sports</h3>" +
  "<h4>working</h4>" +
  "<h1>the second h1 tag </h1>" +
  "<h2>Game</h2>" +
  "<h3>Information</h3>" +
  "</body>"+
  "</html>";

  public static final String content4 = "<html>" +
  "<head>" +
  "<title>My own HTML file</title>" +
  "</head>" +
  "<body>" +
  "<a>dsfsdfsdf</a>" +
  "</body>" +
  "</html>";

  public void testGenerateTOC() throws Exception {
    TOCGeneratorService tocService = (TOCGeneratorService) container.getComponentInstanceOfType(TOCGeneratorService.class);
    Session session = repositoryService.getRepository("repository").getSystemSession("collaboration");
    Map<String,String> fileMap = new HashMap<String,String>();
    fileMap.put("htmlOne", content1);
    fileMap.put("htmlTwo", content2);
    fileMap.put("htmlThree", content3);
    fileMap.put("htmlFour", content4);
    
    
    Node myWebContent = createWebContentNodeToTest(session, "myWebContent", fileMap);
    HTMLDocument document = HTMLParser.createDocument(content1);
    Node htmlOne = myWebContent.getNode("htmlOne");
    List<Heading> headings = tocService.extractHeadings(document);
    tocService.updateTOC(htmlOne,headings);
    session.save();
    //tocService.generateTOC(myWebContent);

    Value[] values = htmlOne.getProperty("exo:htmlTOC").getValues();
    assertEquals(6, values.length);
    String tag1 = "tagName=<h1>the first h1 tag</h1>|headingLevel=1|headingNumberText=1";
    String tag2 = "tagName=<h2>the first h2 tag</h2>|headingLevel=2|headingNumberText=1.1";
    String tag3 = "tagName=<h2>the second h2 tag</h2>|headingLevel=2|headingNumberText=1.2";
    String tag4 = "tagName=<h1>the second h1 tag</h1>|headingLevel=1|headingNumberText=2";
    String tag5 = "tagName=<h2>the third second h2 tag</h2>|headingLevel=2|headingNumberText=2.1";
    String tag6 = "tagName=<h3>the first h3 tag</h3>|headingLevel=3|headingNumberText=2.1.1";
    assertEquals(tag1, values[0].getString().trim());
    assertEquals(tag2, values[1].getString().trim());
    assertEquals(tag3, values[2].getString().trim());
    assertEquals(tag4, values[3].getString().trim());
    assertEquals(tag5, values[4].getString().trim());
    assertEquals(tag6, values[5].getString().trim());
    String contentList = tocService.getTOC(myWebContent.getNode("htmlOne"));
    assertTrue(contentList != null);  
    String htmlFourList = tocService.getTOC(myWebContent.getNode("htmlFour"));
    assertEquals(null, htmlFourList);
    myWebContent.remove();
    session.save();
  }

  private Node createWebContentNodeToTest(Session session, String nodeName, Map<String,String> fMap) throws Exception {
    Node rootNode = session.getRootNode();
    Node testNode = rootNode.addNode("test");
    Node webContentNode = rootNode.addNode(nodeName, "exo:webContent");
    Set<String> keySet = fMap.keySet();
    for(String key: keySet) {
      createHtmlNodeToTest(webContentNode, key, "nt:file", fMap.get(key));
    }
    session.save();

    return webContentNode;
  }

  private Node createHtmlNodeToTest(Node parentNode, String nodeName, String nodeType, String content) throws Exception {
    Node htmlNode = parentNode.addNode(nodeName, nodeType);
    htmlNode.addMixin("exo:htmlFile");
    Node jcrContent = htmlNode.addNode("jcr:content", "nt:resource");
    jcrContent.setProperty("jcr:encoding", "UTF-8");
    Calendar cal = Calendar.getInstance();
    jcrContent.setProperty("jcr:lastModified", cal);
    jcrContent.setProperty("jcr:mimeType", "text/html");
    jcrContent.setProperty("jcr:data", content);

    return htmlNode;
  }
}