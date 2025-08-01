package com.proshine.claudeplatformbackend.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class ChatRequest {
    
    private String conversationId;
    
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 5000, message = "消息内容长度不能超过5000个字符")
    private String message;
    
    private List<String> fileIds;
    
    @Size(max = 200, message = "对话标题长度不能超过200个字符")
    private String title;
}