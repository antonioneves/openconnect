/*
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openconnect_vpn.android;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.content.ContentResolver;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.animation.LinearInterpolator;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * Turns the green connector of the action bar logo clockwise while a connection is being set up.
 *
 * The connector eases in from its resting position, so it leaves the app icon smoothly, and when
 * the spinning is no longer wanted it slows down and comes to rest exactly where the icon has it,
 * so it never jumps. The motion is computed here instead of with a fixed-length animation, so a
 * stop (or a restart) takes effect straight away, whatever the connector is doing at the time.
 */
public class LogoSpinner {

	// full speed, in degrees per millisecond: one turn in a little under a second
	private static final double SPEED = 0.45;
	// how quickly it speeds up and slows down, in degrees per millisecond squared: ~0.7 s
	private static final double ACCEL = SPEED / 700.0;
	// below this speed it has barely started moving, so it can simply be put back at rest
	private static final double MIN_SPEED = 0.02;

	private final Drawable mConnector;
	private final ContentResolver mResolver;
	private final ValueAnimator mTicker = ValueAnimator.ofFloat(0f, 1f);

	private boolean mSpin;
	private boolean mEasingOut;
	// when the current phase began (uptime, ms), and the angle (degrees) and speed at that moment
	private double mT0, mA0, mV0;
	// while easing out: how long it takes to come to rest
	private double mDuration;

	/**
	 * Gives the activity's action bar a logo whose connector can be spun.
	 * Returns null if there is no action bar.
	 */
	public static LogoSpinner attach(AppCompatActivity activity) {
		ActionBar bar = activity.getSupportActionBar();
		Drawable logo = ContextCompat.getDrawable(activity, R.drawable.ic_header_logo);
		if (bar == null || !(logo instanceof LayerDrawable)) {
			return null;
		}
		// the logo is rotated in place, so it must not share its state with other users of the resource
		logo = logo.mutate();
		Drawable connector = ((LayerDrawable) logo).findDrawableByLayerId(R.id.logo_connector);
		if (connector == null) {
			return null;
		}
		bar.setLogo(logo);
		return new LogoSpinner(connector, activity.getContentResolver());
	}

	private LogoSpinner(Drawable connector, ContentResolver resolver) {
		mConnector = connector;
		mResolver = resolver;
		mTicker.setDuration(1000);
		mTicker.setRepeatCount(ValueAnimator.INFINITE);
		mTicker.setInterpolator(new LinearInterpolator());
		mTicker.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				onFrame();
			}
		});
		mTicker.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mConnector.setLevel(0);
			}
		});
	}

	/** Start turning, or (with false) slow down and come to rest at the icon's position. */
	public void setSpinning(boolean spin) {
		// leave the logo alone if the user has turned animations off
		mSpin = spin && Settings.Global.getFloat(mResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0;
		if (mSpin && !mTicker.isRunning()) {
			mEasingOut = false;
			mT0 = SystemClock.uptimeMillis();
			mA0 = 0;
			mV0 = 0;
			mTicker.start();
		}
	}

	/** Back to rest at once, e.g. when the activity is paused. */
	public void stopNow() {
		mSpin = false;
		mTicker.cancel();
		mConnector.setLevel(0);
	}

	private void onFrame() {
		double now = SystemClock.uptimeMillis();
		double t = now - mT0;
		double angle;
		double speed;

		if (mEasingOut) {
			if (t >= mDuration) {
				// arrived: the angle is a whole number of turns, which is the icon's resting position.
				// (cancel, not end: end() replays the update callback, which would re-enter this method)
				mTicker.cancel();
				return;
			}
			double decel = mV0 / mDuration;
			angle = mA0 + mV0 * t - 0.5 * decel * t * t;
			speed = mV0 - decel * t;
		} else {
			// speeding up until full speed, then turning at full speed
			double tFull = (SPEED - mV0) / ACCEL;
			if (t < tFull) {
				angle = mA0 + mV0 * t + 0.5 * ACCEL * t * t;
				speed = mV0 + ACCEL * t;
			} else {
				angle = mA0 + mV0 * tFull + 0.5 * ACCEL * tFull * tFull + SPEED * (t - tFull);
				speed = SPEED;
			}
		}

		// switch phase from where the connector is right now, so there is no jump
		if (!mEasingOut && !mSpin) {
			if (speed < MIN_SPEED) {
				mTicker.cancel();
				return;
			}
			beginEaseOut(now, angle, speed);
		} else if (mEasingOut && mSpin) {
			mEasingOut = false;
			mT0 = now;
			mA0 = angle;
			mV0 = speed;
		}

		setAngle(angle);
	}

	private void beginEaseOut(double now, double angle, double speed) {
		// slow down at the same rate it sped up (or more gently), and land on a whole turn
		double distance = 360 - mod360(angle);
		double minDistance = speed * speed / (2 * ACCEL);
		while (distance < minDistance) {
			distance += 360;
		}
		mEasingOut = true;
		mT0 = now;
		mA0 = angle;
		mV0 = speed;
		mDuration = 2 * distance / speed;
	}

	private void setAngle(double angle) {
		// the connector drawable turns a full circle over levels 0..10000
		mConnector.setLevel((int) (mod360(angle) / 360.0 * 10000));
	}

	private static double mod360(double angle) {
		double a = angle % 360;
		return a < 0 ? a + 360 : a;
	}
}
