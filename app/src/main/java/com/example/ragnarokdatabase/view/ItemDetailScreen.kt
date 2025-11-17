package com.example.ragnarokdatabase.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ragnarokdatabase.model.Item
import com.example.ragnarokdatabase.ui.theme.*
import com.example.ragnarokdatabase.viewmodel.ItemDetailUiState
import com.example.ragnarokdatabase.viewmodel.ItemDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: Int,
    onNavigateBack: () -> Unit,
    viewModel: ItemDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(itemId) {
        viewModel.loadItemDetail(itemId)
    }

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
        when (val state = uiState) {
            is ItemDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Amber400)
                }
            }
            is ItemDetailUiState.NotFound -> {
                NotFoundContent(onNavigateBack = onNavigateBack)
            }
            is ItemDetailUiState.Success -> {
                ItemDetailContent(
                    item = state.item,
                    onNavigateBack = onNavigateBack
                )
            }
            is ItemDetailUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onNavigateBack = onNavigateBack
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailContent(
    item: Item,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top App Bar
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Slate200
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Slate900.copy(alpha = 0.95f)
            )
        )

        // Item Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Slate800),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = item.getCollectionImageUrl(),
                contentDescription = item.name,
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Item Name
            Text(
                text = item.name,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = LilitaOneFont
                ),
                color = Amber400,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Item Type
            Text(
                text = item.type + (item.subtype?.let { " - $it" } ?: ""),
                style = MaterialTheme.typography.titleMedium,
                color = Slate400,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Description
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Slate800
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "DESCRIPTION",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = LilitaOneFont
                        ),
                        color = Amber400
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate200
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PriceCard(
                    title = "BUY PRICE",
                    price = item.buyPrice,
                    modifier = Modifier.weight(1f)
                )
                PriceCard(
                    title = "SELL PRICE",
                    price = item.sellPrice,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Slate800
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "STATS",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = LilitaOneFont
                        ),
                        color = Amber400
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    item.stats.atk?.let {
                        StatRow(label = "Attack", value = it.toString())
                    }
                    item.stats.matk?.let {
                        StatRow(label = "Magic Attack", value = it.toString())
                    }
                    item.stats.defense?.let {
                        StatRow(label = "Defense", value = it.toString())
                    }
                    StatRow(label = "Weight", value = (item.stats.weight / 10).toString())
                    StatRow(label = "Slots", value = item.stats.slots.toString())
                }
            }

            // Requirements Card - Only show if there are meaningful requirements
            if (item.requiredLevel > 1 || item.requiredJob != null || item.gender != null || item.location != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Slate800
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "REQUIREMENTS",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = LilitaOneFont
                            ),
                            color = Amber400
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (item.requiredLevel > 1) {
                            RequirementRow(label = "Level", value = item.requiredLevel.toString())
                        }
                        item.getRequiredJobsText()?.let {
                            RequirementRow(label = "Job", value = it)
                        }
                        item.gender?.let {
                            RequirementRow(label = "Gender", value = it)
                        }
                        item.location?.let {
                            RequirementRow(label = "Location", value = it)
                        }
                    }
                }
            }

            // Script Card - Show if any script exists
            if (item.script != null || item.equipScript != null || item.unequipScript != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Slate800
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "SCRIPT",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = LilitaOneFont
                            ),
                            color = Amber400
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        item.script?.let { script ->
                            Text(
                                text = script,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                ),
                                color = Slate200
                            )
                        }

                        item.equipScript?.let { equipScript ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "On Equip:",
                                style = MaterialTheme.typography.labelMedium,
                                color = Amber400
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = equipScript,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                ),
                                color = Slate200
                            )
                        }

                        item.unequipScript?.let { unequipScript ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "On Unequip:",
                                style = MaterialTheme.typography.labelMedium,
                                color = Amber400
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = unequipScript,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                ),
                                color = Slate200
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PriceCard(
    title: String,
    price: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Slate800
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = LilitaOneFont
                ),
                color = Slate400
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${price}z",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Amber400
            )
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Slate300
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Slate100
        )
    }
}

@Composable
fun RequirementRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Slate300
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Amber400
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotFoundContent(
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Slate200
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Slate900.copy(alpha = 0.95f)
            )
        )

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
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = LilitaOneFont
                    ),
                    color = Slate400,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "The item you're looking for doesn't exist",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Slate500,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorContent(
    message: String,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Slate200
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Slate900.copy(alpha = 0.95f)
            )
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ErrorCard(message = message)
        }
    }
}

