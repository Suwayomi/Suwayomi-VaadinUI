/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.exceptions;

public class AndroidOnlyException extends RuntimeException {
    public AndroidOnlyException() {
        super("It seems this Extensions does not work with Suwayomi.");
    }
}
