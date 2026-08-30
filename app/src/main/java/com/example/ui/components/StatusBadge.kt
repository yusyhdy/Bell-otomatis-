package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CyanPrimary

@Composable
fun StatusBadge(
    text: String,
    statusType: String = "SUCCESS", // "SUCCESS", "WARNING", "ERROR", "CYAN", "INFO"
    modifier: Modifier = Modifier
) {
    val (bgColor, dotColor, textColor) = when (statusType) {
        "SUCCESS" -> Triple(AccentGreen.copy(alpha = 0.15f), AccentGreen, AccentGreen)
        "WARNING" -> Triple(AccentOrange.copy(alpha = 0.15f), AccentOrange, AccentOrange)
        "ERROR" -> Triple(AccentRed.copy(alpha = 0.15f), AccentRed, AccentRed)
        "CYAN" -> Triple(CyanPrimary.copy(alpha = 0.15f), CyanPrimary, CyanPrimary)
        else -> Triple(Color(0xFF64748B).copy(alpha = 0.15f), Color(0xFF64748B), Color(0xFFCBD5E1))
    }

    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(100.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(dotColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
