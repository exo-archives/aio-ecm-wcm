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
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.ext.classify.NodeClassifyPlugin;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Apr 9, 2008  
 */
public class DateTimeClassifyPlugin extends NodeClassifyPlugin {

  public void classifyChildrenNode(Node parent) throws Exception {
    Session session = parent.getSession();
    NodeIterator nodeIterator = parent.getNodes();
    ArrayList<Integer> years = new ArrayList<Integer>();    
    while(nodeIterator.hasNext()){      
      Node child = nodeIterator.nextNode();
      String srcPath = child.getPath();
      Calendar calendar = child.getProperty("exo:dateCreated").getDate();
      int _year = calendar.get(Calendar.YEAR);
      int _month = calendar.get(Calendar.MONTH);
      int _week = calendar.get(Calendar.WEEK_OF_MONTH);
           
      if(!years.contains(_year)){
        years.add(_year);
        Node yearNode = parent.addNode(Integer.toString(_year), "nt:unstructured");  
        Node monthNode = yearNode.addNode(Integer.toString(_month), "nt:unstructured");
        Node weekNode = monthNode.addNode(Integer.toString(_week), "nt:unstructured");
        String destPath = weekNode.getPath() + "/" + child.getName();
        session.move(srcPath, destPath);
      }
      else{        
        NodeIterator monthIterator = parent.getNode(Integer.toString(_year)).getNodes();
        try{          
          int num = 0;
          while(monthIterator.hasNext()){
            Node monthNode = monthIterator.nextNode();
            if(Integer.parseInt(monthNode.getName()) != _month){ num ++; }
            else{              
              NodeIterator weekIterator = monthNode.getNodes();              
              try{
                int numWeek = 0;
                while(weekIterator.hasNext()){
                  Node weekNode = weekIterator.nextNode();
                  if(Integer.parseInt(weekNode.getName()) != _week){ numWeek ++; }
                  else{
                    String destPath = monthNode.getNode(Integer.toString(_week)).getPath() + "/" + child.getName();
                    session.move(srcPath, destPath);
                    break;
                  }
                }
                if(numWeek == weekIterator.getSize()){
                  Node weekNode = monthNode.addNode(Integer.toString(_week), "nt:unstructured");
                  String destPath = weekNode.getPath() + "/" + child.getName();
                  session.move(srcPath, destPath);
                }
              }catch(PathNotFoundException ex){}
              break;
            }
          }
          if(num == monthIterator.getSize()){
            Node monthNode = parent.getNode(Integer.toString(_year)).addNode(Integer.toString(_month), "nt:unstructured");
            Node weekNode = monthNode.addNode(Integer.toString(_week));
            String destPath = weekNode.getPath() + "/" + child.getName();
            session.move(srcPath, destPath);
          }          
        }catch(PathNotFoundException ex){}                
      }
    }    
    session.save();    
  }
  
}
