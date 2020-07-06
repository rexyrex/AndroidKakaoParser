package com.rexyrex.kakaoparser.Database.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rexyrex.kakaoparser.Database.Models.WordModel;
import com.rexyrex.kakaoparser.Entities.StringIntPair;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface WordDAO {
    @Insert
    public void insert(WordModel... words);
    @Insert
    public void insertAll(ArrayList<WordModel> words);
    @Update
    public void update(WordModel... words);
    @Delete
    public void delete(WordModel word);

    @Query("SELECT * FROM tb_word")
    public List<WordModel> getItems();

    @Query("SELECT * FROM tb_word WHERE id = :id")
    public WordModel getItemById(Long id);

    @Query("DELETE FROM tb_word")
    public void truncateTable();

    @Query("SELECT COUNT(*) FROM tb_word")
    public int getCount();

    @Query("SELECT COUNT(DISTINCT(word)) FROM tb_word")
    public int getDistinctCount();

    @Query("SELECT DISTINCT(word) AS word, COUNT(*) AS frequency FROM tb_word GROUP BY word ORDER BY frequency desc")
    public List<StringIntPair> getFreqWordList();

    //get frequency of given word
    @Query("SELECT DISTINCT(word) AS word, COUNT(*) AS frequency FROM tb_word WHERE word = :searchTerm GROUP BY word ORDER BY frequency desc LIMIT 1")
    public StringIntPair getFreqWordListSearch(String searchTerm);

    //get user-frequency mapping of given word
    @Query("SELECT DISTINCT(author) AS word, COUNT(*) AS frequency FROM tb_word WHERE word = :searchTerm GROUP BY author ORDER BY frequency desc")
    public List<StringIntPair> getFreqWordListSearchByAuthor(String searchTerm);
}
