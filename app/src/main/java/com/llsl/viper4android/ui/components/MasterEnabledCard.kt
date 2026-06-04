package com.llsl.viper4android.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.llsl.viper4android.ui.theme.Dimens

/**
 * The hero master-toggle card at the top of the main screen: a heavily-rounded primaryContainer
 * pill ([Dimens.heroCorner]) with the bold [label] ("Enabled"/"Disabled") on the left and the
 * master [Switch] on the right. Tapping anywhere on the card flips the switch. The start/end
 * padding is deliberately asymmetric so the look matches the reference app.
 */
@Composable
fun MasterEnabledCard(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = Dimens.heroMinHeight),
        shape = RoundedCornerShape(Dimens.heroCorner),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        onClick = { onEnabledChange(!enabled) },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = Dimens.heroPaddingStart, end = Dimens.heroPaddingEnd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange,
            )
        }
    }
}
