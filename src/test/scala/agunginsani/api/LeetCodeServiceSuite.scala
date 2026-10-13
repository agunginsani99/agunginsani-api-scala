package agunginsani.api

import munit.FunSuite
import agunginsani.api.LeetCodeService

final class LongestSubstringWithoutRepeatingCharactersSuite extends FunSuite:
  test("returns 0 for an empty string"):
    assertEquals(
      LeetCodeService.longestSubstringWithoutRepeatingCharacters(""),
      0
    )

  test("returns the string length when all characters are unique"):
    assertEquals(
      LeetCodeService.longestSubstringWithoutRepeatingCharacters("abc"),
      3
    )

  test("handles repeated characters"):
    assertEquals(
      LeetCodeService.longestSubstringWithoutRepeatingCharacters("bbbbb"),
      1
    )

  test("finds the longest substring in a mixed string"):
    assertEquals(
      LeetCodeService.longestSubstringWithoutRepeatingCharacters("abcabcbb"),
      3
    )
    assertEquals(
      LeetCodeService.longestSubstringWithoutRepeatingCharacters("abcdbe"),
      4
    )

final class CompareVersionSuite extends FunSuite:
  test("returns 0 when comparing two empty versions"):
    assertEquals(
      LeetCodeService.compareVersion("", ""),
      0
    )
    assertEquals(
      LeetCodeService.compareVersion("1", "1"),
      0
    )
    assertEquals(
      LeetCodeService.compareVersion("1.1", "1.1"),
      0
    )
    assertEquals(
      LeetCodeService.compareVersion("1", "1.0"),
      0
    )
    assertEquals(
      LeetCodeService.compareVersion("1.0.0", "1.0"),
      0
    )
    assertEquals(
      LeetCodeService.compareVersion("1.0.0", "1.0.0"),
      0
    )
    assertEquals(
      LeetCodeService.compareVersion("1.10", "1.2"),
      1
    )
    assertEquals(
      LeetCodeService.compareVersion("1.2", "1.10"),
      -1
    )

  test("handles versions with different lengths"):
    assertEquals(
      LeetCodeService.compareVersion("1.0", "1"),
      0
    )
    assertEquals(
      LeetCodeService.compareVersion("1.0.1", "1"),
      1
    )
    assertEquals(
      LeetCodeService.compareVersion("1", "1.0.1"),
      -1
    )

  test("handles versions with leading zeros"):
    assertEquals(
      LeetCodeService.compareVersion("1.01", "1.001"),
      0
    )
    assertEquals(
      LeetCodeService.compareVersion("1.0.1", "1.00.1"),
      0
    )
