package com.app.wordpressnewsapps.database.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.app.wordpressnewsapps.model.PostEntity;

import java.util.List;

@Dao
public interface DAO {

    @Query("INSERT INTO tbl_favorite (saved_date, id, image, title, excerpt, category, date, content, comment_count, link) VALUES (:save_date, :id, :image, :title, :excerpt, :category, :date, :content, :comment_count, :link)")
    void addFavorite(long save_date, int id, String image, String title, String excerpt, String category, String date, String content, int comment_count, String link);

    @Query("DELETE FROM tbl_favorite WHERE id = :id")
    void deleteFavorite(int id);

    @Query("DELETE FROM tbl_favorite")
    void deleteAllFavorite();

    @Query("SELECT * FROM tbl_favorite ORDER BY saved_date DESC")
    List<PostEntity> getAllFavorite();

    @Query("SELECT COUNT(id) FROM tbl_favorite")
    Integer getAllFavoriteCount();

    @Query("SELECT * FROM tbl_favorite WHERE id = :id LIMIT 1")
    PostEntity getFavorite(int id);

}