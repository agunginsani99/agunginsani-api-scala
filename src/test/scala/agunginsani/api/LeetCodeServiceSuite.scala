package agunginsani.api

import munit.FunSuite
import agunginsani.api.LeetCodeService

final class LeetCodeServiceSuite extends FunSuite:
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
