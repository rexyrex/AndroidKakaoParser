package com.rexyrex.kakaoparser.Database.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;
import androidx.room.Update;

import com.rexyrex.kakaoparser.Database.Converters.DateConverter;
import com.rexyrex.kakaoparser.Database.Models.AnalysedChatModel;
import com.rexyrex.kakaoparser.Database.Models.ChatLineModel;
import com.rexyrex.kakaoparser.Database.Models.WordModel;
import com.rexyrex.kakaoparser.Entities.DateIntPair;
import com.rexyrex.kakaoparser.Entities.StringIntPair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Dao
@TypeConverters(DateConverter.class)
public interface AnalysedChatDAO {
    @Insert
    public void insert(AnalysedChatModel... chats);
    @Insert
    public void insertAll(ArrayList<AnalysedChatModel> chats);
    @Update
    public void update(AnalysedChatModel... chats);
    @Delete
    public void delete(AnalysedChatModel chat);

    @Query("SELECT * FROM tb_analysed_chat WHERE id = :id")
    public AnalysedChatModel getItemById(int id);

    @Query("SELECT * FROM tb_analysed_chat WHERE title = :title AND dt = :dt")
    public AnalysedChatModel getItemByTitleDt(String title, String dt);

    @Query("SELECT * FROM tb_analysed_chat")
    public List<AnalysedChatModel> getItems();

    @Query("SELECT * FROM tb_analysed_chat ORDER BY highscore DESC, dt DESC")
    public List<AnalysedChatModel> getItemsByScore();

    @Query("SELECT id FROM tb_analysed_chat WHERE title = :title AND dt = :dt")
    public int getId(String title, String dt);

    @Query("SELECT COUNT(*) FROM tb_analysed_chat WHERE title = :title AND dt = :dt")
    public int countChats(String title, String dt);

    @Query("DELETE FROM tb_analysed_chat")
    public void truncateTable();
}
