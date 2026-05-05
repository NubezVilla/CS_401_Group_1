package model;

public enum RequestType {
    
	LOGIN, //To login sends LoginInfo
    LOGOUT, //No object sent
    REGISTER, //Sends an entire user
    GET_USER_INFO, //Sends entire user
    SEND_MESSAGE, //Sends message
    
    ADD_PARTICIPANT, //Sends an entire user
    REMOVE_PARTICIPANT, //Sends an entire user
    CREATE_CONVERSATION, //Sends an entire (Group)conversation
    CREATE_GROUP_CONVERSATION, //Sends an entire conversation
    
    
    GET_CONVERSATION, //Sends the ID of a conversation

    
    
    UPDATE_ACTIVE_CONVERSATION, //Sends an entire conversation
    
    
    UPDATE_USER_INFO, //Sends an entire user
    
    
    CHANGE_GROUP_NAME, //Sends an entire conversation
    
    
    
    
    QUERY_CONVERSATION_LOG_BY_USER, //Sends an entire user
    QUERY_CONVERSATION_LOG_BY_ID, //Sends a String of ID(coversation)
    
    
    
// Just added after
    SEARCH_SIMILAR_USERS, //Sends a String of Matching(find users that names and IDs start with Matching)
    QUERY_CONVERSATION_LOG, REQUEST_CONVERSATION_LOG,
}; 