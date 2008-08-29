package org.exoplatform.services.wcm.search;

import javax.jcr.Node;

import org.exoplatform.portal.config.model.PageNode;

public class WcmResult {
	private Node node;
	private PageNode pageNode;
	private Long score;
	private String type;
	
	public static String PAGE_NODE = "pageNode";
	public static String NODE = "node";
	
	
	public WcmResult (PageNode pageNode, Long score) {
		this.pageNode=pageNode;
		this.score=score;
		this.type=PAGE_NODE;
		this.node=null;
	}
	
	public WcmResult (Node node, Long score) {
		this.pageNode=null;
		this.score=score;
		this.type=NODE;
		this.node=node;
	}
	
	public Node getNode () {
		return this.node;
	}
	
	public PageNode getPageNode () {
		return this.pageNode;
	}
	
	public String getType() {
		return this.type;
	}
	
	public Long getScore() {
		return this.score;
	}
}
