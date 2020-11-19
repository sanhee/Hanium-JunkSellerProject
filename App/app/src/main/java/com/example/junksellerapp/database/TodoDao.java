package com.example.junksellerapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao

//Todo라는 테이블의 내용이 여러개 있을 것이기 때문에 리스트로 받음.
public interface TodoDao{
    @Query("SELECT * FROM Todo")
    List<Todo> getAll();

    @Insert
    void insert(Todo todo);

    @Update
    void update(Todo todo);

    @Query("DELETE FROM Todo")
    void delete();
}
