package test;

import test.ws.fakeclient.ClientWeb;
import test.ws.unused.UnsusedCustomer;
import thirdpartypusher.old.pusher.AdvancedServer;

public class FirstTest {

    public static void main(String... a) throws Exception {
        AdvancedServer.main();
        // unsusedCustomer.sendPost();
        //ClientWeb clientWeb = new ClientWeb();
        //clientWeb.connect("ws://localhost:8080/");//send en même temps
        //UnsusedCustomer unsusedCustomer = new UnsusedCustomer();
        //unsusedCustomer.sendPost("la ", "monuuid");

    }

}
