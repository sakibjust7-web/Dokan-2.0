package com.example.ui.screens.utilities

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ShopNote(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val isBangla = currentLanguage == AppLanguage.BANGLA
    val prefs = remember { context.getSharedPreferences("shop_notes_prefs", Context.MODE_PRIVATE) }

    var notes by remember {
        val raw = prefs.getString("notes_json", null)
        val list = mutableListOf<ShopNote>()
        if (!raw.isNullOrBlank()) {
            try {
                val array = JSONArray(raw)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        ShopNote(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            content = obj.getString("content"),
                            timestamp = obj.getLong("timestamp")
                        )
                    )
                }
            } catch (e: Exception) {}
        }
        if (list.isEmpty()) {
            list.add(
                ShopNote(
                    id = "note_1",
                    title = if (isBangla) "দোকানের জরুরি নোট" else "Store Memo",
                    content = if (isBangla) "প্রতি শুক্রবার মহাজনদের তালিকা যাচাই করে দেনা পরিশোধ করা।" else "Review supplier payables every Friday.",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        mutableStateOf(list.toList())
    }

    fun saveNotes(newList: List<ShopNote>) {
        notes = newList
        val array = JSONArray()
        newList.forEach { n ->
            array.put(
                JSONObject().apply {
                    put("id", n.id)
                    put("title", n.title)
                    put("content", n.content)
                    put("timestamp", n.timestamp)
                }
            )
        }
        prefs.edit().putString("notes_json", array.toString()).apply()
    }

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBangla) "দোকানের খাতা ও নোটবুক" else "Store Notebook",
                        style = AppTypographyScale.screenTitle,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSlateSurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = VividEmeraldPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        },
        containerColor = DarkSlateBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notes) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
                        border = BorderStroke(1.dp, DarkSlateCardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = note.title,
                                    style = AppTypographyScale.normalText.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                IconButton(
                                    onClick = {
                                        saveNotes(notes.filterNot { it.id == note.id })
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted)
                                }
                            }
                            Text(text = note.content, style = AppTypographyScale.normalText, color = TextPrimary)
                            Text(text = dateFormat.format(Date(note.timestamp)), style = AppTypographyScale.hintText, color = TextMuted)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = DarkSlateSurface,
            title = {
                Text(
                    text = if (isBangla) "নতুন নোট লিখুন" else "Add Note",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (isBangla) "নোটের শিরোনাম *" else "Title *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text(if (isBangla) "বিস্তারিত বিবরণ" else "Content") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val newNote = ShopNote(
                                id = "note_${System.currentTimeMillis()}",
                                title = title.trim(),
                                content = content.trim(),
                                timestamp = System.currentTimeMillis()
                            )
                            saveNotes(listOf(newNote) + notes)
                            showAddDialog = false
                        }
                    },
                    enabled = title.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = VividEmeraldPrimary)
                ) {
                    Text(if (isBangla) "সেভ করুন" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel", color = TextMuted)
                }
            }
        )
    }
}
