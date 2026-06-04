package com.llsl.viper4android.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.llsl.viper4android.R
import com.llsl.viper4android.ui.components.SettingRow
import com.llsl.viper4android.ui.components.SettingSubRow
import com.llsl.viper4android.ui.components.SettingsCardGroup
import com.llsl.viper4android.ui.components.SettingsSectionHeader
import com.llsl.viper4android.ui.screens.device.DeviceDialog
import com.llsl.viper4android.ui.screens.main.MainViewModel
import com.llsl.viper4android.ui.theme.Dimens

/**
 * Full-screen Settings, reached from the main top bar. Gathers everything that doesn't belong on
 * the effect list: the legacy/global processing toggle, auto-start, preset / kernel / VDC imports,
 * a Devices entry point, and app info (updates, repository, version). The app-version row also
 * hides a 7-tap shortcut that turns on debug mode. (Driver version/architecture live in the
 * Driver Status dialog opened from the main screen.)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    val autoStart by viewModel.autoStartEnabled.collectAsStateWithLifecycle()
    val globalMode by viewModel.globalModeEnabled.collectAsStateWithLifecycle()
    val debugMode by viewModel.debugModeEnabled.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val devices by viewModel.deviceSettingsList.collectAsStateWithLifecycle()

    val appVersionName =
        remember {
            try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
            } catch (_: Exception) {
                ""
            }
        }

    // File pickers for the three import actions below. Each accepts multiple files.
    val importSuccessStr = stringResource(R.string.import_success)
    val importFailedStr = stringResource(R.string.import_failed)
    val importPresetStr = stringResource(R.string.settings_import_preset)
    val importKernelStr = stringResource(R.string.settings_import_kernel)
    val importVdcStr = stringResource(R.string.settings_import_vdc)

    val importPresetLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenMultipleDocuments(),
        ) { uris ->
            if (uris.isNotEmpty()) {
                viewModel.importPresetFiles(uris, notificationTitle = importPresetStr, successStr = importSuccessStr) { success ->
                    val msg = if (success) importSuccessStr else importFailedStr
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

    val importKernelLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenMultipleDocuments(),
        ) { uris ->
            if (uris.isNotEmpty()) {
                viewModel.importKernels(uris, notificationTitle = importKernelStr, successStr = importSuccessStr) { success ->
                    val msg = if (success) importSuccessStr else importFailedStr
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

    val importVdcLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenMultipleDocuments(),
        ) { uris ->
            if (uris.isNotEmpty()) {
                viewModel.importVdcs(uris, notificationTitle = importVdcStr, successStr = importSuccessStr) { success ->
                    val msg = if (success) importSuccessStr else importFailedStr
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

    var audioFilesExpanded by remember { mutableStateOf(false) }
    var checkUpdatesExpanded by remember { mutableStateOf(false) }
    var repositoryExpanded by remember { mutableStateOf(false) }
    var showDeviceDialog by remember { mutableStateOf(false) }

    val debugModeEnabledStr = stringResource(R.string.debug_mode_enabled)
    val debugAlreadyEnabledStr = stringResource(R.string.debug_already_enabled)
    var versionTapCount by remember { mutableIntStateOf(0) }

    if (showDeviceDialog) {
        DeviceDialog(
            devices = devices,
            activeDeviceId = state.activeDeviceId,
            onRename = viewModel::renameDevice,
            onLoad = viewModel::loadDevicePreset,
            onSave = viewModel::saveDevicePreset,
            onDelete = viewModel::deleteDeviceSettings,
            onDismiss = { showDeviceDialog = false },
        )
    }

    val windowColor =
        if (isSystemInDarkTheme()) {
            MaterialTheme.colorScheme.surfaceDim
        } else {
            MaterialTheme.colorScheme.surfaceBright
        }

    Scaffold(
        containerColor = windowColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.menu_settings),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    // Tonal circular back button. A small start nudge lines the circle's left edge
                    // up with the cards below (which inset by listSideMargin), rather than hugging
                    // the screen edge. Added to the app bar's own ~8dp base offset, this lands the
                    // circle at the same gutter as the content.
                    FilledTonalIconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(start = Dimens.spaceXs),
                        colors =
                            IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_close),
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = windowColor,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.listSideMargin),
        ) {
            SettingsSectionHeader(title = stringResource(R.string.settings_section_processing))
            SettingsCardGroup(
                buildList {
                    add {
                        SettingRow(
                            title = stringResource(R.string.settings_legacy_mode),
                            subtitle = stringResource(R.string.settings_legacy_mode_desc),
                            trailing = {
                                Switch(checked = globalMode, onCheckedChange = viewModel::toggleGlobalMode)
                            },
                        )
                    }
                    add {
                        SettingRow(
                            title = stringResource(R.string.settings_auto_start),
                            subtitle = stringResource(R.string.settings_auto_start_desc),
                            trailing = {
                                Switch(checked = autoStart, onCheckedChange = viewModel::toggleAutoStart)
                            },
                        )
                    }
                    add {
                        SettingRow(
                            title = stringResource(R.string.settings_audio_files),
                            subtitle = stringResource(R.string.settings_audio_files_desc),
                            expanded = audioFilesExpanded,
                            onClick = { audioFilesExpanded = !audioFilesExpanded },
                            content = {
                                SettingSubRow(
                                    title = stringResource(R.string.settings_import_preset),
                                    icon = Icons.Default.FileDownload,
                                    onClick = {
                                        importPresetLauncher.launch(
                                            arrayOf("application/json", "text/xml", "application/xml", "*/*"),
                                        )
                                    },
                                )
                                SettingSubRow(
                                    title = stringResource(R.string.settings_import_kernel),
                                    icon = Icons.Default.FileDownload,
                                    onClick = {
                                        importKernelLauncher.launch(
                                            arrayOf("audio/*", "application/octet-stream", "*/*"),
                                        )
                                    },
                                )
                                SettingSubRow(
                                    title = stringResource(R.string.settings_import_vdc),
                                    icon = Icons.Default.FileDownload,
                                    onClick = { importVdcLauncher.launch(arrayOf("*/*")) },
                                )
                                SettingSubRow(
                                    title = stringResource(R.string.settings_download_audio_files),
                                    icon = Icons.Default.Download,
                                    onClick = { openUrl(context, context.getString(R.string.presets_download_url)) },
                                )
                            },
                        )
                    }
                    add {
                        SettingRow(
                            title = stringResource(R.string.settings_open_audio),
                            subtitle = stringResource(R.string.settings_open_audio_desc),
                            onClick = {
                                try {
                                    context.startActivity(
                                        Intent(Settings.ACTION_SOUND_SETTINGS)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                    )
                                } catch (_: Exception) {
                                }
                            },
                        )
                    }
                    add {
                        SettingRow(
                            title = stringResource(R.string.settings_open_notifications),
                            onClick = {
                                try {
                                    context.startActivity(
                                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                    )
                                } catch (_: Exception) {
                                }
                            },
                        )
                    }
                },
            )

            SettingsSectionHeader(title = stringResource(R.string.menu_devices))
            SettingsCardGroup(
                listOf {
                    SettingRow(
                        title = stringResource(R.string.menu_devices),
                        onClick = { showDeviceDialog = true },
                    )
                },
            )

            // App info. "Check updates" and "Repository on GitHub" each expand to a pair of rows,
            // one for the app and one for the driver, because the two live in separate repositories.
            SettingsSectionHeader(title = stringResource(R.string.settings_section_app))
            SettingsCardGroup(
                buildList {
                    add {
                        SettingRow(
                            title = stringResource(R.string.settings_check_updates),
                            expanded = checkUpdatesExpanded,
                            onClick = { checkUpdatesExpanded = !checkUpdatesExpanded },
                            content = {
                                SettingSubRow(
                                    title = stringResource(R.string.settings_update_app),
                                    subtitle = stringResource(R.string.settings_update_app_desc),
                                    icon = ImageVector.vectorResource(R.drawable.ic_viper),
                                    onClick = { openUrl(context, context.getString(R.string.releases_url)) },
                                )
                                SettingSubRow(
                                    title = stringResource(R.string.settings_update_driver),
                                    subtitle = stringResource(R.string.settings_update_driver_desc),
                                    icon = Icons.Default.Memory,
                                    onClick = { openUrl(context, context.getString(R.string.driver_releases_url)) },
                                )
                            },
                        )
                    }
                    add {
                        SettingRow(
                            title = stringResource(R.string.settings_repository),
                            expanded = repositoryExpanded,
                            onClick = { repositoryExpanded = !repositoryExpanded },
                            content = {
                                SettingSubRow(
                                    title = stringResource(R.string.settings_update_app),
                                    subtitle = stringResource(R.string.settings_github_go),
                                    icon = ImageVector.vectorResource(R.drawable.ic_github),
                                    onClick = { openUrl(context, context.getString(R.string.repo_url)) },
                                )
                                SettingSubRow(
                                    title = stringResource(R.string.settings_update_driver),
                                    subtitle = stringResource(R.string.settings_github_go),
                                    icon = ImageVector.vectorResource(R.drawable.ic_github),
                                    onClick = { openUrl(context, context.getString(R.string.driver_repo_url)) },
                                )
                            },
                        )
                    }
                    add {
                        SettingRow(
                            title = stringResource(R.string.settings_app_version),
                            subtitle = appVersionName,
                            // Hidden shortcut: tap 7 times to enable debug mode (Android-style countdown).
                            onClick = {
                                if (debugMode) {
                                    Toast.makeText(context, debugAlreadyEnabledStr, Toast.LENGTH_SHORT).show()
                                } else {
                                    versionTapCount++
                                    val remaining = TAPS_TO_DEBUG - versionTapCount
                                    when {
                                        remaining <= 0 -> {
                                            versionTapCount = 0
                                            viewModel.enableDebugMode()
                                            Toast.makeText(context, debugModeEnabledStr, Toast.LENGTH_SHORT).show()
                                        }
                                        // Stay silent for the first taps, then show the countdown.
                                        remaining < TAPS_TO_DEBUG - 2 -> {
                                            Toast.makeText(
                                                context,
                                                context.resources.getQuantityString(
                                                    R.plurals.debug_taps_remaining,
                                                    remaining,
                                                    remaining,
                                                ),
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        }
                                    }
                                }
                            },
                        )
                    }
                },
            )

            // Credits: a row of contributor avatars, each opening that person's GitHub profile.
            SettingsSectionHeader(title = stringResource(R.string.settings_credits))
            CreditsRow(onOpen = { url -> openUrl(context, url) })

            Spacer(modifier = Modifier.size(Dimens.spaceXl))
        }
    }
}

/** A credited contributor. [handle] is their GitHub username; null for name-only credits. */
private data class Contributor(
    val name: String,
    val handle: String? = null,
)

private val CONTRIBUTORS =
    listOf(
        Contributor("iscle", "iscle"),
        Contributor("Kisaratan", "Kisaratan"),
        Contributor("likelikeslike", "likelikeslike"),
        Contributor("Martmists-GH", "Martmists-GH"),
        Contributor("maurerdv", "maurerdv"),
        Contributor("Pittvandewitt", "Pittvandewitt"),
        Contributor("syntaxticsugr", "syntaxticsugr"),
        Contributor("WSTxda", "WSTxda"),
        Contributor("ViPER520"),
        Contributor("Zhuhang"),
    )

/**
 * GitHub serves each user's avatar at github.com/<handle>.png; we route it through the
 * images.weserv.nl proxy to resize and circle-mask it, so the app ships no avatar bitmaps of its
 * own. 144px comfortably covers the 44dp slot up to xxxhdpi.
 */
private fun avatarUrl(handle: String): String =
    "https://images.weserv.nl/?url=github.com/$handle.png&w=144&h=144&fit=cover&output=png"

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreditsRow(onOpen: (String) -> Unit) {
    FlowRow(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.cardPadding, vertical = Dimens.space),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceMd, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceMd),
    ) {
        CONTRIBUTORS.forEach { contributor ->
            val handle = contributor.handle
            val clickModifier =
                if (handle != null) {
                    Modifier.clickable { onOpen("https://github.com/$handle") }
                } else {
                    Modifier
                }
            // A circular slot: contributors with a GitHub handle get their avatar loaded over
            // it; name-only credits keep the initial as a placeholder. The filled circle also
            // shows through while the avatar loads or if it can't be fetched offline.
            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .then(clickModifier),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = contributor.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (handle != null) {
                    AsyncImage(
                        model =
                            ImageRequest.Builder(LocalContext.current)
                                .data(avatarUrl(handle))
                                .crossfade(true)
                                .build(),
                        contentDescription = contributor.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

/** Number of taps on the app-version row required to enable debug mode. */
private const val TAPS_TO_DEBUG = 7

private fun openUrl(
    context: android.content.Context,
    url: String,
) {
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    } catch (_: Exception) {
    }
}
