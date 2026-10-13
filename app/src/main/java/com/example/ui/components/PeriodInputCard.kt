package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PeriodInputCard(
    periodInput: String,
    currentCycle: Int,
    isFixedResult: Boolean,
    onPeriodChanged: (String) -> Unit,
    onAnalyzeClicked: () -> Unit,
    onNextPeriod: () -> Unit,
    onStartNewCycle: () -> Unit,
    isAnalyzing: Boolean,
    modifier: Modifier = Modifier
) {
    val isValid = periodInput.length == 3
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }

    val isRolloverPeriod = periodInput == "999"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(DarkSurfaceElevated)
            .border(
                width = 1.2.dp,
                brush = if (isValid) {
                    Brush.horizontalGradient(listOf(NeonCyan, NeonPurple))
                } else {
                    SolidColor(DarkCardBorder)
                },
                shape = RoundedCornerShape(22.dp)
            )
            .padding(18.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().animateContentSize()
        ) {
            // Top Bar: Current Cycle Badge + Rollover Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cycle Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F1B2E))
                        .border(1.dp, NeonCyan, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("current_cycle_badge")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(NeonCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CYCLE #$currentCycle",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Range / Next Cycle trigger
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Range: 000–999",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    IconButton(
                        onClick = onStartNewCycle,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("new_cycle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "New Cycle",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Enter 3-Digit Period Number",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Results are permanently fixed per period in Cycle #$currentCycle",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Visual 3-digit pin boxes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        focusRequester.requestFocus()
                    },
                contentAlignment = Alignment.Center
            ) {
                // Invisible actual input handler
                BasicTextField(
                    value = periodInput,
                    onValueChange = onPeriodChanged,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = if (isValid) ImeAction.Done else ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isValid && !isAnalyzing) {
                                focusManager.clearFocus()
                                onAnalyzeClicked()
                            }
                        }
                    ),
                    textStyle = TextStyle(color = Color.Transparent),
                    cursorBrush = SolidColor(Color.Transparent),
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .testTag("period_input")
                        .size(1.dp)
                )

                // Render 3 sleek digital boxes
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 3) {
                        val digitChar = periodInput.getOrNull(i)
                        val isCurrentTarget = periodInput.length == i

                        val boxBorderBrush = when {
                            digitChar != null -> Brush.linearGradient(listOf(NeonCyan, NeonPurple))
                            isCurrentTarget -> SolidColor(NeonCyan)
                            else -> SolidColor(DarkCardBorder)
                        }

                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0C1322))
                                .border(
                                    width = if (isCurrentTarget || digitChar != null) 1.5.dp else 1.dp,
                                    brush = boxBorderBrush,
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (digitChar != null) {
                                Text(
                                    text = digitChar.toString(),
                                    color = NeonCyan,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Text(
                                    text = "-",
                                    color = TextMuted,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Light,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Quick Clear Button if text is entered
                    if (periodInput.isNotEmpty()) {
                        IconButton(
                            onClick = { onPeriodChanged("") },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("clear_period_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Input",
                                tint = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notice badge if current period is 999 (Rollover Warning)
            if (isRolloverPeriod) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF281C08))
                        .border(1.dp, NeonAmber, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Loop,
                            contentDescription = null,
                            tint = NeonAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Next will roll over 999 → 000 & start Cycle #${currentCycle + 1}",
                            color = NeonAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            } else if (isFixedResult && isValid) {
                // Notice badge indicating result is locked
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0D251C))
                        .border(1.dp, NeonEmerald, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Fixed Result Saved for Period $periodInput in Cycle #$currentCycle",
                            color = NeonEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Validation Helper Hint
            AnimatedVisibility(
                visible = !isValid && periodInput.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "Please enter 3 digits between 000 and 999 (${periodInput.length}/3 entered)",
                    color = NeonPink,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Primary Action Buttons Row (Analyze + Next Period)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Analyze Button
                Button(
                    onClick = {
                        if (isValid && !isAnalyzing) {
                            focusManager.clearFocus()
                            onAnalyzeClicked()
                        }
                    },
                    enabled = isValid && !isAnalyzing,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isValid && !isAnalyzing) {
                                Brush.horizontalGradient(listOf(NeonCyan, NeonPurple, NeonPink))
                            } else {
                                SolidColor(Color(0xFF1B2436))
                            }
                        )
                        .testTag("analyze_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                color = NeonCyan,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ANALYZING...",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoGraph,
                                contentDescription = null,
                                tint = if (isValid) Color.Black else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isFixedResult) "VIEW RESULT" else "ANALYZE",
                                color = if (isValid) Color.Black else TextMuted,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }

                // 2. Next Period Button (+1)
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onNextPeriod()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF131D2E),
                        contentColor = NeonCyan
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.2.dp, NeonCyan, RoundedCornerShape(14.dp))
                        .testTag("next_period_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isRolloverPeriod) "NEXT (000)" else "NEXT +1",
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Period",
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
