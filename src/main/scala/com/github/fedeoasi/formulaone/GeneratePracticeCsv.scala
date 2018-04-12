package com.github.fedeoasi.formulaone

import java.io.File

import com.github.tototoshi.csv.CSVWriter

object GeneratePracticeCsv {
  def main(args: Array[String]): Unit = {
    val inputFile = new File(args(0))
    val drivers = PracticeSheetConverter.convert(inputFile)
    println(s"Found ${drivers.size} drivers")
    val csv = CsvConverter.toCsv(drivers)
    val writer = CSVWriter.open(new File(inputFile.getName.split("\\.").head + ".csv"))
    writer.writeAll(csv)
    writer.close()
  }
}