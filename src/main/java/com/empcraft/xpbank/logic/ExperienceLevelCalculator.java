package com.empcraft.xpbank.logic;

public class ExperienceLevelCalculator {
  /**
   * This table represents the experience points you need to gain a specific level. For example: If
   * you have 7-16 points, you have level 1.
   */
  private static final int[] expList = { 0, 7, 16, 27, 40, 55, 72, 91, 112, 135, 160, 187, 216,
      247, 280, 315, 352, 394, 441, 493, 550, 612, 679, 751, 828, 910, 997, 1089, 1186, 1288, 1395,
      1507, 1628, 1758, 2048, 2202, 2368, 2543, 2727, 2920 };

  /**
   * Returns the player's level for the given experience points.
   * @param experience The experience points.
   * @return the level a player had with these points.
   */
  public static int getLevel(int experience) {
    /*
     * We need to count down to get the maximum possible level.
     */
    int level = expList.length - 1;

    if (experience < 0) {
      return 0;
    }

    while (true) {
      if (experience >= expList[level]) {
        return level;
      }

      level--;
    }
  }
}
