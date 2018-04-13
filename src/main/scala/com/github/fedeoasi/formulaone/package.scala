package com.github.fedeoasi

package object formulaone {
  case class Lap(number: Int, pits: Boolean, time: String)
  case class DriverWithLaps(name: String, number: Int, laps: Seq[Lap])
}
