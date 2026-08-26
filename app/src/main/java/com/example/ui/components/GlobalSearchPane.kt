package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VsCodeAccent
import com.example.ui.theme.VsCodeSidebar
import com.example.ui.theme.VsCodeText
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.viewmodel.GlobalSearchMatch

@Composable
fun GlobalSearchPane(
    query: String,
    results: List<GlobalSearchMatch>,
    onQueryChange: (String) -> Unit,
    onMatchClick: (GlobalSearchMatch) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        searchFocusRequester.requestFocus()
    }

    Surface(
        color = VsCodeSidebar,
        modifier = modifier
            .width(220.dp)
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(6.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SEARCH IN PROJECT",
                    color = VsCodeTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose, modifier = Modifier.size(20.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Search",
                        tint = VsCodeTextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Search input field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF3C3C3C), RoundedCornerShape(3.dp))
                    .padding(horizontal = 6.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = VsCodeTextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        cursorBrush = SolidColor(VsCodeAccent),
                        textStyle = TextStyle(
                            color = VsCodeText,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(searchFocusRequester),
                        decorationBox = { innerTextField ->
                            if (query.isEmpty()) {
                                Text(
                                    text = "Search across files...",
                                    color = VsCodeTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // Results count
            Text(
                text = if (query.isEmpty()) "Type to search..." else "${results.size} result(s) found",
                color = VsCodeTextMuted,
                fontSize = 10.sp
            )

            Spacer(Modifier.height(4.dp))

            // Results list
            LazyColumn(modifier = Modifier.fillMaxHeight()) {
                items(results) { match ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMatchClick(match) }
                            .padding(vertical = 3.dp, horizontal = 4.dp)
                            .background(Color(0xFF252526), RoundedCornerShape(2.dp))
                            .padding(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = match.fileName,
                                color = VsCodeAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = ":${match.lineNumber}",
                                color = VsCodeTextMuted,
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = match.lineText,
                            color = VsCodeText,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 2
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                }
            }
        }
    }
}
