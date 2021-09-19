package com.rexyrex.kakaoparser.Database.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Update;

import com.rexyrex.kakaoparser.Database.Models.ChatLineModel;
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

    @Query("SELECT COUNT(*) FROM tb_word WHERE author = :author")
    public int getTotalWordCountByAuthor(String author);

    @Query("SELECT author as word, COUNT(word) as frequency FROM tb_word GROUP BY author ORDER BY COUNT(word) desc")
    public List<StringIntPair> getTotalWordCountByRank();

    @Query("SELECT COUNT(DISTINCT(word)) FROM tb_word WHERE author = :author")
    public int getDistinctWordCountByAuthor(String author);

    @Query("SELECT author as word, COUNT(DISTINCT(word)) as frequency FROM tb_word GROUP BY author ORDER BY COUNT(DISTINCT(word)) desc")
    public List<StringIntPair> getDistinctWordCountByRank();

    @Query("SELECT COUNT(DISTINCT(word)) FROM tb_word")
    public int getDistinctCount();

    //get top 10 most freq chatters
    @Query("SELECT author as word, count(word) as frequency FROM tb_word GROUP BY author ORDER BY count(word) desc LIMIT 10")
    public List<StringIntPair> getTop10ChattersByWords();

    //get top 10 most freq chatters
    @Query("SELECT author as word, count(word) as frequency FROM tb_word GROUP BY author ORDER BY count(word) desc")
    public List<StringIntPair> getTopChattersByWords();

    @Query("SELECT DISTINCT(word) AS word, COUNT(*) AS frequency FROM tb_word WHERE author = :author AND LENGTH(word)>1 GROUP BY word ORDER BY RANDOM() LIMIT 5")
    public List<StringIntPair> getFreqWordListRandomSamplesByAuthor(String author);

    @Query("SELECT DISTINCT(word) AS word, COUNT(*) AS frequency FROM tb_word GROUP BY word ORDER BY frequency desc LIMIT 10000")
    public List<StringIntPair> getFreqWordList();

    @Query("SELECT DISTINCT(word) AS word, COUNT(*) AS frequency FROM tb_word WHERE author = :author GROUP BY word ORDER BY frequency desc LIMIT 10000")
    public List<StringIntPair> getWordsByAuthor(String author);

    @Query("SELECT DISTINCT(word) AS word, COUNT(*) AS frequency FROM tb_word WHERE author = :author GROUP BY word ORDER BY frequency desc LIMIT 10")
    public List<StringIntPair> getTop10WordsByAuthor(String author);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT DISTINCT(word) AS word, COUNT(*) AS frequency, COUNT(DISTINCT(author)) AS authcount FROM tb_word tw WHERE LENGTH(word) > 1 " +
            "GROUP BY word " +
            "HAVING authcount > 1 " +
            "ORDER BY frequency desc LIMIT 100")
    public List<StringIntPair> getFreqWordListForQuiz();

    @Query("SELECT DISTINCT(word) AS word, COUNT(*) AS frequency FROM tb_word tw WHERE LENGTH(word) > 2 " +
            "GROUP BY word ORDER BY frequency desc LIMIT 100")
    public List<StringIntPair> getFreqWordListForQuiz2();

    @Query("SELECT DISTINCT(word) AS word, COUNT(*) AS frequency FROM tb_word WHERE instr(word, :word) GROUP BY word ORDER BY frequency desc LIMIT 10000")
    public List<StringIntPair> searchFreqWordList(String word);

    //get frequency of given word
    @Query("SELECT DISTINCT(word) AS word, COUNT(*) AS frequency FROM tb_word WHERE word = :searchTerm GROUP BY word ORDER BY frequency desc LIMIT 1")
    public StringIntPair getFreqWordListSearch(String searchTerm);

    //get user-frequency mapping of given word
    @Query("SELECT DISTINCT(author) AS word, COUNT(*) AS frequency FROM tb_word WHERE word = :searchTerm GROUP BY author ORDER BY frequency desc")
    public List<StringIntPair> getFreqWordListSearchByAuthor(String searchTerm);

    //get lines containing word
    @Query("SELECT * FROM tb_chat_line WHERE id IN (SELECT line_id FROM tb_word WHERE word = :word)")
    public List<ChatLineModel> getChatLinesContainingWord(String word);

    @Query("SELECT COUNT(*) FROM tb_word WHERE isPic = 1")
    public int getPicCount();

    @Query("SELECT COUNT(*) FROM tb_word WHERE isPic = 1 AND author = :author")
    public int getPicCountByAuthor(String author);

    @Query("SELECT author as word, count(word) as frequency FROM tb_word WHERE isPic = 1 GROUP BY author ORDER BY count(word) desc")
    public List<StringIntPair> getPicRanking();

    @Query("SELECT author as word, count(word) as frequency FROM tb_word WHERE isPic = 1 GROUP BY author ORDER BY count(word) desc LIMIT 10")
    public List<StringIntPair> getTop10ChattersByPic();

    @Query("SELECT author as word, count(word) as frequency FROM tb_word WHERE isPic = 1 GROUP BY author ORDER BY count(word) desc")
    public List<StringIntPair> getTopChattersByPic();

    @Query("SELECT COUNT(*) FROM tb_word WHERE isVideo = 1")
    public int getVideoCount();

    @Query("SELECT COUNT(*) FROM tb_word WHERE isVideo = 1 AND author = :author")
    public int getVideoCountByAuthor(String author);

    @Query("SELECT author as word, count(word) as frequency FROM tb_word WHERE isVideo = 1 GROUP BY author ORDER BY count(word) desc")
    public List<StringIntPair> getVideoRanking();

    @Query("SELECT author as word, count(word) as frequency FROM tb_word WHERE isVideo = 1 GROUP BY author ORDER BY count(word) desc LIMIT 10")
    public List<StringIntPair> getTop10ChattersByVideo();

    @Query("SELECT author as word, count(word) as frequency FROM tb_word WHERE isVideo = 1 GROUP BY author ORDER BY count(word) desc")
    public List<StringIntPair> getTopChattersByVideo();

    @Query("SELECT COUNT(*) FROM tb_word WHERE isLink = 1")
    public int getLinkCount();

    @Query("SELECT COUNT(*) FROM tb_word WHERE isLink = 1 AND author = :author")
    public int getLinkCountByAuthor(String author);

    @Query("SELECT author as word, count(word) as frequency FROM tb_word WHERE isLink = 1 GROUP BY author ORDER BY count(word) desc")
    public List<StringIntPair> getLinkRanking();

    @Query("SELECT author as word, count(word) as frequency FROM tb_word WHERE isLink = 1 GROUP BY author ORDER BY count(word) desc LIMIT 10")
    public List<StringIntPair> getTop10ChattersByLink();

    @Query("SELECT author as word, count(word) as frequency FROM tb_word WHERE isLink = 1 GROUP BY author ORDER BY count(word) desc")
    public List<StringIntPair> getTopChattersByLink();

    @Query("SELECT COUNT(*) FROM tb_word WHERE isPowerpoint = 1")
    public int getPowerpointCount();

    @Query("SELECT AVG(letterCount) FROM tb_word")
    public double getAverageLetterCount();

    @Query("SELECT AVG(letterCount) FROM tb_word WHERE author = :author")
    public double getAverageLetterCountByAuthor(String author);

    @Query("SELECT author as word, AVG(letterCount) as frequency FROM tb_word GROUP BY author ORDER BY AVG(letterCount) desc")
    public List<StringIntPair> getAverageLetterCountByRank();
}
