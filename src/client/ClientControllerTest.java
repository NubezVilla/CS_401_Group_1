package client;

import model.*;
import client.Client.ClientRunner;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

@TestMethodOrder(MethodOrderer.MethodName.class)
class ClientControllerTest {

    private FakeClientRunner runner;
    private ClientController controller;

    @BeforeEach
    void setUp() {
        runner = new FakeClientRunner();
        controller = new ClientController(runner);
        runner.bindController(controller);
    }

    @AfterEach
    void tearDown() {
        runner.joinDeliveries();
    }


    static class FakeClientRunner extends ClientRunner {
        final List<SendCall> calls = new ArrayList<>();
        final List<Wrapper> queuedResponses = new ArrayList<>();
        private ClientController controllerRef;
        private final List<Thread> deliveryThreads = new ArrayList<>();

        FakeClientRunner() {
            super(null, null, null);
        }

        void bindController(ClientController c) {
            this.controllerRef = c;
        }

        void enqueueResponse(Wrapper w) {
            queuedResponses.add(w);
        }

        @Override
        public void send(Object payload, RequestType type) {
            calls.add(new SendCall(payload, type, null));
            scheduleNextDelivery();
        }

        @Override
        public void send(Object payload, ResponseType type) {
            calls.add(new SendCall(payload, null, type));
            scheduleNextDelivery();
        }

        @Override
        public void shutdown() {
        }

        private void scheduleNextDelivery() {
            if (queuedResponses.isEmpty() || controllerRef == null) return;
            Wrapper toDeliver = queuedResponses.remove(0);
            Thread t = new Thread(() -> {
                try { Thread.sleep(20); } catch (InterruptedException ignored) {}
                controllerRef.deliverResponse(toDeliver);
            }, "fake-responder");
            deliveryThreads.add(t);
            t.start();
        }

        SendCall lastCall() {
            assertFalse(calls.isEmpty(), "Expected at least one send() call");
            return calls.get(calls.size() - 1);
        }

        void joinDeliveries() {
            for (Thread t : deliveryThreads) {
                try { t.join(2000); } catch (InterruptedException ignored) {}
            }
        }
    }

    static class SendCall {
        final Object payload;
        final RequestType requestType;   
        final ResponseType responseType; 

        SendCall(Object payload, RequestType requestType, ResponseType responseType) {
            this.payload = payload;
            this.requestType = requestType;
            this.responseType = responseType;
        }
    }

    private static Wrapper wrap(Object payload, ResponseType type) {
        return new Wrapper(payload, type);
    }



    @Test
    @DisplayName("requestConversationById returns null when response is not CONVERSATION_SENT")
    void requestConversationById_wrongResponseType() {
        runner.enqueueResponse(wrap(null, ResponseType.CONVERSATION_NOT_SENT));

        Conversation result = controller.requestConversationById("convo-1");

        assertNull(result);
        assertEquals(1, runner.calls.size());
        assertEquals("convo-1", runner.lastCall().payload);
        assertEquals(RequestType.GET_CONVERSATION, runner.lastCall().requestType);
    }

    @Test
    @DisplayName("requestConversationById returns the conversation on CONVERSATION_SENT")
    void requestConversationById_success() {
        Conversation expected = new Conversation();
        runner.enqueueResponse(wrap(expected, ResponseType.CONVERSATION_SENT));

        Conversation result = controller.requestConversationById("c-1");

        assertSame(expected, result);
    }



    @Test
    @DisplayName("loginAttempt returns false on LOGIN_FAIL")
    void loginAttempt_failure() {
        runner.enqueueResponse(wrap(null, ResponseType.LOGIN_FAIL));

        boolean ok = controller.loginAttempt("alice", "wrong-pw");

        assertFalse(ok);
        assertEquals(1, runner.calls.size());
        assertTrue(runner.lastCall().payload instanceof LoginInfo,
                "Login payload should be a LoginInfo");
        assertEquals(RequestType.LOGIN, runner.lastCall().requestType);
    }

    @Test
    @DisplayName("logoutAttempt is a no-op when no user is logged in")
    void logoutAttempt_noUser() {
        controller.logoutAttempt();
        assertEquals(0, runner.calls.size(), "Should not call runner when no user is logged in");
    }


    @Test
    @DisplayName("createNewUser returns true on REGISTER_USER_SUCCESS and sends ordered ArrayList")
    void createNewUser_success() {
        User created = new User("alice", "pw");
        runner.enqueueResponse(wrap(created, ResponseType.REGISTER_USER_SUCCESS));

        Boolean ok = controller.createNewUser("Alice", "Engineer", "alice", "pw");

        assertEquals(Boolean.TRUE, ok);
        assertEquals(1, runner.calls.size());
        assertEquals(RequestType.REGISTER, runner.lastCall().requestType);

        Object payload = runner.lastCall().payload;
        assertTrue(payload instanceof ArrayList<?>, "Payload should be an ArrayList");
        ArrayList<?> info = (ArrayList<?>) payload;
        assertEquals(4, info.size(), "Expect [username, password, name, position]");
        assertEquals("alice",    info.get(0));
        assertEquals("pw",       info.get(1));
        assertEquals("Alice",    info.get(2));
        assertEquals("Engineer", info.get(3));
    }

    @Test
    @DisplayName("createNewUser returns false on REGISTER_USER_FAIL")
    void createNewUser_failure() {
        runner.enqueueResponse(wrap(null, ResponseType.REGISTER_USER_FAIL));

        Boolean ok = controller.createNewUser("Bob", "QA", "bob", "pw");

        assertEquals(Boolean.FALSE, ok);
    }

    @Test
    @DisplayName("createNewITUser sends a User object with REGISTER_IT and returns true on success")
    void createNewITUser_success() {
        runner.enqueueResponse(wrap(null, ResponseType.REGISTER_USER_SUCCESS));

        Boolean ok = controller.createNewITUser("IT Bob", "Admin", "bob", "pw");

        assertEquals(Boolean.TRUE, ok);
        assertEquals(RequestType.REGISTER_IT, runner.lastCall().requestType);
        assertTrue(runner.lastCall().payload instanceof User,
                "REGISTER_IT payload should be a User object");
    }

    @Test
    @DisplayName("createNewITUser returns false on REGISTER_USER_FAIL")
    void createNewITUser_failure() {
        runner.enqueueResponse(wrap(null, ResponseType.REGISTER_USER_FAIL));

        Boolean ok = controller.createNewITUser("IT Bob", "Admin", "bob", "pw");

        assertEquals(Boolean.FALSE, ok);
    }



    @Test
    @DisplayName("sendMessage sends SEND_MESSAGE request with the text payload")
    void sendMessage_sendsCorrectRequest() {
        runner.enqueueResponse(wrap(null, ResponseType.MESSAGE_NOT_SENT));

        controller.sendMessage("hello world");

        assertEquals(1, runner.calls.size());
        assertEquals("hello world", runner.lastCall().payload);
        assertEquals(RequestType.SEND_MESSAGE, runner.lastCall().requestType);
    }

    @Test
    @DisplayName("sendMessage returns silently on MESSAGE_NOT_SENT (no exception)")
    void sendMessage_failureIsSwallowed() {
        runner.enqueueResponse(wrap(null, ResponseType.MESSAGE_NOT_SENT));
        assertDoesNotThrow(() -> controller.sendMessage("nope"));
    }



    @Test
    @DisplayName("startNewGroupConversation returns null on GROUP_CREATION_FAIL")
    void startNewGroupConversation_failure() {
        runner.enqueueResponse(wrap(null, ResponseType.GROUP_CREATION_FAIL));

        GroupConversation result = controller.startNewGroupConversation(new Conversation());

        assertNull(result);
        assertEquals(RequestType.CREATE_GROUP_CONVERSATION, runner.lastCall().requestType);
    }


    @Test
    @DisplayName("startNewConversation returns null on CREATE_CONVERSATION_FAIL")
    void startNewConversation_failure() {
        runner.enqueueResponse(wrap(null, ResponseType.CREATE_CONVERSATION_FAIL));

        Conversation result = controller.startNewConversation(new User("bob", "pw"));

        assertNull(result);
        assertEquals(RequestType.CREATE_CONVERSATION, runner.lastCall().requestType);
    }



    @Test
    @DisplayName("queryConversationLogsByUser returns the payload list on a matching response")
    void queryConversationLogsByUser_success() {
        ArrayList<Conversation> payload = new ArrayList<>();
        payload.add(new Conversation());
        payload.add(new Conversation());
        runner.enqueueResponse(wrap(payload, ResponseType.CONVERSATION_LOG_QUERY_RESULT));

        ArrayList<Conversation> result =
                controller.queryConversationLogsByUser(new User("alice", "pw"));

        assertEquals(2, result.size());
        assertEquals(RequestType.QUERY_CONVERSATION_LOG, runner.lastCall().requestType);
    }

    @Test
    @DisplayName("queryConversationLogsByUser returns empty list when response is wrong type")
    void queryConversationLogsByUser_wrongType() {
        runner.enqueueResponse(wrap(null, ResponseType.USER_INFO_NOT_SENT));

        ArrayList<Conversation> result =
                controller.queryConversationLogsByUser(new User("alice", "pw"));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("queryConversationLogsByID returns empty list when response is wrong type")
    void queryConversationLogsByID_wrongType() {
        runner.enqueueResponse(wrap(null, ResponseType.CONVERSATION_NOT_SENT));

        ArrayList<Conversation> result = controller.queryConversationLogsByID("anything");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("searchUsers returns the payload list on USER_INFO_SENT")
    void searchUsers_success() {
        ArrayList<User> payload = new ArrayList<>();
        payload.add(new User("alice", "pw"));
        runner.enqueueResponse(wrap(payload, ResponseType.USER_INFO_SENT));

        ArrayList<User> result = controller.searchUsers("ali");

        assertEquals(1, result.size());
        assertEquals("ali", runner.lastCall().payload);
        assertEquals(RequestType.SEARCH_SIMILAR_USERS, runner.lastCall().requestType);
    }

    @Test
    @DisplayName("searchUsers returns empty list on a non-matching response")
    void searchUsers_failure() {
        runner.enqueueResponse(wrap(null, ResponseType.USER_INFO_NOT_SENT));

        ArrayList<User> result = controller.searchUsers("zzz");

        assertTrue(result.isEmpty());
    }



    @Test
    @DisplayName("updateUser returns null on UPDATED_USER_NOT_RECEIVED")
    void updateUser_failure() {
        runner.enqueueResponse(wrap(null, ResponseType.UPDATED_USER_NOT_RECEIVED));

        Boolean result = controller.updateUser(
                new User("alice", "pw"), "n", "p", "u", "pw");

        assertNull(result);
    }

    @Test
    @DisplayName("updateUser returns true and updates target name/position on success")
    void updateUser_success() {
        runner.enqueueResponse(wrap(null, ResponseType.UPDATED_USER_RECEIVED));

        User target = new User("alice", "oldPw");
        Boolean result = controller.updateUser(target, "newName", "newPos", "newUser", "newPw");

        assertEquals(Boolean.TRUE, result);
        assertEquals("newName", target.getName());
        assertEquals("newPos",  target.getPosition());
    }
}