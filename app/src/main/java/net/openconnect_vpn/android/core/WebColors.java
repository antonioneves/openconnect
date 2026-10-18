/*
 * Copyright (c) 2026, Antonio Neves
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * In addition, as a special exception, the copyright holders give
 * permission to link the code of portions of this program with the
 * OpenSSL library.
 */

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
