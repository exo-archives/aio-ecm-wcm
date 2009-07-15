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
package org.exoplatform.services.wcm.newsletter.handler;

import javax.jcr.Node;

import org.exoplatform.services.wcm.BaseWCMTestCase;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform
 * chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com Jul 14, 2009
 */
public class TestNewsletterCategoryHandler extends BaseWCMTestCase {

  public void testAddNewsletter() throws Exception {
    Node node = createWebcontentNode(session.getRootNode(), "node1");
    String path = node.getPath();
    session.save();

    Node test = (Node) session.getItem(path);
    assertEquals("node1", test.getName());
  }

}
