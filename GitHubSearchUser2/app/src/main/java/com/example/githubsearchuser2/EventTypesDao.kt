package com.example.githubsearchuser2

import androidx.room.*
import com.example.githubsearchuser2.GCheckListSearchDB

@Dao
interface EventTypesDao {
    @Query("SELECT * FROM like_user_tb ORDER BY login ASC")
    fun getEventTypes(): List<GCheckListSearchDB>

    @Query("SELECT * FROM like_user_tb WHERE login like :login ORDER BY login ASC")
    fun getEventTypeWithId(login: String): List<GCheckListSearchDB>

/*    @Query("SELECT id FROM like_user_tb WHERE title = :title COLLATE NOCASE")
    fun getEventTypeIdWithTitle(title: String): Long?

    @Query("SELECT * FROM like_user_tb WHERE caldav_calendar_id = :Id")
    fun getEventTypeWithId(Id: Int): GCheckListSearchDB?
*/
    @Query("DELETE FROM like_user_tb WHERE node_id = :node_id")
    fun deleteEventTypesWithGCheckListId(node_id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(eventType: GCheckListSearchDB): Long

    @Delete
    fun deleteEventTypes(eventTypes: List<GCheckListSearchDB>)
}
