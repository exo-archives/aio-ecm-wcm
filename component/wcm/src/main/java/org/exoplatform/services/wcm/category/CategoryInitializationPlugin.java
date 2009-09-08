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
package org.exoplatform.services.wcm.category;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.cms.actions.impl.ActionConfig;
import org.exoplatform.services.cms.actions.impl.ActionConfig.TaxonomyAction;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.cms.impl.DMSRepositoryConfiguration;
import org.exoplatform.services.cms.impl.Utils;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.cms.taxonomy.impl.TaxonomyAlreadyExistsException;
import org.exoplatform.services.cms.taxonomy.impl.TaxonomyConfig;
import org.exoplatform.services.cms.taxonomy.impl.TaxonomyPlugin;
import org.exoplatform.services.cms.taxonomy.impl.TaxonomyConfig.Taxonomy;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 21, 2009  
 */
public class CategoryInitializationPlugin extends TaxonomyPlugin {

  private String treeName = "";
  
  private boolean autoCreateInNewRepository = true;
  
  private RepositoryService repositoryService;
  
  private TaxonomyService taxonomyService;

  private ActionServiceContainer actionServiceContainer;
  
  private DMSConfiguration dmsConfiguration;
  
  private InitParams params;
  
  public CategoryInitializationPlugin(InitParams params, 
                                      RepositoryService repositoryService,
                                      NodeHierarchyCreator nodeHierarchyCreator, 
                                      TaxonomyService taxonomyService,
                                      ActionServiceContainer actionServiceContainer, 
                                      DMSConfiguration dmsConfiguration) throws Exception {
    super(params, repositoryService, nodeHierarchyCreator, taxonomyService, actionServiceContainer, dmsConfiguration);
    this.repositoryService = repositoryService;
    this.taxonomyService = taxonomyService;
    this.actionServiceContainer = actionServiceContainer;
    this.dmsConfiguration = dmsConfiguration;
    this.params = params;
    ValueParam autoCreated = params.getValueParam("autoCreateInNewRepository");
    ValueParam workspaceParam = params.getValueParam("workspace");
    ValueParam pathParam = params.getValueParam("path");
    ValueParam nameParam = params.getValueParam("treeName");
    if (autoCreated != null)
      this.autoCreateInNewRepository = Boolean.parseBoolean(autoCreated.getValue());
    if (pathParam == null || workspaceParam == null || workspaceParam.getValue().trim().length() == 0) {
      setPath(nodeHierarchyCreator.getJcrPath(BasePath.TAXONOMIES_TREE_STORAGE_PATH));
    } else {
      setPath(pathParam.getValue());
      setWorkspace(workspaceParam.getValue());
    }
    if (nameParam != null) {
      this.treeName = nameParam.getValue();
    }
  }

  public void init() throws Exception {
    if (this.autoCreateInNewRepository) {
      for (RepositoryEntry repositoryEntry : this.repositoryService.getConfig()
          .getRepositoryConfigurations()) {
        importPredefineTaxonomies(repositoryEntry.getName());
      }
      return;
    }
    ValueParam param = this.params.getValueParam("repository");
    String repository = null;
    if (param == null) {
      repository = this.repositoryService.getDefaultRepository().getConfiguration().getName();
    } else {
      repository = param.getValue();
    }
    importPredefineTaxonomies(repository);
  }

  @SuppressWarnings("unchecked")
  private void importPredefineTaxonomies(String repository) throws Exception {
    ManageableRepository manageableRepository = this.repositoryService.getRepository(repository);
    DMSRepositoryConfiguration dmsRepoConfig = this.dmsConfiguration.getConfig(repository);
    if ("".equals(getWorkspace())) {
      setWorkspace(dmsRepoConfig.getSystemWorkspace());
    }
    Session session = manageableRepository.getSystemSession(getWorkspace());
    Node taxonomyStorageNode = (Node) session.getItem(getPath());
    Node taxonomyStorageNodeSystem = null;
    if (taxonomyStorageNode.hasProperty("exo:isImportedChildren")) {
      session.logout();
      return;
    }
    taxonomyStorageNode.setProperty("exo:isImportedChildren", true);
    Iterator<ObjectParameter> it = this.params.getObjectParamIterator();
    while (it.hasNext()) {
      ObjectParameter objectParam = it.next();
      if (objectParam.getName().equals("permission.configuration")) {
        taxonomyStorageNodeSystem = Utils.makePath(taxonomyStorageNode, this.treeName, "exo:taxonomy",
            null);
        session.save();
        TaxonomyConfig config = (TaxonomyConfig) objectParam.getObject();
        for (Taxonomy taxonomy : config.getTaxonomies()) {
          Map mapPermissions = getPermissions(taxonomy.getPermissions());
          if (mapPermissions != null) {
            ((ExtendedNode) taxonomyStorageNodeSystem).setPermissions(mapPermissions);
          }
          if (taxonomyStorageNodeSystem.canAddMixin("mix:referenceable")) {
            taxonomyStorageNodeSystem.addMixin("mix:referenceable");
          }
        }
      } else if (objectParam.getName().equals("taxonomy.configuration")) {
        TaxonomyConfig config = (TaxonomyConfig) objectParam.getObject();
        for (Taxonomy taxonomy : config.getTaxonomies()) {
          Node taxonomyNode = Utils.makePath(taxonomyStorageNodeSystem, taxonomy.getPath(),
              "exo:taxonomy", getPermissions(taxonomy.getPermissions()));
          if (taxonomyNode.canAddMixin("mix:referenceable")) {
            taxonomyNode.addMixin("mix:referenceable");
          }
          taxonomyNode.getSession().save();
        }
      } else if (objectParam.getName().equals("predefined.actions")) {
        taxonomyStorageNodeSystem = Utils.makePath(taxonomyStorageNode, this.treeName, "exo:taxonomy",
            null);
        session.save();
        ActionConfig config = (ActionConfig) objectParam.getObject();
        List actions = config.getActions();
        for (Iterator iter = actions.iterator(); iter.hasNext();) {
          TaxonomyAction action = (TaxonomyAction) iter.next();
          addAction(action, taxonomyStorageNodeSystem, repository);
        }
      }

    }
    taxonomyStorageNode.save();
    try {
      this.taxonomyService.addTaxonomyTree(taxonomyStorageNodeSystem);
    } catch (TaxonomyAlreadyExistsException e) {}
    session.save();
    session.logout();
  }
  
  @SuppressWarnings("unchecked")
  private void addAction(ActionConfig.TaxonomyAction action, Node srcNode, String repository) throws Exception {
    Map<String, JcrInputProperty> sortedInputs = new HashMap<String, JcrInputProperty>();
    JcrInputProperty jcrInputName = new JcrInputProperty();
    jcrInputName.setJcrPath("/node/exo:name");
    jcrInputName.setValue(action.getName());
    sortedInputs.put("/node/exo:name", jcrInputName);
    
    JcrInputProperty jcrInputDes = new JcrInputProperty();
    jcrInputDes.setJcrPath("/node/exo:description");
    jcrInputDes.setValue(action.getDescription());
    sortedInputs.put("/node/exo:description", jcrInputDes);
    
    JcrInputProperty jcrInputLife = new JcrInputProperty();
    jcrInputLife.setJcrPath("/node/exo:lifecyclePhase");
    jcrInputLife.setValue(action.getLifecyclePhase());
    sortedInputs.put("/node/exo:lifecyclePhase", jcrInputLife);
    
    JcrInputProperty jcrInputHomePath = new JcrInputProperty();
    jcrInputHomePath.setJcrPath("/node/exo:storeHomePath");
    jcrInputHomePath.setValue(action.getHomePath());
    sortedInputs.put("/node/exo:storeHomePath", jcrInputHomePath);
    
    JcrInputProperty jcrInputTargetWspace = new JcrInputProperty();
    jcrInputTargetWspace.setJcrPath("/node/exo:targetWorkspace");
    jcrInputTargetWspace.setValue(action.getTargetWspace());
    sortedInputs.put("/node/exo:targetWorkspace", jcrInputTargetWspace);
    
    JcrInputProperty jcrInputTargetPath = new JcrInputProperty();
    jcrInputTargetPath.setJcrPath("/node/exo:targetPath");
    jcrInputTargetPath.setValue(action.getTargetPath());
    sortedInputs.put("/node/exo:targetPath", jcrInputTargetPath);
    
    JcrInputProperty rootProp = sortedInputs.get("/node");
    if (rootProp == null) {
      rootProp = new JcrInputProperty();
      rootProp.setJcrPath("/node");
      rootProp.setValue((sortedInputs.get("/node/exo:name")).getValue());
      sortedInputs.put("/node", rootProp);
    } else {
      rootProp.setValue((sortedInputs.get("/node/exo:name")).getValue());
    }
    this.actionServiceContainer.addAction(srcNode, repository, action.getType(), sortedInputs);
    Node actionNode = this.actionServiceContainer.getAction(srcNode, action.getName());
    if (action.getRoles() != null) {
      String[] roles = StringUtils.split(action.getRoles(), ";");
      actionNode.setProperty("exo:roles", roles);
    }
    
    Iterator mixins = action.getMixins().iterator();
    while (mixins.hasNext()) { 
      ActionConfig.Mixin mixin = (ActionConfig.Mixin) mixins.next();
      actionNode.addMixin(mixin.getName());
      Map<String, String> props = mixin.getParsedProperties();
      Set<String> keys = props.keySet();
      for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
        String key = iterator.next();
        actionNode.setProperty(key, props.get(key));
      }
    }
    actionNode.getSession().save();
  }
}
