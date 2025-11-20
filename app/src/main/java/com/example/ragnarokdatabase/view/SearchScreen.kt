package com.example.ragnarokdatabase.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ragnarokdatabase.model.Item
import com.example.ragnarokdatabase.ui.theme.*
import com.example.ragnarokdatabase.view.components.AppTopBar
import com.example.ragnarokdatabase.viewmodel.MainViewModel
import com.example.ragnarokdatabase.viewmodel.SearchUiState
import com.example.ragnarokdatabase.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    initialQuery: String = "",
    onNavigateBack: () -> Unit,
    onHomeClick: () -> Unit,
    onTypeFilterClick: (String) -> Unit,
    onItemClick: (Int) -> Unit,
    viewModel: SearchViewModel,
    mainViewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val itemTypes by mainViewModel.itemTypes.collectAsState()

    // Set initial query and trigger search if provided (only once on first composition)
    LaunchedEffect(Unit) {
        if (initialQuery.isNotEmpty() && searchQuery.isEmpty()) {
            viewModel.onSearchQueryChanged(initialQuery)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Ragnarok Database",
                showBackButton = true,
                onBackClick = onNavigateBack,
                onHomeClick = onHomeClick,
                itemTypes = itemTypes,
                onTypeSelected = onTypeFilterClick
            )
        },
        containerColor = Slate950
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Slate900,
                            Slate950
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                placeholder = {
                    Text(
                        text = "Search by ID or name...",
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
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearSearch() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Slate400
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Slate100,
                    unfocusedTextColor = Slate200,
                    focusedBorderColor = Amber400,
                    unfocusedBorderColor = Slate600,
                    cursorColor = Amber400
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            // Search Results
            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    SearchIdleState()
                }
                is SearchUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Amber400)
                    }
                }
                is SearchUiState.Empty -> {
                    SearchEmptyState()
                }
                is SearchUiState.NotFound -> {
                    SearchNotFoundState()
                }
                is SearchUiState.Success -> {
                    SearchResultsList(
                        items = state.items,
                        onItemClick = onItemClick
                    )
                }
                is SearchUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ErrorCard(message = state.message)
                    }
                }
            }
        }
        }
    }
}

@Composable
fun SearchIdleState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Slate600,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Search for items",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = LilitaOneFont
                ),
                color = Slate400,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter an item ID or name to start searching",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate500,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SearchEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No results found",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = LilitaOneFont
                ),
                color = Slate400,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Try searching with different keywords",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate500,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SearchNotFoundState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Item not found",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = LilitaOneFont
                ),
                color = Slate400,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "The item you're looking for doesn't exist",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate500,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SearchResultsList(
    items: List<Item>,
    onItemClick: (Int) -> Unit
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "${items.size} result${if (items.size != 1) "s" else ""} found",
                style = MaterialTheme.typography.labelLarge,
                color = Slate400,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(items) { item ->
            SearchResultCard(
                item = item,
                onItemClick = { onItemClick(item.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SearchResultCard(
    item: Item,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
                .fillMaxWidth()
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
                val displayName = if (item.stats.slots > 0) {
                    "${item.name} [${item.stats.slots}]"
                } else {
                    item.name
                }

                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Slate100,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.type,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                    item.subtype?.let { subtype ->
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                        Text(
                            text = subtype,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "ID: ${item.id}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
            }
        }
    }
}

