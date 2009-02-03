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
package org.exoplatform.services.wcm.search;

import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.ItemVisitor;
import javax.jcr.MergeException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Row;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.phamvu@exoplatform.com
 * Feb 2, 2009  
 */

/**
 * The Class ResultNode.
 */
public class ResultNode implements Node{ 

  /** The node. */
  private Node node;

  /** The score. */
  private float score;

  /** The excerpt. */
  private String excerpt;        

  /**
   * Instantiates a new result node.
   * 
   * @param node the node
   * @param row the row
   * 
   * @throws RepositoryException the repository exception
   */
  public ResultNode(Node node, Row row) throws RepositoryException{
    this.node = node;      
    this.excerpt = row.getValue("rep:excerpt(.)").getString();
    this.score = row.getValue("jcr:score").getLong();
  }

  /**
   * Gets the node.
   * 
   * @return the node
   */
  public Node getNode() { return node; }
  /**
   * Sets the node.
   * 
   * @param node the new node
   */
  public void setNode(Node node) { this.node = node; }

  /**
   * Gets the score.
   * 
   * @return the score
   */
  public float getScore() { return score; }

  /**
   * Sets the score.
   * 
   * @param score the new score
   */
  public void setScore(float score) { this.score = score; }

  /**
   * Gets the excerpt.
   * 
   * @return the excerpt
   */
  public String getExcerpt() {
    return excerpt;
  }

  /**
   * Sets the excerpt.
   * 
   * @param excerpt the new excerpt
   */
  public void setExcerpt(String excerpt) {
    this.excerpt = excerpt;
  }

  public String getTitle() throws Exception {
    if(node.hasProperty("exo:title")) {
      return node.getProperty("exo:title").getString();
    }
    return node.getName();
  }

  public String getSummary() throws Exception {
    if(node.hasProperty("exo:summary")) {
      return node.getProperty("exo:summary").getString();
    }
    return null;
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#addMixin(java.lang.String)
   */
  public void addMixin(String name) throws NoSuchNodeTypeException, VersionException,
  ConstraintViolationException, LockException, RepositoryException {
    node.addMixin(name);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#addNode(java.lang.String)
   */
  public Node addNode(String name) throws ItemExistsException, PathNotFoundException,
  VersionException, ConstraintViolationException, LockException, RepositoryException {
    return node.addNode(name);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#addNode(java.lang.String, java.lang.String)
   */
  public Node addNode(String name, String type) throws ItemExistsException,
  PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException,
  ConstraintViolationException, RepositoryException {
    return node.addNode(name,type);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#canAddMixin(java.lang.String)
   */
  public boolean canAddMixin(String name) throws NoSuchNodeTypeException, RepositoryException {
    return node.canAddMixin(name);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#cancelMerge(javax.jcr.version.Version)
   */
  public void cancelMerge(Version version) throws VersionException, InvalidItemStateException,
  UnsupportedRepositoryOperationException, RepositoryException {
    node.cancelMerge(version);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#checkin()
   */
  public Version checkin() throws VersionException, UnsupportedRepositoryOperationException,
  InvalidItemStateException, LockException, RepositoryException {
    return node.checkin();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#checkout()
   */
  public void checkout() throws UnsupportedRepositoryOperationException, LockException,
  RepositoryException {
    node.checkout();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#doneMerge(javax.jcr.version.Version)
   */
  public void doneMerge(Version version) throws VersionException, InvalidItemStateException,
  UnsupportedRepositoryOperationException, RepositoryException {
    node.doneMerge(version);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#getBaseVersion()
   */
  public Version getBaseVersion() throws UnsupportedRepositoryOperationException,
  RepositoryException {
    return node.getBaseVersion();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#getCorrespondingNodePath(java.lang.String)
   */
  public String getCorrespondingNodePath(String nodePath) throws ItemNotFoundException,
  NoSuchWorkspaceException, AccessDeniedException, RepositoryException {
    return node.getCorrespondingNodePath(nodePath);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#getDefinition()
   */
  public NodeDefinition getDefinition() throws RepositoryException {
    return node.getDefinition();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#getIndex()
   */
  public int getIndex() throws RepositoryException {
    return node.getIndex();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#getLock()
   */
  public Lock getLock() throws UnsupportedRepositoryOperationException, LockException,
  AccessDeniedException, RepositoryException {
    return node.getLock();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#getMixinNodeTypes()
   */
  public NodeType[] getMixinNodeTypes() throws RepositoryException {
    return node.getMixinNodeTypes();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#getNode(java.lang.String)
   */
  public Node getNode(String name) throws PathNotFoundException, RepositoryException {
    return node.getNode(name);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#getNodes()
   */
  public NodeIterator getNodes() throws RepositoryException {
    return node.getNodes();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#getNodes(java.lang.String)
   */
  public NodeIterator getNodes(String name) throws RepositoryException {
    return node.getNodes(name);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#getPrimaryItem()
   */
  public Item getPrimaryItem() throws ItemNotFoundException, RepositoryException {
    return node.getPrimaryItem();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#getPrimaryNodeType()
   */
  public NodeType getPrimaryNodeType() throws RepositoryException {
    return node.getPrimaryNodeType();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#getProperties()
   */
  public PropertyIterator getProperties() throws RepositoryException {
    return node.getProperties();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#getProperties(java.lang.String)
   */
  public PropertyIterator getProperties(String name) throws RepositoryException {
    return node.getProperties(name);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#getReferences()
   */
  public PropertyIterator getReferences() throws RepositoryException {
    return node.getReferences();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#getUUID()
   */
  public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {
    return node.getUUID();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#getVersionHistory()
   */
  public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException,
  RepositoryException {
    return node.getVersionHistory();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#hasNode(java.lang.String)
   */
  public boolean hasNode(String name) throws RepositoryException {
    return node.hasNode(name);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#hasNodes()
   */
  public boolean hasNodes() throws RepositoryException {
    return node.hasNodes();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#hasProperties()
   */
  public boolean hasProperties() throws RepositoryException {
    return node.hasProperties();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#hasProperty(java.lang.String)
   */
  public boolean hasProperty(String name) throws RepositoryException {
    return node.hasProperty(name);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#holdsLock()
   */
  public boolean holdsLock() throws RepositoryException {
    return node.holdsLock();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#isCheckedOut()
   */
  public boolean isCheckedOut() throws RepositoryException {
    return node.isCheckedOut();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#isLocked()
   */
  public boolean isLocked() throws RepositoryException {
    return node.isLocked();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#isNodeType(java.lang.String)
   */
  public boolean isNodeType(String type) throws RepositoryException {
    return node.isNodeType(type);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#lock(boolean, boolean)
   */
  public Lock lock(boolean arg0, boolean arg1) throws UnsupportedRepositoryOperationException,
  LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
    return node.lock(arg0, arg1);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#merge(java.lang.String, boolean)
   */
  public NodeIterator merge(String arg0, boolean arg1) throws NoSuchWorkspaceException,
  AccessDeniedException, MergeException, LockException, InvalidItemStateException,
  RepositoryException {
    return node.merge(arg0, arg1);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#orderBefore(java.lang.String, java.lang.String)
   */
  public void orderBefore(String arg0, String arg1)
  throws UnsupportedRepositoryOperationException, VersionException,
  ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException {
    node.orderBefore(arg0, arg1);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#removeMixin(java.lang.String)
   */
  public void removeMixin(String arg0) throws NoSuchNodeTypeException, VersionException,
  ConstraintViolationException, LockException, RepositoryException {
    node.removeMixin(arg0);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#restore(java.lang.String, boolean)
   */
  public void restore(String arg0, boolean arg1) throws VersionException, ItemExistsException,
  UnsupportedRepositoryOperationException, LockException, InvalidItemStateException,
  RepositoryException {
    node.restore(arg0, arg1);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#restore(javax.jcr.version.Version, boolean)
   */
  public void restore(Version arg0, boolean arg1) throws VersionException, ItemExistsException,
  UnsupportedRepositoryOperationException, LockException, RepositoryException {

    node.restore(arg0, arg1) ;
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#restore(javax.jcr.version.Version, java.lang.String, boolean)
   */
  public void restore(Version arg0, String arg1, boolean arg2) throws PathNotFoundException,
  ItemExistsException, VersionException, ConstraintViolationException,
  UnsupportedRepositoryOperationException, LockException, InvalidItemStateException,
  RepositoryException {
    node.restore(arg0, arg1, arg2);

  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#restoreByLabel(java.lang.String, boolean)
   */
  public void restoreByLabel(String arg0, boolean arg1) throws VersionException,
  ItemExistsException, UnsupportedRepositoryOperationException, LockException,
  InvalidItemStateException, RepositoryException {
    node.restoreByLabel(arg0, arg1);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#setProperty(java.lang.String, javax.jcr.Value)
   */
  public Property setProperty(String arg0, Value arg1) throws ValueFormatException,
  VersionException, LockException, ConstraintViolationException, RepositoryException {
    return node.setProperty(arg0, arg1);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#setProperty(java.lang.String, javax.jcr.Value[])
   */
  public Property setProperty(String arg0, Value[] arg1) throws ValueFormatException,
  VersionException, LockException, ConstraintViolationException, RepositoryException {
    return node.setProperty(arg0, arg1);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#setProperty(java.lang.String, java.lang.String[])
   */
  public Property setProperty(String arg0, String[] arg1) throws ValueFormatException,
  VersionException, LockException, ConstraintViolationException, RepositoryException {
    return node.setProperty(arg0, arg1);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#setProperty(java.lang.String, java.lang.String)
   */
  public Property setProperty(String arg0, String arg1) throws ValueFormatException,
  VersionException, LockException, ConstraintViolationException, RepositoryException {
    return node.setProperty(arg0, arg1);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#setProperty(java.lang.String, java.io.InputStream)
   */
  public Property setProperty(String arg0, InputStream arg1) throws ValueFormatException,
  VersionException, LockException, ConstraintViolationException, RepositoryException {
    return node.setProperty(arg0, arg1);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#setProperty(java.lang.String, boolean)
   */
  public Property setProperty(String arg0, boolean arg1) throws ValueFormatException,
  VersionException, LockException, ConstraintViolationException, RepositoryException {
    return node.setProperty(arg0, arg1);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#setProperty(java.lang.String, double)
   */
  public Property setProperty(String arg0, double arg1) throws ValueFormatException,
  VersionException, LockException, ConstraintViolationException, RepositoryException {
    return node.setProperty(arg0, arg1);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#setProperty(java.lang.String, long)
   */
  public Property setProperty(String arg0, long arg1) throws ValueFormatException,
  VersionException, LockException, ConstraintViolationException, RepositoryException {
    return node.setProperty(arg0, arg1);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#setProperty(java.lang.String, java.util.Calendar)
   */
  public Property setProperty(String arg0, Calendar arg1) throws ValueFormatException,
  VersionException, LockException, ConstraintViolationException, RepositoryException {
    return node.setProperty(arg0, arg1);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#setProperty(java.lang.String, javax.jcr.Node)
   */
  public Property setProperty(String arg0, Node arg1) throws ValueFormatException,
  VersionException, LockException, ConstraintViolationException, RepositoryException {
    return node.setProperty(arg0, arg1);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#setProperty(java.lang.String, javax.jcr.Value, int)
   */
  public Property setProperty(String arg0, Value arg1, int arg2) throws ValueFormatException,
  VersionException, LockException, ConstraintViolationException, RepositoryException {
    return node.setProperty(arg0, arg1, arg2);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#setProperty(java.lang.String, javax.jcr.Value[], int)
   */
  public Property setProperty(String arg0, Value[] arg1, int arg2) throws ValueFormatException,
  VersionException, LockException, ConstraintViolationException, RepositoryException {
    return node.setProperty(arg0, arg1, arg2);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#setProperty(java.lang.String, java.lang.String[], int)
   */
  public Property setProperty(String arg0, String[] arg1, int arg2) throws ValueFormatException,
  VersionException, LockException, ConstraintViolationException, RepositoryException {
    return node.setProperty(arg0, arg1, arg2);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#setProperty(java.lang.String, java.lang.String, int)
   */
  public Property setProperty(String arg0, String arg1, int arg2) throws ValueFormatException,
  VersionException, LockException, ConstraintViolationException, RepositoryException {
    return node.setProperty(arg0, arg1, arg2);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#unlock()
   */
  public void unlock() throws UnsupportedRepositoryOperationException, LockException,
  AccessDeniedException, InvalidItemStateException, RepositoryException {
    node.unlock();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#update(java.lang.String)
   */
  public void update(String arg0) throws NoSuchWorkspaceException, AccessDeniedException,
  LockException, InvalidItemStateException, RepositoryException {
    node.update(arg0);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Item#accept(javax.jcr.ItemVisitor)
   */
  public void accept(ItemVisitor arg0) throws RepositoryException {
    node.accept(arg0);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Item#getAncestor(int)
   */
  public Item getAncestor(int arg0) throws ItemNotFoundException, AccessDeniedException,
  RepositoryException {
    return node.getAncestor(arg0);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Item#getDepth()
   */
  public int getDepth() throws RepositoryException {
    return node.getDepth();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Item#getName()
   */
  public String getName() throws RepositoryException {
    return node.getName();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Item#isModified()
   */
  public boolean isModified() {
    return node.isModified();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Item#isNew()
   */
  public boolean isNew() {
    return node.isNew();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Item#isNode()
   */
  public boolean isNode() {
    return node.isNode();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Item#isSame(javax.jcr.Item)
   */
  public boolean isSame(Item arg0) throws RepositoryException {
    return node.isSame(arg0);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Item#refresh(boolean)
   */
  public void refresh(boolean arg0) throws InvalidItemStateException, RepositoryException {
    node.refresh(arg0);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Item#remove()
   */
  public void remove() throws VersionException, LockException, ConstraintViolationException,
  RepositoryException {
    node.remove();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Item#save()
   */
  public void save() throws AccessDeniedException, ItemExistsException,
  ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException,
  VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
    node.save();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Node#getProperty(java.lang.String)
   */
  public Property getProperty(String arg0) throws PathNotFoundException, RepositoryException {
    return node.getProperty(arg0);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Item#getParent()
   */
  public Node getParent() throws ItemNotFoundException, AccessDeniedException,
  RepositoryException {
    return node.getParent();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Item#getPath()
   */
  public String getPath() throws RepositoryException {
    return node.getPath();
  }

  /* (non-Javadoc)
   * @see javax.jcr.Item#getSession()
   */
  public Session getSession() throws RepositoryException {
    return node.getSession();
  }
}
