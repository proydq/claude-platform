package com.proshine.claudeplatformbackend.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    
    private String conversationId;
    private String response;
    private Integer tokensUsed;
    private boolean success;
    private String error;
    private Long timestamp;
    
    public static ChatResponse success(String conversationId, String response, Integer tokensUsed) {
        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setConversationId(conversationId);
        chatResponse.setResponse(response);
        chatResponse.setTokensUsed(tokensUsed);
        chatResponse.setSuccess(true);
        chatResponse.setTimestamp(System.currentTimeMillis());
        return chatResponse;
    }
    
    public static ChatResponse error(String conversationId, String error) {
        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setConversationId(conversationId);
        chatResponse.setError(error);
        chatResponse.setTokensUsed(0);
        chatResponse.setSuccess(false);
        chatResponse.setTimestamp(System.currentTimeMillis());
        return chatResponse;
    }
}