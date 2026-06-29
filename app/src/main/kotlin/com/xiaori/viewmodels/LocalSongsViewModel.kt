

package t4ulquiorra.xiaori.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import t4ulquiorra.xiaori.localmedia.LocalSongScanConfig
import t4ulquiorra.xiaori.db.MusicDatabase
import t4ulquiorra.xiaori.localmedia.LocalSongScanSummary
import t4ulquiorra.xiaori.localmedia.LocalSongScanner
import t4ulquiorra.xiaori.utils.reportException
import javax.inject.Inject

@HiltViewModel
class LocalSongsViewModel
@Inject
constructor(
    database: MusicDatabase,
    private val localSongScanner: LocalSongScanner,
) : ViewModel() {
    private val _scanState = MutableStateFlow(LocalSongsScanState())
    val scanState = _scanState.asStateFlow()

    val songs = database.localSongs().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    fun scanDevice(scanConfig: LocalSongScanConfig = LocalSongScanConfig()) {
        if (_scanState.value.isScanning) return
        viewModelScope.launch(Dispatchers.IO) {
            _scanState.value = _scanState.value.copy(isScanning = true, errorMessage = null)
            runCatching { localSongScanner.scanDevice(scanConfig) }
                .onSuccess { summary ->
                    _scanState.value = LocalSongsScanState(
                        isScanning = false,
                        lastSummary = summary,
                        errorMessage = null,
                    )
                }
                .onFailure { error ->
                    reportException(error)
                    _scanState.value = _scanState.value.copy(
                        isScanning = false,
                        errorMessage = error.message,
                    )
                }
        }
    }
}

data class LocalSongsScanState(
    val isScanning: Boolean = false,
    val lastSummary: LocalSongScanSummary? = null,
    val errorMessage: String? = null,
)