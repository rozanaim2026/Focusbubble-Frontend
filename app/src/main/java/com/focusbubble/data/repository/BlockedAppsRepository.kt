package com.focusbubble.data.repository

import com.focusbubble.data.dao.BlockedAppDao
import com.focusbubble.data.entities.BlockedApp
import com.focusbubble.data.model.BlockedAppCreate
import com.focusbubble.data.model.BlockedAppResponse
import com.focusbubble.data.model.RefreshBlocksResponse
import com.focusbubble.data.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockedAppsRepository @Inject constructor(
    private val dao: BlockedAppDao
) {
    private val api = RetrofitClient.api

    // Local database flow
    // Local database flow — now takes a userId; see BlockedAppsViewModel for
    // how this stays reactive as the logged-in user changes.
    fun blockedAppsForUser(userId: Int): Flow<List<BlockedApp>> = dao.getAllBlockedApps(userId)

    suspend fun clearAllForUser(userId: Int) {
        dao.clearAllForUser(userId)
    }
    // ========== LOCAL DATABASE OPERATIONS ==========

    suspend fun addBlockedApp(app: BlockedApp) {
        // Save locally
        dao.insertBlockedApp(app)

        // Sync to backend (optional - if you want to create individual blocks)
        withContext(Dispatchers.IO) {
            try {
                // Note: Your backend expects userId, so you'll need to pass it
                // For now, this is commented out - use createBlocksOnBackend instead
                // RetrofitInstance.api.addBlockedApp(app)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteBlockedApp(app: BlockedApp) {
        dao.deleteBlockedApp(app)
        withContext(Dispatchers.IO) {
            try {
                // Backend doesn't have individual delete - blocks expire automatically
                // Or you can stop the session to deactivate all blocks
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ========== BACKEND API OPERATIONS ==========

    /**
     * Get active blocked apps from backend for a specific user
     */
    suspend fun getActiveBlocksFromBackend(userId: Int): Response<List<BlockedAppResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                api.getActiveBlocks(userId)
            } catch (e: Exception) {
                e.printStackTrace()
                Response.error(500, okhttp3.ResponseBody.create(null, ""))
            }
        }
    }

    /**
     * Create blocked apps on backend (when starting a session)
     */
    suspend fun createBlocksOnBackend(
        userId: Int,
        blocks: List<BlockedAppCreate>
    ): Response<List<BlockedAppResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                api.createBlocks(userId, blocks)
            } catch (e: Exception) {
                e.printStackTrace()
                Response.error(500, okhttp3.ResponseBody.create(null, ""))
            }
        }
    }

    /**
     * Manually trigger backend to expire old blocks
     */
    suspend fun refreshBlocksOnBackend(): Response<RefreshBlocksResponse> {
        return withContext(Dispatchers.IO) {
            try {
                api.refreshBlocks()
            } catch (e: Exception) {
                e.printStackTrace()
                Response.error(500, okhttp3.ResponseBody.create(null, ""))
            }
        }
    }

    /**
     * Sync backend blocks to local database
     * Call this after starting a session to get the blocks created by backend
     */
    suspend fun syncFromBackend(userId: Int) {
        withContext(Dispatchers.IO) {
            try {
                android.util.Log.d(
                    "BlocksSync",
                    "🔄 Syncing blocks for user $userId"
                )

                val response = getActiveBlocksFromBackend(userId)

                android.util.Log.d(
                    "BlocksSync",
                    "📡 Response: ${response.code()}"
                )

                if (response.isSuccessful) {
                    response.body()?.let { backendBlocks ->
                        android.util.Log.d(
                            "BlocksSync",
                            "✅ Got ${backendBlocks.size} blocks from backend"
                        )

                        val localBlocks = backendBlocks.map { backendBlock ->
                            BlockedApp(
                                userId = userId,
                                packageName = backendBlock.packageName,
                                appName = backendBlock.appName
                                    ?: backendBlock.packageName,
                                durationMinutes = 25,
                                id = backendBlock.id
                            )
                        }

                        dao.replaceAllForUser(userId, localBlocks)
                    }
                } else {
                    android.util.Log.e(
                        "BlocksSync",
                        "❌ Failed: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e(
                    "BlocksSync",
                    "❌ Error syncing blocks",
                    e
                )
            }
        }
    }

    /**
     * Legacy method - kept for backward compatibility
     */
    suspend fun refreshFromBackend() {
        // This can now call syncFromBackend with a userId
        // You'll need to store userId somewhere (SharedPreferences, etc.)
    }

    suspend fun replaceAllForUser(userId: Int, apps: List<BlockedApp>) {
        dao.replaceAllForUser(userId, apps)
    }
}