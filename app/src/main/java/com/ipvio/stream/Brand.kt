package com.ipvio.stream

import androidx.compose.foundation.Image
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Composable

/** PASHA TV Lite forward-motion brand mark, shared with the launcher icon. */
@Composable
fun BrandMark(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.ic_launcher_foreground),
        contentDescription = "PASHA TV Lite",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}
