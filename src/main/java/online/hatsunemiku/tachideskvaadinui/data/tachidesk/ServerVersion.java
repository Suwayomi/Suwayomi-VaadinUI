/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.data.tachidesk;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

/** Represents the version of the Suwayomi Server. */
@Getter
public class ServerVersion {

  /** The version of the server. Format: vX.Y.Z */
  private final String version;

  /** The revision of the server. Format: r1234 */
  private final String revision;

  /**
   * Creates a new instance of the {@link ServerVersion} class.
   * @param version the version of the server
   * @param revision the revision of the server
   */
  //Private constructor so only Spring can create instances of this class
  @JsonCreator
  private ServerVersion(String version, String revision) {
    this.version = version;
    this.revision = revision;
  }

  /**
   * Gets the major version of the server. For example if the version is v1.2.3, this method will
   * return 1.
   *
   * @return the major version of the server
   */
  public int getMajorVersion() {
    String majorVersion = version.substring(1, version.indexOf('.'));
    return Integer.parseInt(majorVersion);
  }

  /**
   * Gets the minor version of the server. For example if the version is v1.2.3, this method will
   * return 2.
   *
   * @return the minor version of the server
   */
  public int getMinorVersion() {
    int firstDot = version.indexOf('.');
    int secondDot = version.indexOf('.', firstDot + 1);
    String minorVersion = version.substring(firstDot + 1, secondDot);
    return Integer.parseInt(minorVersion);
  }

  /**
   * Gets the patch version of the server. For example if the version is v1.2.3, this method will
   * return 3.
   *
   * @return the patch version of the server
   */
  public int getPatchVersion() {
    int lastDot = version.lastIndexOf('.');
    String patchVersion = version.substring(lastDot + 1);
    return Integer.parseInt(patchVersion);
  }

  /**
   * Gets the revision number of the server. For example if the revision is r1234, this method will
   * return 1234.
   *
   * @return the revision number of the server
   */
  public int getRevisionNumber() {
    String revisionNumber = revision.substring(1);
    return Integer.parseInt(revisionNumber);
  }
}
