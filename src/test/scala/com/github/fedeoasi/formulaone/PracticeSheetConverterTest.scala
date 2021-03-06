package com.github.fedeoasi.formulaone

import java.io.FileOutputStream

import com.github.fedeoasi.TemporaryFiles
import org.scalatest.{FunSpec, Matchers}

import scalaj.http.Http

class PracticeSheetConverterTest extends FunSpec with Matchers with TemporaryFiles {
  it("converts the 2017 Italian FP2 sheet") {
    val inputURL = "https://www.fia.com/file/61024/download?token=bUJqfvnt"

    val response = Http(inputURL).header("User-Agent", "scalaj-http").asBytes
    response.is2xx shouldBe true

    withTmpFile("ita2017", ".pdf") { tmpFile =>
      val outputStream = new FileOutputStream(tmpFile.toFile)
      outputStream.write(response.body)
      val drivers = PracticeSheetConverter.convert(tmpFile.toFile)
      outputStream.close()
      drivers.size shouldBe 20
      val stoffel = drivers.head
      stoffel.name shouldBe "S. VANDOORNE"
      stoffel.number shouldBe 2
      stoffel.laps.size shouldBe 30
      val pascal = drivers.last
      pascal.name shouldBe "P. WEHRLEIN"
      pascal.number shouldBe 94
      pascal.laps.size shouldBe 25
    }
  }

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
      stoffel.number shouldBe 2
      stoffel.laps.size shouldBe 35
      val valtteri = drivers.last
      valtteri.name shouldBe "Valtteri BOTTAS"
      valtteri.number shouldBe 77
      valtteri.laps.size shouldBe 31
    }
  }
}
