package com.tarlad.client.dao

import androidx.room.*
import com.tarlad.client.models.db.User
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface UserDao {

    @Query("SELECT * FROM user WHERE id == :id")
    fun getById(id: Long): User?

    @Query("SELECT user.* FROM user JOIN chatList ON user.id = chatList.user_id JOIN chat ON chat.id = chatList.chat_id WHERE chat.id == :id")
    fun getUserFromChat(id: Long): Observable<List<User>>

    fun getDistinctUserFromChat(id: Long): Observable<List<User>>
            = getUserFromChat(id).distinctUntilChanged()

    @Query("SELECT * FROM user WHERE id <> :id LIMIT 10 OFFSET (:page * 10)")
    fun getAll(id: Long, page: Int): List<User>

    @Query("SELECT * FROM user WHERE nickname LIKE :q || '%' AND id <> :id LIMIT 10 OFFSET (:page * 10)")
    fun getByNickname(q: String, id: Long, page: Int): List<User>



    @Query("SELECT * FROM user WHERE id NOT IN (SELECT user_id FROM chatlist WHERE chat_id = :id) LIMIT 10 OFFSET (:page * 10)")
    fun getAllForChat(id: Long, page: Int): List<User>

    @Query("SELECT * FROM user WHERE nickname LIKE :q || '%' AND id NOT IN (SELECT user_id FROM chatlist WHERE chat_id = :id) LIMIT 10 OFFSET (:page * 10)")
    fun getByNicknameForChat(q: String, id: Long, page: Int): List<User>





    @Query("SELECT * FROM user WHERE id <> :id")
    fun getAllObservable(id: Long): Observable<List<User>>

    @Query("SELECT * FROM user WHERE nickname LIKE :q || '%' AND id <> :id")
    fun getByNicknameObservable(q: String, id: Long): Observable<List<User>>

//    @Query("SELECT * FROM user WHERE id IN (:userIds)")
//    fun loadAllByIds(userIds: IntArray): List<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<User>)

    @Delete
    fun delete(user: User)

    @Delete
    fun deleteAll(users: List<User>)
}
