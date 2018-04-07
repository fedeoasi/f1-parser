package com.github.fedeoasi.formulaone

object CsvConverter {
  def toCsv(driversWithLaps: Seq[DriverWithLaps]): List[List[String]] = {
    val header = List[String]("lapNumber", "lapTime", "name", "pit")
    header +: driversWithLaps.flatMap { driverWithLaps =>
      driverWithLaps.laps.map { lap =>
        List[String](lap.number.toString, lap.time, driverWithLaps.name, lap.pits.toString)
      }
    }.toList
  }
}
