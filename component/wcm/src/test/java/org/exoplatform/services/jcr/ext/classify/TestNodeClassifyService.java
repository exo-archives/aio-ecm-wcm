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
package org.exoplatform.services.jcr.ext.classify;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.ext.classify.impl.AlphabetClassifyPlugin;
import org.exoplatform.services.jcr.ext.classify.impl.DateTimeClassifyPlugin;
import org.exoplatform.services.jcr.ext.classify.impl.TypeClassifyPlugin;
import org.exoplatform.services.wcm.BaseTestCase;

/**
 * Created by The eXo Platform SAS
 * Author : Anh Do Ngoc
 *          anh.do@exoplatform.com
 * Jun 2, 2008  
 */
public class TestNodeClassifyService extends BaseTestCase {
  
  public void testClassifyPluginManager() throws Exception {
    NodeClassifyService classifyService = 
      (NodeClassifyService)container.getComponentInstanceOfType(NodeClassifyService.class) ;
    String strAlphabetClassify = "org.exoplatform.services.jcr.ext.classify.impl.AlphabetClassifyPlugin";
    NodeClassifyPlugin classifyPlugin = classifyService.getNodeClassifyPlugin(strAlphabetClassify);
    assertNotNull(classifyPlugin);
  }
  
  public void testAlphabetClassify() throws Exception {    
    NodeClassifyService classifyService = 
      (NodeClassifyService)container.getComponentInstanceOfType(NodeClassifyService.class) ;
    AlphabetClassifyPlugin alphabetClassifyPlugin = 
      (AlphabetClassifyPlugin)classifyService.getNodeClassifyPlugin(AlphabetClassifyPlugin.class.getName()) ;
    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);    
    Node root = session.getRootNode();    
    Node test = root.addNode("test", "nt:unstructured");
    test.addNode("ebook", "nt:unstructured");
    test.addNode("economy", "nt:unstructured");
    test.addNode("emule", "nt:unstructured");
    test.addNode("document", "nt:unstructured");
    test.addNode("dot", "nt:unstructured");
    test.addNode("temp", "nt:unstructured");   
    session.save();
    
    assertEquals(6, test.getNodes().getSize());
    
    alphabetClassifyPlugin.classifyChildrenNode(test) ;    
      
    try{
//    -> classified nodes: E_Node, D_Node and T_Node (sub nodes of test node)      
      Node E_node = test.getNode("E_Node");
      Node D_node = test.getNode("D_Node");
      Node T_node = test.getNode("T_Node");

      assertEquals(3, test.getNodes().getSize());
      assertNotNull(E_node);
      assertNotNull(D_node);
      assertNotNull(T_node);
      
//    sub nodes of E_node: ebook, emule, economy  
      assertEquals(3, E_node.getNodes().getSize());
      assertNotNull(E_node.getNode("ebook"));
      assertNotNull(E_node.getNode("emule"));
      assertNotNull(E_node.getNode("economy"));
      
//    sub nodes of D_node: document, dot
      assertEquals(2, D_node.getNodes().getSize());
      assertNotNull(D_node.getNode("document"));
      assertNotNull(D_node.getNode("dot"));
      
//    sub nodes of T_node: temp      
      assertEquals(1, T_node.getNodes().getSize());
      assertNotNull(T_node.getNode("temp"));             
    }catch(PathNotFoundException ex){}    
    
    test.remove();
    session.save();
  }
  
  public void testDateTimeClassify() throws Exception{
    NodeClassifyService classifyService = 
      (NodeClassifyService)container.getComponentInstanceOfType(NodeClassifyService.class) ;
    DateTimeClassifyPlugin dateClassifyPlugin = 
      (DateTimeClassifyPlugin)classifyService.getNodeClassifyPlugin(DateTimeClassifyPlugin.class.getName()) ;
    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);    
    Node root = session.getRootNode();    
    Node test = root.addNode("test", "nt:unstructured");
        
    Calendar c1 = new GregorianCalendar();
    c1.set(Calendar.YEAR, 1986);
    c1.set(Calendar.MONTH, 3);
    c1.set(Calendar.DATE, 21);
    
    Calendar c2 = new GregorianCalendar();
    c2.set(Calendar.YEAR, 1986);
    c2.set(Calendar.MONTH, 3);
    c2.set(Calendar.DATE, 23);

    Calendar c3 = new GregorianCalendar();
    c3.set(Calendar.YEAR, 1986);
    c3.set(Calendar.MONTH, 3);
    c3.set(Calendar.DATE, 2);
    
    Calendar c4 = new GregorianCalendar();
    c4.set(Calendar.YEAR, 1986);
    c4.set(Calendar.MONTH, 8);
    c4.set(Calendar.DATE, 10);    
    
    Calendar c5 = new GregorianCalendar();
    c5.set(Calendar.YEAR, 1987);
    c5.set(Calendar.MONTH, 11);
    c5.set(Calendar.DATE, 18);
    
    Node node1 = test.addNode("bird", "nt:unstructured");
    Node node2 = test.addNode("dog", "nt:unstructured");
    Node node3 = test.addNode("cat", "nt:unstructured");
    Node node4 = test.addNode("fish", "nt:unstructured");
    Node node5 = test.addNode("snack", "nt:unstructured");
    
    node1.setProperty("exo:dateCreated", c1);
    node2.setProperty("exo:dateCreated", c2);
    node3.setProperty("exo:dateCreated", c3);
    node4.setProperty("exo:dateCreated", c4);
    node5.setProperty("exo:dateCreated", c5);
    
    session.save();   
    
    dateClassifyPlugin.classifyChildrenNode(test);    
    
    try{
      //2 sub nodes of test: 1986 and 1987      
      Node n_1986 = test.getNode("1986");
      Node n_1987 = test.getNode("1987");
      
      assertEquals(2, test.getNodes().getSize());
      assertNotNull(n_1986);
      assertNotNull(n_1987);
  
      //2 sub nodes of n_1986: 3 and 8
      Node n_1986_3 = n_1986.getNode("3");
      Node n_1986_8 = n_1986.getNode("8");
      
      assertEquals(2, n_1986.getNodes().getSize());
      assertNotNull(n_1986_3);
      assertNotNull(n_1986_8);
      
      //2 sub nodes of n_1986_3: 1 and 4
      Node n_1986_3_1 = n_1986_3.getNode("1");
      Node n_1986_3_4 = n_1986_3.getNode("4");
      
      assertEquals(2, n_1986_3.getNodes().getSize());
      assertNotNull(n_1986_3_1);
      assertNotNull(n_1986_3_4);
      
      //2 sub nodes of n_1986_3_4: bird and dog
      Node n_1986_3_4_bird = n_1986_3_4.getNode("bird");
      Node n_1986_3_4_dog = n_1986_3_4.getNode("dog");
      
      assertEquals(2, n_1986_3_4.getNodes().getSize());
      assertNotNull(n_1986_3_4_bird);
      assertNotNull(n_1986_3_4_dog);
      
      //1 sub node of n_1986_3_1: cat
      Node n_1986_3_1_cat = n_1986_3_1.getNode("cat");
      
      assertEquals(1, n_1986_3_1.getNodes().getSize());
      assertNotNull(n_1986_3_1_cat);
      
      //1 sub node of n_1986_8: 2
      Node n_1986_8_2 = n_1986_8.getNode("2");
      
      assertEquals(1, n_1986_8.getNodes().getSize());
      assertNotNull(n_1986_8_2);
      
      //1 sub node of n_1986_8_2: fish
      Node n_1986_8_2_fish = n_1986_8_2.getNode("fish");
      
      assertEquals(1, n_1986_8_2.getNodes().getSize());
      assertNotNull(n_1986_8_2_fish);
      
      //1 sub node of n_1987: 11
      Node n_1987_11 = n_1987.getNode("11");
      
      assertEquals(1, n_1987.getNodes().getSize());
      assertNotNull(n_1987_11);      
      
      //1 sub node of n_1987_11: 3
      Node n_1987_11_3 = n_1987_11.getNode("3");
      
      assertEquals(1, n_1987_11.getNodes().getSize());
      assertNotNull(n_1987_11_3);
      
      //1 sub node of n_1987_11_3: snack
      Node n_1987_11_3_snack = n_1987_11_3.getNode("snack");
      
      assertEquals(1, n_1987_11_3.getNodes().getSize());
      assertNotNull(n_1987_11_3_snack);
    }catch(PathNotFoundException ex){}    
     
    test.remove();
    session.save();    
  }
  
  public void testTypeClassify() throws Exception{    
    NodeClassifyService classifyService = 
      (NodeClassifyService)container.getComponentInstanceOfType(NodeClassifyService.class) ;
    TypeClassifyPlugin typeClassifyPlugin = 
      (TypeClassifyPlugin)classifyService.getNodeClassifyPlugin(TypeClassifyPlugin.class.getName()) ;
    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);    
    Node root = session.getRootNode();    
    Node test = root.addNode("test", "nt:unstructured");
    test.addNode("chicken", "nt:folder");
    test.addNode("dog", "nt:folder");
    test.addNode("bird", "nt:folder");
    test.addNode("ball", "nt:unstructured");
    test.addNode("hat", "nt:unstructured");
    session.save();
    
    assertEquals(5, test.getNodes().getSize());
    
    typeClassifyPlugin.classifyChildrenNode(test);
    
    try{
//    -> classified nodes: nt:folder_Nodes, nt:unstructured_Nodes (sub nodes of test node)
      Node folder_node = test.getNode("nt:folder_Nodes");
      Node unstructured_node = test.getNode("nt:unstructured_Nodes");

      assertEquals(2, test.getNodes().getSize());
      assertNotNull(folder_node);
      assertNotNull(unstructured_node );
      
//    sub nodes of nt:folder_Nodes : chicken, dog, bird
      assertEquals(3, folder_node.getNodes().getSize());
      assertNotNull(folder_node.getNode("chicken"));
      assertNotNull(folder_node.getNode("dog"));
      assertNotNull(folder_node.getNode("bird"));
      
//    sub nodes of nt:unstructured_Nodes: ball, hat
      assertEquals(2, unstructured_node.getNodes().getSize());
      assertNotNull(unstructured_node.getNode("ball"));
      assertNotNull(unstructured_node.getNode("hat"));
                  
    }catch(PathNotFoundException ex){} 
  }
  
}
