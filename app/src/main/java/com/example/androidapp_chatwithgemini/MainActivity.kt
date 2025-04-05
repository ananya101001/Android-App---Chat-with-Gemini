package com.example.androidapp_chatwithgemini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Theme definition
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFF3700B3)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFF3700B3)
)

@Composable
fun AndroidAppChatWithGeminiTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}

class MainActivity : ComponentActivity() {
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = "AIzaSyA1sCjjS7l80ik4Sdg-KZ_DPAWzL4pU6Ck" // Replace with your actual API key
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidAppChatWithGeminiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatScreen(generativeModel = generativeModel)
                }
            }
        }
    }
}

@Composable
fun ChatScreen(generativeModel: GenerativeModel) {
    var messages by remember { mutableStateOf(emptyList<ChatMessage>()) }
    var userInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chat messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                ChatBubble(message = message)
            }
        }

        // Input field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type your message...") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (userInput.isNotBlank() && !isLoading) {
                            sendMessage(
                                userInput,
                                messages,
                                generativeModel,
                                onSend = { newMessages ->
                                    messages = newMessages
                                    userInput = ""
                                },
                                onLoadingChange = { isLoading = it },
                                coroutineScope = coroutineScope
                            )
                        }
                    }
                )
            )

            IconButton(
                onClick = {
                    if (userInput.isNotBlank() && !isLoading) {
                        sendMessage(
                            userInput,
                            messages,
                            generativeModel,
                            onSend = { newMessages ->
                                messages = newMessages
                                userInput = ""
                            },
                            onLoadingChange = { isLoading = it },
                            coroutineScope = coroutineScope
                        )
                    }
                },
                enabled = userInput.isNotBlank() && !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send message"
                )
            }
        }
    }
}

private fun sendMessage(
    message: String,
    currentMessages: List<ChatMessage>,
    generativeModel: GenerativeModel,
    onSend: (List<ChatMessage>) -> Unit,
    onLoadingChange: (Boolean) -> Unit,
    coroutineScope: CoroutineScope
) {
    val userMessage = ChatMessage(text = message, isFromUser = true)
    val updatedMessages = currentMessages + userMessage
    onSend(updatedMessages)
    onLoadingChange(true)

    coroutineScope.launch {
        try {
            val response = generativeModel.generateContent(message)
            val aiMessage = ChatMessage(
                text = response.text ?: "Sorry, I couldn't generate a response.",
                isFromUser = false
            )
            onSend(updatedMessages + aiMessage)
        } catch (e: Exception) {
            val errorMessage = ChatMessage(
                text = "Error: ${e.localizedMessage}",
                isFromUser = false
            )
            onSend(updatedMessages + errorMessage)
        } finally {
            onLoadingChange(false)
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (message.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(16.dp),
                color = if (message.isFromUser)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean
)

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    AndroidAppChatWithGeminiTheme {
        ChatScreen(
            generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = "test-key"
            )
        )
    }
}