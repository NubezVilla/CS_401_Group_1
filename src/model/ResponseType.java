package model;

public enum ResponseType {
	//Alejandro
    LOGIN_SUCCESS, LOGIN_FAIL, //For user loging in, object is User 
    LOGOUT_SUCCESS, LOGOUT_FAIL, //For user loging out, object is Message
    REGISTER_USER_SUCCESS, REGISTER_USER_FAIL,  //To register a new account, object is User
    USER_INFO_SENT, USER_INFO_NOT_SENT, //To search a specific user, object is User
    MESSAGE_SENT, MESSAGE_NOT_SENT, //For the original client who sent the message, object is Message
    SENDING_MESSAGE, //For everyone else, no request form the user, object is Envelope
    ADD_PARTICIPANT_SUCCESS, ADD_PARTICIPANT_FAIL, //To add participant to a group message, object is Message(Message is 'participitant is added')
    REMOVE_PARTICIPANT_SUCCESS, REMOVE_PARTICIPANT_FAIL, //To remove participant from a group message, object is Message(Message is 'participitant is removed')
    GROUP_CREATION_SUCCESS, GROUP_CREATION_FAIL, //To create a group conversation, object is Conversation
    CREATE_CONVERSATION_SUCCESS, CREATE_CONVERSATION_FAIL, //To create a conversation with a specific person, object is Conversation
    
    
    CONVERSATION_SENT, CONVERSATION_NOT_SENT, //For IT users to look up a specific conversation, object is Conversation
    
    
    
    ACTIVE_CONVERSATION_UPDATED, //Pointer to what conversation the user is looking at, object is Message(Messge is 'active conversation updated')
    SENDING_CONVERSATIONS, //To send an ArrayList of conversations from server to user, no request from user, object is an ArrayList of Conversations
    SENDING_ACTIVE_USERS, //To send an ArrayList of users who are currently active, no request from user, object is an ArrayList of users
    UPDATED_USER_RECEIVED, UPDATED_USER_NOT_RECEIVED, //To update the current user information, objest is User
    USER_LOGGED_IN, USER_LOGGED_OUT,  //To inform user that a different user logged in or out from the server, no request from user, object is User
    PARTICIPANT_ADDED, PARTICIPANT_REMOVED, //To inform user that a different user has been removed from a specific group conversation, no request from user, object is conversation (user removed must be gone from the list of user IDs)
    GROUP_NAME_CHANGED, //To inform user that a specific group chats name has been changed, no request from user, object is conversation (after name is changed)
    
    
    
    //Riya
    CONVERSATION_LOG_QUERY_RESULT, // TO send an ArrayList of conversations of a specific user that the user is asking about, the user who asked must be a IT user, object is array list of conversations




    //Just added after
    LIST_OF_USERS_SIMILAR //To send an arrayList of matching users based on what the user sent. object is ArrayList of users
, CONVERSATION_LOG_NOT_SENT, CONVERSATION_LOG_SENT
}