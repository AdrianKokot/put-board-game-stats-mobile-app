package com.example.boardgamestats.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldWithDropdown(
    modifier: Modifier = Modifier,
    options: List<String>,
    value: String = "",
    label: @Composable (() -> Unit)?,
    onValueChange: ((String) -> Unit)?,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var selectedOptionText by rememberSaveable { mutableStateOf(value) }
    val filteringOptions = options.filter { it.contains(selectedOptionText, ignoreCase = true) }
    val isActuallyExpanded = expanded && filteringOptions.isNotEmpty()

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = isActuallyExpanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        TextField(
            maxLines = 1,
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            value = selectedOptionText,
            onValueChange = {
                selectedOptionText = it
                onValueChange?.invoke(selectedOptionText)
            },
            label = label,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && filteringOptions.isNotEmpty()) },
            keyboardActions = keyboardActions,
            keyboardOptions = keyboardOptions,
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            singleLine = true,
            isError = isError,
            supportingText = supportingText
        )

        ExposedDropdownMenu(
            expanded = isActuallyExpanded,
            onDismissRequest = { expanded = false },
        ) {
            filteringOptions.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        selectedOptionText = selectionOption
                        onValueChange?.invoke(selectedOptionText)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}