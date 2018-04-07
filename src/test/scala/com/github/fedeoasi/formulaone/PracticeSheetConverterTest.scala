package com.github.fedeoasi.formulaone

import java.io.FileOutputStream

import com.github.fedeoasi.TemporaryFiles
import org.scalatest.{FunSpec, Matchers}

import scalaj.http.Http

class PracticeSheetConverterTest extends FunSpec with Matchers with TemporaryFiles {
  it("converts the 2018 Bahrain FP2 sheet") {
    val inputURL = "https://www.fia.com/file/66971/download?token=9ajeKTMP"

    val response = Http(inputURL).header("User-Agent", "scalaj-http").asBytes
    response.is2xx shouldBe true

    withTmpFile("bah2018", ".pdf") { tmpFile =>
      val outputStream = new FileOutputStream(tmpFile.toFile)
      outputStream.write(response.body)
      val drivers = PracticeSheetConverter.convert(tmpFile.toFile)
      outputStream.close()
      drivers.size shouldBe 20
      val stoffel = drivers.head
      stoffel.name shouldBe "Stoffel VANDOORNE"
      stoffel.laps.size shouldBe 35
      val valtteri = drivers.last
      valtteri.name shouldBe "Valtteri BOTTAS"
      valtteri.laps.size shouldBe 31
    }
  }
}
