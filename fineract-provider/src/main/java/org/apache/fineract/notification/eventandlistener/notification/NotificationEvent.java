package org.apache.fineract.notification.eventandlistener.notification;

import org.apache.fineract.notification.data.NotificationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

@Service
public class NotificationEvent {

    private final JmsTemplate jmsTemplate;

    @Autowired
    public NotificationEvent(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void broadcastNotification(final Destination destination, final NotificationData notificationData) {
        this.jmsTemplate.send(destination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                return session.createObjectMessage(notificationData);
            }
        });
    }
}
