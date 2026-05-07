package server;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    AuthenticateHandlerTest.class,
    ConversationHandlerTest.class,
    FileManagerTest.class,
    LogTest.class,
    MessageHandlerTest.class,
    //ResponseHandlerTest.class,
    ServerTest.class,
    UserDataTest.class
})
public class AllServerTests {
}