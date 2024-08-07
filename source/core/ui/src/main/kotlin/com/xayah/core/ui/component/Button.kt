package com.xayah.core.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.xayah.core.ui.R
import com.xayah.core.ui.material3.DisabledAlpha
import com.xayah.core.ui.material3.toColor
import com.xayah.core.ui.material3.tokens.ColorSchemeKeyTokens
import com.xayah.core.ui.model.ImageVectorToken
import com.xayah.core.ui.model.StringResourceToken
import com.xayah.core.ui.token.SizeTokens
import com.xayah.core.ui.util.fromDrawable
import com.xayah.core.ui.util.fromVector
import com.xayah.core.ui.util.value

@Composable
fun IconButton(modifier: Modifier = Modifier, icon: ImageVectorToken, tint: Color = LocalContentColor.current, enabled: Boolean = true, onClick: () -> Unit) {
    IconButton(modifier = modifier, enabled = enabled, onClick = onClick) {
        Icon(
            imageVector = icon.value,
            contentDescription = null,
            tint = if (enabled) tint else tint.copy(alpha = DisabledAlpha),
        )
    }
}

@Composable
fun FilledIconButton(
    modifier: Modifier = Modifier,
    icon: ImageVectorToken,
    containerColor: ColorSchemeKeyTokens = ColorSchemeKeyTokens.Primary,
    contentColor: ColorSchemeKeyTokens = ColorSchemeKeyTokens.OnPrimary,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    FilledIconButton(
        modifier = modifier,
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = containerColor.toColor(enabled), contentColor = contentColor.toColor(enabled)),
        enabled = enabled,
        onClick = onClick
    ) {
        Icon(
            imageVector = icon.value,
            contentDescription = null,
        )
    }
}

@Composable
fun ArrowBackButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    IconButton(modifier = modifier, icon = ImageVectorToken.fromVector(Icons.Rounded.ArrowBack), onClick = onClick)
}

@Composable
fun TextButton(modifier: Modifier = Modifier, text: StringResourceToken, onClick: () -> Unit) {
    TextButton(
        modifier = modifier,
        onClick = onClick,
        content = { Text(text = text.value, fontWeight = FontWeight.Bold) },
        contentPadding = ButtonDefaults.ContentPadding
    )
}

@Composable
fun CheckIconButton(modifier: Modifier = Modifier, enabled: Boolean = true, checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?) {
    IconButton(
        modifier = modifier,
        enabled = enabled,
        icon = if (checked) ImageVectorToken.fromDrawable(R.drawable.ic_rounded_check_circle) else ImageVectorToken.fromVector(Icons.Rounded.Circle),
        tint = if (checked) ColorSchemeKeyTokens.Primary.toColor() else ColorSchemeKeyTokens.SurfaceVariant.toColor()
    ) {
        onCheckedChange?.invoke(checked)
    }
}

@Composable
fun FilledTonalIconTextButton(
    modifier: Modifier = Modifier,
    icon: ImageVectorToken,
    text: StringResourceToken,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    FilledTonalButton(modifier = modifier, enabled = enabled, onClick = onClick, contentPadding = PaddingValues(SizeTokens.Level16, SizeTokens.Level8)) {
        Icon(
            modifier = Modifier.size(SizeTokens.Level20),
            tint = ColorSchemeKeyTokens.Primary.toColor(),
            imageVector = icon.value,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(SizeTokens.Level8))
        AutoSizeText(modifier = Modifier.weight(1f), text = text.value, textAlign = TextAlign.Center)
    }
}

@Composable
fun OutlinedButtonIconTextButton(
    modifier: Modifier = Modifier,
    icon: ImageVectorToken,
    text: StringResourceToken,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(modifier = modifier, enabled = enabled, onClick = onClick, contentPadding = PaddingValues(SizeTokens.Level16, SizeTokens.Level8)) {
        Icon(
            modifier = Modifier.size(SizeTokens.Level20),
            tint = ColorSchemeKeyTokens.Primary.toColor(),
            imageVector = icon.value,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(SizeTokens.Level8))
        AutoSizeText(modifier = Modifier.weight(1f), text = text.value, textAlign = TextAlign.Center)
    }
}