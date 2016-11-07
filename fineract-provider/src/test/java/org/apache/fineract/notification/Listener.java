package org.apache.fineract.notification;

import org.springframework.jms.listener.SessionAwareMessageListener;

import javax.jms.*;

public class Listener implements SessionAwareMessageListener {

    @Override
    public void onMessage(Message message, Session session) throws JMSException {
        TextMessage msg = (TextMessage) message;
        System.out.println("Received: " + msg.getText());
    }
}
