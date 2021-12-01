package ilove.quark.us;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

@ServerEndpoint("/chat/{name}")
@ApplicationScoped
public class StartWebSocket {
    Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen (Session session, @PathParam("username") String username){
        sessions.put(username, session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("username") String username){
        sessions.remove(username);
        broadcast("User" + username + "left");
    }


    @OnError
    public void onError(Session session, @PathParam("username") String username, Throwable throwable)
    {
        sessions.remove(username);
        broadcast("User " + username + " left on error: " + throwable);
    }
    private void broadcast(String message) {
        sessions.values().forEach(s -> {
            s.getAsyncRemote().sendObject(message, result ->  {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            });
        });
    }


}
