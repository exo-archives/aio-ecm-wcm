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
package org.exoplatform.services.wcm.publication.defaultlifecycle;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Oct 2, 2008  
 */
public class Util {
  
  public static List<PageNode> findPageNodeByPageId(PageNavigation nav, String pageId) throws Exception {
    List<PageNode> list = new ArrayList<PageNode>();
    for (PageNode node : nav.getNodes()) {
      findPageNodeByPageId(node, pageId, list);
    }
    return list;
  }

  public static void findPageNodeByPageId(PageNode node, String pageId, List<PageNode> allPageNode)
      throws Exception {
    if (pageId.equals(node.getPageReference())) {
      allPageNode.add(node.clone());
    }
    List<PageNode> children = node.getChildren();
    if (children == null)
      return;
    for (PageNode child : children) {
      findPageNodeByPageId(child, pageId, allPageNode);
    }
  }
   
  public static List<String> findAppInstancesByName(Page page, String applicationName) {
    List<String> results = new ArrayList<String>();
    findAppInstancesByContainerAndName(page, applicationName, results);
    return results;
  }
  
  private static void findAppInstancesByContainerAndName(Container container, String applicationName, List<String> results) {
    ArrayList<Object> chidren = container.getChildren();
    if(chidren == null) return ;
    for(Object object: chidren) {
      if(object instanceof Application) {
        Application application = Application.class.cast(object);
        if(application.getInstanceId().contains(applicationName)) {
          results.add(application.getInstanceId());
        }
      }else if(object instanceof Container) {
        Container child = Container.class.cast(object);
        findAppInstancesByContainerAndName(child, applicationName, results);
      }
    }
  }
  
  public static List<String> getValuesAsString(Node node, String propName) throws Exception {
    if(!node.hasProperty(propName)) return new ArrayList<String>();
    List<String> results = new ArrayList<String>();
    for(Value value: node.getProperty(propName).getValues()) {
      results.add(value.getString());
    }
    return results;
  }
  
  public static Value[] toValues(ValueFactory factory, List<String> values) {
    List<Value> list = new ArrayList<Value>();
    for(String value: values) {
      list.add(factory.createValue(value));
    }
    return list.toArray(new Value[list.size()]);
  }
} 
