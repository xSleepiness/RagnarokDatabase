package com.example.ragnarokdatabase.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.ragnarokdatabase.viewmodel.FilterUiState
import com.example.ragnarokdatabase.viewmodel.FilterViewModel
import com.example.ragnarokdatabase.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilteredItemsScreen(
    itemType: String,
    onItemClick: (Int) -> Unit = {},
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onTypeFilterClick: (String) -> Unit = {},
    viewModel: FilterViewModel = viewModel(),
    mainViewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val totalItems by viewModel.totalItems.collectAsState()
    val itemTypes by mainViewModel.itemTypes.collectAsState()

    LaunchedEffect(itemType) {
        viewModel.loadItemsByType(itemType)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Ragnarok Database",
                showBackButton = true,
                onBackClick = onBackClick,
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Type info
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = itemType.uppercase(),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = LilitaOneFont
                        ),
                        color = Amber400
                    )
                    if (totalItems > 0) {
                        Text(
                            text = "$totalItems items found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

            // Content
            when (val state = uiState) {
                is FilterUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Amber400
                        )
                    }
                }
                is FilterUiState.Success -> {
                    val listState = rememberLazyListState()

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.items) { item ->
                            FilteredItemCard(
                                item = item,
                                onItemClick = { onItemClick(item.id) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pagination controls
                    PaginationControls(
                        currentPage = currentPage,
                        totalPages = state.totalPages,
                        onPreviousClick = { viewModel.loadPreviousPage() },
                        onNextClick = { viewModel.loadNextPage() },
                        onPageClick = { page -> viewModel.goToPage(page) }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
                is FilterUiState.Empty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Slate400,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is FilterUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
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
fun FilteredItemCard(
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
                Text(
                    text = item.name,
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

@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onPageClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous button
        IconButton(
            onClick = onPreviousClick,
            enabled = currentPage > 0
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous",
                tint = if (currentPage > 0) Amber400 else Slate600
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Page numbers
        val startPage = maxOf(0, currentPage - 2)
        val endPage = minOf(totalPages - 1, currentPage + 2)

        if (startPage > 0) {
            PageButton(
                page = 0,
                isSelected = false,
                onClick = { onPageClick(0) }
            )
            if (startPage > 1) {
                Text(
                    text = "...",
                    color = Slate500,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        for (page in startPage..endPage) {
            PageButton(
                page = page,
                isSelected = page == currentPage,
                onClick = { onPageClick(page) }
            )
        }

        if (endPage < totalPages - 1) {
            if (endPage < totalPages - 2) {
                Text(
                    text = "...",
                    color = Slate500,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            PageButton(
                page = totalPages - 1,
                isSelected = false,
                onClick = { onPageClick(totalPages - 1) }
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Next button
        IconButton(
            onClick = onNextClick,
            enabled = currentPage < totalPages - 1
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next",
                tint = if (currentPage < totalPages - 1) Amber400 else Slate600
            )
        }
    }
}

@Composable
fun PageButton(
    page: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Amber400 else Slate800,
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "${page + 1}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) Slate950 else Slate300
            )
        }
    }
}

