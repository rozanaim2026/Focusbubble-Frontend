package com.focusbubble.ui.screens

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusbubble.R
import com.focusbubble.data.AppDatabase
import com.focusbubble.data.entities.UserEntity
import com.focusbubble.data.model.TokenIn
import com.focusbubble.data.model.UserCreate
import com.focusbubble.data.network.RetrofitClient
import com.focusbubble.ui.utils.UserSession
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onContinue: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val showBottomSheet = remember { mutableStateOf(false) }
    val isLoading = remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("GoogleSignIn", "🔵 Result received - Result Code: ${result.resultCode}, RESULT_OK: ${Activity.RESULT_OK}")

        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("GoogleSignIn", "✅ Result OK - Processing sign-in")
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val account = task.getResult(ApiException::class.java)
                Log.d("GoogleSignIn", "📧 Account Email: ${account?.email}")
                Log.d("GoogleSignIn", "👤 Account Name: ${account?.displayName}")

                val idToken = account?.idToken

                Log.d("GoogleSignIn", "🔑 ID Token: ${if (idToken != null) "Present (${idToken.take(20)}...)" else "NULL"}")

                val firebaseUser = FirebaseAuth.getInstance().currentUser
                val firebaseName = firebaseUser?.displayName ?: ""
                val finalName = if (firebaseName.isNotBlank()) firebaseName else (account?.displayName ?: "User")

                Log.d("GoogleSignIn", "👋 Final Name: $finalName")

                val sharedPrefs = context.getSharedPreferences("user_prefs", Activity.MODE_PRIVATE)
                sharedPrefs.edit().putString("profile_name", finalName).apply()
                // Send token to backend BEFORE navigating
                if (idToken != null) {
                    Log.d("GoogleSignIn", "📤 Sending token to backend...")
                    scope.launch {
                        try {
                            sendIdTokenToBackend(context, idToken)
                            Log.d("GoogleSignIn", "✅ Token sent successfully")
                            Toast.makeText(context, "✅ Welcome $finalName!", Toast.LENGTH_SHORT).show()
                            onContinue(finalName)
                        } catch (e: Exception) {
                            Log.e("GoogleSignIn", "❌ Failed to send token to backend", e)
                            Toast.makeText(context, "⚠️ Signed in but backend sync failed", Toast.LENGTH_LONG).show()
                            onContinue(finalName)
                        }
                    }
                } else {
                    Log.e("GoogleSignIn", "❌ ID Token is null - Cannot authenticate with backend")
                    Toast.makeText(context, "✅ Welcome $finalName!", Toast.LENGTH_SHORT).show()
                    onContinue(finalName)
                }

            } catch (e: ApiException) {
                Log.e("GoogleSignIn", "❌ ApiException - Status Code: ${e.statusCode}, Message: ${e.message}", e)
                Toast.makeText(context, "Sign-in failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                isLoading.value = false
                showBottomSheet.value = true
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "❌ Unexpected exception during sign-in", e)
                Toast.makeText(context, "Unexpected error: ${e.message}", Toast.LENGTH_LONG).show()
                isLoading.value = false
                showBottomSheet.value = true
            }

        } else {
            Log.w("GoogleSignIn", "⚠️ User cancelled sign-in or error occurred - Result Code: ${result.resultCode}")
            Toast.makeText(context, "Sign-in cancelled", Toast.LENGTH_SHORT).show()
            isLoading.value = false
            showBottomSheet.value = true
            // Do NOT call onContinue here — cancelling must not create or
            // log in any user. Staying on the "Choose how to continue" sheet
            // lets the person just try again.
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.welcome_background),
            contentDescription = "Welcome Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Welcome to\nFocus Bubble App",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Transform your study sessions with focused productivity",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { showBottomSheet.value = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "Get Started",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "By continuing, you agree to our Privacy Policy",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Bottom Sheet
    if (showBottomSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet.value = false },
            sheetState = bottomSheetState,
            containerColor = Color.Black,
            shape = RoundedCornerShape(topStart = 23.dp, topEnd = 23.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.3f))
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Welcome\nChoose how to continue",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Google Sign-In Button
                Button(
                    onClick = {
                        Log.d("GoogleSignIn", "🔵 Google Sign-In button clicked")
                        isLoading.value = true

                        val clientId = context.getString(R.string.server_client_id)
                        Log.d("GoogleSignIn", "🔑 Client ID: $clientId")

                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(clientId)
                            .requestEmail()
                            .build()

                        val client = GoogleSignIn.getClient(context, gso)
                        Log.d("GoogleSignIn", "📱 Signing out previous session...")

                        client.signOut().addOnCompleteListener {
                            Log.d("GoogleSignIn", "✅ Sign out complete")
                            client.revokeAccess().addOnCompleteListener {
                                Log.d("GoogleSignIn", "✅ Access revoked, launching sign-in intent...")
                                googleSignInLauncher.launch(client.signInIntent)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isLoading.value,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    if (isLoading.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Loading...",
                            fontSize = 14.sp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continue with Google",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Skip Button - CREATE USER ON BACKEND
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val guestEmail = UserSession.getOrCreateGuestEmail(context)
                                val response = RetrofitClient.api.createUser(
                                    UserCreate(
                                        email = guestEmail,
                                        name = "Guest"
                                    )
                                )

                                if (response.isSuccessful) {
                                    response.body()?.let { user ->
                                        UserSession.saveUser(context, user.id, user.email)
                                        AppDatabase.getInstance(context).userDao().upsertUser(
                                            UserEntity(
                                                id = user.id,
                                                email = user.email,
                                                name = user.name ?: "Guest",
                                                pictureUrl = user.picture
                                            )
                                        )
                                        Log.d("BackendAuth", "✅ Guest user ready: ID=${user.id}")
                                    }
                                } else {
                                    Log.e("BackendAuth", "Failed to create guest user: ${response.code()}")
                                }
                            } catch (e: Exception) {
                                Log.e("BackendAuth", "Error creating guest user", e)
                            }

                            val sharedPrefs = context.getSharedPreferences("user_prefs", Activity.MODE_PRIVATE)
                            sharedPrefs.edit().putString("profile_name", "Guest").apply()

                            showBottomSheet.value = false
                            onContinue("Guest")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isLoading.value,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4285F4),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Skip and Continue",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
// Updated: Sends ID token to backend using RetrofitClient
suspend fun sendIdTokenToBackend(context: Context, idToken: String) {
    withContext(Dispatchers.IO) {
        try {
            Log.d("BackendAuth", "🔵 Sending ID token to backend...")
            Log.d("BackendAuth", "🔑 Token preview: ${idToken.take(30)}...")

            val response = RetrofitClient.api.googleSignIn(
                TokenIn(idToken)
            )

            Log.d("BackendAuth", "📡 Response code: ${response.code()}")

            if (response.isSuccessful) {
                response.body()?.let { user ->
                    UserSession.saveUser(context, user.id, user.email)
                    // Local mirror of the signed-in user — this is the row that
                    // was missing entirely, which is why the Database Inspector
                    // showed no user data even after signing in. UserSession
                    // only ever cached id/email in SharedPreferences; nothing
                    // was writing a queryable row into Room.
                    AppDatabase.getInstance(context).userDao().upsertUser(
                        UserEntity(
                            id = user.id,
                            email = user.email,
                            name = user.name ?: "User",
                            pictureUrl = user.picture
                        )
                    )
                    Log.d("BackendAuth", "✅ Google user authenticated successfully!")
                    Log.d("BackendAuth", "👤 User ID: ${user.id}")
                    Log.d("BackendAuth", "📧 Email: ${user.email}")
                    Log.d("BackendAuth", "🏷️ Name: ${user.name}")
                } ?: run {
                    Log.e("BackendAuth", "❌ Response body is null")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("BackendAuth", "❌ Auth failed with code: ${response.code()}")
                Log.e("BackendAuth", "❌ Error body: $errorBody")
            }
        } catch (e: Exception) {
            Log.e("BackendAuth", "❌ Exception while sending token: ${e.javaClass.simpleName}", e)
            Log.e("BackendAuth", "❌ Error message: ${e.message}")
            throw e  // Re-throw to be caught by the caller
        }
    }
}