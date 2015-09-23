package org.kie.tests.wb.eap;

import static org.junit.Assert.fail;
import static org.kie.remote.tests.base.DeployUtil.getWebArchive;
import static org.kie.remote.tests.base.DeployUtil.replaceJars;
import static org.kie.tests.wb.base.util.TestConstants.PROJECT_VERSION;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieWbWarJbossEapDeploy {

    protected static final Logger logger = LoggerFactory.getLogger(KieWbWarJbossEapDeploy.class);

    private static final String classifier = "eap6_4";

    public static WebArchive createTestWar() {
        WebArchive war = getWebArchive("org.kie", "kie-wb-distribution-wars", classifier, PROJECT_VERSION);

        String[][] jarsToReplace = {
                // data services NPE?
                { "org.jbpm", "jbpm-kie-services" },

                { "org.jbpm", "jbpm-console-ng-rest" },

                // kie-remote
                { "org.kie.remote", "kie-remote-jaxb" },
                { "org.kie.remote", "kie-remote-services" }
        };
        replaceJars(war, PROJECT_VERSION, jarsToReplace);

        updateJbpmXml(war);

        if( false ) {
            replaceWebXmlForWebServices(war);
        }

        return war;
    }

    private static void updateJbpmXml(WebArchive war) {
        String jbpmXmlPath = "org.kie.workbench.KIEWebapp/profiles/jbpm.xml";
        Asset jbpmXmlAsset = war.get(jbpmXmlPath).getAsset();
        String jbpmXmlStr = new String(getBytesFromAsset(jbpmXmlAsset));
        jbpmXmlStr = jbpmXmlStr.replace("storesvgonsave enabled=\"false\"",
                           "storesvgonsave enabled=\"true\"");
        File newJbpmXmlFile = storeStringInFile(jbpmXmlStr);
        war.delete(jbpmXmlPath);

        FileAsset jbpmXmlFileAsset = new FileAsset(newJbpmXmlFile);
        war.add(jbpmXmlFileAsset, jbpmXmlPath);
    }

    private static File storeStringInFile(String jbpmXmlFileInputString) {
        File newJbpmXmlFile = null;
        try {
            newJbpmXmlFile = File.createTempFile("arquillian-", "-jbpm.xml");
            newJbpmXmlFile.deleteOnExit();
        } catch( IOException ioe ) {
            ioe.printStackTrace();
            fail("Unable to create temporary file to store modified jbpm.xml content: " + ioe.getMessage());
        }
        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(newJbpmXmlFile));
            writer.write(jbpmXmlFileInputString);
        } catch( IOException ioe ) {
            ioe.printStackTrace();
            fail("Unable to write content to temporary jbpm.xml file : " + ioe.getMessage());
        } finally {
            if( writer != null ) {
                try {
                    writer.close();
                } catch( IOException e ) {
                    // do nothing
                }
            }
        }
        return newJbpmXmlFile;
    }
    private static byte[] getBytesFromAsset(Asset asset) {
        InputStream is = asset.openStream();
        byte[] bytes = null;
        try {
            bytes = IOUtils.toByteArray(is);
        } catch( IOException ioe ) {
            ioe.printStackTrace();
            fail( "Unable to convert input stream to byte array: " + ioe.getMessage());
        }
        IOUtils.closeQuietly(is);
        return bytes;
    }

    private static void replaceWebXmlForWebServices( WebArchive war ) {
        war.delete("WEB-INF/web.xml");
        war.addAsWebInfResource("WEB-INF/web.xml", "web.xml");
    }

    protected void printTestName() {
        StackTraceElement ste = new Throwable().getStackTrace()[1];
        logger.info("] Starting " + ste.getMethodName());
    }

}
