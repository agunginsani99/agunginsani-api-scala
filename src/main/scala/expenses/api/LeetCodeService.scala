package expenses.api

import scala.annotation.tailrec

object LeetCodeService:
  def longestSubstringWithoutRepeatingCharacters(s: String): Int =
    val (_, _, maxLength) =
      s.zipWithIndex.foldLeft((0, Map.empty[Char, Int], 0)) {
        case ((left, seen, maxLength), (c, i)) =>
          val newLeft = seen.get(c) match
            case Some(prev) => Math.max(left, prev + 1)
            case None       => left

          (newLeft, seen.updated(c, i), Math.max(maxLength, i - newLeft + 1))
      }

    maxLength
