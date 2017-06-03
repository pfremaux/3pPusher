package test.ws;

import org.junit.Test;
import test.ws.fakeclient.ClientWeb;
import test.ws.fakecustomer.OldServerWeb;
import verticle.thirdpartyws.MainVerticle;

import java.io.IOException;


public class Ordonnanceur {

    public static void main(String... strings) throws InterruptedException, IOException {
        new Ordonnanceur().testWsPush();
    }

    @Test
    public void testWsPush() throws InterruptedException, IOException {
        MainVerticle mainVerticle = new MainVerticle();
        //Thread.sleep(3000);
        OldServerWeb oldServerWeb = new OldServerWeb(mainVerticle.getVertx());
        oldServerWeb.makeCall("localhost", 8081, "/", OldServerWeb.SUBSCRIBE);
        ClientWeb clientWeb = new ClientWeb(mainVerticle.getVertx());
        clientWeb.connect2("localhost", 8080, "/");
        Thread.sleep(500);
        oldServerWeb.makeCall("localhost", 8081, "/", OldServerWeb.SENT_MESSAGE);
        Thread.sleep(500);
        clientWeb.close();
        oldServerWeb.close();
        mainVerticle.close();
        //Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        //threadSet.forEach(System.out::println);
        System.exit(0);
    }
}
