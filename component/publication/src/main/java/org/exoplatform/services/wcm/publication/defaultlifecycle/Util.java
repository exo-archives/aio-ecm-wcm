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

import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Oct 2, 2008  
 */
public class Util {
  
  public static List<PageNode> findPageNodeByPageReference(PageNavigation nav, String pageReferencedId) throws Exception {
    List<PageNode> list = new ArrayList<PageNode>();
    for (PageNode node : nav.getNodes()) {
      findPageNodeByPageReference(node, pageReferencedId, list);
    }
    return list;
  }

  public static void findPageNodeByPageReference(PageNode node, String pageReferencedId, List<PageNode> allPageNode)
      throws Exception {
    if (pageReferencedId.equals(node.getPageReference())) {
      allPageNode.add(node.clone());
    }
    List<PageNode> children = node.getChildren();
    if (children == null)
      return;
    for (PageNode child : children) {
      findPageNodeByPageReference(child, pageReferencedId, allPageNode);
    }
  }
  
}
