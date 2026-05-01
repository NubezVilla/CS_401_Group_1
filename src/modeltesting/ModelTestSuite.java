package modeltesting;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    MessageTest.class,
    LoginInfoTest.class,
    UserTest.class,
    ReportTest.class,
    ConversationTest.class,
    GroupConversationTest.class,
    WrapperTest.class
})
class ModelTestSuite {
}
