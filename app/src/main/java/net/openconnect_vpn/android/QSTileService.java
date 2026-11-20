/*
 * Copyright (c) 2019.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openconnect_vpn.android;

import android.annotation.TargetApi;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import net.openconnect_vpn.android.core.OpenConnectManagementThread;
import net.openconnect_vpn.android.core.OpenVpnService;
import net.openconnect_vpn.android.core.VPNConnector;

/**
 * @author Terry E-mail: yaoxinghuo at qq dot com
 * @date 2019-6-2 18:33
 * @description
 */
@TargetApi(24)
public class QSTileService extends TileService {

    private static final String TAG = QSTileService.class.getName();

    private int mConnectionState;
    private VPNConnector mConn;

    @Override
    public void onStartListening() {
        super.onStartListening();

        // Show something right away: binding to the VPN service is asynchronous, and the tile
        // would be empty until it answers. Also forget the last state, so that it is redrawn
        // even if it has not changed since the tile was last visible.
        mConnectionState = 0;
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setIcon(Icon.createWithResource(this, R.drawable.ic_qs_tile));
            tile.setLabel(getString(R.string.app));
            tile.setState(Tile.STATE_INACTIVE);
            tile.updateTile();
        }

        mConn = new VPNConnector(this, true) {
            @Override
            public void onUpdate(OpenVpnService service) {
                updateState(service);
            }
        };
    }

    @Override
    public void onStopListening() {
        super.onStopListening();

        if (mConn != null) {
            mConn.stopActiveDialog();
            mConn.unbind();
            mConn = null;
        }
    }

    @Override
    public void onClick() {
        super.onClick();

        toggle();
    }

    private void updateState(OpenVpnService service) {
        int newState = service.getConnectionState();

        if (mConnectionState != newState) {
            String tileLabel = null;
            int tileState;
            String profileName;
            switch (newState) {
                case OpenConnectManagementThread.STATE_CONNECTED:
                    tileState = Tile.STATE_ACTIVE;
                    tileLabel = getString(R.string.disconnect);
                    profileName = service.getReconnectName();
                    if (profileName != null) {
                        tileLabel = tileLabel + " " + profileName;
                    }
//                    Toast.makeText(this, getString(R.string.state_connected_to, service.profile.getName()), Toast.LENGTH_SHORT).show();
                    break;
                case OpenConnectManagementThread.STATE_DISCONNECTED:
                    tileState = Tile.STATE_INACTIVE;
                    profileName = service.getReconnectName();
                    if (profileName != null) {
                        tileLabel = getString(R.string.reconnect_to, profileName);
                    }
                    break;
                default:
                    tileLabel = service.getConnectionStateName();
                    tileState = Tile.STATE_UNAVAILABLE;
                    break;
            }
            mConnectionState = newState;

            if (tileLabel == null) {
                tileLabel = getString(R.string.app);
            }

            Tile tile = getQsTile();
            if (tile == null) {
                return;
            }
            tile.setIcon(Icon.createWithResource(this, R.drawable.ic_qs_tile));
            tile.setState(tileState);
            tile.setLabel(tileLabel);
            Log.d(TAG, "set tile state: " + tileState + ", label: " + tileLabel);
            tile.updateTile();
        }
    }

    private void toggle() {
        if (mConn == null || mConn.service == null) {
            return;
        }
        if (mConnectionState == OpenConnectManagementThread.STATE_CONNECTED) {
            mConn.service.stopVPN();
        } else if (mConnectionState == OpenConnectManagementThread.STATE_DISCONNECTED) {
            mConn.service.startReconnectActivity(this);
        }
    }
}
