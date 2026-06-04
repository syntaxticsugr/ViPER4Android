package com.llsl.viper4android.ui.screens.status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.llsl.viper4android.R
import com.llsl.viper4android.ui.screens.main.DriverStatus
import com.llsl.viper4android.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverStatusDialog(
    driverStatus: DriverStatus,
    onDismiss: () -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(
                modifier = Modifier.padding(vertical = Dimens.dialogPadding),
            ) {
                // The driver/info dialogs keep the title left-aligned with no icon, the way the
                // com.wstxda.viper4android app presents them.
                Text(
                    text = stringResource(R.string.menu_driver_status),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier =
                        Modifier
                            .padding(
                                start = Dimens.dialogPadding,
                                end = Dimens.dialogPadding,
                                bottom = Dimens.spaceLg,
                            ),
                )

                if (!driverStatus.installed) {
                    Text(
                        text = stringResource(R.string.driver_not_found),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier =
                            Modifier.padding(horizontal = Dimens.dialogPadding),
                    )
                } else {
                    Column {
                        StatusRow(
                            label = stringResource(R.string.driver_version_name),
                            value = driverStatus.versionName,
                        )
                        StatusRow(
                            label = stringResource(R.string.driver_version_code),
                            value = driverStatus.versionCode.toString(),
                        )
                        StatusRow(
                            label = stringResource(R.string.driver_architecture),
                            value = driverStatus.architecture,
                        )
                        StatusRow(
                            label = stringResource(R.string.driver_aidl),
                            value =
                                if (driverStatus.aidlMode) {
                                    stringResource(R.string.master_enabled)
                                } else {
                                    stringResource(R.string.master_disabled)
                                },
                        )
                        StatusRow(
                            label = stringResource(R.string.driver_streaming),
                            value =
                                if (driverStatus.streaming) {
                                    stringResource(R.string.status_active)
                                } else {
                                    stringResource(R.string.status_inactive)
                                },
                        )
                        StatusRow(
                            label = stringResource(R.string.driver_sampling_rate),
                            value =
                                if (driverStatus.samplingRate > 0) {
                                    "${driverStatus.samplingRate} Hz"
                                } else {
                                    stringResource(R.string.status_unknown)
                                },
                        )
                    }
                }

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = Dimens.space, end = Dimens.space),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.action_close))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    value: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.dialogPadding, vertical = Dimens.spaceSm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
