package com.rexyrex.kakaoparser.Database.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;
import androidx.room.Update;

import com.rexyrex.kakaoparser.Database.Converters.DateConverter;
import com.rexyrex.kakaoparser.Database.Models.ChatLineModel;
import com.rexyrex.kakaoparser.Entities.StringIntPair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Dao
@TypeConverters(DateConverter.class)
public interface ChatLineDAO {
    @Insert
    public void insert(ChatLineModel... chatLines);
    @Insert
    public void insertAll(ArrayList<ChatLineModel> chatLines);
    @Update
    public void update(ChatLineModel... users);
    @Delete
    public void delete(ChatLineModel user);

    @Query("SELECT * FROM tb_chat_line")
    public List<ChatLineModel> getItems();

    @Query("SELECT * FROM tb_chat_line WHERE id = :id")
    public ChatLineModel getItemById(Long id);

    @Query("DELETE FROM tb_chat_line")
    public void truncateTable();

    @Query("SELECT COUNT(*) FROM tb_chat_line WHERE author = :author")
    public int getChatCountByAuthor(String author);

    @Query("SELECT COUNT(*) FROM tb_chat_line")
    public int getCount();

    @Query("SELECT COUNT (DISTINCT(dateDayString)) FROM tb_chat_line")
    public int getDayCount();

    @Query("SELECT COUNT(DISTINCT(author)) FROM tb_chat_line")
    public int getChatterCount();

    @Query("SELECT DISTINCT(author) FROM tb_chat_line")
    public List<String> getChatters();

    //get chatter-frequency pairs
    @Query("SELECT author as word, count(author) as frequency FROM tb_chat_line GROUP BY author ORDER BY count(author) desc")
    public List<StringIntPair> getChatterFrequencyPairs();

    //get top 10 most freq chatters
    @Query("SELECT author as word, count(author) as frequency FROM tb_chat_line GROUP BY author ORDER BY count(author) desc LIMIT 10")
    public List<StringIntPair> getTop10Chatters();

    @Query("SELECT COUNT(content) FROM tb_chat_line WHERE author = :author")
    public int getChatterChatLineCount(String author);

    @Query("SELECT MIN(date) FROM tb_chat_line")
    public Date getStartDate();

    @Query("SELECT Max(date) FROM tb_chat_line")
    public Date getEndDate();

    @Query("SELECT * FROM tb_chat_line WHERE instr(content, :word)")
    public List<ChatLineModel> getChatLinesContainingWord(String word);

    @Query("SELECT * FROM tb_chat_line WHERE id < :id+20 AND id > :id-20")
    public List<ChatLineModel> getSurroundingChatLines(int id);
}
