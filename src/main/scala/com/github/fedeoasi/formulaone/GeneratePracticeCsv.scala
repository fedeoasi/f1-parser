package com.github.fedeoasi.formulaone

import java.io.File

import com.github.tototoshi.csv.CSVWriter

object GeneratePracticeCsv {
  sealed trait ExpectedState
  case object Driver extends ExpectedState
  case class FirstLapNumber(driver: DriverWithLaps) extends ExpectedState
  case class Time(number: Int, pits: Boolean, driver: DriverWithLaps) extends ExpectedState
  case class LapNumber(driver: DriverWithLaps) extends ExpectedState
  case class LapTime(number: Int, pits: Boolean, driver: DriverWithLaps) extends ExpectedState

  private val DriverRegex = "[a-zA-z]+ [A-Z]+".r
  private val TimeRegex = "\\d+:\\d+:\\d+".r
  private val LapNumberRegex = "(\\d+)([P]?)".r
  private val LapTimeRegex = "\\d+:\\d+\\.\\d+".r

  def main(args: Array[String]): Unit = {
    val inputFile = new File(args(0))
    val chunks = PdfTextExtractor.extractText(inputFile)
    val drivers = extractDriversWithLaps(chunks)
    val csv = CsvConverter.toCsv(drivers)
    val writer = CSVWriter.open(new File(inputFile.getName.split("\\.").head + ".csv"))
    writer.writeAll(csv)
    writer.close()
  }

  private def extractDriversWithLaps(chunks: Seq[PdfText]): Seq[DriverWithLaps] = {
    val initialState = (Seq.empty[DriverWithLaps], Driver: ExpectedState)
    val (drivers, _) = chunks.foldLeft(initialState) { case ((result, expectedState), chunk) =>
      val text = chunk.value
      expectedState match {
        case state @ Driver =>
          val nextState = text match {
            case DriverRegex() => FirstLapNumber(DriverWithLaps(text, Seq.empty))
            case _ => state
          }
          (result, nextState)
        case state @ FirstLapNumber(driver) =>
          val nextState = text match {
            case LapNumberRegex(lap, pits) => Time(lap.toInt, pits.contains("P"), driver)
            case _ => state
          }
          (result, nextState)
        case state @ Time(lap, pits, driver) =>
          val nextState = text match {
            case TimeRegex() => LapNumber(driver.copy(laps = Seq(Lap(lap, pits, text))))
            case _ => state
          }
          (result, nextState)
        case state @ LapNumber(driver) =>
          text match {
            case LapNumberRegex(lap, pits) =>
              (result, LapTime(lap.toInt, pits.contains("P"), driver))
            case DriverRegex() => (result :+ driver, FirstLapNumber(DriverWithLaps(text, Seq.empty)))
            case v if v.contains("Page") => (result :+ driver, Driver)
            case _ => (result, state)
          }
        case state @ LapTime(number, pits, driver) =>
          text match {
            case LapTimeRegex() =>
              val driverWithLap = driver.copy(laps = driver.laps :+ Lap(number, pits, text))
              (result, LapNumber(driverWithLap))
            case DriverRegex() => (result :+ driver, FirstLapNumber(DriverWithLaps(text, Seq.empty)))
            case _ => (result, state)
          }
      }
    }
    drivers
  }
}