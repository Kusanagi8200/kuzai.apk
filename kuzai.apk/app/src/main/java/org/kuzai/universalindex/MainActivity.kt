package org.kuzai.universalindex

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.BufferedReader

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = androidx.compose.ui.graphics.Color(0xFF00E5FF),
                    secondary = androidx.compose.ui.graphics.Color(0xFF7A3CFF),
                    background = androidx.compose.ui.graphics.Color(0xFF0B0F1A),
                    surface = androidx.compose.ui.graphics.Color(0xFF12172A),
                    onBackground = androidx.compose.ui.graphics.Color(0xFFE6F1FF),
                    onSurface = androidx.compose.ui.graphics.Color(0xFFE6F1FF),
                )
            ) {
                KuzAIApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KuzAIApp() {
    val ctx = LocalContext.current
    val tools = remember { loadTools(ctx) }
    val categories = remember { loadCategories(ctx, tools) }
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Toutes catégories") }

    val filtered by remember(query, category, tools) {
        mutableStateOf(
            tools.filter {
                (query.isBlank() || it.NAME.contains(query, ignoreCase = true)) &&
                        (category == "Toutes catégories" || it.CATEGORY.equals(category, ignoreCase = true))
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.kuzai_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f)
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    buildAnnotatedString {
                                        withStyle(
                                            style = SpanStyle(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(
                                                        androidx.compose.ui.graphics.Color(0xFF00E5FF),
                                                        androidx.compose.ui.graphics.Color.White,
                                                        androidx.compose.ui.graphics.Color(0xFF7A3CFF)
                                                    )
                                                ),
                                                fontWeight = FontWeight.Bold
                                            )
                                        ) {
                                            append("KUZAI - UNIVERSAL AI INDEX")
                                        }
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    "BETA-0.01.25",
                                    color = androidx.compose.ui.graphics.Color.White,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    )
                },
                containerColor = androidx.compose.ui.graphics.Color.Transparent
            ) { padding ->
                Column(Modifier.padding(padding).padding(12.dp)) {
                    // 🔎 Champ recherche par nom
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Recherche par nom") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(onSearch = {}),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    // ⬇️ Sélecteur de catégories
                    CategorySelector(categories, category, onChange = { category = it })

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "RÉSULTATS : ${filtered.size}",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = androidx.compose.ui.graphics.Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filtered) { tool ->
                            ToolCard(tool)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    categories: List<String>,
    selected: String,
    onChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val all = remember(categories) { listOf("Toutes catégories") + categories.distinct() }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selected,
            onValueChange = {},
            label = { Text("Catégorie") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            all.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = { onChange(it); expanded = false }
                )
            }
        }
    }
}

@Composable
fun ToolCard(tool: Tool) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(
                tool.NAME,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(tool.CATEGORY, style = MaterialTheme.typography.labelMedium)
            if (tool.DESCRIPTION.isNotBlank()) {
                Text(tool.DESCRIPTION, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text(tool.LINK, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Serializable
data class Tool(
    val NAME: String = "",
    val CATEGORY: String = "",
    val DESCRIPTION: String = "",
    val LINK: String = "",
    val PUBLISHER: String = "",
    val LICENCE: String = "",
    val API: String = "",
    val BUSINESSMODEL: String? = null,
    val ORIGIN: String = ""
)

fun loadTools(context: Context): List<Tool> = try {
    val jsonStr = context.assets.open("tools.json")
        .bufferedReader()
        .use(BufferedReader::readText)
    Json { ignoreUnknownKeys = true }.decodeFromString<List<Tool>>(jsonStr)
} catch (e: Exception) {
    emptyList()
}

fun loadCategories(context: Context, tools: List<Tool>): List<String> {
    val fromFile: List<String> = try {
        val raw = context.assets.open("search-engine.txt")
            .bufferedReader()
            .use(BufferedReader::readText)
        raw.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
    } catch (_: Exception) {
        emptyList()
    }
    return if (fromFile.isNotEmpty()) fromFile else tools.map { it.CATEGORY }.toSet().sorted()
}
