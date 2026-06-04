package com.llsl.viper4android.ui.screens.main

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.llsl.viper4android.R
import com.llsl.viper4android.audio.ViperParams
import com.llsl.viper4android.ui.components.CardGroupPosition
import com.llsl.viper4android.ui.components.LocalCardGroupPosition
import com.llsl.viper4android.ui.components.MasterEnabledCard
import com.llsl.viper4android.ui.screens.debug.DebugLogDialog
import com.llsl.viper4android.ui.screens.preset.PresetDialog
import com.llsl.viper4android.ui.screens.status.DriverStatusDialog
import com.llsl.viper4android.ui.theme.Dimens
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onOpenSettings: () -> Unit,
) {
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.saveSettingsOnBackground()
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val presets by viewModel.presetList.collectAsStateWithLifecycle()
    val driverStatus by viewModel.driverStatus.collectAsStateWithLifecycle()
    val debugMode by viewModel.debugModeEnabled.collectAsStateWithLifecycle()

    var showPresetDialog by remember { mutableStateOf(false) }
    var showDriverStatusDialog by remember { mutableStateOf(false) }
    var showDebugLog by remember { mutableStateOf(false) }
    var debugLogClearTime by remember { mutableLongStateOf(0L) }

    if (showPresetDialog) {
        PresetDialog(
            presets = presets,
            onSave = viewModel::savePreset,
            onLoad = { id ->
                viewModel.loadPreset(id)
                showPresetDialog = false
            },
            onDelete = viewModel::deletePreset,
            onRename = viewModel::renamePreset,
            onDismiss = { showPresetDialog = false },
        )
    }

    if (showDriverStatusDialog) {
        LaunchedEffect(Unit) {
            while (true) {
                viewModel.queryDriverStatus()
                delay(500)
            }
        }
        DriverStatusDialog(
            driverStatus = driverStatus,
            onDismiss = { showDriverStatusDialog = false },
        )
    }

    if (showDebugLog) {
        DebugLogDialog(
            clearTimestamp = debugLogClearTime,
            onClear = { debugLogClearTime = System.currentTimeMillis() },
            onDisableDebug = {
                viewModel.disableDebugMode()
                showDebugLog = false
            },
            onDismiss = { showDebugLog = false },
        )
    }

    val isSpkMode = state.fxType == ViperParams.FX_TYPE_SPEAKER
    val masterEnabled = if (isSpkMode) state.spkMasterEnabled else state.masterEnabled
    val onMasterEnabledChange: (Boolean) -> Unit =
        if (isSpkMode) viewModel::setSpkMasterEnabled else viewModel::setMasterEnabled

    // Window background: a dim surface in dark mode, a bright one in light, matching the reference app.
    val windowColor =
        if (isSystemInDarkTheme()) {
            MaterialTheme.colorScheme.surfaceDim
        } else {
            MaterialTheme.colorScheme.surfaceBright
        }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = windowColor,
        topBar = {
            TopAppBar(
                title = {
                    // The action icons on the right are 48dp tap targets centring a 24dp glyph, so
                    // their glyphs sit a step inside the card line even though the tap region meets
                    // it. Inset the plain title text to match that, rather than hugging the gutter,
                    // so the two ends of the bar read as balanced.
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = Dimens.spaceXs),
                    )
                },
                scrollBehavior = scrollBehavior,
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = windowColor,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                actions = {
                    if (debugMode) {
                        IconButton(onClick = { showDebugLog = true }) {
                            Icon(
                                Icons.Default.BugReport,
                                contentDescription = stringResource(R.string.debug_log_title),
                            )
                        }
                    }
                    IconButton(onClick = { showPresetDialog = true }) {
                        Icon(
                            Icons.Default.LibraryMusic,
                            contentDescription = stringResource(R.string.menu_presets),
                        )
                    }
                    IconButton(onClick = { showDriverStatusDialog = true }) {
                        Icon(
                            Icons.Default.Memory,
                            contentDescription = stringResource(R.string.menu_driver_status),
                        )
                    }
                    // End padding on the last icon nudges the whole action row in so the rightmost
                    // icon's tap ripple lines up with the cards' right edge (listSideMargin, 12dp)
                    // instead of overhanging it.
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.padding(end = Dimens.spaceXs),
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.menu_settings),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.setFxType(
                        if (isSpkMode) ViperParams.FX_TYPE_HEADPHONE else ViperParams.FX_TYPE_SPEAKER,
                    )
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(
                    imageVector = if (isSpkMode) Icons.Default.Speaker else Icons.Default.Headphones,
                    contentDescription = stringResource(R.string.mode_switch_cd),
                )
            }
        },
    ) { paddingValues ->
        // The effect sections render as one continuous, segmented card group. Each section's corner
        // rounding and the seam between cards come from its index in this ordered list, so the order
        // here is also the visual order on screen.
        val sections: List<@Composable () -> Unit> =
            buildList {
                add { MasterLimiterSection(state, viewModel, isSpkMode) }
                add { PlaybackGainSection(state, viewModel, isSpkMode) }
                add { LUFSTargetingSection(state, viewModel, isSpkMode) }
                add { MultibandCompressorSection(state, viewModel, isSpkMode) }
                add { FetCompressorSection(state, viewModel, isSpkMode) }
                add { DdcSection(state, viewModel, isSpkMode) }
                add { SpectrumExtensionSection(state, viewModel, isSpkMode) }
                add { EqualizerSection(state, viewModel, isSpkMode) }
                add { DynamicEqSection(state, viewModel, isSpkMode) }
                add { ConvolverSection(state, viewModel, isSpkMode) }
                add { FieldSurroundSection(state, viewModel, isSpkMode) }
                add { DiffSurroundSection(state, viewModel, isSpkMode) }
                add { StereoImagerSection(state, viewModel, isSpkMode) }
                add { HeadphoneSurroundSection(state, viewModel, isSpkMode) }
                add { ReverberationSection(state, viewModel, isSpkMode) }
                add { DynamicSystemSection(state, viewModel, isSpkMode) }
                add { TubeSimulatorSection(state, viewModel, isSpkMode) }
                add { PsychoacousticBassSection(state, viewModel, isSpkMode) }
                add { ViperBassSection(state, viewModel, isSpkMode) }
                add { ViperBassMonoSection(state, viewModel, isSpkMode) }
                add { ViperClaritySection(state, viewModel, isSpkMode) }
                add { AuditoryProtectionSection(state, viewModel, isSpkMode) }
                add { AnalogXSection(state, viewModel, isSpkMode) }
                if (isSpkMode) {
                    add { SpeakerOptSection(state, viewModel) }
                }
            }

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            contentPadding =
                PaddingValues(
                    start = Dimens.screenPadding,
                    end = Dimens.screenPadding,
                    top = Dimens.spaceSm,
                    // Extra bottom clearance so the info footer isn't hidden behind the FAB.
                    bottom = 96.dp,
                ),
        ) {
            item {
                MasterEnabledCard(
                    label =
                        stringResource(
                            if (masterEnabled) R.string.master_enabled else R.string.master_disabled,
                        ),
                    enabled = masterEnabled,
                    onEnabledChange = onMasterEnabledChange,
                    modifier = Modifier.padding(top = Dimens.heroMargin, bottom = Dimens.heroMargin),
                )
            }

            itemsIndexed(sections) { index, section ->
                CompositionLocalProvider(
                    LocalCardGroupPosition provides CardGroupPosition.of(index, sections.size),
                ) {
                    section()
                }
            }

            item {
                EffectListFooter(modifier = Modifier.padding(top = Dimens.spaceXl))
            }
        }
    }
}

@Composable
private fun EffectListFooter(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.TouchApp,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Dimens.rowIconSize),
        )
        Spacer(modifier = Modifier.size(Dimens.spaceSm))
        Text(
            text = stringResource(R.string.option_long_press_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start,
        )
    }
}

