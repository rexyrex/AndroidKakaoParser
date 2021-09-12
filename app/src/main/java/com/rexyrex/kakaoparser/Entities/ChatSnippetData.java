package com.rexyrex.kakaoparser.Entities;

import com.rexyrex.kakaoparser.Database.Models.ChatLineModel;

import java.io.Serializable;
import java.util.List;

public class ChatSnippetData implements Serializable {

    List<ChatLineModel> clm;
    ChatLineModel highlightChatLine;

    public ChatSnippetData(){

    }

    public List<ChatLineModel> getClm() {
        return clm;
    }

    public void setClm(List<ChatLineModel> clm) {
        this.clm = clm;
    }

    public ChatLineModel getHighlightChatLine() {
        return highlightChatLine;
    }

    public void setHighlightChatLine(ChatLineModel highlightChatLine) {
        this.highlightChatLine = highlightChatLine;
    }
}
