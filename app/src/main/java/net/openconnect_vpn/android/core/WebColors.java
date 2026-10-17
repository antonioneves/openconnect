package net.openconnect_vpn.android.core;

import android.content.Context;
import android.graphics.Color;
import com.google.android.material.color.MaterialColors;

public class WebColors {

	/** A <style> block with text and link colors taken from the current (light or dark) theme. */
	public static String style(Context ctx) {
		int text = MaterialColors.getColor(ctx, com.google.android.material.R.attr.colorOnSurface, Color.WHITE);
		int link = MaterialColors.getColor(ctx, androidx.appcompat.R.attr.colorPrimary, Color.CYAN);
		return String.format("<style>body{color:#%06x}a{color:#%06x}</style>",
				text & 0xffffff, link & 0xffffff);
	}
}
