package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.remote.supabase.SupabaseManager
import com.example.ui.components.HelpBottomSheet
import com.example.ui.components.HelpIconButton
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.MedotBackgroundLight
import com.example.ui.theme.MedotNavy
import com.example.ui.theme.MedotTeal
import kotlinx.coroutines.launch

enum class AuthMode {
    REGISTER,
    SIGN_IN
}

@Composable
fun AuthScreen(
    onAuthenticated: (name: String, email: String) -> Unit,
    modifier: Modifier = Modifier,
    seniorMode: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val supabaseManager = remember { SupabaseManager.getInstance(context) }

    var authMode by remember { mutableStateOf(AuthMode.REGISTER) }
    var showHelp by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Form inputs
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                HelpIconButton(onClick = { showHelp = true })
            }
        },
        containerColor = MedotBackgroundLight
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // medot Official Brand Logo
            Image(
                painter = painterResource(id = R.drawable.medot_logo),
                contentDescription = "medot - Never Miss Your Medication",
                modifier = Modifier
                    .fillMaxWidth(0.76f)
                    .height(160.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Auth Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(22.dp)) {

                    // Tab Switcher: Register / Sign In
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp)
                    ) {
                        // Register Tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(if (seniorMode) 54.dp else 46.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(
                                    if (authMode == AuthMode.REGISTER) DeepNavy else Color.Transparent
                                )
                                .clickable {
                                    authMode = AuthMode.REGISTER
                                    errorMessage = null
                                }
                                .testTag("tab_register"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Create Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = if (seniorMode) 17.sp else 15.sp,
                                color = if (authMode == AuthMode.REGISTER) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Sign In Tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(if (seniorMode) 54.dp else 46.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(
                                    if (authMode == AuthMode.SIGN_IN) DeepNavy else Color.Transparent
                                )
                                .clickable {
                                    authMode = AuthMode.SIGN_IN
                                    errorMessage = null
                                }
                                .testTag("tab_signin"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sign In",
                                fontWeight = FontWeight.Bold,
                                fontSize = if (seniorMode) 17.sp else 15.sp,
                                color = if (authMode == AuthMode.SIGN_IN) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = if (authMode == AuthMode.REGISTER) "Register to Start" else "Welcome Back",
                        fontSize = if (seniorMode) 24.sp else 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                    Text(
                        text = if (authMode == AuthMode.REGISTER)
                            "Create your personal profile to manage and protect your daily medication clock."
                        else
                            "Sign in with your email to access your medication schedule.",
                        fontSize = if (seniorMode) 16.sp else 14.sp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    // Form Fields
                    if (authMode == AuthMode.REGISTER) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                errorMessage = null
                            },
                            label = { Text("Full Name", fontSize = if (seniorMode) 18.sp else 16.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = AntiqueGold)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_name_field"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = { Text("Email Address", fontSize = if (seniorMode) 18.sp else 16.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Mail, contentDescription = null, tint = AntiqueGold)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_field"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Password", fontSize = if (seniorMode) 18.sp else 16.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = AntiqueGold)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_field"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    if (authMode == AuthMode.REGISTER) {
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                errorMessage = null
                            },
                            label = { Text("Confirm Password", fontSize = if (seniorMode) 18.sp else 16.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = AntiqueGold)
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_confirm_password_field"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Action Button (Min 56dp height)
                    Button(
                        onClick = {
                            if (isLoading) return@Button
                            if (authMode == AuthMode.REGISTER) {
                                if (name.isBlank()) {
                                    errorMessage = "Please enter your full name."
                                } else if (email.isBlank() || !email.contains("@") || !email.contains(".")) {
                                    errorMessage = "Please enter a valid email address."
                                } else if (password.length < 6) {
                                    errorMessage = "Password must be at least 6 characters."
                                } else if (password != confirmPassword) {
                                    errorMessage = "Passwords do not match."
                                } else {
                                    errorMessage = null
                                    if (supabaseManager.isConfigured) {
                                        isLoading = true
                                        coroutineScope.launch {
                                            val res = supabaseManager.signUp(email.trim(), password, name.trim())
                                            isLoading = false
                                            res.onSuccess {
                                                onAuthenticated(name.trim(), email.trim())
                                            }.onFailure { err ->
                                                val msg = err.message ?: "Registration failed."
                                                errorMessage = if (msg.contains("already registered", ignoreCase = true)) {
                                                    "This email is already registered. Please switch to the Sign In tab."
                                                } else {
                                                    "Supabase Error: $msg"
                                                }
                                            }
                                        }
                                    } else {
                                        onAuthenticated(name.trim(), email.trim())
                                    }
                                }
                            } else {
                                if (email.isBlank() || !email.contains("@")) {
                                    errorMessage = "Please enter a valid email address."
                                } else if (password.length < 6) {
                                    errorMessage = "Password must be at least 6 characters."
                                } else {
                                    errorMessage = null
                                    if (supabaseManager.isConfigured) {
                                        isLoading = true
                                        coroutineScope.launch {
                                            val res = supabaseManager.signIn(email.trim(), password)
                                            isLoading = false
                                            res.onSuccess { authResp ->
                                                val profileName = authResp.user?.userMetadata?.get("full_name") as? String
                                                    ?: email.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
                                                onAuthenticated(profileName, email.trim())
                                            }.onFailure { err ->
                                                errorMessage = "Supabase Auth: ${err.message ?: "Invalid email or password."}"
                                            }
                                        }
                                    } else {
                                        val fallbackName = email.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
                                        onAuthenticated(fallbackName, email.trim())
                                    }
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (seniorMode) 66.dp else 58.dp)
                            .testTag("auth_submit_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepNavy,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = if (authMode == AuthMode.REGISTER) "Create Account & Enter medot" else "Sign In & Enter medot",
                                fontSize = if (seniorMode) 19.sp else 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (errorMessage != null && supabaseManager.isConfigured) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                val resolvedName = if (name.isNotBlank()) name.trim() else email.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
                                onAuthenticated(resolvedName, email.trim())
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Continue Offline (Skip Cloud Auth)",
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Trust & Privacy Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = AntiqueGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Medical Privacy: All medication records stay on this device.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showHelp) {
        HelpBottomSheet(
            title = "Registration & Privacy",
            contentItems = listOf(
                "Why do I need to register?" to "Registering sets up your personalized medication profile so your daily alarm schedule and history are safely protected.",
                "Is my information secure?" to "Yes! Your medication names, dosages, and logs are kept private and secure directly on your device."
            ),
            onDismiss = { showHelp = false },
            seniorMode = seniorMode
        )
    }
}
