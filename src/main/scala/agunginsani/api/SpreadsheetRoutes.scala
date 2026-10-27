package agunginsani.api

import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import cats.effect.IO
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityEncoder.*
import agunginsani.api.SpreadsheetService.readRange

final case class ExpensesTrackerResponse(data: List[Expense])

object DescriptionQueryParam
    extends OptionalQueryParamDecoderMatcher[String]("description")

object CategoryQueryParam
    extends OptionalMultiQueryParamDecoderMatcher[String]("category")

object SpreadsheetRoutes {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "expenses-tracker" / "expenses" :?
        DescriptionQueryParam(description) +&
        CategoryQueryParam(categories) =>
      readRange("Raw!A2:G").flatMap { rows =>
        val errors = rows collect { case Left(error) => error }

        IO.println(s"Rows: ${rows.size}; parse errors: $errors") *>
          {
            val expenses = rows collect { case Right(expense) => expense }

            val categoryList = categories.toOption.getOrElse(Nil)

            val filteredExpenses = expenses filter { expense =>
              (expense.description.toLowerCase contains description
                .getOrElse("")
                .toLowerCase) &&
              (categoryList.isEmpty || categoryList.exists { category =>
                expense.category.toLowerCase contains category.toLowerCase
              })
            }

            Ok(ExpensesTrackerResponse(filteredExpenses))
          }
      }
  }
}
