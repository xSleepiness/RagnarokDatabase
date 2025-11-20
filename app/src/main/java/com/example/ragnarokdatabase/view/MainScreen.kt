package com.example.ragnarokdatabase.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ragnarokdatabase.R
import com.example.ragnarokdatabase.model.PopularItem
import com.example.ragnarokdatabase.ui.theme.*
import com.example.ragnarokdatabase.viewmodel.MainUiState
import com.example.ragnarokdatabase.viewmodel.MainViewModel

val LilitaOneFont = FontFamily(Font(R.font.lilitaone_regular))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onItemClick: (Int) -> Unit = {},
    onSearchClick: (String) -> Unit = {},
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val totalItemsCount by viewModel.totalItemsCount.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Slate900,
                        Slate950
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(60.dp))

                // Header
                Text(
                    text = "RAGNAROK",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = LilitaOneFont
                    ),
                    color = Amber400,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "DATABASE",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = LilitaOneFont
                    ),
                    color = Slate200,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = {
                        Text(
                            text = "Search items, monsters...",
                            color = Slate500
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Amber400
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate200,
                        focusedBorderColor = Amber400,
                        unfocusedBorderColor = Slate600,
                        cursorColor = Amber400
                    ),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (searchQuery.isNotEmpty()) {
                                focusManager.clearFocus()
                                val query = searchQuery
                                searchQuery = "" // Reset after capturing
                                onSearchClick(query)
                            }
                        }
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Total Items Count Card
                totalItemsCount?.let { count ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Slate800,
                        tonalElevation = 2.dp
                    ) {
                        Text(
                            text = "There exist $count items in the database.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate200,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Popular Items Section
                Text(
                    text = "POPULAR ITEMS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = LilitaOneFont
                    ),
                    color = Slate200,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Period Filter Chips
                PeriodFilterChips(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { viewModel.loadPopularItems(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Popular Items List
            when (val state = uiState) {
                is MainUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Amber400
                            )
                        }
                    }
                }
                is MainUiState.Success -> {
                    items(state.items) { item ->
                        PopularItemCard(
                            item = item,
                            onItemClick = { onItemClick(item.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                is MainUiState.Error -> {
                    item {
                        ErrorCard(message = state.message)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun PeriodFilterChips(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit
) {
    val periods = listOf(
        "today" to "Today",
        "yesterday" to "Yesterday",
        "last7days" to "Last 7 Days",
        "last30days" to "Last 30 Days"
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(
            count = periods.size,
            key = { index -> periods[index].first }
        ) { index ->
            val (key, label) = periods[index]
            FilterChip(
                selected = selectedPeriod == key,
                onClick = { onPeriodSelected(key) },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Amber400,
                    selectedLabelColor = Slate950,
                    containerColor = Slate800,
                    labelColor = Slate300
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedPeriod == key,
                    selectedBorderColor = Amber400,
                    borderColor = Slate600,
                    borderWidth = 1.dp
                )
            )
        }
    }
}

@Composable
fun PopularItemCard(item: PopularItem, onItemClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onItemClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Slate800
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item Icon
            AsyncImage(
                model = item.getIconUrl(),
                contentDescription = item.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate700),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Item Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Slate100,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.type,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
            }

            // View Count Badge
            Surface(
                shape = CircleShape,
                color = Amber400.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "${item.viewCount} views",
                    style = MaterialTheme.typography.labelSmall,
                    color = Amber400,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun ErrorCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Red900.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = LilitaOneFont
                ),
                color = Red400
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Slate300,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    RagnarokDatabaseTheme {
        MainScreen()
    }
}

