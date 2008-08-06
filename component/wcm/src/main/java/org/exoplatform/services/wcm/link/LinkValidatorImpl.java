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
package org.exoplatform.services.wcm.link;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 *          chuong_phan@exoplatform.com
 * Aug 4, 2008  
 */
public class LinkValidatorImpl implements LinkValidator  {

  private RepositoryService repositoryService;
  
  public void validate() throws Exception {
    Session session = null ;
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    ManageableRepository repository = repositoryService.getDefaultRepository();
    String[] workspaces = repository.getWorkspaceNames();      
    for (int intWorkspaces = 0; intWorkspaces < workspaces.length; intWorkspaces++) {
      String workspaceName = workspaces[intWorkspaces];
      session = repository.getSystemSession(workspaceName);
      ValueFactory valueFactory = session.getValueFactory();
      QueryManager queryManager = session.getWorkspace().getQueryManager();
      Query query = queryManager.createQuery("select * from exo:linkable", Query.SQL);
      QueryResult results = query.execute();
      NodeIterator iter = results.getNodes();
      for (;iter.hasNext();){
        Node node = iter.nextNode();
        Property links = node.getProperty("exo:links");
        Value[] oldValues = links.getValues();
        Value[] newValues = new Value[oldValues.length]; 
        for (int iValues = 0; iValues < oldValues.length; iValues++) {
          String strUrl = oldValues[iValues].getString().split(LinkBean.SEPARATOR)[1].replaceAll(LinkBean.URL, "");
          String strStatus = getLinkStatus(strUrl);
          newValues[iValues] = valueFactory.createValue(updateLinkStatus(strUrl, strStatus));
        }
        links.setValue(newValues);
      }
      session.save();
    }
  }
  
  public String getLinkStatus (String strUrl){
    try {
      HttpClient httpClient = new HttpClient(new SimpleHttpConnectionManager());
      PostMethod postMethod = new PostMethod(strUrl);
      if (httpClient.executeMethod(postMethod) == 200) {
        return LinkBean.STATUS_ACTIVE;
      }
      else { 
        return LinkBean.STATUS_BROKEN;
      }
    } catch(Exception e) {
      return LinkBean.STATUS_BROKEN;
    }
  }

  public String updateLinkStatus(String strUrl, String strStatus) throws Exception {
    return LinkBean.STATUS + strStatus + LinkBean.SEPARATOR + LinkBean.URL + strUrl;
  }

}
