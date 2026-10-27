package agunginsani.api

import cats.effect.IO
import scala.jdk.CollectionConverters.*
import scala.util.Try
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.{Sheets, SheetsScopes}
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

final case class Expense(
    date: String,
    description: String,
    category: String,
    amount: Int,
    currency: String,
    createdAt: Option[LocalDateTime],
    id: String
)

object SpreadsheetService:
  val SpreadsheetId = "1j3_rqSZ9PD-8A4VSwJfUjqbqM9QQhEkeJp6M92ndoF0"

  /** Parses a row from the spreadsheet into an Expense object.
    *
    * @param row
    *   A list of strings representing a row from the spreadsheet.
    * @return
    *   Either an error message or a parsed Expense object.
    */
  private def parseRow(row: List[String]): Either[String, Expense] =
    val createdAtFormatter = DateTimeFormatter.ofPattern("d/M/uuuu H:mm:ss")

    row match
      case date :: description :: category :: amount :: currency :: createdAt :: id :: Nil =>
        for
          parsedAmount <- amount.toIntOption.toRight(
            s"Cannot parse amount: $amount"
          )
          parsedCratedAt <- Try(
            LocalDateTime.parse(createdAt, createdAtFormatter)
          ).toEither.left
            .map(_.getMessage)
        yield Expense(
          date,
          description,
          category,
          parsedAmount,
          currency,
          Some(parsedCratedAt),
          id
        )

      case date :: description :: category :: amount :: currency :: createdAt :: Nil =>
        for
          parsedAmount <- amount.toIntOption.toRight(
            s"Cannot parse amount: $amount"
          )
          parsedCratedAt <- Try(
            LocalDateTime.parse(createdAt, createdAtFormatter)
          ).toEither.left
            .map(_.getMessage)
        yield Expense(
          date,
          description,
          category,
          parsedAmount,
          currency,
          Some(parsedCratedAt),
          ""
        )

      case date :: description :: category :: amount :: currency :: Nil =>
        for parsedAmount <- amount.toIntOption.toRight(
            s"Cannot parse amount: $amount"
          )
        yield Expense(
          date,
          description,
          category,
          parsedAmount,
          currency,
          None,
          ""
        )

      case _ => Left(s"Unexpected column count: ${row.size}")

  // Uses the service account key file pointed to by GOOGLE_APPLICATION_CREDENTIALS.
  // The spreadsheet must be shared with the service account's email.
  private def client: IO[Sheets] = IO.blocking:
    val credentials = GoogleCredentials
      .getApplicationDefault()
      .createScoped(SheetsScopes.SPREADSHEETS_READONLY)
    Sheets
      .Builder(
        GoogleNetHttpTransport.newTrustedTransport(),
        GsonFactory.getDefaultInstance(),
        HttpCredentialsAdapter(credentials)
      )
      .setApplicationName("agunginsani-api-scala")
      .build()

  /** Reads a range such as "Sheet1!A1:D10" and returns rows as lists of
    * strings.
    */
  def readRange(range: String): IO[List[Either[String, Expense]]] =
    for
      sheets <- client
      response <- IO.blocking(
        sheets.spreadsheets().values().get(SpreadsheetId, range).execute()
      )
    yield Option(response.getValues).toList
      .flatMap(
        _.asScala.toList
          .map(_.asScala.toList.map(_.toString))
          .filter(_.nonEmpty)
          .map(parseRow)
      )
