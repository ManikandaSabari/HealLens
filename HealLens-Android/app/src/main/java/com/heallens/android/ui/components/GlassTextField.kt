package com.heallens.android.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heallens.android.ui.theme.CyanPrimary
import com.heallens.android.ui.theme.SurfaceGlass
import com.heallens.android.ui.theme.SurfaceGlassBorder
import com.heallens.android.ui.theme.TextMuted
import com.heallens.android.ui.theme.TextPrimary

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp, lineHeight = 16.sp),
        label = label?.takeIf { it.isNotEmpty() }?.let { { Text(text = it, color = TextMuted, fontSize = 12.sp) } },
        placeholder = placeholder?.let { { Text(text = it, color = TextMuted, fontSize = 12.sp) } },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        isError = isError,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SurfaceGlass,
            unfocusedContainerColor = SurfaceGlass,
            disabledContainerColor = SurfaceGlass,
            errorContainerColor = SurfaceGlass,
            focusedBorderColor = CyanPrimary,
            unfocusedBorderColor = SurfaceGlassBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = CyanPrimary
        ),
        modifier = modifier.fillMaxWidth()
    )
}
