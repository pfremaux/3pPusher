package test.ws.unused;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * @deprecated
 */
@ServerEndpoint(value = "/echo")
public class EchoEndpointAnnotated {
    @OnMessage
    public String onMessage(String message, Session session) {
        return message;
    }
}