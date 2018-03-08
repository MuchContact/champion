package com.egova.websocket.jetty;

import com.egova.websocket.jetty.client.SimpleClientWebSocketHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.CORBA.INTERNAL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.sun.org.apache.xerces.internal.util.PropertyState.is;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ChampionApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChampionApplicationTests {
    private static Log logger = LogFactory.getLog(ChampionApplicationTests.class);

    @LocalServerPort
    private int port = 2345;
    private final String WEBSOCKET_URI = String.format("ws://localhost:%d/echo/websocket", port);
	@Test
	public void should_echo_message() {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(
                ClientConfiguration.class, PropertyPlaceholderAutoConfiguration.class)
                .properties("websocket.uri:ws://localhost:" + this.port
                        + "/echo/websocket")
                .run("--spring.main.web_environment=false");
        long count = context.getBean(ClientConfiguration.class).latch.getCount();
        AtomicReference<String> messagePayloadReference = context
                .getBean(ClientConfiguration.class).messagePayload;
        context.close();
        assertThat(count).isEqualTo(0);
        assertThat(messagePayloadReference.get())
                .isEqualTo("Echo \"Hello world!\"?");
//        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(
//                asList(new WebSocketTransport(new StandardWebSocketClient()))));
//        try {
//            assertThat(WEBSOCKET_URI).isEqualTo("ws://localhost:2345/echo/websocket");
//            StompSession stompSession = stompClient
//                    .connect(WEBSOCKET_URI, new StompSessionHandlerAdapter() {
//                    })
//                    .get(1, SECONDS);
//            stompSession.send("", "test".getBytes());
//
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } catch (TimeoutException e) {
//            e.printStackTrace();
//        }
    }

    @Configuration
    static class ClientConfiguration implements CommandLineRunner {
        @Value("${websocket.uri}")
        private String webSocketUri;

        public CountDownLatch latch = new CountDownLatch(1);
        public AtomicReference<String> messagePayload = new AtomicReference<>();


        @Override
        public void run(String... strings) throws Exception {
            logger.info("Waiting for response: latch=" + this.latch.getCount());
            if (this.latch.await(10, TimeUnit.SECONDS)) {
                logger.info("Got response: " + this.messagePayload.get());
            }
            else {
                logger.info("Response not received: latch=" + this.latch.getCount());
            }
        }

        @Bean
        public WebSocketConnectionManager wsConnectionManager() {

            WebSocketConnectionManager manager = new WebSocketConnectionManager(client(),
                    handler(), this.webSocketUri);
            manager.setAutoStartup(true);

            return manager;
        }

        @Bean
        public StandardWebSocketClient client() {
            return new StandardWebSocketClient();
        }

        @Bean
        public MySimpleClientWebSocketHandler handler() {
            return new MySimpleClientWebSocketHandler(this.latch,
                    this.messagePayload);
        }
    }

    static class MySimpleClientWebSocketHandler extends TextWebSocketHandler {
        private final CountDownLatch latch;
        private final AtomicReference<String> messagePayload;

        public MySimpleClientWebSocketHandler(CountDownLatch latch, AtomicReference<String> messagePayload) {

            this.latch = latch;
            this.messagePayload = messagePayload;
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            session.sendMessage(new TextMessage("1,muco"));
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            logger.warn("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            logger.warn(message.getPayload());
            logger.warn("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            messagePayload.set(message.getPayload());
            latch.countDown();
        }
    }
}
