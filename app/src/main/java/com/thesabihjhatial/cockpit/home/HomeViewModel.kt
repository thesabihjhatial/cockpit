package com.thesabihjhatial.cockpit.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thesabihjhatial.cockpit.apps.AppInfo
import com.thesabihjhatial.cockpit.apps.AppListCache
import com.thesabihjhatial.cockpit.apps.loadApps
import com.thesabihjhatial.cockpit.apps.packageChangeFlow
import com.thesabihjhatial.cockpit.pins.PinSlots
import com.thesabihjhatial.cockpit.pins.PinnedStore
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val PackageDebounceMs = 400L
private const val PackageSeparator = '/'

private const val WarmRescanDelayMs = 2500L

@OptIn(FlowPreview::class)
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val appList = MutableStateFlow(emptyList<AppInfo>())
    private val loaded = MutableStateFlow(false)
    private val homeSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val pinnedStore = PinnedStore(application)
    private val appListCache = AppListCache(application)
    private val scanLock = Mutex()

    val apps: StateFlow<List<AppInfo>> = appList.asStateFlow()

    val ready: StateFlow<Boolean> = loaded.asStateFlow()
    val homeRequests: SharedFlow<Unit> = homeSignal.asSharedFlow()
    val pinned: StateFlow<List<AppInfo?>> = combine(appList, pinnedStore.slots) { apps, slots ->
        slots.map { id -> apps.firstOrNull { it.id == id } ?: apps.firstOrNull { it.id.substringBefore(PackageSeparator) == id } }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, List(PinSlots) { null })
    val slots: StateFlow<List<String?>> = pinnedStore.slots

    init {
        val cached = appListCache.read()
        if (cached.isNotEmpty()) {
            appList.value = cached
            loaded.value = true
        }

        viewModelScope.launch {
            packageChangeFlow(application).debounce(PackageDebounceMs).collectLatest { scan() }
        }

        viewModelScope.launch {
            if (cached.isNotEmpty()) delay(WarmRescanDelayMs)
            scan()
        }
    }

    private suspend fun scan() = scanLock.withLock {
        val apps = loadApps(getApplication())
        appList.value = apps
        appListCache.write(apps)
        if (apps.isNotEmpty()) pinnedStore.reconcile(apps.validPinIds())
        loaded.value = true
    }

    fun dock(id: String) = pinnedStore.dock(id)

    fun pin(id: String, slot: Int) = pinnedStore.pin(id, slot)

    fun requestHome() {
        homeSignal.tryEmit(Unit)
    }

    fun undock(id: String) = pinnedStore.undock(id)

    fun unpin(slot: Int) = pinnedStore.unpin(slot)
}

private fun List<AppInfo>.validPinIds(): Set<String> =
    flatMap { listOf(it.id, it.id.substringBefore(PackageSeparator)) }.toSet()
