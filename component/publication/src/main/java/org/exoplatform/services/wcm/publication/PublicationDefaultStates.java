package org.exoplatform.services.wcm.publication;

public interface PublicationDefaultStates {

	public final static String ARCHIVED = "archived";
	public final static String OBSOLETE = "obsolete";
	public final static String UNPUBLISHED = "unpublished";
	public final static String ENROLLED = "enrolled";
	public final static String DRAFT = "draft";
	public final static String PENDING = "pending";
	public final static String APPROVED = "approved";
	public final static String STAGED = "staged";
	public final static String PUBLISHED = "published";
	
	/*
	 * Active Modes :
	 * Edit - DRAFT -> PUBLISHED
	 * Preview - PENDING -> PUBLISHED
	 * Live - STAGED -> PUBLISHED
	 */
}
