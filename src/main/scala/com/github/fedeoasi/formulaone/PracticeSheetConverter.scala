package com.github.fedeoasi.formulaone

import java.io.File

import com.github.fedeoasi.formulaone.GeneratePracticeCsv._

object PracticeSheetConverter {
  private val DriverRegex = "[a-zA-z]+ [A-Z]+".r
  private val TimeRegex = "\\d+:\\d+:\\d+".r
  private val LapNumberRegex = "(\\d+)([P]?)".r
  private val LapTimeRegex = "\\d+:\\d+\\.\\d+".r

  def convert(inputFile: File): Seq[DriverWithLaps] = {
    val chunks = PdfTextExtractor.extractText(inputFile)
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