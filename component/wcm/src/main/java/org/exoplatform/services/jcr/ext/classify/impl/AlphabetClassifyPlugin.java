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
package org.exoplatform.services.jcr.ext.classify.impl;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.exoplatform.services.jcr.ext.classify.NodeClassifyPlugin;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Apr 9, 2008  
 */
public class AlphabetClassifyPlugin extends NodeClassifyPlugin {  
  public void classifyChildrenNode(Node parent) throws Exception {    
    Session session = parent.getSession();
    NodeIterator nodeIterator = parent.getNodes();        
    ArrayList<Character> classifiedNodes = new ArrayList<Character>();
    while(nodeIterator.hasNext()){
      Node child = nodeIterator.nextNode();
      char firstCharacter = child.getName().charAt(0);
      int num = 0;
      for(char classifiedChar: classifiedNodes ){        
        if(firstCharacter != classifiedChar){ num ++; }
        else{
          String srcPath = child.getPath();
          String destPath = parent.getNode(Character.toUpperCase(firstCharacter) + "_Node").getPath()+ "/"+  child.getName();
          session.move(srcPath, destPath);          
          break;
        }
      }      
      if(num == classifiedNodes.size()){
        classifiedNodes.add(firstCharacter);
        Node newClassifiedNode = parent.addNode(Character.toUpperCase(firstCharacter)+ "_Node", "nt:unstructured");         
        String srcPath = child.getPath();
        String destPath = newClassifiedNode.getPath()+ "/"+  child.getName();
        session.move(srcPath, destPath);        
      }                      
    }
    session.save();
  }
}
