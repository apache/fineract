package org.apache.fineract.notification;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

@RunWith(MockitoJUnitRunner.class)
public class ListenerTest {

    private Listener listener;
    private Session session;
    private TextMessage textMessageMock;

    @Before
    public void setUp() {
        listener = new Listener();
        session = Mockito.mock(Session.class);
        textMessageMock = Mockito.mock(TextMessage.class);
    }

    @Test
    public void testListener() throws JMSException {
        Mockito.when(textMessageMock.getText()).thenReturn("content");
        listener.onMessage(textMessageMock, session);
        Mockito.verify(textMessageMock).getText();
    }
}
