package net.openconnect_vpn.android;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.view.Window;
import androidx.preference.EditTextPreference;
import androidx.preference.EditTextPreferenceDialogFragmentCompat;
import androidx.preference.ListPreference;
import androidx.preference.ListPreferenceDialogFragmentCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

/**
 * androidx.preference builds its dialogs with the plain AppCompat builder, which does not draw
 * the Material 3 dialog surface. These fragments give them the same shape and color as
 * MaterialAlertDialogBuilder.
 */
public class MaterialPreferenceDialogs {

	private static final String TAG = "net.openconnect_vpn.android.PreferenceDialog";

	static void style(Dialog dialog) {
		Window window = dialog.getWindow();
		if (window == null) {
			return;
		}
		Context ctx = dialog.getContext();
		float radius = 28 * ctx.getResources().getDisplayMetrics().density;
		MaterialShapeDrawable bg = new MaterialShapeDrawable(
				ShapeAppearanceModel.builder().setAllCornerSizes(radius).build());
		bg.setFillColor(ColorStateList.valueOf(MaterialColors.getColor(ctx,
				com.google.android.material.R.attr.colorSurfaceContainerHigh, 0)));
		window.setBackgroundDrawable(new InsetDrawable(bg, (int)(4 * ctx.getResources().getDisplayMetrics().density)));
	}

	/** None of the preferences have icons, so don't leave empty space where one would go. */
	public static void removeIconSpace(PreferenceGroup group) {
		group.setIconSpaceReserved(false);
		for (int i = 0; i < group.getPreferenceCount(); i++) {
			Preference p = group.getPreference(i);
			if (p instanceof PreferenceGroup) {
				removeIconSpace((PreferenceGroup)p);
			} else {
				p.setIconSpaceReserved(false);
			}
		}
	}

	private static Bundle keyArgs(String key) {
		Bundle args = new Bundle(1);
		args.putString("key", key);
		return args;
	}

	public static class ListDialog extends ListPreferenceDialogFragmentCompat {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Dialog dialog = super.onCreateDialog(savedInstanceState);
			style(dialog);
			return dialog;
		}
	}

	public static class EditTextDialog extends EditTextPreferenceDialogFragmentCompat {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Dialog dialog = super.onCreateDialog(savedInstanceState);
			style(dialog);
			return dialog;
		}
	}

	/** Shows the dialog for a preference; returns false if the preference has no dialog of ours. */
	@SuppressWarnings("deprecation")
	public static boolean show(PreferenceFragmentCompat host, Preference preference) {
		PreferenceDialogFragmentCompat frag;
		if (preference instanceof ListPreference) {
			frag = new ListDialog();
		} else if (preference instanceof EditTextPreference) {
			frag = new EditTextDialog();
		} else if (preference instanceof ClearPasswordPreference) {
			frag = new ClearPasswordPreference.Dialog();
		} else {
			return false;
		}
		frag.setArguments(keyArgs(preference.getKey()));
		frag.setTargetFragment(host, 0);
		frag.show(host.getParentFragmentManager(), TAG);
		return true;
	}
}
