package com.metehanyl.ezanvakti.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.metehanyl.ezanvakti.LocationPickerState
import com.metehanyl.ezanvakti.R
import com.metehanyl.ezanvakti.data.model.CityOption
import com.metehanyl.ezanvakti.data.model.DistrictOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerDialog(
    state: LocationPickerState,
    hasManualLocation: Boolean,
    onDismiss: () -> Unit,
    onSelectCity: (CityOption) -> Unit,
    onSelectDistrict: (DistrictOption) -> Unit,
    onConfirm: () -> Unit,
    onUseAutomaticLocation: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.manual_location_title)) },
        text = {
            Column {
                OptionDropdown(
                    label = stringResource(R.string.select_city_label),
                    options = state.cities,
                    optionLabel = { it.name },
                    selected = state.selectedCity,
                    isLoading = state.isLoadingCities,
                    onSelect = onSelectCity
                )
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    OptionDropdown(
                        label = stringResource(R.string.select_district_label),
                        options = state.districts,
                        optionLabel = { it.name },
                        selected = state.selectedDistrict,
                        isLoading = state.isLoadingDistricts,
                        enabled = state.selectedCity != null,
                        onSelect = onSelectDistrict
                    )
                }
                state.errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                }
                if (hasManualLocation) {
                    TextButton(onClick = onUseAutomaticLocation, modifier = Modifier.padding(top = 8.dp)) {
                        Text(stringResource(R.string.action_use_automatic_location))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = state.selectedCity != null && state.selectedDistrict != null) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> OptionDropdown(
    label: String,
    options: List<T>,
    optionLabel: (T) -> String,
    selected: T?,
    isLoading: Boolean,
    enabled: Boolean = true,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled && options.isNotEmpty(),
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        TextField(
            value = selected?.let(optionLabel) ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded && enabled && options.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
