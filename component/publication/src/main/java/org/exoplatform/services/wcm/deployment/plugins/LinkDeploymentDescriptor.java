package org.exoplatform.services.wcm.deployment.plugins;

/**
 * @deprecated This class is deprecated since WCM 1.2.6
 * @see org.exoplatform.services.wcm.webcontent.LinkDeploymentDescriptor
 */
@Deprecated

public class LinkDeploymentDescriptor {

	private String sourcePath;
	private String targetPath;
	public String getSourcePath() {
		return sourcePath;
	}
	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}
	public String getTargetPath() {
		return targetPath;
	}
	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}
	
	
}
