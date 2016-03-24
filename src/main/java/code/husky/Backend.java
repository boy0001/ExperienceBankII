package code.husky;

/**
 * The backend chosen for data storage.
 */
public enum Backend {
  /**
   * Use YAML flat file backend. Not recommended.
   */
  YAML,
  /**
   * Use SQLite as Backend. Good default for up to 15 players.
   */
  SQLITE,
  /**
   * Use MySQL Backend for high-performance-servers.
   */
  MYSQL;

  /**
   * Gets the backend from String (Configuration config.yml).
   *
   * @param backendString
   *          the String from config.
   * @return a backend instance.
   */
  public static Backend getBackend(String backendString) {
    if (backendString == null) {
      return null;
    }

    for (Backend be : values()) {
      if (be.name().toLowerCase().equals(backendString.toLowerCase())) {
        return be;
      }
    }

    return null;
  }
}
