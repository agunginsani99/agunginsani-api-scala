package expenses.api

import org.http4s.{HttpRoutes, Response, headers, Status, MediaType}
import org.http4s.dsl.io.*
import org.http4s.circe.*
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityEncoder.*
import cats.effect.IO
import expenses.api.LeetCodeService

final case class LongestSubstringWithoutRepeatingCharactersResponse(
    input: String,
    length: Int
)

object InputParam extends QueryParamDecoderMatcher[String]("input")

object LeetCodeRoutes {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "leetcode" / "longest-substring-without-repeating-characters" :? InputParam(
          input
        ) =>
      Ok(
        LongestSubstringWithoutRepeatingCharactersResponse(
          input,
          LeetCodeService.longestSubstringWithoutRepeatingCharacters(input)
        )
      )
  }
}
