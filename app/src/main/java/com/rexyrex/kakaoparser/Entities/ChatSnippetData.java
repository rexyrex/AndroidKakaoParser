package com.rexyrex.kakaoparser.Entities;

import com.rexyrex.kakaoparser.Database.Models.ChatLineModel;

import java.util.List;

public class ChatSnippetData {

    List<ChatLineModel> clm;
    ChatLineModel highlightChatLine;

    private static ChatSnippetData the_instance;
    public static ChatSnippetData getInstance() {
        if (the_instance == null) {
            the_instance = new ChatSnippetData();
        }
        return the_instance;
    }

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
