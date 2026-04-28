package modeltesting;

import model.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class WrapperTest {

    @Test
    void constructorSetsFieldsCorrectly() {
        Object payload = "TestPayload";
        RequestType type = RequestType.LOGIN;

        Wrapper wrapper = new Wrapper(payload, type);

        assertEquals(payload, wrapper.getPayload());
        assertEquals(type, wrapper.getRequestType());
    }

    @Test
    void payloadCanBeNull() {
        Wrapper wrapper = new Wrapper(null, RequestType.LOGOUT);

        assertNull(wrapper.getPayload());
        assertEquals(RequestType.LOGOUT, wrapper.getRequestType());
    }

    @Test
    void requestTypeCanBeAnyEnumValue() {
        Wrapper wrapper1 = new Wrapper("data1", RequestType.REGISTER);
        Wrapper wrapper2 = new Wrapper("data2", RequestType.SEND_MESSAGE);
        Wrapper wrapper3 = new Wrapper("data3", RequestType.GET_MESSAGES);

        assertEquals(RequestType.REGISTER, wrapper1.getRequestType());
        assertEquals(RequestType.SEND_MESSAGE, wrapper2.getRequestType());
        assertEquals(RequestType.GET_MESSAGES, wrapper3.getRequestType());
    }

    @Test
    void requestTypeCanBeNull() {
        Wrapper wrapper = new Wrapper("data", null);

        assertEquals("data", wrapper.getPayload());
        assertNull(wrapper.getRequestType());
    }

    @Test
    void payloadCanBeDifferentTypes() {
        Wrapper stringWrapper = new Wrapper("string", RequestType.LOGIN);
        Wrapper intWrapper = new Wrapper(123, RequestType.GET_USER_INFO);
        Wrapper objectWrapper = new Wrapper(new Message("hi", "user"), RequestType.SEND_MESSAGE);

        assertEquals("string", stringWrapper.getPayload());
        assertEquals(123, intWrapper.getPayload());
        assertTrue(objectWrapper.getPayload() instanceof Message);
    }

    @Test
    void allEnumValuesAreSupported() {
        for (RequestType type : RequestType.values()) {
            Wrapper wrapper = new Wrapper("test", type);
            assertEquals(type, wrapper.getRequestType());
        }
    }
}
