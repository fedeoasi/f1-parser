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


  def main(args: Array[String]): Unit = {
    val inputFile = new File(args(0))
    val drivers = PracticeSheetConverter.convert(inputFile)
    val csv = CsvConverter.toCsv(drivers)
    val writer = CSVWriter.open(new File(inputFile.getName.split("\\.").head + ".csv"))
    writer.writeAll(csv)
    writer.close()
  }
}