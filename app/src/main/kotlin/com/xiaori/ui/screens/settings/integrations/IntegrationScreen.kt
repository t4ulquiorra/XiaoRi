

package com.xiaori.ui.screens.settings.integrations

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.xiaori.LocalPlayerAwareWindowInsets
import com.xiaori.R
import com.xiaori.ui.component.IconButton
import com.xiaori.ui.component.IntegrationCard
import com.xiaori.ui.component.IntegrationCardItem
import com.xiaori.ui.utils.backToMain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegrationScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
highlightKey: String? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val (listenBrainzEnabled, onListenBrainzEnabledChange) = com.xiaori.utils.rememberPreference(com.xiaori.constants.ListenBrainzEnabledKey, false)
    val (listenBrainzToken, onListenBrainzTokenChange) = com.xiaori.utils.rememberPreference(com.xiaori.constants.ListenBrainzTokenKey, "")

    var showListenBrainzTokenEditor = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)))
        Spacer(modifier = Modifier.padding(top = 16.dp))

        com.xiaori.ui.component.SwitchPreference(
            title = { Text(stringResource(R.string.listenbrainz_scrobbling)) },
            description = stringResource(R.string.listenbrainz_scrobbling_description),
            icon = { Icon(painterResource(R.drawable.token), null) },
            checked = listenBrainzEnabled,
            onCheckedChange = onListenBrainzEnabledChange,
        )

        com.xiaori.ui.component.PreferenceEntry(
            title = {
                Text(
                    if (listenBrainzToken.isBlank()) {
                        stringResource(R.string.set_listenbrainz_token)
                    } else {
                        "ListenBrainz token set"
                    },
                )
            },
            icon = { Icon(painterResource(R.drawable.edit), null) },
            onClick = { showListenBrainzTokenEditor.value = true },
        )
    
        Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom)))
    }

    TopAppBar(
        title = { Text(stringResource(R.string.listenbrainz_scrobbling)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                )
            }
        }
    )

    if (showListenBrainzTokenEditor.value) {
        com.xiaori.ui.component.TextFieldDialog(
            initialTextFieldValue =
                androidx.compose.ui.text.input
                    .TextFieldValue(listenBrainzToken),
            onDone = { data ->
                onListenBrainzTokenChange(data)
                showListenBrainzTokenEditor.value = false
            },
            onDismiss = { showListenBrainzTokenEditor.value = false },
            singleLine = true,
            maxLines = 1,
            isInputValid = {
                it.isNotEmpty()
            },
            extraContent = {
                com.xiaori.ui.component.InfoLabel(text = stringResource(R.string.listenbrainz_scrobbling_description))
            },
        )
    }
}
