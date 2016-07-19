package org.apache.fineract.notification;

import com.mockrunner.mock.jms.MockQueue;
import org.apache.fineract.notification.data.NotificationData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

@ContextConfiguration(locations = {
        "classpath:META-INF/testNotificationContext.xml",
})
@RunWith(SpringJUnit4ClassRunner.class)
public class SenderTest {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private MockQueue mockQueue;

    @Test
    public void notificationCreation() {

        NotificationData notificationData = new NotificationData.NotificationBuilder()
                .withUserId(1L)
                .withObjectType("CLIENT")
                .withObjectIdentifier(1L)
                .withAction("CREATED")
                .withActor("ADMIN")
                .withNotificationContent("A client was created")
                .withSystemGenerated(false)
                .build();

        jmsTemplate.send(mockQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                System.out.println("Message send successfully");
                return session.createObjectMessage(notificationData);
            }
        });
    }
}
