package com.focusbubble.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.focusbubble.data.dao.BlockedAppDao;
import com.focusbubble.data.entities.BlockedApp;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&\u00a8\u0006\u0005"}, d2 = {"Lcom/focusbubble/data/AppDatabase;", "Landroidx/room/RoomDatabase;", "()V", "blockedAppDao", "Lcom/focusbubble/data/dao/BlockedAppDao;", "app_debug"})
@androidx.room.Database(entities = {com.focusbubble.data.entities.BlockedApp.class}, version = 1)
public abstract class AppDatabase extends androidx.room.RoomDatabase {
    
    public AppDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.focusbubble.data.dao.BlockedAppDao blockedAppDao();
}