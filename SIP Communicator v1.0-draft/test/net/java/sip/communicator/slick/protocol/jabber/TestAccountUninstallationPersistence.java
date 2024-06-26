/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.slick.protocol.jabber;

import org.osgi.framework.*;
import junit.framework.*;
import net.java.sip.communicator.service.configuration.*;
import net.java.sip.communicator.service.protocol.*;

/**
 * Contains tests verifying persistence of account uninstallation. In other
 * words we try to make sure that once uninstalled an account remains
 * uninstalled.
 *
 * @author Emil Ivov
 */
public class TestAccountUninstallationPersistence
        extends TestCase
{
    /**
     * Creates a new test instance wrapper around the test with the specified
     * name.
     * @param testName the name of the test that we will be executing.
     */
    public TestAccountUninstallationPersistence(String testName)
    {
        super(testName);
    }

    /**
     * Retrieves a reference to the Jabber bundle, stops it and uninstalls it and
     * then reinstalls it in order to make sure that accounts are not reloaded
     * once removed.
     *
     * @throws java.lang.Exception if something goes wrong while manipulating
     * the bundles.
     */
    public void testAccountUninstallationPersistence()
        throws Exception
    {
        Bundle providerBundle = JabberSlickFixture.providerBundle;

        providerBundle.stop();

        assertTrue("Couldn't stop the protocol provider bundle. State was "
                   + providerBundle.getState()
                   ,    Bundle.ACTIVE   != providerBundle.getState()
                     && Bundle.STOPPING != providerBundle.getState());

        providerBundle.uninstall();

        assertEquals("Couldn't stop the protocol provider bundle."
                     , Bundle.UNINSTALLED, providerBundle.getState());

        //Now reinstall the bundle and restart the provider
        providerBundle
            = JabberSlickFixture.bc.installBundle(providerBundle.getLocation());

        assertEquals("Couldn't re-install protocol provider bundle."
                     , Bundle.INSTALLED, providerBundle.getState());

        AccountManagerUtils.startBundleAndWaitStoredAccountsLoaded(
            JabberSlickFixture.bc, providerBundle, ProtocolNames.JABBER);
        assertEquals("Couldn't re-start protocol provider bundle."
                     , Bundle.ACTIVE, providerBundle.getState());


        //verify that the provider is not reinstalled
        ServiceReference[] jabberProviderRefs = null;
        try
        {
            jabberProviderRefs = JabberSlickFixture.bc.getServiceReferences(
                ProtocolProviderService.class.getName(),
                "(" + ProtocolProviderFactory.PROTOCOL
                    + "=" +ProtocolNames.JABBER + ")");
        }
        catch (InvalidSyntaxException ex)
        {
            fail("We apparently got our filter wrong " + ex.getMessage());
        }

        //make sure we didn't retrieve a service
        assertTrue("A Jabber Protocol Provider Service was still regged as an "
                      +"osgi service after it was explicitly uninstalled"
                      ,jabberProviderRefs == null || jabberProviderRefs.length == 0);

        //and a nasty hack at the end - delete the configuration file so that
        //we get a fresh start on next run.
        ServiceReference confReference
            = JabberSlickFixture.bc.getServiceReference(
                ConfigurationService.class.getName());
        ConfigurationService configurationService
            = (ConfigurationService) JabberSlickFixture.bc.getService(confReference);

        configurationService.purgeStoredConfiguration();
    }
}
