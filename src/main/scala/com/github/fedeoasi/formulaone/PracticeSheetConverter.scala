package com.github.fedeoasi.formulaone

import java.io.File

object PracticeSheetConverter {
  sealed trait ExpectedState
  case object Driver extends ExpectedState
  case class FirstLapNumber(driver: DriverWithLaps) extends ExpectedState
  case class Time(number: Int, pits: Boolean, driver: DriverWithLaps) extends ExpectedState
  case class LapNumber(driver: DriverWithLaps) extends ExpectedState
  case class LapTime(number: Int, pits: Boolean, driver: DriverWithLaps) extends ExpectedState

  private val DriverRegex = "[a-zA-z]+ [A-Z]+".r
  private val OldDriverRegex = "[A-Z]\\. [A-Z]+".r
  private val TimeRegex = "\\d+:\\d+:\\d+".r
  private val LapNumberRegex = "(\\d+)([P]?)".r
  private val LapTimeRegex = "\\d+:\\d+\\.\\d+".r

  def convert(inputFile: File): Seq[DriverWithLaps] = {
    val chunks = PdfTextExtractor.extractText(inputFile)
    val numberByDriver = buildDriversByNumber(chunks)
    val initialState = (Seq.empty[DriverWithLaps], Driver: ExpectedState)
      val (drivers, _) = chunks.foldLeft(initialState) { case ((result, expectedState), chunk) =>
        val text = chunk.value
        expectedState match {
          case state @ Driver =>
            val nextState = text match {
              case DriverRegex() | OldDriverRegex() if numberByDriver.contains(text) =>
                FirstLapNumber(DriverWithLaps(text, numberByDriver(text), Seq.empty))
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
              case v if v.contains("Page") => (result :+ driver, Driver)
              case _ => (result, state)
            }
          case state @ LapTime(number, pits, driver) =>
            text match {
              case LapTimeRegex() =>
                val driverWithLap = driver.copy(laps = driver.laps :+ Lap(number, pits, text))
                (result, LapNumber(driverWithLap))
              case DriverRegex() | OldDriverRegex() if numberByDriver.contains(text) => (result :+ driver, FirstLapNumber(DriverWithLaps(text, numberByDriver(text), Seq.empty)))
              case _ => (result, state)
            }
        }
      }
      drivers
    }

  private def buildDriversByNumber(chunks: Seq[PdfText]): Map[String, Int] = {
    chunks.sliding(2).flatMap {
      case Seq(c1, c2) =>
        c2.value match {
          case DriverRegex() | OldDriverRegex() if c1.value.forall(_.isDigit) => Seq(c2.value -> c1.value.toInt)
          case _ => Seq.empty
        }
      case Seq(_) => Seq.empty
    }.toMap
  }
}
