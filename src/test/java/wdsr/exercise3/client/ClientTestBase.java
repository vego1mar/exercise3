package wdsr.exercise3.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.junit.After;
import org.junit.Before;

import io.undertow.Undertow;

public class ClientTestBase {
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 9691;

    protected UndertowJaxrsServer server;
    protected Client client;

    @Before
    public void setUp() {
        server = new UndertowJaxrsServer();
        server.start(Undertow.builder().addHttpListener(SERVER_PORT, SERVER_HOST));
        client = ClientBuilder.newClient();
    }

    @After
    public void tearDown() {
        server.stop();
        client.close();
    }

}
