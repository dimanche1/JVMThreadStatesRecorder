package Core;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;

public class MBeanConnection {
    private MBeanServerConnection server;
    private JMXConnector jmxc;
    private JMXServiceURL jmxServiceUrl;

    public MBeanServerConnection getServerConnectionByPID(String pid) {
        String CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";

        VirtualMachine vm = null;
        try {
            vm = VirtualMachine.attach(pid);
        } catch (AttachNotSupportedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String url = null;
        try {
            url = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            jmxServiceUrl = new JMXServiceURL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return getServerInstance();
    }

    public MBeanServerConnection getServerConnectionRemote(String jmxHost, int jmxPort) {
        String urlPath = "/jndi/rmi://" + jmxHost + ":" + jmxPort + "/jmxrmi";

        try {
            jmxServiceUrl = new JMXServiceURL("rmi", "", 0, urlPath);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return getServerInstance();
    }

    private MBeanServerConnection getServerInstance() {
        try {
            jmxc = JMXConnectorFactory.connect(jmxServiceUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            server = jmxc.getMBeanServerConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return server;
    }

    public void close() throws IOException {
        jmxc.close();
    }
}