package com.example.ragnarokdatabase.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.ragnarokdatabase.R
import com.example.ragnarokdatabase.model.ItemType
import com.example.ragnarokdatabase.ui.theme.*

val LilitaOneFont = FontFamily(Font(R.font.lilitaone_regular))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String = "RAGNAROK DATABASE",
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    itemTypes: List<ItemType> = emptyList(),
    onTypeSelected: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = LilitaOneFont
                ),
                color = Amber400
            )
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Slate200,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        actions = {
            // Menu dropdown for item types
            if (itemTypes.isNotEmpty()) {
                Box {
                    IconButton(
                        onClick = { expanded = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Item Types Menu",
                            tint = Amber400,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(Slate800)
                            .widthIn(min = 200.dp, max = 300.dp)
                    ) {
                        // Home option
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "🏠 Home",
                                    color = Amber400,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            onClick = {
                                onHomeClick()
                                expanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = Amber400
                            )
                        )

                        HorizontalDivider(color = Slate700, thickness = 1.dp)

                        Text(
                            text = "FILTER BY TYPE",
                            style = MaterialTheme.typography.labelMedium,
                            color = Slate400,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        HorizontalDivider(color = Slate700, thickness = 1.dp)

                        itemTypes.forEach { itemType ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = itemType.type,
                                            color = Slate100,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Amber400.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "${itemType.count}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Amber400,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onTypeSelected(itemType.type)
                                    expanded = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = Slate100
                                )
                            )
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Slate900.copy(alpha = 0.95f),
            titleContentColor = Amber400,
            navigationIconContentColor = Slate200,
            actionIconContentColor = Amber400
        )
    )
}

