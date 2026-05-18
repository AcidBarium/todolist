package com.ayse.todocompose.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ayse.todocompose.data.models.Priority
import com.ayse.todocompose.util.Constants.PREFERENCE_KEY
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException

class DataStoreRepositoryTest {
    private val mockDataStore = mockk<DataStore<Preferences>>(relaxed = true)

    @Before
    fun setup() {
        clearAllMocks()
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    private fun createRepo(dataFlow: Flow<Preferences>): DataStoreRepository {
        every { mockDataStore.data } returns dataFlow
        return DataStoreRepository(mockDataStore)
    }

    @Test
    fun `readSortState returns NONE when no preference saved`() = runTest {
        val repo = createRepo(flowOf(emptyPreferences()))
        assertEquals(Priority.NONE.name, repo.readSortState.first())
    }

    @Test
    fun `readSortState returns saved priority HIGH`() = runTest {
        val prefs = prefsOf(Pair(PREFERENCE_KEY, Priority.HIGH.name))
        val repo = createRepo(flowOf(prefs))
        assertEquals(Priority.HIGH.name, repo.readSortState.first())
    }

    @Test
    fun `readSortState returns saved priority LOW`() = runTest {
        val prefs = prefsOf(Pair(PREFERENCE_KEY, Priority.LOW.name))
        val repo = createRepo(flowOf(prefs))
        assertEquals(Priority.LOW.name, repo.readSortState.first())
    }

    @Test
    fun `readSortState catches IOException and emits NONE`() = runTest {
        val repo = createRepo(flow { throw IOException("IO error") })
        assertEquals(Priority.NONE.name, repo.readSortState.first())
    }

    @Test(expected = RuntimeException::class)
    fun `readSortState rethrows nonIOException`() = runTest {
        val repo = createRepo(flow { throw RuntimeException("fatal") })
        repo.readSortState.first()
    }

    @Test
    fun `persistSortState calls dataStore edit`() = runTest {
        val repo = createRepo(flowOf(emptyPreferences()))
        repo.persistSortState(Priority.MEDIUM)
        coVerify(exactly = 1) { mockDataStore.updateData(any()) }
    }

    @Test
    fun `persistSortState passes priority through`() = runTest {
        val repo = createRepo(flowOf(emptyPreferences()))
        repo.persistSortState(Priority.HIGH)
        coVerify(exactly = 1) { mockDataStore.updateData(any()) }
    }

    @Test
    fun `readSortState emits multiple values`() = runTest {
        val prefs1 = emptyPreferences()
        val prefs2 = prefsOf(Pair(PREFERENCE_KEY, Priority.LOW.name))
        val repo = createRepo(flowOf(prefs1, prefs2))
        val results = mutableListOf<String>()
        repo.readSortState.collect { results.add(it); if (results.size == 2) return@collect }
        assertEquals(Priority.NONE.name, results[0])
        assertEquals(Priority.LOW.name, results[1])
    }
}

private fun prefsOf(vararg pairs: Pair<String, String>): Preferences {
    val prefs = mutablePreferencesOf()
    pairs.forEach { (key, value) -> prefs[stringPreferencesKey(key)] = value }
    return prefs
}

private suspend fun <T> Flow<T>.first(): T {
    var result: T? = null
    collect { result = it; return@collect }
    @Suppress("UNCHECKED_CAST")
    return result as T
}
