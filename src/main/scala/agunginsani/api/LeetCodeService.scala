package agunginsani.api

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

  def compareVersion(a: String, b: String): Int =
    val as = a.split('.').map(_.toIntOption.getOrElse(0))
    val bs = b.split('.').map(_.toIntOption.getOrElse(0))
    as.zipAll(bs, 0, 0).collectFirst {
      case (aVer, bVer) if aVer != bVer => aVer.compareTo(bVer)
    }.getOrElse(0)
