package org.exoplatform.wcm.extentions.component.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.StringInputTransformer;
import org.exoplatform.services.rest.transformer.StringOutputTransformer;
import org.exoplatform.services.wcm.extensions.security.SHAMessageDigester;

@URITemplate("/copyfile/")
public class CopyContentFile implements ResourceContainer {

	private static final Log log = ExoLogger.getLogger(CopyContentFile.class);
	private static final String OK_RESPONSE = "OK".intern();
	private static final String KO_RESPONSE = "KO".intern();
	private static String stagingStorage;
	private static String targetKey;

	public CopyContentFile(InitParams params) {
		stagingStorage = params.getValueParam("stagingStorage").getValue();
		targetKey = params.getValueParam("targetKey").getValue();
	}

	@HTTPMethod("POST")
	@URITemplate("/copy/")
	@InputTransformer(StringInputTransformer.class)
	@OutputTransformer(StringOutputTransformer.class)
	public Response copyFile(String param) throws Exception {
		if (log.isInfoEnabled()) {
			log.info("Start Execute CopyContentFile Web Service");
		}
		try {

			String[] tabParam = param.split("&&");
			String timesTamp = tabParam[0].split("=")[1];
			String clientHash = tabParam[1].split("=")[1];
			String contents = tabParam[2].split("contentsfile=")[1];
			String superPassword = targetKey.split(":")[1];
			String message = timesTamp + ":" + superPassword;

			String serverHash = SHAMessageDigester.getHash(message);
			if (serverHash != null && serverHash.equals(clientHash)) {
				Date date = new Date();
				long time = date.getTime();
				File stagingFolder = new File(stagingStorage);
				if (!stagingFolder.exists())
					stagingFolder.mkdirs();
				File contentsFile = new File(stagingStorage + File.separator
						+ clientHash + "-" + time + ".xml");
				OutputStream ops = new FileOutputStream(contentsFile);
				ops.write(contents.getBytes());
				ops.close();
			} else {
				log.info("echec authentification...");
				return Response.Builder.ok(KO_RESPONSE, "text/plain").build();
			}
		} catch (Exception ex) {
			log.error("error when copying content file");
			return Response.Builder.ok(KO_RESPONSE, "text/plain").build();
		}
		if (log.isInfoEnabled()) {
			log.info("Start Execute CopyContentFile Web Service");
		}
		return Response.Builder.ok(OK_RESPONSE, "text/plain").build();

	}

}
