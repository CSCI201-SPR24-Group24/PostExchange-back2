package com.postexchange.controller;


import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint("/activity_socket")
public class ActivityWebSocket {

    //Clients
    private static Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    /**
     * Executed when there's a new user connecting.
     * @param newUser The session for the new user.
     */
    @OnOpen
    public void onOpen(Session newUser)
    {
        sessions.add(newUser);
    }

    /**
     * Execute when user has closed a connection.
     *
     * @param userSession
     */
    @OnClose
    public void onClose(Session userSession)
    {
        sessions.remove(userSession);
    }

    //Activity type, to-user, from-user( name+id), postcardId
    public static void broadCast(String activity) throws IOException
    {
        for (Session s : sessions)
        {
            s.getBasicRemote().sendText(activity);
        }
    }




}
