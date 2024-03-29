package com.rexyrex.kakaoparser.Database.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;
import androidx.room.Update;

import com.rexyrex.kakaoparser.Database.Converters.DateConverter;
import com.rexyrex.kakaoparser.Database.Models.ChatLineModel;
import com.rexyrex.kakaoparser.Entities.DateIntPair;
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
    @Query("SELECT author as word, count(author) as frequency FROM tb_chat_line GROUP BY author ORDER BY count(author) desc LIMIT 10000")
    public List<StringIntPair> getChatterFrequencyPairs();

    //get top 10 most freq chatters
    @Query("SELECT author as word, count(author) as frequency FROM tb_chat_line GROUP BY author ORDER BY count(author) desc LIMIT 10")
    public List<StringIntPair> getTop10Chatters();

    @Query("SELECT COUNT(content) FROM tb_chat_line WHERE author = :author")
    public int getChatterChatLineCount(String author);

    @Query("SELECT author as word, COUNT(content) as frequency FROM tb_chat_line GROUP BY author ORDER BY COUNT(content) desc")
    public List<StringIntPair> getChatterChatLineByRank();

    @Query("SELECT * FROM tb_chat_line WHERE author = :author AND LENGTH(content)>2 ORDER BY RANDOM() LIMIT 1")
    public ChatLineModel getChatterRandomChatlineSample(String author);

    @Query("SELECT * FROM tb_chat_line WHERE author = :author ORDER BY RANDOM() LIMIT 1")
    public ChatLineModel getChatterRandomChatlineSampleShort(String author);

    @Query("SELECT * FROM tb_chat_line WHERE author = :author ORDER BY RANDOM() LIMIT 5")
    public List<ChatLineModel> getChatterRandomChatlineSamples(String author);

    @Query("SELECT * FROM tb_chat_line WHERE author != :author AND content != :content AND LENGTH(content) > 10 ORDER BY RANDOM() LIMIT 4")
    public List<ChatLineModel> getOtherRandomChatlineSamples(String author, String content);

    @Query("SELECT MIN(date) FROM tb_chat_line")
    public Date getStartDate();

    @Query("SELECT Max(date) FROM tb_chat_line")
    public Date getEndDate();

    @Query("SELECT * FROM tb_chat_line WHERE instr(content, :word)")
    public List<ChatLineModel> getChatLinesContainingWord(String word);

    @Query("SELECT * FROM tb_chat_line WHERE id < :id+30 AND id > :id-30")
    public List<ChatLineModel> getSurroundingChatLines(int id);

    @Query("SELECT AVG(wordCount) FROM tb_chat_line")
    public double getAverageWordCount();

    @Query("SELECT AVG(wordCount) FROM tb_chat_line WHERE author = :author")
    public double getAverageWordCountByAuthor(String author);

    @Query("SELECT author as word, AVG(wordCount) as frequency FROM tb_chat_line group by author ORDER BY AVG(wordCount) DESC")
    public List<StringIntPair> getAverageWordCountRanking();

    @Query("SELECT COUNT(DISTINCT(dateDayString)) FROM tb_chat_line WHERE author = :author")
    public int getDaysActiveByAuthor(String author);

    @Query("SELECT AVG(count) FROM (SELECT COUNT(DISTINCT(dateDayString)) as count FROM tb_chat_line GROUP BY author)")
    public int getDaysActiveAverage();

    @Query("SELECT author as word, COUNT(DISTINCT(dateDayString)) as frequency FROM tb_chat_line GROUP BY author ORDER BY COUNT(DISTINCT(dateDayString)) desc")
    public List<StringIntPair> getDaysActiveRank();

    //get frequency by day
    @Query("SELECT COUNT(*) as frequency, date as date FROM tb_chat_line GROUP BY dateDayString ORDER BY date asc")
    public List<DateIntPair> getFreqByDay();

    //get frequency by day filter by author
    @Query("SELECT COUNT(*) as frequency, date as date FROM tb_chat_line WHERE author = :author GROUP BY dateDayString ORDER BY date asc")
    public List<DateIntPair> getFreqByDayByAuthor(String author);

    //get frequency by month
    @Query("SELECT COUNT(*) as frequency, dateMonthString as word FROM tb_chat_line GROUP BY dateMonthString ORDER BY date asc")
    public List<StringIntPair> getFreqByMonth();

    //get frequency by month filter by author
    @Query("SELECT COUNT(*) as frequency, dateMonthString as word FROM tb_chat_line WHERE author = :author GROUP BY dateMonthString ORDER BY date asc")
    public List<StringIntPair> getFreqByMonthByAuthor(String author);

    //get frequency by year
    @Query("SELECT COUNT(*) as frequency, dateYearString as word FROM tb_chat_line GROUP BY dateYearString ORDER BY date asc")
    public List<StringIntPair> getFreqByYear();

    //get frequency by year  filter by author
    @Query("SELECT COUNT(*) as frequency, dateYearString as word FROM tb_chat_line WHERE author = :author GROUP BY dateYearString ORDER BY date asc")
    public List<StringIntPair> getFreqByYearByAuthor(String author);

    //get frequency by time of day
    @Query("SELECT COUNT(*) as frequency, dateHourOfDayString as word FROM tb_chat_line GROUP BY dateHourOfDayString ORDER BY dateHourOfDayString")
    public List<StringIntPair> getFreqByTimeOfDay();

    //get frequency by time of day filter by author
    @Query("SELECT COUNT(*) as frequency, dateHourOfDayString as word FROM tb_chat_line WHERE author = :author GROUP BY dateHourOfDayString ORDER BY dateHourOfDayString")
    public List<StringIntPair> getFreqByTimeOfDayByAuthor(String author);

    //get frequency by day of week
    @Query("SELECT COUNT(*) as frequency, dateDayOfWeekString as word FROM tb_chat_line GROUP BY dateDayOfWeekString")
    public List<StringIntPair> getFreqByDayOfWeek();

    //get frequency by day of week filter by author
    @Query("SELECT COUNT(*) as frequency, dateDayOfWeekString as word FROM tb_chat_line WHERE author = :author GROUP BY dateDayOfWeekString")
    public List<StringIntPair> getFreqByDayOfWeekByAuthor(String author);

    //get max frequency by dayOfWeek
    @Query("SELECT COUNT(*) as frequency FROM tb_chat_line GROUP BY dateDayOfWeekString ORDER BY frequency desc LIMIT 1")
    public int getMaxFreqDayOfWeek();

    //get deleted msgs count
    @Query("SELECT COUNT(*) FROM tb_chat_line WHERE content = '삭제된 메시지입니다.'")
    public int getDeletedMsgCount();

    //get deleted msgs count
    @Query("SELECT COUNT(*) FROM tb_chat_line WHERE content = '삭제된 메시지입니다.' AND author = :author")
    public int getDeletedMsgCountByAuthor(String author);

    //get del chatters ranking
    @Query("SELECT author as word, count(author) as frequency FROM tb_chat_line WHERE content = '삭제된 메시지입니다.' GROUP BY author ORDER BY count(author) desc")
    public List<StringIntPair> getDeletedMsgRanking();

    //get top 10 most freq del chatters
    @Query("SELECT author as word, count(author) as frequency FROM tb_chat_line WHERE content = '삭제된 메시지입니다.' GROUP BY author ORDER BY count(author) desc LIMIT 10")
    public List<StringIntPair> getTop10ChattersByDeletedMsg();

    //get top 10 most freq del chatters
    @Query("SELECT author as word, count(author) as frequency FROM tb_chat_line WHERE content = '삭제된 메시지입니다.' GROUP BY author ORDER BY count(author) desc")
    public List<StringIntPair> getTopChattersByDeletedMsg();

    //get chats by date desc
    @Query("SELECT * FROM tb_chat_line ORDER BY date DESC LIMIT 10000")
    public List<ChatLineModel> getAllChatsByDateDesc();

    //get chats by date asc
    @Query("SELECT * FROM tb_chat_line ORDER BY date ASC LIMIT 10000")
    public List<ChatLineModel> getAllChatsByDateAsc();

    //get chats by length desc
    @Query("SELECT * FROM tb_chat_line ORDER BY length DESC LIMIT 10000")
    public List<ChatLineModel> getAllChatsByLengthDesc();

    //get chats by length asc
    @Query("SELECT * FROM tb_chat_line ORDER BY length ASC LIMIT 10000")
    public List<ChatLineModel> getAllChatsByLengthAsc();

    //get chats by date desc (Filter Author)
    @Query("SELECT * FROM tb_chat_line WHERE author = :author ORDER BY date DESC LIMIT 10000")
    public List<ChatLineModel> getAllChatsByDateDescFilterAuthor(String author);

    //get chats by date asc (Filter Author)
    @Query("SELECT * FROM tb_chat_line WHERE author = :author ORDER BY date ASC LIMIT 10000")
    public List<ChatLineModel> getAllChatsByDateAscFilterAuthor(String author);

    //get chats by WordCount desc (Filter Author)
    @Query("SELECT * FROM tb_chat_line WHERE author = :author ORDER BY length DESC LIMIT 10000")
    public List<ChatLineModel> getAllChatsByLengthDescFilterAuthor(String author);

    //get chats by WordCount asc (Filter Author)
    @Query("SELECT * FROM tb_chat_line WHERE author = :author ORDER BY length ASC LIMIT 10000")
    public List<ChatLineModel> getAllChatsByLengthAscFilterAuthor(String author);

    //get chats by date asc (Filter Chat)
    @Query("SELECT * FROM tb_chat_line WHERE instr(content, :search) ORDER BY date ASC LIMIT 10000")
    public List<ChatLineModel> getAllChatsByDateAscFilterChat(String search);

    //get chats by date desc (Filter Chat)
    @Query("SELECT * FROM tb_chat_line WHERE instr(content, :search) ORDER BY date DESC LIMIT 10000")
    public List<ChatLineModel> getAllChatsByDateDescFilterChat(String search);

    //get chats by length asc (Filter Chat)
    @Query("SELECT * FROM tb_chat_line WHERE instr(content, :search) ORDER BY length ASC LIMIT 10000")
    public List<ChatLineModel> getAllChatsByLengthAscFilterChat(String search);

    //get chats by length desc (Filter Chat)
    @Query("SELECT * FROM tb_chat_line WHERE instr(content, :search) ORDER BY length DESC LIMIT 10000")
    public List<ChatLineModel> getAllChatsByLengthDescFilterChat(String search);

    //get chats by length asc (Filter Chat, Filter Author)
    @Query("SELECT * FROM tb_chat_line WHERE instr(content, :search) AND author = :author ORDER BY length ASC LIMIT 10000")
    public List<ChatLineModel> getAllChatsByLengthAscFilterChatFilterAuthor(String search, String author);

    //get chats by length desc (Filter Chat, Filter Author)
    @Query("SELECT * FROM tb_chat_line WHERE instr(content, :search) AND author = :author ORDER BY length DESC LIMIT 10000")
    public List<ChatLineModel> getAllChatsByLengthDescFilterChatFilterAuthor(String search, String author);

    //get chats by date asc (Filter Chat, Filter Author)
    @Query("SELECT * FROM tb_chat_line WHERE instr(content, :search) AND author = :author ORDER BY date ASC LIMIT 10000")
    public List<ChatLineModel> getAllChatsByDateAscFilterChatFilterAuthor(String search, String author);

    //get chats by date desc (Filter Chat, Filter Author)
    @Query("SELECT * FROM tb_chat_line WHERE instr(content, :search) AND author = :author ORDER BY date DESC LIMIT 10000")
    public List<ChatLineModel> getAllChatsByDateDescFilterChatFilterAuthor(String search, String author);

}
