package com.xayah.feature.main.medium.list

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import com.xayah.core.ui.material3.SnackbarHost
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xayah.core.ui.viewmodel.IndexUiEffect
import com.xayah.core.model.ModeState
import com.xayah.core.model.OpType
import com.xayah.core.model.getLabel
import com.xayah.core.model.util.formatSize
import com.xayah.core.ui.component.AddIconButton
import com.xayah.core.ui.component.CheckIconButton
import com.xayah.core.ui.component.ChecklistIconButton
import com.xayah.core.ui.component.ChipRow
import com.xayah.core.ui.component.ContentWithConfirm
import com.xayah.core.ui.component.DeleteIconButton
import com.xayah.core.ui.component.FilterChip
import com.xayah.core.ui.component.InnerTopSpacer
import com.xayah.core.ui.component.RoundChip
import com.xayah.core.ui.component.SearchBar
import com.xayah.core.ui.component.SecondaryMediumTopBar
import com.xayah.core.ui.component.paddingBottom
import com.xayah.core.ui.component.paddingHorizontal
import com.xayah.core.ui.material3.pullrefresh.PullRefreshIndicator
import com.xayah.core.ui.material3.pullrefresh.pullRefresh
import com.xayah.core.ui.material3.pullrefresh.rememberPullRefreshState
import com.xayah.core.ui.material3.toColor
import com.xayah.core.ui.material3.tokens.ColorSchemeKeyTokens
import com.xayah.core.ui.model.ImageVectorToken
import com.xayah.core.ui.model.StringResourceToken
import com.xayah.core.ui.token.PaddingTokens
import com.xayah.core.ui.util.LocalNavController
import com.xayah.core.ui.util.fromStringId
import com.xayah.core.ui.util.fromVector
import com.xayah.core.ui.util.value
import com.xayah.feature.main.medium.MediaCard
import com.xayah.feature.main.medium.R
import com.xayah.feature.main.medium.countBackups

@ExperimentalLayoutApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@Composable
fun PageMedium() {
    val context = LocalContext.current
    val viewModel = hiltViewModel<IndexViewModel>()
    val navController = LocalNavController.current!!
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val topBarState by viewModel.topBarState.collectAsStateWithLifecycle()
    val mediumState by viewModel.mediumState.collectAsStateWithLifecycle()
    val activatedState by viewModel.activatedState.collectAsStateWithLifecycle()
    val processingCountState by viewModel.processingCountState.collectAsStateWithLifecycle()
    val locationIndexState by viewModel.locationIndexState.collectAsStateWithLifecycle()
    val modeState by viewModel.modeState.collectAsStateWithLifecycle()
    val accountsState by viewModel.accountsState.collectAsStateWithLifecycle()
    val snackbarHostState = viewModel.snackbarHostState
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val isRefreshing = uiState.isRefreshing
    val pullRefreshState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = { viewModel.emitIntentOnIO(IndexUiIntent.OnRefresh) })
    val enabled = topBarState.progress == 1f
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SecondaryMediumTopBar(
                scrollBehavior = scrollBehavior,
                topBarState = topBarState,
                actions = {
                    AddIconButton {
                        viewModel.emitIntentOnIO(IndexUiIntent.AddMedia(context = context))
                    }
                    if (isRefreshing.not() && modeState != ModeState.OVERVIEW) {
                        ChecklistIconButton {
                            viewModel.emitIntentOnIO(IndexUiIntent.SelectAll)
                        }
                        if (modeState == ModeState.BATCH_RESTORE) {
                            ContentWithConfirm(
                                content = { expanded ->
                                    DeleteIconButton(enabled = activatedState) {
                                        expanded.value = true
                                    }
                                },
                                onConfirm = {
                                    viewModel.emitIntent(IndexUiIntent.DeleteSelected)
                                }
                            )
                        }
                        CheckIconButton(enabled = activatedState) {
                            viewModel.emitIntentOnIO(IndexUiIntent.Process(navController = navController))
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize()) {
            InnerTopSpacer(innerPadding = innerPadding)

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState),
                    verticalArrangement = Arrangement.spacedBy(PaddingTokens.Level4)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(PaddingTokens.Level4))
                        SearchBar(
                            modifier = Modifier.paddingHorizontal(PaddingTokens.Level4),
                            enabled = enabled,
                            placeholder = StringResourceToken.fromStringId(R.string.search_bar_hint_medium),
                            onTextChange = {
                                viewModel.emitIntentOnIO(IndexUiIntent.FilterByKey(key = it))
                            }
                        )
                    }

                    item {
                        Column {
                            ChipRow {
                                val modes by remember { mutableStateOf(listOf(ModeState.OVERVIEW, ModeState.BATCH_BACKUP, ModeState.BATCH_RESTORE)) }
                                FilterChip(
                                    enabled = enabled,
                                    leadingIcon = ImageVectorToken.fromVector(Icons.Rounded.FactCheck),
                                    selectedIndex = modes.indexOf(modeState),
                                    list = modes.map { it.getLabel(context) },
                                    onSelected = { index, _ ->
                                        viewModel.emitIntentOnIO(IndexUiIntent.SetMode(index = index, mode = modes[index]))
                                    },
                                    onClick = {}
                                )
                                if (modeState != ModeState.OVERVIEW)
                                    FilterChip(
                                        enabled = enabled,
                                        leadingIcon = ImageVectorToken.fromVector(Icons.Rounded.LocationOn),
                                        selectedIndex = locationIndexState,
                                        list = accountsState.map { "${context.getString(R.string.cloud)}: ${it.name}" }.toMutableList().also { it.add(0, context.getString(R.string.local)) },
                                        onSelected = { index, _ ->
                                            viewModel.emitIntentOnIO(IndexUiIntent.FilterByLocation(index = index))
                                        },
                                        onClick = {}
                                    )
                            }
                        }
                    }

                    items(
                        items = mediumState,
                        key = { "${it.entity.name}: ${it.entity.indexInfo.opType}-${it.entity.preserveId}-${it.entity.indexInfo.cloud}-${it.entity.indexInfo.backupDir}" }) { item ->
                        Row(
                            modifier = Modifier
                                .animateItemPlacement()
                                .paddingHorizontal(PaddingTokens.Level4),
                        ) {
                            val name = item.entity.name
                            val path = item.entity.path
                            val preserveId = item.entity.preserveId
                            val displayStatsFormat = item.entity.mediaInfo.displayBytes.toDouble().formatSize()
                            val backupsCount = when (item.entity.indexInfo.opType) {
                                OpType.BACKUP -> item.count - 1
                                OpType.RESTORE -> item.count
                            }
                            val itemEnabled = isRefreshing.not() && processingCountState.not()
                            MediaCard(
                                name = name,
                                path = path,
                                enabled = itemEnabled,
                                cardSelected = if (modeState == ModeState.OVERVIEW) false else item.entity.extraInfo.activated,
                                onCardClick = {
                                    when (modeState) {
                                        ModeState.OVERVIEW -> {
                                            viewModel.emitIntentOnIO(IndexUiIntent.ToPageMediaDetail(navController, item.entity))
                                        }

                                        else -> {
                                            viewModel.emitIntentOnIO(IndexUiIntent.Select(item.entity))
                                        }
                                    }
                                },
                                onCardLongClick = {},
                            ) {
                                when (modeState) {
                                    ModeState.OVERVIEW, ModeState.BATCH_BACKUP -> {
                                        if (backupsCount > 0) RoundChip(
                                            enabled = itemEnabled,
                                            text = countBackups(context = context, count = backupsCount),
                                            color = ColorSchemeKeyTokens.Primary.toColor(),
                                        ) {
                                            viewModel.emitEffectOnIO(IndexUiEffect.DismissSnackbar)
                                        }
                                    }

                                    ModeState.BATCH_RESTORE -> {
                                        RoundChip(
                                            enabled = itemEnabled,
                                            text = "${StringResourceToken.fromStringId(R.string.id).value}: $preserveId",
                                            color = ColorSchemeKeyTokens.Primary.toColor(),
                                        ) {
                                            viewModel.emitEffectOnIO(IndexUiEffect.DismissSnackbar)
                                        }
                                    }
                                }

                                RoundChip(enabled = itemEnabled, text = displayStatsFormat) {
                                    viewModel.emitEffectOnIO(IndexUiEffect.DismissSnackbar)
                                    viewModel.emitEffectOnIO(IndexUiEffect.ShowSnackbar("${context.getString(R.string.data_size)}: $displayStatsFormat"))
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.paddingBottom(PaddingTokens.Level4))
                    }
                }

                PullRefreshIndicator(refreshing = isRefreshing, state = pullRefreshState, modifier = Modifier.align(Alignment.TopCenter))
            }
        }
    }
}
