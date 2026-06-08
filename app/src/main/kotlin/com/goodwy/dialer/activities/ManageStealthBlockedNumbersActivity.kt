package com.goodwy.dialer.activities

import android.app.Application
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.goodwy.commons.activities.BaseSimpleActivity
import com.goodwy.commons.compose.extensions.enableEdgeToEdgeSimple
import com.goodwy.commons.compose.theme.AppThemeSurface
import com.goodwy.commons.helpers.APP_ICON_IDS
import com.goodwy.commons.helpers.APP_LAUNCHER_NAME
import com.goodwy.dialer.R
import com.goodwy.dialer.helpers.StealthBlockedNumbersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ManageStealthBlockedNumbersActivity : BaseSimpleActivity() {
    private val stealthViewModel by viewModels<StealthBlockedNumbersViewModel>()

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun getRepositoryName() = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeSimple()

        setContent {
            val stealthNumbers by stealthViewModel.stealthNumbers.collectAsStateWithLifecycle()
            AppThemeSurface {
                StealthBlockedNumbersScreen(
                    stealthNumbers = stealthNumbers,
                    onAddStealthNumber = { stealthViewModel.addStealthNumber(it) },
                    onRemoveStealthNumber = { stealthViewModel.removeStealthNumber(it) }
                )
            }
        }
    }

    internal class StealthBlockedNumbersViewModel(application: Application) : AndroidViewModel(application) {
        private val _stealthNumbers = MutableStateFlow<List<String>>(emptyList())
        val stealthNumbers = _stealthNumbers.asStateFlow()

        init {
            updateStealthNumbers()
        }

        fun addStealthNumber(number: String) {
            viewModelScope.launch(Dispatchers.IO) {
                StealthBlockedNumbersRepository.addStealthBlockedNumber(getApplication(), number)
                updateStealthNumbers()
            }
        }

        fun removeStealthNumber(number: String) {
            viewModelScope.launch(Dispatchers.IO) {
                StealthBlockedNumbersRepository.removeStealthBlockedNumber(getApplication(), number)
                updateStealthNumbers()
            }
        }

        private fun updateStealthNumbers() {
            viewModelScope.launch(Dispatchers.IO) {
                _stealthNumbers.value = StealthBlockedNumbersRepository.getStealthBlockedNumbers(getApplication())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StealthBlockedNumbersScreen(
    stealthNumbers: List<String>,
    onAddStealthNumber: (String) -> Unit,
    onRemoveStealthNumber: (String) -> Unit
) {
    val context = LocalContext.current
    var inputValue by rememberSaveable { mutableStateOf("") }
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SmallTopAppBar(title = { Text(stringResource(R.string.stealth_blocked_numbers_title)) })

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputValue,
                onValueChange = {
                    inputValue = it
                    if (errorText != null) errorText = null
                },
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.add_stealth_blocked_number_hint)) },
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    val normalized = PhoneNumberUtils.normalizeNumber(inputValue.trim())
                    when {
                        normalized.isBlank() -> errorText = stringResource(R.string.stealth_blocked_number_invalid)
                        stealthNumbers.contains(normalized) -> errorText = stringResource(R.string.stealth_blocked_number_duplicate)
                        else -> {
                            onAddStealthNumber(normalized)
                            inputValue = ""
                            Toast.makeText(context, stringResource(R.string.stealth_blocked_number_added), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = stringResource(R.string.add_button))
            }
        }

        if (!errorText.isNullOrBlank()) {
            Text(
                text = errorText!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.stealth_blocked_numbers_list_label),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (stealthNumbers.isEmpty()) {
            Text(
                text = stringResource(R.string.stealth_blocked_numbers_empty),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stealthNumbers, key = { it }) { number ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = number,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            IconButton(onClick = {
                                onRemoveStealthNumber(number)
                                Toast.makeText(context, stringResource(R.string.stealth_blocked_number_removed), Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = stringResource(R.string.remove_stealth_blocked_number)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
